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
 * Transforms Entity classes into PrimeMover entities using ASM bytecode manipulation.
 * 
 * This class generates the bytecode necessary to convert regular Java classes
 * annotated with @Entity into simulation-aware entities that can participate
 * in the PrimeMover discrete event simulation framework.
 * 
 * Key responsibilities:
 * - Transform method calls into simulation events
 * - Generate EntityReference implementation methods (__invoke, __signatureFor)
 * - Handle method remapping for event processing
 * - Manage primitive type boxing/unboxing for event parameters
 *
 * @author hal.hildebrand
 */
public class EntityGeneratorRefactored {

    // === Method Names ===
    private static final String INVOKE = "__invoke";
    private static final String SIGNATURE_FOR = "__signatureFor";
    private static final String GET_CONTROLLER = "getController";
    private static final String POST_EVENT = "postEvent";
    private static final String POST_CONTINUING_EVENT = "postContinuingEvent";
    
    // === Templates ===
    private static final String METHOD_REMAP_KEY_TEMPLATE = "%s.%s%s";
    private static final String REMAPPED_TEMPLATE = "%s$event";
    
    // === ASM Method and Type References ===
    private static final MethodAccessor methodAccessor = new MethodAccessor();
    private static final Type OBJECT_TYPE = Type.getType(Object.class);
    private static final Type STRING_BUILDER_TYPE = Type.getType(StringBuilder.class);

    // === Instance State ===
    private final ClassInfo clazz;
    private final String internalName;
    private final String timestamp;
    private final Type type;
    
    // Event Processing State
    private final Set<Method> events;
    private final Set<Method> blocking;
    private final Set<MethodInfo> remapped;
    private final Map<Method, Integer> inverse;
    private final Map<Integer, MethodInfo> mapped;

    /**
     * Helper class to manage ASM Method references and reduce static initialization complexity
     */
    private static class MethodAccessor {
        final Method invokeMethod;
        final Method signatureForMethod;
        final Method getControllerMethod;
        final Method postEventMethod;
        final Method postContinuingEventMethod;
        final Method stringBuilderConstructor;
        final Method appendMethod;
        final Method toStringMethod;
        
        // Boxing method references
        final Map<Class<?>, Method> valueOfMethods;
        final Map<Class<?>, Method> valueAccessMethods;

        MethodAccessor() {
            this.invokeMethod = getMethod(EntityReference.class, INVOKE, int.class, Object[].class);
            this.signatureForMethod = getMethod(EntityReference.class, SIGNATURE_FOR, int.class);
            this.getControllerMethod = getMethod(Framework.class, GET_CONTROLLER);
            this.postEventMethod = getMethod(Devi.class, POST_EVENT, EntityReference.class, int.class, Object[].class);
            this.postContinuingEventMethod = getMethod(Devi.class, POST_CONTINUING_EVENT, EntityReference.class, int.class, Object[].class);
            
            this.stringBuilderConstructor = getConstructor(StringBuilder.class);
            this.appendMethod = getMethod(StringBuilder.class, "append", String.class);
            this.toStringMethod = getMethod(StringBuilder.class, "toString");
            
            this.valueOfMethods = createValueOfMethods();
            this.valueAccessMethods = createValueAccessMethods();
        }
        
        private Method getMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
            try {
                return Method.getMethod(clazz.getMethod(name, paramTypes));
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Cannot find method " + name + " in " + clazz.getSimpleName(), e);
            }
        }
        
        private Method getConstructor(Class<?> clazz, Class<?>... paramTypes) {
            try {
                return Method.getMethod(clazz.getConstructor(paramTypes));
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Cannot find constructor in " + clazz.getSimpleName(), e);
            }
        }
        
        private Map<Class<?>, Method> createValueOfMethods() {
            var map = new HashMap<Class<?>, Method>();
            map.put(Boolean.class, getMethod(Boolean.class, "valueOf", boolean.class));
            map.put(Byte.class, getMethod(Byte.class, "valueOf", byte.class));
            map.put(Character.class, getMethod(Character.class, "valueOf", char.class));
            map.put(Double.class, getMethod(Double.class, "valueOf", double.class));
            map.put(Float.class, getMethod(Float.class, "valueOf", float.class));
            map.put(Integer.class, getMethod(Integer.class, "valueOf", int.class));
            map.put(Long.class, getMethod(Long.class, "valueOf", long.class));
            map.put(Short.class, getMethod(Short.class, "valueOf", short.class));
            return map;
        }
        
        private Map<Class<?>, Method> createValueAccessMethods() {
            var map = new HashMap<Class<?>, Method>();
            map.put(Boolean.class, getMethod(Boolean.class, "booleanValue"));
            map.put(Byte.class, getMethod(Byte.class, "byteValue"));
            map.put(Character.class, getMethod(Character.class, "charValue"));
            map.put(Double.class, getMethod(Double.class, "doubleValue"));
            map.put(Float.class, getMethod(Float.class, "floatValue"));
            map.put(Integer.class, getMethod(Integer.class, "intValue"));
            map.put(Long.class, getMethod(Long.class, "longValue"));
            map.put(Short.class, getMethod(Short.class, "shortValue"));
            return map;
        }
    }

    /**
     * Creates a new EntityGeneratorOriginal for the specified class and events.
     * 
     * @param clazz The class to transform
     * @param events The set of methods to transform into events
     */
    public EntityGeneratorRefactored(ClassInfo clazz, Set<MethodInfo> events) {
        this(clazz, events, Instant.now().toString());
    }
    
    /**
     * Creates a new EntityGeneratorOriginal for the specified class and events with a specific timestamp.
     * 
     * @param clazz The class to transform
     * @param events The set of methods to transform into events
     * @param timestamp The timestamp to use in the @Transformed annotation
     */
    public EntityGeneratorRefactored(ClassInfo clazz, Set<MethodInfo> events, String timestamp) {
        this.clazz = clazz;
        this.timestamp = timestamp;
        this.type = Type.getObjectType(clazz.getName().replace('.', '/'));
        this.internalName = clazz.getName().replace('.', '/');
        
        // Initialize collections
        this.mapped = new HashMap<>();
        this.remapped = new OpenSet<>();
        this.blocking = new OpenSet<>();
        this.inverse = new HashMap<>();
        this.events = new OpenSet<>();
        
        initializeEventMappings(events);
    }

    /**
     * Initialize event mappings and determine which methods are blocking/remapped
     */
    private void initializeEventMappings(Set<MethodInfo> eventMethods) {
        var key = 0;
        for (var mi : eventMethods.stream().sorted().toList()) {
            mapped.put(key, mi);
            final var event = new Method(mi.getName(), mi.getTypeDescriptorStr());
            inverse.put(event, key++);
            this.events.add(event);
            
            // Determine if this is a blocking event
            if (isBlockingEvent(mi)) {
                blocking.add(event);
            }
            
            // Determine if this method should be remapped
            if (isDeclaredInClass(mi)) {
                remapped.add(mi);
            }
        }
    }
    
    private boolean isBlockingEvent(MethodInfo mi) {
        return !mi.getTypeDescriptor().getResultType().toString().equals("void") 
            || mi.hasAnnotation(Blocking.class);
    }
    
    private boolean isDeclaredInClass(MethodInfo mi) {
        return clazz.getDeclaredMethodInfo(mi.getName())
                   .stream()
                   .anyMatch(m -> mi.equals(m));
    }

    /**
     * Generates the transformed bytecode for the entity class.
     * 
     * @return ClassWriter containing the generated bytecode
     * @throws MalformedURLException if the class resource URL is malformed
     * @throws IOException if there's an error reading the class resource
     */
    public ClassWriter generate() throws MalformedURLException, IOException {
        try (var is = clazz.getResource().open()) {
            final var classReader = new ClassReader(is);
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            
            // Apply event transformations
            var transformVisitor = createEventTransformVisitor(classWriter);
            classReader.accept(transformVisitor, ClassReader.EXPAND_FRAMES);
            
            // Add required interfaces and annotations
            addEntityReferenceInterface(classWriter);
            addTransformedAnnotation(transformVisitor);
            
            // Generate required methods
            generateInvokeMethod(classWriter);
            generateSignatureForMethod(classWriter);
            
            // Finalize class structure
            finalizeClassStructure(classWriter);
            
            return classWriter;
        }
    }

    /**
     * Creates the class visitor that handles event transformation
     */
    private ClassVisitor createEventTransformVisitor(ClassWriter classWriter) {
        var methodMappings = buildMethodMappings();
        var apiRemapper = createApiRemapper();
        
        return new EventTransformVisitor(classWriter, methodMappings, apiRemapper);
    }
    
    private Map<String, String> buildMethodMappings() {
        var mappings = new HashMap<String, String>();
        var superMappings = new HashMap<String, String>();
        var superClass = clazz.getSuperclass();
        
        for (var mi : remapped) {
            String key = METHOD_REMAP_KEY_TEMPLATE.formatted(
                clazz.getName().replace('.', '/'), mi.getName(), mi.getTypeDescriptorStr());
            mappings.put(key, REMAPPED_TEMPLATE.formatted(mi.getName()));
            
            if (superClass != null && isSuperEntityMethod(superClass, mi)) {
                String superKey = METHOD_REMAP_KEY_TEMPLATE.formatted(
                    superClass.getName().replace('.', '/'), mi.getName(), mi.getTypeDescriptorStr());
                superMappings.put(superKey, REMAPPED_TEMPLATE.formatted(mi.getName()));
            }
        }
        
        mappings.putAll(superMappings);
        return mappings;
    }
    
    private boolean isSuperEntityMethod(ClassInfo superClass, MethodInfo mi) {
        return superClass.getAnnotationInfo(Entity.class) != null &&
               superClass.getMethodAndConstructorInfo()
                         .stream()
                         .anyMatch(e -> methodMatches(e, mi));
    }
    
    private boolean methodMatches(MethodInfo e, MethodInfo mi) {
        return mi.getName().equals(e.getName()) && 
               mi.getTypeDescriptor().equals(e.getTypeDescriptor());
    }
    
    private SimpleRemapper createApiRemapper() {
        var map = new HashMap<String, String>();
        map.put(Kronos.class.getCanonicalName().replace('.', '/'), 
                Kairos.class.getCanonicalName().replace('.', '/'));
        return new SimpleRemapper(map);
    }

    /**
     * Inner class to handle event transformation logic
     */
    private class EventTransformVisitor extends ClassVisitor {
        private final Map<String, String> methodMappings;
        private final SimpleRemapper apiRemapper;
        private final ClassWriter classWriter;

        public EventTransformVisitor(ClassWriter classWriter, Map<String, String> methodMappings, SimpleRemapper apiRemapper) {
            super(Opcodes.ASM9, new ClassRemapper(classWriter, apiRemapper));
            this.classWriter = classWriter;
            this.methodMappings = methodMappings;
            this.apiRemapper = apiRemapper;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            var method = new Method(name, descriptor);
            
            if (events.contains(method)) {
                generateEventMethod(method, access, name, descriptor, exceptions, classWriter);
            }
            
            return createMethodVisitor(access, name, descriptor, signature, exceptions);
        }
        
        private MethodVisitor createMethodVisitor(int access, String name, String descriptor, String signature, String[] exceptions) {
            final var remappedName = methodMappings.getOrDefault(
                METHOD_REMAP_KEY_TEMPLATE.formatted(type.getInternalName(), name, descriptor), name);
            
            if (!remappedName.equals(name)) {
                access = Opcodes.ACC_PROTECTED;
            }
            
            return new MethodRemappingVisitor(
                super.visitMethod(access, remappedName, descriptor, signature, exceptions),
                name, descriptor);
        }
        
        private class MethodRemappingVisitor extends MethodVisitor {
            private final String originalName;
            private final String originalDescriptor;

            public MethodRemappingVisitor(MethodVisitor mv, String originalName, String originalDescriptor) {
                super(Opcodes.ASM9, mv);
                this.originalName = originalName;
                this.originalDescriptor = originalDescriptor;
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                String newName = name;
                if (Opcodes.INVOKESPECIAL == opcode && name.equals(originalName) && descriptor.equals(originalDescriptor)) {
                    newName = methodMappings.getOrDefault(
                        METHOD_REMAP_KEY_TEMPLATE.formatted(owner, name, descriptor), name);
                }
                super.visitMethodInsn(opcode, owner, newName, descriptor, isInterface);
            }
        }
    }

    /**
     * Generates an event method that posts to the simulation controller
     */
    private void generateEventMethod(Method method, int access, String name, String descriptor, String[] exceptions, ClassVisitor cv) {
        var exceptionTypes = exceptions == null ? null : 
            Arrays.stream(exceptions)
                  .map(Type::getObjectType)
                  .toArray(Type[]::new);

        var mg = new GeneratorAdapter(access, method, descriptor, exceptionTypes, cv);
        
        // Load this and get controller
        mg.loadThis();
        mg.invokeStatic(Type.getType(Framework.class), methodAccessor.getControllerMethod);
        
        // Load this (event target)
        mg.loadThis();
        
        // Load event ID
        mg.push(inverse.get(method));
        
        // Create and populate parameter array
        generateParameterArray(mg, method);
        
        // Call appropriate post method
        if (Type.VOID_TYPE.equals(method.getReturnType())) {
            var postMethod = blocking.contains(method) ? 
                methodAccessor.postContinuingEventMethod : methodAccessor.postEventMethod;
            mg.invokeVirtual(Type.getType(Devi.class), postMethod);
        } else {
            mg.invokeVirtual(Type.getType(Devi.class), methodAccessor.postContinuingEventMethod);
            mg.checkCast(method.getReturnType());
        }
        
        mg.visitMaxs(0, 0);
        mg.returnValue();
    }
    
    private void generateParameterArray(GeneratorAdapter mg, Method method) {
        mg.push(method.getArgumentTypes().length);
        mg.newArray(OBJECT_TYPE);
        
        int argIndex = 0;
        for (var argType : method.getArgumentTypes()) {
            mg.dup();
            mg.push(argIndex);
            mg.loadArg(argIndex++);
            boxPrimitive(mg, argType);
            mg.arrayStore(OBJECT_TYPE);
        }
    }

    /**
     * Box primitive types for storage in Object array
     */
    private void boxPrimitive(GeneratorAdapter adapter, Type type) {
        switch (type.getSort()) {
            case Type.BYTE -> adapter.invokeStatic(Type.getType(Byte.class), methodAccessor.valueOfMethods.get(Byte.class));
            case Type.CHAR -> adapter.invokeStatic(Type.getType(Character.class), methodAccessor.valueOfMethods.get(Character.class));
            case Type.DOUBLE -> adapter.invokeStatic(Type.getType(Double.class), methodAccessor.valueOfMethods.get(Double.class));
            case Type.FLOAT -> adapter.invokeStatic(Type.getType(Float.class), methodAccessor.valueOfMethods.get(Float.class));
            case Type.INT -> adapter.invokeStatic(Type.getType(Integer.class), methodAccessor.valueOfMethods.get(Integer.class));
            case Type.LONG -> adapter.invokeStatic(Type.getType(Long.class), methodAccessor.valueOfMethods.get(Long.class));
            case Type.SHORT -> adapter.invokeStatic(Type.getType(Short.class), methodAccessor.valueOfMethods.get(Short.class));
            case Type.BOOLEAN -> adapter.invokeStatic(Type.getType(Boolean.class), methodAccessor.valueOfMethods.get(Boolean.class));
            case Type.ARRAY, Type.OBJECT -> { /* No boxing needed */ }
            default -> throw new IllegalArgumentException("Unknown parameter type: " + type);
        }
    }

    /**
     * Unbox primitive types from Object array
     */
    private void unboxPrimitive(GeneratorAdapter adapter, MethodParameterInfo paramInfo) {
        final var descriptor = paramInfo.getTypeDescriptor();
        
        if (descriptor instanceof ArrayTypeSignature ats) {
            adapter.checkCast(Type.getObjectType(ats.getTypeSignatureStr()));
        } else if (descriptor instanceof BaseTypeSignature bts) {
            unboxBaseType(adapter, bts.getTypeSignatureChar());
        } else {
            adapter.checkCast(Type.getObjectType(descriptor.toString().replace('.', '/')));
        }
    }
    
    private void unboxBaseType(GeneratorAdapter adapter, char typeChar) {
        switch (typeChar) {
            case 'B' -> {
                adapter.checkCast(Type.getType(Byte.class));
                adapter.invokeVirtual(Type.getType(Byte.class), methodAccessor.valueAccessMethods.get(Byte.class));
            }
            case 'C' -> {
                adapter.checkCast(Type.getType(Character.class));
                adapter.invokeVirtual(Type.getType(Character.class), methodAccessor.valueAccessMethods.get(Character.class));
            }
            case 'D' -> {
                adapter.checkCast(Type.getType(Double.class));
                adapter.invokeVirtual(Type.getType(Double.class), methodAccessor.valueAccessMethods.get(Double.class));
            }
            case 'F' -> {
                adapter.checkCast(Type.getType(Float.class));
                adapter.invokeVirtual(Type.getType(Float.class), methodAccessor.valueAccessMethods.get(Float.class));
            }
            case 'I' -> {
                adapter.checkCast(Type.getType(Integer.class));
                adapter.invokeVirtual(Type.getType(Integer.class), methodAccessor.valueAccessMethods.get(Integer.class));
            }
            case 'J' -> {
                adapter.checkCast(Type.getType(Long.class));
                adapter.invokeVirtual(Type.getType(Long.class), methodAccessor.valueAccessMethods.get(Long.class));
            }
            case 'S' -> {
                adapter.checkCast(Type.getType(Short.class));
                adapter.invokeVirtual(Type.getType(Short.class), methodAccessor.valueAccessMethods.get(Short.class));
            }
            case 'Z' -> {
                adapter.checkCast(Type.getType(Boolean.class));
                adapter.invokeVirtual(Type.getType(Boolean.class), methodAccessor.valueAccessMethods.get(Boolean.class));
            }
            default -> throw new IllegalArgumentException("Unknown primitive type: " + typeChar);
        }
    }

    /**
     * Generate the __invoke method for EntityReference interface
     */
    private void generateInvokeMethod(ClassWriter classWriter) {
        var keys = getEventKeys();
        if (keys.length == 0) return;
        
        var mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, methodAccessor.invokeMethod, null,
                                      new Type[] { Type.getType(Throwable.class) }, classWriter);
        
        mg.loadArg(0);
        mg.tableSwitch(keys, createEventSwitchGenerator(mg));
        mg.visitMaxs(0, 0);
        mg.endMethod();
    }
    
    private TableSwitchGenerator createEventSwitchGenerator(GeneratorAdapter adapter) {
        return new TableSwitchGenerator() {
            final Object[] locals = { internalName, Opcodes.INTEGER, Type.getType(Object[].class).getInternalName() };
            final Object[] stack = {};

            @Override
            public void generateCase(int key, Label end) {
                adapter.visitFrame(Opcodes.F_NEW, 3, locals, 0, stack);
                generateInvokeCase(adapter, key);
            }

            @Override
            public void generateDefault() {
                adapter.visitFrame(Opcodes.F_NEW, 3, locals, 0, stack);
                adapter.throwException(Type.getType(IllegalStateException.class), "Unknown event type");
            }
        };
    }
    
    private void generateInvokeCase(GeneratorAdapter adapter, int key) {
        var methodInfo = mapped.get(key);
        if (methodInfo == null) {
            throw new IllegalArgumentException("No method mapped for key: " + key);
        }
        
        adapter.loadThis();
        
        // Unbox parameters from Object array
        int paramIndex = 0;
        for (var paramInfo : methodInfo.getParameterInfo()) {
            adapter.loadArg(1);
            adapter.push(paramIndex++);
            adapter.arrayLoad(OBJECT_TYPE);
            unboxPrimitive(adapter, paramInfo);
        }
        
        // Call the remapped method
        final var remappedName = REMAPPED_TEMPLATE.formatted(methodInfo.getName());
        if (remapped.contains(methodInfo)) {
            adapter.invokeVirtual(type, new Method(remappedName, methodInfo.getTypeDescriptorStr()));
        } else {
            adapter.visitMethodInsn(Opcodes.INVOKESPECIAL, 
                                   clazz.getSuperclass().getName().replace('.', '/'),
                                   remappedName, methodInfo.getTypeDescriptorStr(), 
                                   methodInfo.isAbstract());
        }
        
        // Handle return value
        if (methodInfo.getTypeDescriptor().getResultType().toStringWithSimpleNames().equals("void")) {
            adapter.loadThis();
        }
        adapter.returnValue();
    }

    /**
     * Generate the __signatureFor method for EntityReference interface
     */
    private void generateSignatureForMethod(ClassWriter classWriter) {
        var keys = getEventKeys();
        if (keys.length == 0) return;
        
        var mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, methodAccessor.signatureForMethod, null, null, classWriter);
        
        mg.loadArg(0);
        mg.tableSwitch(keys, createSignatureSwitchGenerator(mg));
        mg.visitMaxs(0, 0);
        mg.endMethod();
    }
    
    private TableSwitchGenerator createSignatureSwitchGenerator(GeneratorAdapter adapter) {
        return new TableSwitchGenerator() {
            @Override
            public void generateCase(int key, Label end) {
                adapter.visitFrame(Opcodes.F_NEW, 2, new Object[] { internalName, Opcodes.INTEGER }, 0, new Object[0]);
                var methodInfo = mapped.get(key);
                if (methodInfo != null) {
                    adapter.push(buildMethodSignature(methodInfo));
                } else {
                    adapter.push("");
                }
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
    
    private String buildMethodSignature(MethodInfo methodInfo) {
        var signature = new StringBuilder();
        signature.append('<')
                 .append(type.getClassName())
                 .append(": ")
                 .append(methodInfo.getTypeDescriptor().getResultType())
                 .append(" ")
                 .append(methodInfo.getName())
                 .append('(');
        
        boolean first = true;
        for (var param : methodInfo.getParameterInfo()) {
            if (!first) signature.append(", ");
            signature.append(param.getTypeDescriptor());
            first = false;
        }
        
        signature.append(")>");
        return signature.toString();
    }

    /**
     * Helper method to build string concatenation using StringBuilder
     */
    public void appendStrings(GeneratorAdapter mg, String... strings) {
        mg.newInstance(STRING_BUILDER_TYPE);
        mg.dup();
        mg.invokeConstructor(STRING_BUILDER_TYPE, methodAccessor.stringBuilderConstructor);
        
        for (var string : strings) {
            mg.push(string);
            mg.invokeVirtual(STRING_BUILDER_TYPE, methodAccessor.appendMethod);
        }
        
        mg.invokeVirtual(STRING_BUILDER_TYPE, methodAccessor.toStringMethod);
    }

    // === Helper Methods ===
    
    private void addEntityReferenceInterface(ClassWriter classWriter) {
        final var interfaces = clazz.getInterfaces()
                                   .stream()
                                   .map(ci -> ci.getName().replace('.', '/'))
                                   .collect(Collectors.toList());
        interfaces.add(Type.getType(EntityReference.class).getInternalName());
        // Interface addition is handled by the visitor pattern
    }
    
    private void addTransformedAnnotation(ClassVisitor visitor) {
        var annotationVisitor = visitor.visitAnnotation(Type.getType(Transformed.class).getDescriptor(), true);
        annotationVisitor.visit("comment", "PrimeMover ASM Event Transform");
        annotationVisitor.visit("date", timestamp);
        annotationVisitor.visit("value", "PrimeMover ASM");
        annotationVisitor.visitEnd();
    }
    
    private void finalizeClassStructure(ClassWriter classWriter) {
        final var interfaces = clazz.getInterfaces()
                                   .stream()
                                   .map(ci -> ci.getName().replace('.', '/'))
                                   .collect(Collectors.toList());
        interfaces.add(Type.getType(EntityReference.class).getInternalName());
        
        classWriter.visit(clazz.getClassfileMajorVersion(), clazz.getModifiers(), internalName, 
                         clazz.getTypeSignatureStr(),
                         clazz.getSuperclass() == null ? Type.getInternalName(Object.class)
                                                       : clazz.getSuperclass().getName().replace('.', '/'),
                         interfaces.toArray(new String[0]));
        classWriter.visitEnd();
    }
    
    private int[] getEventKeys() {
        return mapped.entrySet()
                     .stream()
                     .filter(entry -> remapped.contains(entry.getValue()))
                     .mapToInt(Map.Entry::getKey)
                     .sorted()
                     .toArray();
    }
}
