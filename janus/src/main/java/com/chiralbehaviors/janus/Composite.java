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

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.V1_5;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

/**
 * Function to construct and assemble composites
 *
 * @author hal.hildebrand
 */
public interface Composite {

    class CompositeClassLoader extends ClassLoader {
        public CompositeClassLoader(ClassLoader parent) {
            super(parent);
        }

        Class<?> define(String compositeName, byte[] definition) {
            return defineClass(compositeName, definition, 0, definition.length);
        }
    }

    class Visitor extends ClassVisitor {
        class MVisitor extends MethodVisitor {
            final private int      access;
            final private String[] exceptions;
            final private String   name, desc, signature;

            public MVisitor(int access, String name, String desc, String signature, String[] exceptions,
                            MethodVisitor mv) {
                super(Opcodes.ASM5, mv);
                this.access = access;
                this.name = name;
                this.desc = desc;
                this.signature = signature;
                this.exceptions = exceptions;
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
                composite.visitMethod(mixIn, fieldName, access, name, desc, signature, exceptions, generatedType,
                                      writer);
            }
        }

        final private Composite composite;

        final private String      fieldName;
        final private Type        generatedType;
        final private Type        mixIn;
        final private ClassWriter writer;

        public Visitor(Type mixIn, String fieldName, Type generatedType, ClassWriter writer, Composite composite) {
            super(Opcodes.ASM5);
            this.mixIn = mixIn;
            this.fieldName = fieldName;
            this.generatedType = generatedType;
            this.writer = writer;
            this.composite = composite;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (access == Opcodes.ACC_STATIC) {
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            return new MVisitor(access, name, desc, signature, exceptions, mv);
        }
    }

    static final String GENERATED_COMPOSITE_TEMPLATE = "%s$composite";

    static final String MIX_IN_VAR_PREFIX = "mixIn_";

    public static ClassReader getClassReader(Class<?> clazz) {
        Type type = Type.getType(clazz);
        String classResourceName = '/' + type.getInternalName() + ".class";
        InputStream is = clazz.getResourceAsStream(classResourceName);
        if (is == null) {
            throw new VerifyError("cannot read class resource for: " + classResourceName);
        }
        ClassReader reader;
        try {
            reader = new ClassReader(is);
        } catch (IOException e) {
            VerifyError v = new VerifyError("cannot read class resource for: " + classResourceName);
            v.initCause(e);
            throw v;
        }
        return reader;
    }

    static Composite instance() {
        return new Composite() {
        };
    }

    /**
     * Assemble a Composite instance implementing the supplied interface using the
     * supplied mix in instances as the constructor arguments
     * 
     * @param composite      - the composite interface to implement
     * @param loader         - the class loader to load the generated composite
     * @param mixInInstances - the constructor mixin parameters for the new instance
     * @return the new instance implementing the composite interface, initialzed
     *         from the supplied mixin instances
     */
    @SuppressWarnings("unchecked")
    default <T> T assemble(Class<T> composite, final CompositeClassLoader loader, Object... mixInInstances) {
        if (!composite.isInterface()) {
            throw new IllegalArgumentException("Supplied composite class is not an interface: " + composite);
        }
        Map<Class<?>, Integer> mixInMap = new HashMap<Class<?>, Integer>();
        var mixIns = mixInTypesFor(composite);
        for (int i = 0; i < mixIns.length; i++) {
            mixInMap.put(mixIns[i], i);
        }
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
        if (mixInInstances.length != mixIns.length) {
            throw new IllegalArgumentException("wrong number of arguments supplied");
        }
        Object[] arguments = new Object[mixIns.length];
        for (Object mixIn : mixInInstances) {
            for (Map.Entry<Class<?>, Integer> mapping : mixInMap.entrySet()) {
                if (mapping.getKey().isAssignableFrom(mixIn.getClass())) {
                    arguments[mapping.getValue()] = mixIn;
                }
            }
        }
        T instance = constructInstance(clazz, mixIns, arguments);
        inject(instance, arguments);
        return instance;
    }

    default byte[] generateClassBits(Class<?> composite) {
        Type compositeType = Type.getType(composite);
        Type generatedType = Type.getObjectType(GENERATED_COMPOSITE_TEMPLATE.formatted(composite.getName()
                                                                                                .replace('.', '/')));
        ClassWriter writer;
        Map<Class<?>, Integer> mixInTypeMapping = new HashMap<Class<?>, Integer>();
        var mixInTypes = mixInTypesFor(composite);
        for (int i = 0; i < mixInTypes.length; i++) {
            mixInTypeMapping.put(mixInTypes[i], i);
        }
        writer = new ClassWriter(COMPUTE_FRAMES);
        writer.visit(V1_5, ACC_PUBLIC, generatedType.getInternalName(), null,
                     Type.getType(Object.class).getInternalName(), new String[] { compositeType.getInternalName() });
        generateConstructor(generatedType, mixInTypeMapping, writer);
        for (Map.Entry<Class<?>, Integer> entry : mixInTypeMapping.entrySet()) {
            String fieldName = MIX_IN_VAR_PREFIX + entry.getValue();
            writer.visitField(ACC_PRIVATE, fieldName, Type.getType(entry.getKey()).getDescriptor(), null, null);
            Visitor visitor = new Visitor(Type.getType(entry.getKey()), fieldName, generatedType, writer, this);
            getClassReader(entry.getKey()).accept(visitor, 0);
        }

        writer.visitEnd();
        return writer.toByteArray();
    }

    default void visitMethod(Type mixIn, String fieldName, int access, String name, String desc, String signature,
                             String[] exceptions, Type generatedType, ClassVisitor writer) {
        Type[] exceptionTypes;
        if (exceptions != null) {
            exceptionTypes = new Type[exceptions.length];
            int i = 0;
            for (String exception : exceptions) {
                exceptionTypes[i++] = Type.getObjectType(exception);
            }
        } else {
            exceptionTypes = new Type[0];
        }
        access = access ^ ACC_ABSTRACT;
        Method method = new Method(name, desc);
        GeneratorAdapter gen = new GeneratorAdapter(access, method, null, exceptionTypes, writer);
        gen.visitCode();
        gen.loadThis();
        gen.getField(generatedType, fieldName, mixIn);
        gen.loadArgs();
        gen.invokeInterface(mixIn, method);
        gen.returnValue();
        gen.endMethod();
    }

    private void addMixInTypesTo(Class<?> iFace, Set<Class<?>> collected) {
        for (Class<?> extended : iFace.getInterfaces()) {
            if (!extended.equals(Object.class)) {
                collected.add(extended);
                addMixInTypesTo(extended, collected);
            }
        }
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

    private void generateConstructor(Type generatedType, Map<Class<?>, Integer> mixInTypeMapping, ClassWriter writer) {
        Type[] orderedMixIns = new Type[mixInTypeMapping.size()];
        for (Map.Entry<Class<?>, Integer> entry : mixInTypeMapping.entrySet()) {
            orderedMixIns[entry.getValue()] = Type.getType(entry.getKey());
        }
        Method constructor = new Method("<init>", Type.getMethodDescriptor(Type.VOID_TYPE, orderedMixIns));
        GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC, constructor, null, new Type[] {}, writer);
        gen.visitCode();
        gen.loadThis();
        gen.invokeConstructor(Type.getType(Object.class),
                              new Method("<init>", Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] {})));
        for (int i = 0; i < orderedMixIns.length; i++) {
            gen.loadThis();
            gen.loadArg(i);
            gen.putField(generatedType, MIX_IN_VAR_PREFIX + i, orderedMixIns[i]);
        }
        gen.returnValue();
        gen.endMethod();
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
