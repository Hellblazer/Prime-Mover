/**
 * (C) Copyright 2008 Chiral Behaviors, LLC. All Rights Reserved
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
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 *
 */

public class CompositeClassGenerator {
    class Visitor extends ClassVisitor {
        class MVisitor extends MethodVisitor {
            int      access;
            String[] exceptions;
            String   name, desc, signature;

            public MVisitor(int access, String name, String desc,
                            String signature, String[] exceptions,
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
                CompositeClassGenerator.this.visitMethod(mixIn, fieldName,
                                                         access, name, desc,
                                                         signature, exceptions);
            }
        }

        protected String fieldName;

        protected Type   mixIn;

        public Visitor(Type mixIn, String fieldName) {
            super(Opcodes.ASM5);
            this.mixIn = mixIn;
            this.fieldName = fieldName;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String signature, String[] exceptions) {
            if (access == Opcodes.ACC_STATIC) {
                return super.visitMethod(access, name, desc, signature,
                                         exceptions);
            }
            MethodVisitor mv = super.visitMethod(access, name, desc, signature,
                                                 exceptions);
            return new MVisitor(access, name, desc, signature, exceptions, mv);
        }
    }

    public static final String    GENERATED_COMPOSITE_SUFFIX = "$composite";

    protected static final String MIX_IN_VAR_PREFIX          = "mixIn_";

    public static ClassReader getClassReader(Class<?> clazz) {
        Type type = Type.getType(clazz);
        String classResourceName = '/' + type.getInternalName() + ".class";
        InputStream is = clazz.getResourceAsStream(classResourceName);
        if (is == null) {
            throw new VerifyError("cannot read class resource for: "
                                  + classResourceName);
        }
        ClassReader reader;
        try {
            reader = new ClassReader(is);
        } catch (IOException e) {
            VerifyError v = new VerifyError("cannot read class resource for: "
                                            + classResourceName);
            v.initCause(e);
            throw v;
        }
        return reader;
    }

    protected Class<?>               composite;
    protected Type                   compositeType;
    protected Type                   generatedType;
    protected Map<Class<?>, Integer> mixInTypeMapping = new HashMap<Class<?>, Integer>();
    protected Class<?>[]             mixInTypes;
    protected ClassWriter            writer;

    public CompositeClassGenerator(Class<?> composite) {
        this.composite = composite;
        initialize();
    }

    public byte[] generateClassBits() {
        writer = new ClassWriter(COMPUTE_FRAMES);
        writer.visit(V1_5, ACC_PUBLIC, generatedType.getInternalName(), null,
                     Type.getType(Object.class).getInternalName(),
                     new String[] { compositeType.getInternalName() });
        generateConstructor();
        for (Map.Entry<Class<?>, Integer> entry : mixInTypeMapping.entrySet()) {
            String fieldName = MIX_IN_VAR_PREFIX + entry.getValue();
            writer.visitField(ACC_PRIVATE, fieldName,
                              Type.getType(entry.getKey()).getDescriptor(),
                              null, null);
            Visitor visitor = new Visitor(Type.getType(entry.getKey()),
                                          fieldName);
            getClassReader(entry.getKey()).accept(visitor, 0);
        }

        writer.visitEnd();
        return writer.toByteArray();
    }

    public String getGeneratedClassName() {
        return generatedType.getClassName();
    }

    protected void addMixInTypesTo(Class<?> iFace, Set<Class<?>> collected) {
        for (Class<?> extended : iFace.getInterfaces()) {
            if (!extended.equals(Object.class)) {
                collected.add(extended);
                addMixInTypesTo(extended, collected);
            }
        }
    }

    protected void generateConstructor() {
        Type[] orderedMixIns = new Type[mixInTypes.length];
        for (Map.Entry<Class<?>, Integer> entry : mixInTypeMapping.entrySet()) {
            orderedMixIns[entry.getValue()] = Type.getType(entry.getKey());
        }
        Method constructor = new Method(
                                        "<init>",
                                        Type.getMethodDescriptor(Type.VOID_TYPE,
                                                                 orderedMixIns));
        GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC, constructor,
                                                    null, new Type[] {}, writer);
        gen.visitCode();
        gen.loadThis();
        gen.invokeConstructor(Type.getType(Object.class),
                              new Method(
                                         "<init>",
                                         Type.getMethodDescriptor(Type.VOID_TYPE,
                                                                  new Type[] {})));
        for (int i = 0; i < orderedMixIns.length; i++) {
            gen.loadThis();
            gen.loadArg(i);
            gen.putField(generatedType, MIX_IN_VAR_PREFIX + i, orderedMixIns[i]);
        }
        gen.returnValue();
        gen.endMethod();
    }

    protected Map<Class<?>, Integer> getMixInTypeMapping() {
        return mixInTypeMapping;
    }

    protected Class<?>[] getMixInTypes() {
        return mixInTypes;
    }

    protected void initialize() {
        compositeType = Type.getType(composite);
        generatedType = Type.getObjectType(compositeType.getInternalName()
                                           + GENERATED_COMPOSITE_SUFFIX);
        mixInTypes = mixInTypesFor();
        for (int i = 0; i < mixInTypes.length; i++) {
            mixInTypeMapping.put(mixInTypes[i], i);
        }
    }

    protected Class<?>[] mixInTypesFor() {
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

    protected void visitMethod(Type mixIn, String fieldName, int access,
                               String name, String desc, String signature,
                               String[] exceptions) {
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
        GeneratorAdapter gen = new GeneratorAdapter(access, method, null,
                                                    exceptionTypes, writer);
        gen.visitCode();
        gen.loadThis();
        gen.getField(generatedType, fieldName, mixIn);
        gen.loadArgs();
        gen.invokeInterface(mixIn, method);
        gen.returnValue();
        gen.endMethod();
    }

}
