/**
 * (C) Copyright 2023 Hal Hildebrand. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.chiralbehaviors.janus;

import java.io.IOException;
import java.lang.classfile.*;
import java.lang.classfile.attribute.ExceptionsAttribute;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassFile API implementation of composite functionality that provides dynamic interface composition using bytecode
 * generation.
 * <p>
 * This interface generates composite classes at runtime using Java 25's ClassFile API. Composites combine multiple
 * mixin implementations into a single object that implements the composite interface, with support for dependency
 * injection via {@link Facet} and {@link This} annotations.
 *
 * @author hal.hildebrand
 */
public interface Composite {

    // Bytecode access flags
    int    ACC_ABSTRACT                 = 0x0400;
    int    ACC_STATIC                   = 0x0008;
    int    JAVA_5_CLASS_VERSION         = 49;

    String GENERATED_COMPOSITE_TEMPLATE = "%s$composite";
    String MIX_IN_VAR_PREFIX            = "mixIn_";

    // Thread-safe cache for generated composite classes
    Map<String, Class<?>> GENERATED_CLASSES = new ConcurrentHashMap<>();

    public static ClassModel getClassModel(Class<?> clazz) {
        var classResourceName = '/' + (clazz.getCanonicalName().replace('.', '/')) + ".class";
        try (var is = clazz.getResourceAsStream(classResourceName)) {
            if (is == null) {
                throw new VerifyError("cannot read class resource for: " + classResourceName);
            }
            var classBytes = is.readAllBytes();
            return ClassFile.of().parse(classBytes);
        } catch (IOException e) {
            var v = new VerifyError("cannot read class resource for: " + classResourceName);
            v.initCause(e);
            throw v;
        }
    }

    static Composite instance() {
        return new Composite() {
        };
    }

    default <T> T assemble(Class<T> composite, final ClassLoader loader, Map<Class<?>, Object> parameters) {
        if (!composite.isInterface()) {
            throw new IllegalArgumentException("Supplied composite class is not an interface: " + composite);
        }
        var mixIns = mixInTypesFor(composite);
        var mixInMap = new HashMap<Class<?>, Integer>();
        for (var i = 0; i < mixIns.length; i++) {
            mixInMap.put(mixIns[i], i);
        }
        if (parameters.size() != mixInMap.size()) {
            throw new IllegalArgumentException(
            "Supplied composite: %s parameters is the wrong size: %s expected: %s".formatted(
            composite.getCanonicalName(), parameters.size(), mixInMap.size()));
        }
        var arguments = new Object[mixInMap.size()];
        for (var pe : parameters.entrySet()) {
            for (var mapping : mixInMap.entrySet()) {
                if (mapping.getKey().isAssignableFrom(pe.getKey())) {
                    arguments[mapping.getValue()] = pe.getValue();
                }
            }
        }

        return assemble(composite, arguments, mixIns, mixInMap, loader, arguments);
    }

    /**
     * Assemble a CompositeOriginal instance implementing the supplied interface using the supplied mix in instances as
     * the constructor arguments
     *
     * @param composite      - the composite interface to implement
     * @param loader         - the class loader to load the generated composite
     * @param mixInInstances - the constructor mixin parameters for the new instance in the declared order of the
     *                       composite interfaces
     * @return the new instance implementing the composite interface, initialzed from the supplied mixin instances
     */
    default <T> T assemble(Class<T> composite, final ClassLoader loader, Object... mixInInstances) {
        if (!composite.isInterface()) {
            throw new IllegalArgumentException("Supplied composite class is not an interface: " + composite);
        }
        var mixIns = mixInTypesFor(composite);
        var mixInMap = new HashMap<Class<?>, Integer>();
        for (var i = 0; i < mixIns.length; i++) {
            mixInMap.put(mixIns[i], i);
        }
        var arguments = new Object[mixInMap.size()];
        for (var mixIn : mixInInstances) {
            for (var mapping : mixInMap.entrySet()) {
                if (mapping.getKey().isAssignableFrom(mixIn.getClass())) {
                    arguments[mapping.getValue()] = mixIn;
                }
            }
        }

        return assemble(composite, arguments, mixIns, mixInMap, loader, mixInInstances);
    }

    /**
     * Generate a CompositeOriginal class implementing the supplied interface using the supplied mix in instances
     * defined by implemented interfaces
     *
     * @param composite
     * @return the bytes of the generated composite class
     */
    default byte[] generateClassBits(Class<?> composite) {
        var compositeDesc = ClassDesc.of(composite.getName());
        var generatedClassName = GENERATED_COMPOSITE_TEMPLATE.formatted(composite.getName());
        var generatedDesc = ClassDesc.of(generatedClassName);

        var mixInTypeMapping = new LinkedHashMap<Class<?>, Integer>();
        var mixInTypes = mixInTypesFor(composite);
        for (var i = 0; i < mixInTypes.length; i++) {
            mixInTypeMapping.put(mixInTypes[i], i);
        }

        return ClassFile.of().build(generatedDesc, classBuilder -> {
            // Set class version to Java 5 to match ASM implementation (V1_5)
            classBuilder.withVersion(JAVA_5_CLASS_VERSION, 0);
            classBuilder.withFlags(ClassFile.ACC_PUBLIC);
            classBuilder.withSuperclass(ConstantDescs.CD_Object);
            classBuilder.withInterfaceSymbols(compositeDesc);

            // Generate constructor
            generateConstructor(classBuilder, generatedDesc, mixInTypeMapping);

            // Generate fields and methods for each mixin
            for (var entry : mixInTypeMapping.entrySet()) {
                var fieldName = MIX_IN_VAR_PREFIX + entry.getValue();
                var mixInDesc = ClassDesc.of(entry.getKey().getName());

                // Add private field for mixin
                classBuilder.withField(fieldName, mixInDesc, ClassFile.ACC_PRIVATE);

                // Process mixin methods
                var mixInModel = getClassModel(entry.getKey());
                processMixInMethods(classBuilder, mixInModel, generatedDesc, fieldName, mixInDesc);
            }
        });
    }

    private void addMixInTypesTo(Class<?> iFace, Set<Class<?>> collected) {
        for (var extended : iFace.getInterfaces()) {
            if (!extended.equals(Object.class)) {
                collected.add(extended);
                addMixInTypesTo(extended, collected);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T assemble(Class<T> composite, Object[] arguments, Class<?>[] mixIns, Map<Class<?>, Integer> mixInMap,
                           final ClassLoader loader, Object... mixInInstances) {
        if (mixInInstances == null) {
            throw new IllegalArgumentException("supplied mixin instances must not be null");
        }
        if (mixInInstances.length != mixInMap.size()) {
            throw new IllegalArgumentException("wrong number of arguments supplied");
        }

        var compositeLoader = loader instanceof CompositeClassLoader l ? l : new CompositeClassLoader(loader);
        var name = GENERATED_COMPOSITE_TEMPLATE.formatted(composite.getName());

        // Thread-safe class caching using computeIfAbsent
        var clazz = (Class<T>) GENERATED_CLASSES.computeIfAbsent(name, key -> {
            try {
                return composite.getClassLoader().loadClass(key);
            } catch (ClassNotFoundException e) {
                return compositeLoader.define(key, generateClassBits(composite));
            }
        });

        var instance = constructInstance(clazz, mixIns, arguments);
        inject(instance, arguments);
        return instance;
    }

    private <T> T constructInstance(Class<T> generated, Class<?>[] mixIns, Object[] arguments) {
        var constructor = getConstructor(generated, mixIns);
        try {
            return constructor.newInstance(arguments);
        } catch (InvocationTargetException e) {
            // Unwrap to show the actual exception thrown by the constructor
            throw new IllegalStateException("Error during composite construction", e.getTargetException());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot construct composite", e);
        }
    }

    /**
     * Generate constructor that matches ASM implementation exactly
     */
    private void generateConstructor(ClassBuilder classBuilder, ClassDesc generatedDesc,
                                     Map<Class<?>, Integer> mixInTypeMapping) {
        // Create ordered array of mixin types for constructor parameters
        var orderedMixIns = new ClassDesc[mixInTypeMapping.size()];
        for (var entry : mixInTypeMapping.entrySet()) {
            orderedMixIns[entry.getValue()] = ClassDesc.of(entry.getKey().getName());
        }

        var constructorType = MethodTypeDesc.of(ConstantDescs.CD_void, orderedMixIns);

        classBuilder.withMethod(ConstantDescs.INIT_NAME, constructorType, ClassFile.ACC_PUBLIC, methodBuilder -> {
            methodBuilder.withCode(codeBuilder -> {
                // Load 'this' and call super constructor Object.<init>()
                codeBuilder.aload(0);
                codeBuilder.invokespecial(ConstantDescs.CD_Object, ConstantDescs.INIT_NAME,
                                          MethodTypeDesc.of(ConstantDescs.CD_void));

                // Initialize all mixin fields
                for (var i = 0; i < orderedMixIns.length; i++) {
                    codeBuilder.aload(0);           // Load 'this'
                    codeBuilder.aload(i + 1);       // Load constructor argument (i+1 because 0 is 'this')
                    var fieldName = MIX_IN_VAR_PREFIX + i;
                    codeBuilder.putfield(generatedDesc, fieldName, orderedMixIns[i]);
                }

                codeBuilder.return_();
            });
        });
    }

    /**
     * Generate a delegation method that calls the corresponding method on the mixin field
     */
    private void generateDelegationMethod(ClassBuilder classBuilder, MethodModel originalMethod,
                                          ClassDesc generatedDesc, String fieldName, ClassDesc mixInDesc) {

        var methodName = originalMethod.methodName().stringValue();
        var methodType = originalMethod.methodTypeSymbol();

        // Convert access flags: remove abstract flag (matches ASM: access = access ^ ACC_ABSTRACT)
        var access = originalMethod.flags().flagsMask() ^ ACC_ABSTRACT;

        // Get exception types from the original method
        var exceptionTypes = getExceptionTypes(originalMethod);

        classBuilder.withMethod(methodName, methodType, access, methodBuilder -> {
            // Add exception declarations if present
            if (exceptionTypes.length > 0) {
                methodBuilder.with(ExceptionsAttribute.ofSymbols(List.of(exceptionTypes)));
            }

            methodBuilder.withCode(codeBuilder -> {
                // Load 'this'
                codeBuilder.aload(0);

                // Get the mixin field
                codeBuilder.getfield(generatedDesc, fieldName, mixInDesc);

                // Load all method arguments
                var paramIndex = 1; // Start at 1 (0 is 'this')
                for (var paramType : methodType.parameterList()) {
                    if (paramType.isPrimitive()) {
                        switch (paramType.descriptorString()) {
                            case "I", "Z", "B", "C", "S" -> codeBuilder.iload(paramIndex);
                            case "J" -> {
                                codeBuilder.lload(paramIndex);
                                paramIndex++;
                            } // long takes 2 slots
                            case "F" -> codeBuilder.fload(paramIndex);
                            case "D" -> {
                                codeBuilder.dload(paramIndex);
                                paramIndex++;
                            } // double takes 2 slots
                        }
                    } else {
                        codeBuilder.aload(paramIndex);
                    }
                    paramIndex++;
                }

                // Call the interface method on the mixin
                codeBuilder.invokeinterface(mixInDesc, methodName, methodType);

                // Return the appropriate value
                var returnType = methodType.returnType();
                if (returnType.equals(ConstantDescs.CD_void)) {
                    codeBuilder.return_();
                } else if (returnType.isPrimitive()) {
                    switch (returnType.descriptorString()) {
                        case "I", "Z", "B", "C", "S" -> codeBuilder.ireturn();
                        case "J" -> codeBuilder.lreturn();
                        case "F" -> codeBuilder.freturn();
                        case "D" -> codeBuilder.dreturn();
                    }
                } else {
                    codeBuilder.areturn();
                }
            });
        });
    }

    private <T> Constructor<T> getConstructor(Class<T> generated, Class<?>[] mixIns) {
        try {
            return generated.getConstructor(mixIns);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Cannot find constructor on generated composite class", e);
        }
    }

    /**
     * Extract exception types from a method model
     */
    private ClassDesc[] getExceptionTypes(MethodModel methodModel) {
        return methodModel.findAttribute(Attributes.exceptions())
                          .map(attr -> attr.exceptions()
                                           .stream()
                                           .map(entry -> entry.asSymbol())
                                           .toArray(ClassDesc[]::new))
                          .orElse(new ClassDesc[0]);
    }

    private void inject(Object value, Field field, Object instance, Class<?> clazz) {
        field.setAccessible(true);
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to set field: " + field + " on class: " + clazz, e);
        }
    }

    private <T> void inject(T instance, Object[] facets) {
        for (var facet : facets) {
            var mixIn = facet.getClass();
            for (var field : mixIn.getDeclaredFields()) {
                if (!injectFacet(field, facets, facet, mixIn)) {
                    injectThis(instance, mixIn, facet, field);
                }
            }
        }
    }

    private boolean injectFacet(Field field, Object[] facets, Object instance, Class<?> clazz) {
        var facetAnnotation = field.getAnnotation(Facet.class);
        if (facetAnnotation != null) {
            for (var facet : facets) {
                if (field.getType().isAssignableFrom(facet.getClass())) {
                    inject(facet, field, instance, clazz);
                    return true;
                }
            }
        }
        return false;
    }

    private <T> boolean injectThis(T instance, Class<?> mixIn, Object mixInInstance, Field field) {
        var thisAnnotation = field.getAnnotation(This.class);
        if (thisAnnotation != null) {
            if (field.getType().isAssignableFrom(instance.getClass())) {
                inject(instance, field, mixInInstance, mixIn);
                return true;
            }
        }
        return false;
    }

    private Class<?>[] mixInTypesFor(Class<?> composite) {
        var mixInTypes = new TreeSet<Class<?>>(Comparator.comparing(Class::getCanonicalName));
        addMixInTypesTo(composite, mixInTypes);
        return mixInTypes.toArray(Class[]::new);
    }

    /**
     * Process methods from a mixin interface and generate delegation methods
     */
    private void processMixInMethods(ClassBuilder classBuilder, ClassModel mixInModel, ClassDesc generatedDesc,
                                     String fieldName, ClassDesc mixInDesc) {

        for (var methodModel : mixInModel.methods()) {
            // Skip static methods (matches ASM implementation logic)
            if ((methodModel.flags().flagsMask() & ACC_STATIC) != 0) {
                continue;
            }

            // Skip constructors
            if (methodModel.methodName().stringValue().equals("<init>")) {
                continue;
            }

            generateDelegationMethod(classBuilder, methodModel, generatedDesc, fieldName, mixInDesc);
        }
    }

    class CompositeClassLoader extends ClassLoader {
        public CompositeClassLoader(ClassLoader parent) {
            super(parent);
        }

        Class<?> define(String compositeName, byte[] definition) {
            return defineClass(compositeName, definition, 0, definition.length);
        }
    }
}
