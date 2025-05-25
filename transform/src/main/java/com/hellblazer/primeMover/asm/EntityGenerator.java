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
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.commons.TableSwitchGenerator;

import com.hellblazer.primeMover.Kronos;
import com.hellblazer.primeMover.annotations.Blocking;
import com.hellblazer.primeMover.annotations.Entity;
import com.hellblazer.primeMover.annotations.Transformed;
import com.hellblazer.primeMover.asm.OpenAddressingSet.OpenSet;
import com.hellblazer.primeMover.runtime.Devi;
import com.hellblazer.primeMover.runtime.EntityReference;
import com.hellblazer.primeMover.runtime.Framework;
import com.hellblazer.primeMover.runtime.Kairos;

import io.github.classgraph.ArrayTypeSignature;
import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.MethodParameterInfo;

/**
 * Transforms Entity classes into PrimeMover entities.
 *
 * @author hal.hildebrand
 */
public class EntityGenerator {

    private static final String APPEND                    = "append";
    private static final Method APPEND_METHOD;
    private static final String BOOLEAN_VALUE             = "booleanValue";
    private static final Method BOOLEAN_VALUE_METHOD;
    private static final Method BOOLEAN_VALUE_OF_METHOD;
    private static final String BYTE_VALUE                = "byteValue";
    private static final Method BYTE_VALUE_METHOD;
    private static final Method BYTE_VALUE_OF_METHOD;
    private static final String CHARACTER_VALUE           = "charValue";
    private static final Method CHARACTER_VALUE_METHOD;
    private static final Method CHARACTER_VALUE_OF_METHOD;
    private static final String DOUBLE_VALUE              = "doubleValue";
    private static final Method DOUBLE_VALUE_METHOD;
    private static final Method DOUBLE_VALUE_OF_METHOD;
    private static final String FLOAT_VALUE               = "floatValue";
    private static final Method FLOAT_VALUE_METHOD;
    private static final Method FLOAT_VALUE_OF_METHOD;
    private static final String GET_CONTROLLER            = "getController";
    private static final Method GET_CONTROLLER_METHOD;
    private static final String INT_VALUE                 = "intValue";
    private static final Method INT_VALUE_METHOD;
    private static final Method INTEGER_VALUE_OF_METHOD;
    private static final String INVOKE                    = "__invoke";
    private static final Method INVOKE_METHOD;
    private static final String LONG_VALUE                = "longValue";
    private static final Method LONG_VALUE_METHOD;
    private static final Method LONG_VALUE_OF_METHOD;
    private static final String METHOD_REMAP_KEY_TEMPLATE = "%s.%s%s";
    private static final Type   OBJECT_TYPE               = Type.getType(Object.class);
    private static final String POST_CONTINUING_EVENT     = "postContinuingEvent";
    private static final Method POST_CONTINUING_EVENT_METHOD;
    private static final String POST_EVENT                = "postEvent";
    private static final Method POST_EVENT_METHOD;
    private static final String REMAPPED_TEMPLATE         = "%s$event";
    private static final String SHORT_VALUE               = "shortValue";
    private static final Method SHORT_VALUE_METHOD;
    private static final Method SHORT_VALUE_OF_METHOD;
    private static final String SIGNATURE_FOR             = "__signatureFor";
    private static final Method SIGNATURE_FOR_METHOD;
    private static final Method STRING_BUILDER_CONSTRUCTOR;
    private static final Type   STRING_BUILDER_TYPE       = Type.getType(StringBuilder.class);
    private static final String TO_STRING                 = "toString";
    private static final Method TO_STRING_METHOD;
    private static final String VALUE_OF                  = "valueOf";

    private final Set<Method>              blocking;
    private final ClassInfo                clazz;
    private final Set<Method>              events;
    private final String                   internalName;
    private final Map<Method, Integer>     inverse;
    private final Map<Integer, MethodInfo> mapped;
    private final Set<MethodInfo>          remapped;
    private final String                   timestamp;
    private final Type                     type;

    static {
        java.lang.reflect.Method method;
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
        try {
            method = Boolean.class.getMethod(VALUE_OF, boolean.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            BOOLEAN_VALUE_OF_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            method = Character.class.getMethod(VALUE_OF, char.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            CHARACTER_VALUE_OF_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            method = Byte.class.getMethod(VALUE_OF, byte.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            BYTE_VALUE_OF_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            method = Double.class.getMethod(VALUE_OF, double.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            DOUBLE_VALUE_OF_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            method = Float.class.getMethod(VALUE_OF, float.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            FLOAT_VALUE_OF_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            method = Integer.class.getMethod(VALUE_OF, int.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            INTEGER_VALUE_OF_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            method = Long.class.getMethod(VALUE_OF, long.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            LONG_VALUE_OF_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            method = Short.class.getMethod(VALUE_OF, short.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            SHORT_VALUE_OF_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }

        try {
            method = Boolean.class.getMethod(BOOLEAN_VALUE);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(BOOLEAN_VALUE), e);
        }
        try {
            BOOLEAN_VALUE_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(BOOLEAN_VALUE), e);
        }
        try {
            method = Character.class.getMethod(CHARACTER_VALUE);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(CHARACTER_VALUE), e);
        }
        try {
            CHARACTER_VALUE_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(CHARACTER_VALUE), e);
        }
        try {
            method = Byte.class.getMethod(BYTE_VALUE);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(BYTE_VALUE), e);
        }
        try {
            BYTE_VALUE_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(BYTE_VALUE), e);
        }
        try {
            method = Double.class.getMethod(DOUBLE_VALUE);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(DOUBLE_VALUE), e);
        }
        try {
            DOUBLE_VALUE_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(DOUBLE_VALUE), e);
        }
        try {
            method = Float.class.getMethod(FLOAT_VALUE);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(FLOAT_VALUE), e);
        }
        try {
            FLOAT_VALUE_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(FLOAT_VALUE), e);
        }
        try {
            method = Integer.class.getMethod(INT_VALUE);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(INT_VALUE), e);
        }
        try {
            INT_VALUE_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(INT_VALUE), e);
        }
        try {
            method = Long.class.getMethod(LONG_VALUE);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(LONG_VALUE), e);
        }
        try {
            LONG_VALUE_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(LONG_VALUE), e);
        }
        try {
            method = Short.class.getMethod(SHORT_VALUE);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(SHORT_VALUE), e);
        }
        try {
            SHORT_VALUE_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(SHORT_VALUE), e);
        }
        try {
            method = StringBuilder.class.getMethod(APPEND, String.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(APPEND), e);
        }
        try {
            APPEND_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(APPEND), e);
        }
        try {
            method = StringBuilder.class.getMethod(TO_STRING);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(TO_STRING), e);
        }
        try {
            TO_STRING_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(TO_STRING), e);
        }
        Constructor<StringBuilder> constructor;
        try {
            constructor = StringBuilder.class.getConstructor();
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get constructor", e);
        }
        STRING_BUILDER_CONSTRUCTOR = Method.getMethod(constructor);
    }

    public EntityGenerator(ClassInfo clazz, Set<MethodInfo> events) {
        this(clazz, events, Instant.now().toString());
    }
    
    public EntityGenerator(ClassInfo clazz, Set<MethodInfo> events, String timestamp) {
        this.clazz = clazz;
        this.timestamp = timestamp;
        type = Type.getObjectType(clazz.getName().replace('.', '/'));
        internalName = clazz.getName().replace('.', '/');
        mapped = new HashMap<>();
        remapped = new OpenSet<>();
        blocking = new OpenSet<>();
        inverse = new HashMap<>();
        this.events = new OpenSet<>();
        var key = 0;
        for (var mi : events.stream().sorted().toList()) {
            mapped.put(key, mi);
            final var event = new Method(mi.getName(), mi.getTypeDescriptorStr());
            inverse.put(event, key++);
            this.events.add(event);
            if (!mi.getTypeDescriptor().getResultType().toString().equals("void") || mi.hasAnnotation(Blocking.class)) {
                blocking.add(event);
            }
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

    public void appendStrings(GeneratorAdapter mg, String... strings) {
        mg.newInstance(STRING_BUILDER_TYPE);
        mg.dup();
        mg.invokeConstructor(STRING_BUILDER_TYPE, STRING_BUILDER_CONSTRUCTOR);
        for (var s : strings) {
            mg.push(s);
            mg.invokeVirtual(STRING_BUILDER_TYPE, APPEND_METHOD);
        }

        mg.invokeVirtual(STRING_BUILDER_TYPE, TO_STRING_METHOD);
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
            av.visit("date", timestamp);
            av.visit("value", "PrimeMover ASM");
            av.visitEnd();
            generateInvoke(cw);
            generateSignatureFor(cw);
            cw.visit(clazz.getClassfileMajorVersion(), clazz.getModifiers(), internalName, clazz.getTypeSignatureStr(),
                     clazz.getSuperclass() == null ? Type.getInternalName(Object.class)
                                                   : clazz.getSuperclass().getName().replace('.', '/'),
                     interfaces.toArray(new String[0]));

            cw.visitEnd();
            return cw;
        }
    }

    private void boxIt(GeneratorAdapter adapter, Type type) {
        switch (type.getSort()) {
        case Type.BYTE:
            adapter.invokeStatic(Type.getType(Byte.class), BYTE_VALUE_OF_METHOD);
            break;
        case Type.CHAR:
            adapter.invokeStatic(Type.getType(Character.class), CHARACTER_VALUE_OF_METHOD);
            break;
        case Type.DOUBLE:
            adapter.invokeStatic(Type.getType(Double.class), DOUBLE_VALUE_OF_METHOD);
            break;
        case Type.FLOAT:
            adapter.invokeStatic(Type.getType(Float.class), FLOAT_VALUE_OF_METHOD);
            break;
        case Type.INT:
            adapter.invokeStatic(Type.getType(Integer.class), INTEGER_VALUE_OF_METHOD);
            break;
        case Type.LONG:
            adapter.invokeStatic(Type.getType(Long.class), LONG_VALUE_OF_METHOD);
            break;
        case Type.SHORT:
            adapter.invokeStatic(Type.getType(Short.class), SHORT_VALUE_OF_METHOD);
            break;
        case Type.BOOLEAN:
            adapter.invokeStatic(Type.getType(Boolean.class), BOOLEAN_VALUE_OF_METHOD);
            break;
        case Type.ARRAY:
        case Type.OBJECT:
            break;

        default:
            throw new IllegalArgumentException("Unknown parameter type: " + type);
        }
    }

    private TableSwitchGenerator eventSwitch(GeneratorAdapter adapter) {
        return new TableSwitchGenerator() {
            final Object[] locals = new Object[] { internalName, Opcodes.INTEGER,
                                                   Type.getType(Object[].class).getInternalName() };
            final Object[] stack  = new Object[] {};

            @Override
            public void generateCase(int key, Label end) {
                adapter.visitFrame(Opcodes.F_NEW, 3, locals, 0, stack);
                var mi = mapped.get(key);
                if (mi == null) {
                    throw new IllegalArgumentException("no key: " + key + " in mapped");
                }
                adapter.loadThis();
                int i = 0;

                for (var pi : mi.getParameterInfo()) {
                    adapter.loadArg(1);
                    adapter.push(i++);
                    adapter.arrayLoad(OBJECT_TYPE);
                    unboxIt(adapter, pi);
                }

                final var renamed = REMAPPED_TEMPLATE.formatted(mi.getName());
                if (remapped.contains(mi)) {
                    adapter.invokeVirtual(type, new Method(renamed, mi.getTypeDescriptorStr()));
                } else {
                    adapter.visitMethodInsn(Opcodes.INVOKESPECIAL, clazz.getSuperclass().getName().replace('.', '/'),
                                            renamed, mi.getTypeDescriptorStr(), mi.isAbstract());
                }
                if (mi.getTypeDescriptor().getResultType().toStringWithSimpleNames().equals("void")) {
                    adapter.loadThis();
                }
                adapter.returnValue();
            }

            @Override
            public void generateDefault() {
                adapter.visitFrame(Opcodes.F_NEW, 3, locals, 0, stack);
                adapter.throwException(Type.getType(IllegalStateException.class), "Unknown event type");
            }
        };
    }

    private ClassVisitor eventTransform(ClassVisitor cv) {
        var mappings = new HashMap<String, String>();
        var superMappings = new HashMap<String, String>();
        var superClass = clazz.getSuperclass();
        for (var mi : remapped) {
            mappings.put(METHOD_REMAP_KEY_TEMPLATE.formatted(clazz.getName().replace('.', '/'), mi.getName(),
                                                             mi.getTypeDescriptorStr()),
                         REMAPPED_TEMPLATE.formatted(mi.getName()));
            if (superClass != null) {
                if (superClass.getAnnotationInfo(Entity.class) != null && superClass.getMethodAndConstructorInfo()
                                                                                    .stream()
                                                                                    .filter(e -> matches(e, mi))
                                                                                    .findAny()
                                                                                    .isPresent()) {
                    superMappings.put(METHOD_REMAP_KEY_TEMPLATE.formatted(superClass.getName().replace('.', '/'),
                                                                          mi.getName(), mi.getTypeDescriptorStr()),
                                      REMAPPED_TEMPLATE.formatted(mi.getName()));
                }
            }
        }
        var eventRemapper = new SimpleRemapper(mappings);
        var superRemapper = new SimpleRemapper(superMappings);

        var map = new HashMap<String, String>();
        map.put(Kronos.class.getCanonicalName().replace('.', '/'), Kairos.class.getCanonicalName().replace('.', '/'));
        var apiRemapper = new SimpleRemapper(map);

        return new ClassVisitor(Opcodes.ASM9, new ClassRemapper(cv, apiRemapper)) {

            @Override
            public MethodVisitor visitMethod(int access, String ogName, String ogDescriptor, String signature,
                                             String[] exceptions) {
                var m = new Method(ogName, ogDescriptor);
                var isEvent = false;
                if (events.contains(m)) {
                    isEvent = true;
                    generateEvent(m, access, ogName, ogDescriptor, exceptions, cv);
                }
                return remapMethod(isEvent, access, ogName, ogDescriptor, signature, exceptions);
            }

            private MethodVisitor remapMethod(boolean isEvent, int access, String ogName, String ogDescriptor,
                                              String signature, String[] exceptions) {
                final var renamed = eventRemapper.mapMethodName(type.getInternalName(), ogName, ogDescriptor);
                if (!renamed.equals(ogName)) {
                    access = Opcodes.ACC_PROTECTED;
                }
                return new MethodVisitor(Opcodes.ASM9,
                                         super.visitMethod(access, renamed, ogDescriptor, signature, exceptions)) {

                    @Override
                    public void visitMethodInsn(int opcodeAndSource, String owner, String name, String descriptor,
                                                boolean isInterface) {
                        String newName = name;
                        if (isEvent && Opcodes.INVOKESPECIAL == opcodeAndSource && name.equals(ogName) &&
                            descriptor.equals(ogDescriptor)) {
                            newName = superRemapper.mapMethodName(owner, name, descriptor);
                        }
                        super.visitMethodInsn(opcodeAndSource, owner, newName, descriptor, isInterface);
                    }
                };
            }
        };
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
        mg.invokeStatic(Type.getType(Framework.class), GET_CONTROLLER_METHOD);
        mg.loadThis();
        mg.push(inverse.get(m));
        var objectType = OBJECT_TYPE;
        mg.push(m.getArgumentTypes().length);
        mg.newArray(objectType);
        int arg = 0;
        for (var type : m.getArgumentTypes()) {
            mg.dup();
            mg.push(arg);
            mg.loadArg(arg++);
            boxIt(mg, type);
            mg.arrayStore(objectType);
        }

        if (Type.VOID_TYPE.equals(m.getReturnType())) {
            mg.invokeVirtual(Type.getType(Devi.class),
                             blocking.contains(m) ? POST_CONTINUING_EVENT_METHOD : POST_EVENT_METHOD);
        } else {
            mg.invokeVirtual(Type.getType(Devi.class), POST_CONTINUING_EVENT_METHOD);
            mg.checkCast(m.getReturnType());
        }
        mg.visitMaxs(0, 0);
        mg.returnValue();
    }

    private void generateInvoke(ClassVisitor cv) {
        var keys = keys();
        if (keys.length == 0) {
            return;
        }
        var mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, INVOKE_METHOD, null,
                                      new Type[] { Type.getType(Throwable.class) }, cv);
        if (keys.length > 0) {
            mg.loadArg(0);
            mg.tableSwitch(keys, eventSwitch(mg));
        } else {
            mg.throwException(Type.getType(IllegalStateException.class), "No events");
        }
        mg.visitMaxs(0, 0);
        mg.endMethod();
    }

    private void generateSignatureFor(ClassVisitor cv) {
        var keys = keys();
        if (keys.length == 0) {
            return;
        }
        var mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, SIGNATURE_FOR_METHOD, null, null, cv);
        if (keys.length > 0) {
            mg.loadArg(0);
            mg.tableSwitch(keys, signatureSwitch(mg));
        } else {
            mg.throwException(Type.getType(IllegalStateException.class), "No events");
        }
        mg.visitMaxs(0, 0);
        mg.endMethod();
    }

    private int[] keys() {
        return mapped.entrySet()
                     .stream()
                     .filter(e -> remapped.contains(e.getValue()))
                     .mapToInt(e -> (int) e.getKey())
                     .sorted()
                     .toArray();
    }

    private boolean matches(MethodInfo e, MethodInfo mi) {
        if (!mi.getName().equals(e.getName())) {
            return false;
        }
        return mi.getTypeDescriptor().equals(mi.getTypeDescriptor());
    }

    private String signatureOf(MethodInfo mi) {
        var b = new StringBuilder();
        b.append('<');
        b.append(type.getClassName());
        b.append(": ");
        b.append(mi.getTypeDescriptor().getResultType());
        b.append(" ");
        b.append(mi.getName());
        b.append('(');
        var frist = true;
        for (var p : mi.getParameterInfo()) {
            if (frist) {
                frist = false;
            } else {
                b.append(", ");
            }
            b.append(p.getTypeDescriptor());
        }
        b.append(")>");
        return b.toString();
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
                adapter.push(signatureOf(mi));
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

    private void unboxIt(GeneratorAdapter adapter, MethodParameterInfo pi) {
        final var descriptor = pi.getTypeDescriptor();
        if (descriptor instanceof ArrayTypeSignature ats) {
            adapter.checkCast(Type.getObjectType(ats.getTypeSignatureStr()));
        } else if (descriptor instanceof BaseTypeSignature bts) {
            switch (bts.getTypeSignatureChar()) {
            case 'B':
                adapter.checkCast(Type.getType(Byte.class));
                adapter.invokeVirtual(Type.getType(Byte.class), BYTE_VALUE_METHOD);
                break;
            case 'C':
                adapter.checkCast(Type.getType(Character.class));
                adapter.invokeVirtual(Type.getType(Character.class), CHARACTER_VALUE_METHOD);
                break;
            case 'D':
                adapter.checkCast(Type.getType(Double.class));
                adapter.invokeVirtual(Type.getType(Double.class), DOUBLE_VALUE_METHOD);
                break;
            case 'F':
                adapter.checkCast(Type.getType(Float.class));
                adapter.invokeVirtual(Type.getType(Float.class), FLOAT_VALUE_METHOD);
                break;
            case 'I':
                adapter.checkCast(Type.getType(Integer.class));
                adapter.invokeVirtual(Type.getType(Integer.class), INT_VALUE_METHOD);
                break;
            case 'J':
                adapter.checkCast(Type.getType(Long.class));
                adapter.invokeVirtual(Type.getType(Long.class), LONG_VALUE_METHOD);
                break;
            case 'S':
                adapter.checkCast(Type.getType(Short.class));
                adapter.invokeVirtual(Type.getType(Short.class), SHORT_VALUE_METHOD);
                break;
            case 'Z':
                adapter.checkCast(Type.getType(Boolean.class));
                adapter.invokeVirtual(Type.getType(Boolean.class), BOOLEAN_VALUE_METHOD);
                break;
            default:
                throw new IllegalArgumentException("Unknown parameter type: " + pi);
            }
        } else {
            adapter.checkCast(Type.getObjectType(descriptor.toString().replace('.', '/')));
        }
    }
}
