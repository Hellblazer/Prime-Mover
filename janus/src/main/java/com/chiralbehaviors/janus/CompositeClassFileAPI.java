/**
 * (C) Copyright 2023 Hal Hildebrand. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chiralbehaviors.janus;

import java.io.IOException;
import java.io.InputStream;
import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.constant.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.objectweb.asm.Type;

/**
 * ClassFile API implementation of Composite functionality that provides identical
 * bytecode generation to the ASM-based Composite implementation.
 * 
 * This class generates the same bytecode as the original Composite but uses
 * Java 24's ClassFile API instead of ASM for bytecode manipulation.
 *
 * @author hal.hildebrand
 */
public interface CompositeClassFileAPI {

    class CompositeClassLoader extends ClassLoader {
        public CompositeClassLoader(ClassLoader parent) {
            super(parent);
        }

        Class<?> define(String compositeName, byte[] definition) {
            return defineClass(compositeName, definition, 0, definition.length);
        }
    }

    String GENERATED_COMPOSITE_TEMPLATE = "%s$composite";
    String MIX_IN_VAR_PREFIX = "mixIn_";

    public static ClassModel getClassModel(Class<?> clazz) {
        Type type = Type.getType(clazz);
        String classResourceName = '/' + type.getInternalName() + ".class";
        InputStream is = clazz.getResourceAsStream(classResourceName);
        if (is == null) {
            throw new VerifyError("cannot read class resource for: " + classResourceName);
        }
        
        try {
            byte[] classBytes = is.readAllBytes();
            ClassFile cf = ClassFile.of();
            return cf.parse(classBytes);
        } catch (IOException e) {
            VerifyError v = new VerifyError("cannot read class resource for: " + classResourceName);
            v.initCause(e);
            throw v;
        }
    }

    static CompositeClassFileAPI instance() {
        return new CompositeClassFileAPI() {
        };
    }

    default <T> T assemble(Class<T> composite, final CompositeClassLoader loader, Map<Class<?>, Object> parameters) {
        if (!composite.isInterface()) {
            throw new IllegalArgumentException("Supplied composite class is not an interface: " + composite);
        }
        Map<Class<?>, Integer> mixInMap = new HashMap<Class<?>, Integer>();
        var mixIns = mixInTypesFor(composite);
        int i = 0;
        for (var in : mixInTypesFor(composite)) {
            mixInMap.put(in, i++);
        }
        if (parameters.size() != mixInMap.size()) {
            throw new IllegalArgumentException("Supplied composite: %s parameters is the wrong size: %s expected: %s".formatted(composite.getCanonicalName(),
                                                                                                                                parameters.size(),
                                                                                                                                mixInMap.size()));
        }
        Object[] arguments = new Object[mixInMap.size()];
        for (Map.Entry<Class<?>, Object> pe : parameters.entrySet()) {
            for (Map.Entry<Class<?>, Integer> mapping : mixInMap.entrySet()) {
                if (mapping.getKey().isAssignableFrom(pe.getKey())) {
                    arguments[mapping.getValue()] = pe.getValue();
                }
            }
        }

        return assemble(composite, arguments, mixIns, mixInMap, loader, arguments);
    }

    /**
     * Assemble a Composite instance implementing the supplied interface using the
     * supplied mix in instances as the constructor arguments
     * 
     * @param composite      - the composite interface to implement
     * @param loader         - the class loader to load the generated composite
     * @param mixInInstances - the constructor mixin parameters for the new instance
     *                       in the declared order of the composite interfaces
     * @return the new instance implementing the composite interface, initialzed
     *         from the supplied mixin instances
     */
    default <T> T assemble(Class<T> composite, final CompositeClassLoader loader, Object... mixInInstances) {
        if (!composite.isInterface()) {
            throw new IllegalArgumentException("Supplied composite class is not an interface: " + composite);
        }
        Map<Class<?>, Integer> mixInMap = new HashMap<Class<?>, Integer>();
        var mixIns = mixInTypesFor(composite);
        int i = 0;
        for (var in : mixInTypesFor(composite)) {
            mixInMap.put(in, i++);
        }
        Object[] arguments = new Object[mixInMap.size()];
        for (Object mixIn : mixInInstances) {
            for (Map.Entry<Class<?>, Integer> mapping : mixInMap.entrySet()) {
                if (mapping.getKey().isAssignableFrom(mixIn.getClass())) {
                    arguments[mapping.getValue()] = mixIn;
                }
            }
        }

        return assemble(composite, arguments, mixIns, mixInMap, loader, mixInInstances);
    }

    /**
     * Generate a Composite class implementing the supplied interface using the
     * supplied mix in instances defined by implemented interfaces
     *
     * @param composite
     * @return the bytes of the generated composite class
     */
    default byte[] generateClassBits(Class<?> composite) {
        ClassDesc compositeDesc = ClassDesc.of(composite.getName());
        String generatedClassName = GENERATED_COMPOSITE_TEMPLATE.formatted(composite.getName());
        ClassDesc generatedDesc = ClassDesc.of(generatedClassName);
        
        Map<Class<?>, Integer> mixInTypeMapping = new LinkedHashMap<Class<?>, Integer>();
        var mixInTypes = mixInTypesFor(composite);
        for (int i = 0; i < mixInTypes.length; i++) {
            mixInTypeMapping.put(mixInTypes[i], i);
        }

        ClassFile cf = ClassFile.of();
        return cf.build(generatedDesc, classBuilder -> {
            // Set class version to Java 5 (49) to match ASM implementation (V1_5)
            classBuilder.withVersion(49, 0);
            classBuilder.withFlags(ClassFile.ACC_PUBLIC);
            classBuilder.withSuperclass(ConstantDescs.CD_Object);
            classBuilder.withInterfaceSymbols(compositeDesc);

            // Generate constructor
            generateConstructor(classBuilder, generatedDesc, mixInTypeMapping);

            // Generate fields and methods for each mixin
            for (Map.Entry<Class<?>, Integer> entry : mixInTypeMapping.entrySet()) {
                String fieldName = MIX_IN_VAR_PREFIX + entry.getValue();
                ClassDesc mixInDesc = ClassDesc.of(entry.getKey().getName());
                
                // Add private field for mixin
                classBuilder.withField(fieldName, mixInDesc, ClassFile.ACC_PRIVATE);
                
                // Process mixin methods
                ClassModel mixInModel = getClassModel(entry.getKey());
                processMixInMethods(classBuilder, mixInModel, generatedDesc, fieldName, mixInDesc);
            }
        });
    }

    /**
     * Generate constructor that matches ASM implementation exactly
     */
    private void generateConstructor(ClassBuilder classBuilder, ClassDesc generatedDesc, Map<Class<?>, Integer> mixInTypeMapping) {
        // Create ordered array of mixin types for constructor parameters
        ClassDesc[] orderedMixIns = new ClassDesc[mixInTypeMapping.size()];
        for (Map.Entry<Class<?>, Integer> entry : mixInTypeMapping.entrySet()) {
            orderedMixIns[entry.getValue()] = ClassDesc.of(entry.getKey().getName());
        }

        MethodTypeDesc constructorType = MethodTypeDesc.of(ConstantDescs.CD_void, orderedMixIns);
        
        classBuilder.withMethod(ConstantDescs.INIT_NAME, constructorType, ClassFile.ACC_PUBLIC, methodBuilder -> {
            methodBuilder.withCode(codeBuilder -> {
                // Load 'this' and call super constructor Object.<init>()
                codeBuilder.aload(0);
                codeBuilder.invokespecial(ConstantDescs.CD_Object, ConstantDescs.INIT_NAME, 
                    MethodTypeDesc.of(ConstantDescs.CD_void));

                // Initialize all mixin fields
                for (int i = 0; i < orderedMixIns.length; i++) {
                    codeBuilder.aload(0);           // Load 'this'
                    codeBuilder.aload(i + 1);       // Load constructor argument (i+1 because 0 is 'this')
                    String fieldName = MIX_IN_VAR_PREFIX + i;
                    codeBuilder.putfield(generatedDesc, fieldName, orderedMixIns[i]);
                }
                
                codeBuilder.return_();
            });
        });
    }

    /**
     * Process methods from a mixin interface and generate delegation methods
     */
    private void processMixInMethods(ClassBuilder classBuilder, ClassModel mixInModel, 
                                   ClassDesc generatedDesc, String fieldName, ClassDesc mixInDesc) {
        
        for (MethodModel methodModel : mixInModel.methods()) {
            // Skip static methods (matches ASM implementation logic)
            if ((methodModel.flags().flagsMask() & 0x0008) != 0) { // ACC_STATIC = 8
                continue;
            }
            
            // Skip constructors
            if (methodModel.methodName().stringValue().equals("<init>")) {
                continue;
            }

            generateDelegationMethod(classBuilder, methodModel, generatedDesc, fieldName, mixInDesc);
        }
    }

    /**
     * Generate a delegation method that calls the corresponding method on the mixin field
     */
    private void generateDelegationMethod(ClassBuilder classBuilder, MethodModel originalMethod,
                                        ClassDesc generatedDesc, String fieldName, ClassDesc mixInDesc) {
        
        String methodName = originalMethod.methodName().stringValue();
        MethodTypeDesc methodType = originalMethod.methodTypeSymbol();
        
        // Convert access flags: remove abstract flag (matches ASM: access = access ^ ACC_ABSTRACT)
        int access = originalMethod.flags().flagsMask();
        access = access ^ 0x0400; // ACC_ABSTRACT = 1024
        
        // Get exception types from the original method
        ClassDesc[] exceptionTypes = getExceptionTypes(originalMethod);

        classBuilder.withMethod(methodName, methodType, access, methodBuilder -> {
            // Add exception declarations if present
            if (exceptionTypes.length > 0) {
                methodBuilder.with(ExceptionsAttribute.ofSymbols(java.util.List.of(exceptionTypes)));
            }
            
            methodBuilder.withCode(codeBuilder -> {
                // Load 'this'
                codeBuilder.aload(0);
                
                // Get the mixin field
                codeBuilder.getfield(generatedDesc, fieldName, mixInDesc);
                
                // Load all method arguments
                int paramIndex = 1; // Start at 1 (0 is 'this')
                for (ClassDesc paramType : methodType.parameterList()) {
                    if (paramType.isPrimitive()) {
                        switch (paramType.descriptorString()) {
                            case "I", "Z", "B", "C", "S" -> codeBuilder.iload(paramIndex);
                            case "J" -> { codeBuilder.lload(paramIndex); paramIndex++; } // long takes 2 slots
                            case "F" -> codeBuilder.fload(paramIndex);
                            case "D" -> { codeBuilder.dload(paramIndex); paramIndex++; } // double takes 2 slots
                        }
                    } else {
                        codeBuilder.aload(paramIndex);
                    }
                    paramIndex++;
                }
                
                // Call the interface method on the mixin
                codeBuilder.invokeinterface(mixInDesc, methodName, methodType);
                
                // Return the appropriate value
                ClassDesc returnType = methodType.returnType();
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

    /**
     * Extract exception types from a method model
     */
    private ClassDesc[] getExceptionTypes(MethodModel methodModel) {
        return methodModel.findAttribute(Attributes.exceptions())
            .map(attr -> attr.exceptions().stream()
                .map(entry -> entry.asSymbol())
                .toArray(ClassDesc[]::new))
            .orElse(new ClassDesc[0]);
    }

    private void addMixInTypesTo(Class<?> iFace, Set<Class<?>> collected) {
        for (Class<?> extended : iFace.getInterfaces()) {
            if (!extended.equals(Object.class)) {
                collected.add(extended);
                addMixInTypesTo(extended, collected);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T assemble(Class<T> composite, Object[] arguments, Class<?>[] mixIns, Map<Class<?>, Integer> mixInMap,
                           final CompositeClassLoader loader, Object... mixInInstances) {
        Class<T> clazz;

        final var name = GENERATED_COMPOSITE_TEMPLATE.formatted(composite.getName());
        try {
            clazz = (Class<T>) composite.getClassLoader().loadClass(name.replace('.', '/'));
        } catch (ClassNotFoundException e) {
            clazz = (Class<T>) loader.define(name, generateClassBits(composite));
        }
        if (mixInInstances == null) {
            throw new IllegalArgumentException("supplied mixin instances must not be null");
        }
        if (mixInInstances.length != mixInMap.size()) {
            throw new IllegalArgumentException("wrong number of arguments supplied");
        }
        T instance = constructInstance(clazz, mixIns, arguments);
        inject(instance, arguments);
        return instance;
    }

    private <T> T constructInstance(Class<T> generated, Class<?>[] mixIns, Object[] arguments) {
        Constructor<T> constructor = getConstructor(generated, mixIns);
        try {
            return constructor.newInstance(arguments);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Illegal arguments in constructing composite", e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Unexpected error in constructing composite", e.getTargetException());
        } catch (InstantiationException e) {
            throw new IllegalStateException("Cannot instantiate composite", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access constructor for composite", e);
        }
    }

    private <T> Constructor<T> getConstructor(Class<T> generated, Class<?>[] mixIns) {
        Constructor<T> constructor;
        try {
            constructor = generated.getConstructor(mixIns);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Cannot find constructor on generated composite class", e);
        }
        return constructor;
    }

    private void inject(Object value, Field field, Object instance, Class<?> clazz) {
        field.setAccessible(true);
        try {
            field.set(instance, value);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Field: " + field + " is not a part of class: " + clazz, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to set field: " + field + " on class: " + clazz, e);
        }
    }

    private <T> void inject(T instance, Object[] facets) {
        for (int i = 0; i < facets.length; i++) {
            Class<?> mixIn = facets[i].getClass();
            Object mixInInstance = facets[i];
            for (Field field : mixIn.getDeclaredFields()) {
                if (!injectFacet(field, facets, mixInInstance, mixIn)) {
                    injectThis(instance, mixIn, mixInInstance, field);
                }
            }
        }
    }

    private <T> boolean injectFacet(Field field, Object[] facets, Object instance, Class<?> clazz) {
        Facet facetAnnotation = field.getAnnotation(Facet.class);
        if (facetAnnotation != null) {
            for (Object facet : facets) {
                if (field.getType().isAssignableFrom(facet.getClass())) {
                    inject(facet, field, instance, clazz);
                    return true;
                }
            }
        }
        return false;
    }

    private <T> boolean injectThis(T instance, Class<?> mixIn, Object mixInInstance, Field field) {
        This thisAnnotation = field.getAnnotation(This.class);
        if (thisAnnotation != null) {
            if (field.getType().isAssignableFrom(instance.getClass())) {
                inject(instance, field, mixInInstance, mixIn);
                return true;
            }
        }
        return false;
    }

    private Class<?>[] mixInTypesFor(Class<?> composite) {
        Comparator<Class<?>> comparator = new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                return o1.getCanonicalName().compareTo(o2.getCanonicalName());
            }
        };
        Set<Class<?>> mixInTypes = new TreeSet<Class<?>>(comparator);
        addMixInTypesTo(composite, mixInTypes);
        return mixInTypes.toArray(new Class<?>[mixInTypes.size()]);
    }
}