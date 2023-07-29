/*
 * Copyright (C) 2023 Hal Hildebrand. All rights reserved.
 * 
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.hellblazer.primeMover.asm;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.commons.MethodRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.commons.TableSwitchGenerator;

import com.hellblazer.primeMover.Kronos;
import com.hellblazer.primeMover.annotations.Transformed;
import com.hellblazer.primeMover.runtime.Devi;
import com.hellblazer.primeMover.runtime.EntityReference;
import com.hellblazer.primeMover.runtime.Framework;
import com.hellblazer.primeMover.runtime.Kairos;
import com.hellblazer.primeMover.soot.util.OpenAddressingSet.OpenSet;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;

/**
 * Transforms Entity classes into PrimeMove entities.
 *
 * @author hal.hildebrand
 */
public class EntityGenerator {
    private class InitVisitor extends AdviceAdapter {

        protected InitVisitor(int api, MethodVisitor methodVisitor, int access, String name, String descriptor) {
            super(api, methodVisitor, access, name, descriptor);
        }

        @Override
        protected void onMethodEnter() {
            super.onMethodEnter();
            loadThis();
            invokeStatic(Type.getType(Framework.class), GET_CONTROLLER_METHOD);
            putField(type, CONTROLLER, Type.getType(Devi.class));
        }

        @Override
        protected void onMethodExit(int opcode) {
            visitMaxs(0, 0);
            super.onMethodExit(opcode);
        }

    }

    private static final String BIND_TO                   = "__bindTo";
    private static final Method BIND_TO_METHOD;
    private static final String CONTROLLER                = "$controller";
    private static final String GET_CONTROLLER            = "getController";
    private static final Method GET_CONTROLLER_METHOD;
    private static final Object INIT                      = "<init>";
    private static final String INVOKE                    = "__invoke";
    private static final Method INVOKE_METHOD;
    private static final String METHOD_REMAP_KEY_TEMPLATE = "%s.%s%s";
    private static final Type   OBJECT_TYPE               = Type.getType(Object.class);
    private static final String POST_CONTINUING_EVENT     = "postContinuingEvent";
    private static final Method POST_CONTINUING_EVENT_METHOD;
    private static final String POST_EVENT                = "postEvent";
    private static final Method POST_EVENT_METHOD;
    private static final String REMAPPED_TEMPLATE         = "gen$%s";
    private static final String SIGNATURE_FOR             = "__signatureFor";
    private static final Method SIGNATURE_FOR_METHOD;

    static {
        java.lang.reflect.Method method;
        try {
            method = EntityReference.class.getMethod(BIND_TO, Devi.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(BIND_TO), e);
        }
        try {
            BIND_TO_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(BIND_TO), e);
        }
        try {
            method = Devi.class.getMethod(POST_CONTINUING_EVENT, EntityReference.class, int.class, Object[].class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(POST_CONTINUING_EVENT), e);
        }
        try {
            POST_CONTINUING_EVENT_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(POST_CONTINUING_EVENT), e);
        }
        try {
            method = Devi.class.getMethod(POST_EVENT, EntityReference.class, int.class, Object[].class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(POST_EVENT), e);
        }
        try {
            POST_EVENT_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(POST_EVENT), e);
        }
        try {
            method = Framework.class.getMethod(GET_CONTROLLER);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(GET_CONTROLLER), e);
        }
        try {
            GET_CONTROLLER_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(GET_CONTROLLER), e);
        }
        try {
            method = EntityReference.class.getMethod(SIGNATURE_FOR, int.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(SIGNATURE_FOR), e);
        }
        try {
            SIGNATURE_FOR_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(SIGNATURE_FOR), e);
        }
        try {
            method = EntityReference.class.getMethod(INVOKE, int.class, Object[].class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(INVOKE), e);
        }
        try {
            INVOKE_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(INVOKE), e);
        }
    }

    private final ClassInfo                clazz;
    private final Set<Method>              events;
    private final String                   internalName;
    private final Map<Method, Integer>     inverse;
    private final Map<Integer, MethodInfo> mapped;
    private final Set<MethodInfo>          remapped;
    private final Type                     type;

    public EntityGenerator(ClassInfo clazz, Set<MethodInfo> events) {
        this.clazz = clazz;
        type = Type.getObjectType(clazz.getName().replace('.', '/'));
        internalName = clazz.getName().replace('.', '/');
        mapped = new HashMap<>();
        remapped = new OpenSet<>();
        inverse = new HashMap<>();
        this.events = new OpenSet<>();
        var key = 0;
        for (var mi : events.stream().sorted().toList()) {
            mapped.put(key, mi);
            final var event = new Method(mi.getName(), mi.getTypeDescriptorStr());
            inverse.put(event, key++);
            this.events.add(event);
            final boolean declared = clazz.getDeclaredMethodInfo(mi.getName())
                                          .stream()
                                          .filter(m -> mi.equals(m))
                                          .findFirst()
                                          .isPresent();
            if (declared) {
                remapped.add(mi);
            }
        }
    }

    public ClassWriter generate() throws MalformedURLException, IOException {
        try (var is = clazz.getResource().open()) {
            final var classReader = new ClassReader(is);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            var transform = eventTransform(cw);
            classReader.accept(transform, ClassReader.EXPAND_FRAMES);
            final var interfaces = clazz.getInterfaces()
                                        .stream()
                                        .map(ci -> ci.getName().replace('.', '/'))
                                        .collect(Collectors.toList());
            interfaces.add(Type.getType(EntityReference.class).getInternalName());
            var av = transform.visitAnnotation(Type.getType(Transformed.class).getDescriptor(), true);
            av.visit("comment", "PrimeMover ASM Event Transform");
            av.visit("date", Instant.now().toString());
            av.visit("value", "PrimeMover ASM");
            av.visitEnd();
            var fieldVisitor = transform.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, CONTROLLER,
                                                    Type.getType(Devi.class).getDescriptor(), null, null);
            fieldVisitor.visitEnd();
            generateInvoke(cw);
            generateSignatureFor(cw);
            generateBindTo(cw);
            cw.visit(clazz.getClassfileMajorVersion(), clazz.getModifiers(), internalName, clazz.getTypeSignatureStr(),
                     clazz.getSuperclass() == null ? Type.getInternalName(Object.class)
                                                   : clazz.getSuperclass().getName().replace('.', '/'),
                     interfaces.toArray(new String[0]));

            cw.visitEnd();
            return cw;
        }
    }

    public ClassInfo getClazz() {
        return clazz;
    }

    private TableSwitchGenerator eventSwitch(GeneratorAdapter adapter) {
        return new TableSwitchGenerator() {

            @Override
            public void generateCase(int key, Label end) {
                var mi = mapped.get(key);
                if (mi == null) {
                    throw new IllegalArgumentException("no key: " + key + " in mapped");
                }
                final var locals = new Object[] { internalName, Opcodes.INTEGER,
                                                  Type.getType(Object[].class).getInternalName() };
                var stack = new Stack<Object>();
                stack.push(internalName);

                adapter.visitFrame(Opcodes.F_NEW, 3, locals, 0, new Object[] {});

                adapter.loadThis();
                adapter.visitFrame(Opcodes.F_NEW, 3, locals, 1, stack.toArray(new Object[0]));

                int i = 0;

                for (var pi : mi.getParameterInfo()) {
                    adapter.loadArg(1);
                    stack.push(Type.getType(Object[].class).getInternalName());
                    adapter.visitFrame(Opcodes.F_NEW, 3, locals, stack.size(), stack.toArray(new Object[0]));

                    adapter.push(i++);
                    stack.push(Opcodes.INTEGER);
                    adapter.visitFrame(Opcodes.F_NEW, 3, locals, stack.size(), stack.toArray(new Object[0]));

                    adapter.arrayLoad(OBJECT_TYPE);

                    stack.pop();
                    stack.pop();

                    stack.add(Type.getType(Object.class).getInternalName());
                    adapter.visitFrame(Opcodes.F_NEW, 3, locals, stack.size(), stack.toArray(new Object[0]));
                    final var paramInternalName = pi.getTypeDescriptor().toString().replace('.', '/');
                    adapter.checkCast(Type.getObjectType(paramInternalName));
                    stack.pop();
                    stack.push(paramInternalName);
                    adapter.visitFrame(Opcodes.F_NEW, 3, locals, stack.size(), stack.toArray(new Object[0]));
                }

                if (remapped.contains(mi)) {
                    adapter.invokeVirtual(type, new Method(REMAPPED_TEMPLATE.formatted(mi.getName()),
                                                           mi.getTypeDescriptorStr()));
                } else {
                    adapter.invokeVirtual(type, Method.getMethod(mi.toString()));
                }
                if (mi.getTypeDescriptor().getResultType().toStringWithSimpleNames().equals("void")) {
                    adapter.loadThis();
                }
                adapter.returnValue();
            }

            @Override
            public void generateDefault() {
                adapter.visitFrame(Opcodes.F_NEW, 3, new Object[] { internalName, Opcodes.INTEGER,
                                                                    Type.getType(Object[].class).getInternalName() },
                                   0, new Object[] {});
                adapter.throwException(Type.getType(IllegalStateException.class), "Unknown event type");
            }
        };
    }

    private ClassVisitor eventTransform(ClassVisitor cv) {
        var mappings = new HashMap<String, String>();
        for (var mi : remapped) {
            mappings.put(METHOD_REMAP_KEY_TEMPLATE.formatted(clazz.getName().replace('.', '/'), mi.getName(),
                                                             mi.getTypeDescriptorStr()),
                         REMAPPED_TEMPLATE.formatted(mi.getName()));
        }
        var eventRemapper = new SimpleRemapper(mappings);

        var map = new HashMap<String, String>();
        map.put(Kronos.class.getCanonicalName().replace('.', '/'), Kairos.class.getCanonicalName().replace('.', '/'));
        var apiRemapper = new SimpleRemapper(map);

        return new ClassVisitor(Opcodes.ASM9, new ClassRemapper(cv, apiRemapper)) {

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                             String[] exceptions) {
                if (name.equals(INIT)) {
                    return new InitVisitor(Opcodes.ASM9,
                                           super.visitMethod(access, name, descriptor, signature, exceptions), access,
                                           name, descriptor);
                }
                var m = new Method(name, descriptor);
                if (events.contains(m)) {
                    generateEvent(m, access, name, descriptor, exceptions, cv);
                }
                return remapMethod(access, name, descriptor, signature, exceptions);
            }

            private MethodVisitor remapMethod(int access, String name, String descriptor, String signature,
                                              String[] exceptions) {
                final var renamed = eventRemapper.mapMethodName(type.getInternalName(), name, descriptor);
                if (!renamed.equals(name)) {
                    access = Opcodes.ACC_PRIVATE;
                }
                var methodVisitor = super.visitMethod(access, renamed, descriptor, signature,
                                                      exceptions == null ? null : exceptions);
                return methodVisitor == null ? null : new MethodRemapper(methodVisitor, eventRemapper);
            }
        };
    }

    private void generateBindTo(ClassVisitor cv) {
        var mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, BIND_TO_METHOD, null, null, cv);
        mg.loadThis();
        mg.returnValue();
        mg.visitMaxs(0, 0);
        mg.endMethod();
    }

    private void generateEvent(Method m, int access, String name, String descriptor, String[] exceptions,
                               ClassVisitor cv) {
        var mg = new GeneratorAdapter(access, m, descriptor,
                                      exceptions == null ? null
                                                         : Arrays.stream(exceptions)
                                                                 .map(t -> Type.getObjectType(t))
                                                                 .toList()
                                                                 .toArray(new Type[0]),
                                      cv);
        mg.loadThis();
        mg.getField(type, CONTROLLER, Type.getType(Devi.class));
        mg.loadThis();
        mg.push(inverse.get(m));
        var objectType = OBJECT_TYPE;
        mg.push(m.getArgumentTypes().length);
        mg.newArray(objectType);
        for (int i = 0; i < m.getArgumentTypes().length; i++) {
            mg.dup();
            mg.push(i);
            mg.loadArg(i);
            mg.arrayStore(objectType);
        }

        if (Type.VOID_TYPE.equals(m.getReturnType())) {
            mg.invokeVirtual(Type.getType(Devi.class), POST_EVENT_METHOD);
        } else {
            mg.invokeVirtual(Type.getType(Devi.class), POST_CONTINUING_EVENT_METHOD);
            mg.checkCast(m.getReturnType());
        }
        mg.visitMaxs(0, 0);
        mg.returnValue();
    }

    private void generateInvoke(ClassVisitor cv) {
        var mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, INVOKE_METHOD, null,
                                      new Type[] { Type.getType(Throwable.class) }, cv);
        var keys = mapped.keySet().stream().mapToInt(i -> (int) i).sorted().toArray();

        mg.loadArg(0);
        mg.tableSwitch(keys, eventSwitch(mg));
        mg.visitMaxs(0, 0);
        mg.endMethod();
    }

    private void generateSignatureFor(ClassVisitor cv) {
        var mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, SIGNATURE_FOR_METHOD, null, null, cv);
        var keys = mapped.keySet().stream().mapToInt(i -> (int) i).sorted().toArray();
        mg.loadArg(0);
        mg.tableSwitch(keys, signatureSwitch(mg));
        mg.visitMaxs(0, 0);
        mg.endMethod();
    }

    private TableSwitchGenerator signatureSwitch(GeneratorAdapter adapter) {
        return new TableSwitchGenerator() {
            @Override
            public void generateCase(int key, Label end) {
                adapter.visitFrame(Opcodes.F_NEW, 2, new Object[] { internalName, Opcodes.INTEGER }, 0, new Object[0]);
                var mi = mapped.get(key);
                if (mi == null) {
                    throw new IllegalArgumentException("no key: " + key + " in mapped");
                }
                adapter.push(mi.toStringWithSimpleNames());
                adapter.visitFrame(Opcodes.F_NEW, 2, new Object[] { internalName, Opcodes.INTEGER }, 1,
                                   new Object[] { Type.getType(String.class).getInternalName() });
                adapter.returnValue();
            }

            @Override
            public void generateDefault() {
                adapter.visitFrame(Opcodes.F_NEW, 2, new Object[] { internalName, Opcodes.INTEGER }, 0, new Object[0]);
                adapter.throwException(Type.getType(IllegalArgumentException.class), "Unknown event");
            }
        };
    }
}
