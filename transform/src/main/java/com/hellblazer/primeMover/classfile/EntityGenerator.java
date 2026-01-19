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
package com.hellblazer.primeMover.classfile;

import com.hellblazer.primeMover.annotations.Blocking;
import com.hellblazer.primeMover.api.Kronos;
import com.hellblazer.primeMover.classfile.OpenAddressingSet.OpenSet;
import com.hellblazer.primeMover.runtime.Kairos;

import java.io.IOException;
import java.lang.classfile.*;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.lang.classfile.instruction.SwitchCase;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.time.Instant;
import java.util.*;

/**
 * Transforms Entity classes into PrimeMover entities using the Java 25 ClassFile API (JEP 484).
 * <p>
 * This class generates bytecode to transform method calls into simulation events using native
 * Java ClassFile API without external dependencies.
 * <p>
 * Key responsibilities:
 * - Transform method calls into simulation events
 * - Generate EntityReference implementation methods (__invoke, __signatureFor)
 * - Handle method remapping for event processing
 * - Manage primitive type boxing/unboxing for event parameters
 *
 * @author hal.hildebrand
 */
public class EntityGenerator {

    // === Method Names ===
    private static final String INVOKE        = "__invoke";
    private static final String SIGNATURE_FOR = "__signatureFor";

    // === Templates ===
    private static final String METHOD_REMAP_KEY_TEMPLATE = "%s.%s%s";
    private static final String REMAPPED_TEMPLATE         = "%s$event";

    // === Method Ordering ===
    private static final Comparator<MethodMetadata> METHOD_ORDER = Comparator.comparing(MethodMetadata::getName)
                                                                             .thenComparing(MethodMetadata::getDescriptor);

    // === Size Limits ===
    private static final long MAX_CLASS_SIZE = 10 * 1024 * 1024; // 10 MB

    // === Cached ClassFile Instance ===
    private static final ClassFile CLASS_FILE = ClassFile.of();

    // === Kronos to Kairos Remapper (reused across all method transforms) ===
    private static final ClassRemapper API_REMAPPER = new ClassRemapper(classDesc -> {
        var className = classDesc.packageName().isEmpty()
                        ? classDesc.displayName()
                        : classDesc.packageName() + "." + classDesc.displayName();
        if (className.equals(Kronos.class.getCanonicalName())) {
            return ClassDesc.of(Kairos.class.getCanonicalName());
        }
        return classDesc;
    });

    // === Wrapper Class Names (for O(1) lookup) ===
    private static final Set<String> WRAPPER_CLASS_NAMES = Set.of(
        "java.lang.Integer",
        "java.lang.Long",
        "java.lang.Float",
        "java.lang.Double",
        "java.lang.Boolean",
        "java.lang.Byte",
        "java.lang.Character",
        "java.lang.Short"
    );

    // === Wrapper Class Descriptors ===
    private static final ClassDesc CD_INTEGER   = ClassDesc.of("java.lang.Integer");
    private static final ClassDesc CD_LONG      = ClassDesc.of("java.lang.Long");
    private static final ClassDesc CD_DOUBLE    = ClassDesc.of("java.lang.Double");
    private static final ClassDesc CD_FLOAT     = ClassDesc.of("java.lang.Float");
    private static final ClassDesc CD_BOOLEAN   = ClassDesc.of("java.lang.Boolean");
    private static final ClassDesc CD_BYTE      = ClassDesc.of("java.lang.Byte");
    private static final ClassDesc CD_SHORT     = ClassDesc.of("java.lang.Short");
    private static final ClassDesc CD_CHARACTER = ClassDesc.of("java.lang.Character");

    // === Primitive Class Descriptors ===
    private static final ClassDesc CD_int     = ConstantDescs.CD_int;
    private static final ClassDesc CD_long    = ConstantDescs.CD_long;
    private static final ClassDesc CD_double  = ConstantDescs.CD_double;
    private static final ClassDesc CD_float   = ConstantDescs.CD_float;
    private static final ClassDesc CD_boolean = ConstantDescs.CD_boolean;
    private static final ClassDesc CD_byte    = ConstantDescs.CD_byte;
    private static final ClassDesc CD_short   = ConstantDescs.CD_short;
    private static final ClassDesc CD_char    = ConstantDescs.CD_char;

    // === ClassFile API Constants ===
    private static final ClassDesc OBJECT_CLASS           = ConstantDescs.CD_Object;
    private static final ClassDesc STRING_CLASS           = ConstantDescs.CD_String;
    private static final ClassDesc OBJECT_ARRAY_CLASS     = ClassDesc.of(Object.class.getCanonicalName()).arrayType();
    private static final ClassDesc ENTITY_REFERENCE_CLASS = ClassDesc.of(
        "com.hellblazer.primeMover.api.EntityReference");
    private static final ClassDesc FRAMEWORK_CLASS        = ClassDesc.of("com.hellblazer.primeMover.runtime.Framework");
    private static final ClassDesc DEVI_CLASS             = ClassDesc.of("com.hellblazer.primeMover.runtime.Devi");
    private static final ClassDesc TRANSFORMED_CLASS      = ClassDesc.of(
        "com.hellblazer.primeMover.annotations.Transformed");

    // === Boxing Method Type Descriptors (valueOf) ===
    private static final MethodTypeDesc MTD_INTEGER_VALUEOF   = MethodTypeDesc.of(CD_INTEGER, CD_int);
    private static final MethodTypeDesc MTD_LONG_VALUEOF      = MethodTypeDesc.of(CD_LONG, CD_long);
    private static final MethodTypeDesc MTD_DOUBLE_VALUEOF    = MethodTypeDesc.of(CD_DOUBLE, CD_double);
    private static final MethodTypeDesc MTD_FLOAT_VALUEOF     = MethodTypeDesc.of(CD_FLOAT, CD_float);
    private static final MethodTypeDesc MTD_BOOLEAN_VALUEOF   = MethodTypeDesc.of(CD_BOOLEAN, CD_boolean);
    private static final MethodTypeDesc MTD_BYTE_VALUEOF      = MethodTypeDesc.of(CD_BYTE, CD_byte);
    private static final MethodTypeDesc MTD_SHORT_VALUEOF     = MethodTypeDesc.of(CD_SHORT, CD_short);
    private static final MethodTypeDesc MTD_CHARACTER_VALUEOF = MethodTypeDesc.of(CD_CHARACTER, CD_char);

    // === Unboxing Method Type Descriptors (xxxValue) ===
    private static final MethodTypeDesc MTD_INT_VALUE     = MethodTypeDesc.of(CD_int);
    private static final MethodTypeDesc MTD_LONG_VALUE    = MethodTypeDesc.of(CD_long);
    private static final MethodTypeDesc MTD_DOUBLE_VALUE  = MethodTypeDesc.of(CD_double);
    private static final MethodTypeDesc MTD_FLOAT_VALUE   = MethodTypeDesc.of(CD_float);
    private static final MethodTypeDesc MTD_BOOLEAN_VALUE = MethodTypeDesc.of(CD_boolean);
    private static final MethodTypeDesc MTD_BYTE_VALUE    = MethodTypeDesc.of(CD_byte);
    private static final MethodTypeDesc MTD_SHORT_VALUE   = MethodTypeDesc.of(CD_short);
    private static final MethodTypeDesc MTD_CHAR_VALUE    = MethodTypeDesc.of(CD_char);

    // === Method Type Descriptors ===
    private static final MethodTypeDesc INVOKE_METHOD_TYPE                = MethodTypeDesc.of(OBJECT_CLASS,
                                                                                              ConstantDescs.CD_int,
                                                                                              OBJECT_ARRAY_CLASS);
    private static final MethodTypeDesc SIGNATURE_FOR_METHOD_TYPE         = MethodTypeDesc.of(STRING_CLASS,
                                                                                              ConstantDescs.CD_int);
    private static final MethodTypeDesc GET_CONTROLLER_METHOD_TYPE        = MethodTypeDesc.of(DEVI_CLASS);
    private static final MethodTypeDesc POST_EVENT_METHOD_TYPE            = MethodTypeDesc.of(ConstantDescs.CD_void,
                                                                                              ENTITY_REFERENCE_CLASS,
                                                                                              ConstantDescs.CD_int,
                                                                                              OBJECT_ARRAY_CLASS);
    private static final MethodTypeDesc POST_CONTINUING_EVENT_METHOD_TYPE = MethodTypeDesc.of(OBJECT_CLASS,
                                                                                              ENTITY_REFERENCE_CLASS,
                                                                                              ConstantDescs.CD_int,
                                                                                              OBJECT_ARRAY_CLASS);

    // === Boxing/Unboxing Lookup Tables ===
    private static final Map<Character, ClassDesc> WRAPPER_CLASSES = Map.of(
        'I', CD_INTEGER,
        'J', CD_LONG,
        'D', CD_DOUBLE,
        'F', CD_FLOAT,
        'Z', CD_BOOLEAN,
        'B', CD_BYTE,
        'S', CD_SHORT,
        'C', CD_CHARACTER
    );

    private static final Map<Character, String> UNBOX_METHOD_NAMES = Map.of(
        'I', "intValue",
        'J', "longValue",
        'D', "doubleValue",
        'F', "floatValue",
        'Z', "booleanValue",
        'B', "byteValue",
        'S', "shortValue",
        'C', "charValue"
    );

    private static final Map<Character, MethodTypeDesc> BOXING_METHOD_TYPES = Map.of(
        'I', MTD_INTEGER_VALUEOF,
        'J', MTD_LONG_VALUEOF,
        'D', MTD_DOUBLE_VALUEOF,
        'F', MTD_FLOAT_VALUEOF,
        'Z', MTD_BOOLEAN_VALUEOF,
        'B', MTD_BYTE_VALUEOF,
        'S', MTD_SHORT_VALUEOF,
        'C', MTD_CHARACTER_VALUEOF
    );

    private static final Map<Character, MethodTypeDesc> UNBOXING_METHOD_TYPES = Map.of(
        'I', MTD_INT_VALUE,
        'J', MTD_LONG_VALUE,
        'D', MTD_DOUBLE_VALUE,
        'F', MTD_FLOAT_VALUE,
        'Z', MTD_BOOLEAN_VALUE,
        'B', MTD_BYTE_VALUE,
        'S', MTD_SHORT_VALUE,
        'C', MTD_CHAR_VALUE
    );

    // === Instance State ===
    private final ClassMetadata clazz;
    private final String        className;
    private final ClassDesc     classDesc;
    private final String        timestamp;

    // Event Processing State
    private final Set<MethodMetadata>          allEventMethods;
    private final Set<MethodMetadata>          blockingMethods;
    private final Set<MethodMetadata>          remappedMethods;
    private final Map<Integer, MethodMetadata> indexToMethod;
    private final Map<MethodMetadata, Integer> methodToIndex;
    private final Map<String, MethodMetadata>  methodIndex;

    /**
     * Creates a new EntityGenerator for the specified class and events.
     *
     * @param clazz  The class to transform
     * @param events The set of methods to transform into events
     */
    public EntityGenerator(ClassMetadata clazz, Set<MethodMetadata> events) {
        this(clazz, events, Instant.now().toString());
    }

    /**
     * Creates a new EntityGenerator for the specified class and events with a specific timestamp.
     *
     * @param clazz     The class to transform
     * @param events    The set of methods to transform into events
     * @param timestamp The timestamp to use in the @Transformed annotation
     */
    public EntityGenerator(ClassMetadata clazz, Set<MethodMetadata> events, String timestamp) {
        this.clazz = Objects.requireNonNull(clazz, "clazz cannot be null");
        this.className = clazz.getName();
        this.classDesc = ClassDesc.of(className);
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp cannot be null");

        // Initialize collections
        this.indexToMethod = new HashMap<>();
        this.methodToIndex = new HashMap<>();
        this.methodIndex = new HashMap<>();
        this.remappedMethods = new OpenSet<>();
        this.blockingMethods = new OpenSet<>();
        this.allEventMethods = new OpenSet<>();
        this.allEventMethods.addAll(Objects.requireNonNull(events, "events cannot be null"));

        initializeEventMappings(events);
    }

    /**
     * Generates the transformed bytecode for the entity class using ClassFile API.
     *
     * @return byte array containing the generated bytecode
     * @throws IOException if there's an error reading the class resource
     */
    public byte[] generate() throws IOException {
        byte[] originalBytes = clazz.getOriginalBytes();
        if (originalBytes.length > MAX_CLASS_SIZE) {
            throw new IllegalStateException(
                "[EntityGenerator] Class file exceeds maximum size: " +
                originalBytes.length + " bytes for class " + clazz.getName() +
                " (maximum: " + MAX_CLASS_SIZE + " bytes)");
        }
        ClassModel originalClass = CLASS_FILE.parse(originalBytes);

        // Rebuild the class completely with correct method ordering
        return CLASS_FILE.build(originalClass.thisClass().asSymbol(), classBuilder -> {
            // Copy class-level attributes but build methods in correct order
            buildTransformedClass(classBuilder, originalClass);
        });
    }

    /**
     * Box a primitive type to its wrapper object using lookup tables
     */
    private void boxPrimitive(CodeBuilder codeBuilder, char primitiveType) {
        var wrapperClass = WRAPPER_CLASSES.get(primitiveType);
        var methodType = BOXING_METHOD_TYPES.get(primitiveType);
        if (wrapperClass == null || methodType == null) {
            throw new IllegalArgumentException(
                "[EntityGenerator] Unknown primitive type for boxing: '" + primitiveType +
                "' in class " + className);
        }
        codeBuilder.invokestatic(wrapperClass, "valueOf", methodType);
    }

    /**
     * Box primitive types for storage in Object array
     */
    private void boxPrimitiveIfNeeded(CodeBuilder codeBuilder, ParameterMetadata param) {
        var type = param.getType();
        if (type.isPrimitive()) {
            boxPrimitive(codeBuilder, type.getPrimitiveChar());
        }
        // Reference types don't need boxing - they're already objects
    }

    /**
     * Box a return value for Object return type
     */
    private void boxReturnValue(CodeBuilder codeBuilder, String returnDesc) {
        char first = returnDesc.charAt(0);
        switch (first) {
            case 'I' -> codeBuilder.invokestatic(CD_INTEGER, "valueOf", MTD_INTEGER_VALUEOF);
            case 'J' -> codeBuilder.invokestatic(CD_LONG, "valueOf", MTD_LONG_VALUEOF);
            case 'F' -> codeBuilder.invokestatic(CD_FLOAT, "valueOf", MTD_FLOAT_VALUEOF);
            case 'D' -> codeBuilder.invokestatic(CD_DOUBLE, "valueOf", MTD_DOUBLE_VALUEOF);
            case 'Z' -> codeBuilder.invokestatic(CD_BOOLEAN, "valueOf", MTD_BOOLEAN_VALUEOF);
            case 'B' -> codeBuilder.invokestatic(CD_BYTE, "valueOf", MTD_BYTE_VALUEOF);
            case 'C' -> codeBuilder.invokestatic(CD_CHARACTER, "valueOf", MTD_CHARACTER_VALUEOF);
            case 'S' -> codeBuilder.invokestatic(CD_SHORT, "valueOf", MTD_SHORT_VALUEOF);
            // Reference types are already Objects
        }
    }

    /**
     * Build a human-readable method signature string
     */
    private String buildMethodSignature(MethodMetadata methodMetadata) {
        var signature = new StringBuilder();
        signature.append('<')
                 .append(className)
                 .append(": ")
                 .append(methodMetadata.getReturnType())
                 .append(" ")
                 .append(methodMetadata.getName())
                 .append('(');

        boolean first = true;
        for (var param : methodMetadata.getParameters()) {
            if (!first) {
                signature.append(", ");
            }
            signature.append(param.getType());
            first = false;
        }

        signature.append(")>");
        return signature.toString();
    }

    /**
     * Build the transformed class with correct method ordering
     */
    private void buildTransformedClass(ClassBuilder classBuilder, ClassModel originalClass) {
        setClassMetadata(classBuilder, originalClass);
        copyNonMethodElements(classBuilder, originalClass);
        configureInterfaces(classBuilder);
        addTransformedAnnotation(classBuilder);
        copyConstructors(classBuilder, originalClass);
        generateEventMethodPairs(classBuilder, originalClass);
        copyRegularMethods(classBuilder, originalClass);
        generateEntityReferenceMethods(classBuilder);
    }

    /**
     * Set basic class metadata (flags and superclass)
     */
    private void setClassMetadata(ClassBuilder classBuilder, ClassModel originalClass) {
        classBuilder.withFlags(originalClass.flags().flagsMask());
        if (originalClass.superclass().isPresent()) {
            classBuilder.withSuperclass(originalClass.superclass().get().asSymbol());
        }
    }

    /**
     * Copy non-method elements from the original class (fields, etc.)
     */
    private void copyNonMethodElements(ClassBuilder classBuilder, ClassModel originalClass) {
        for (ClassElement element : originalClass) {
            if (!(element instanceof MethodModel) && !(element instanceof Interfaces)) {
                classBuilder.with(element);
            }
        }
    }

    /**
     * Configure class interfaces (original interfaces + EntityReference)
     */
    private void configureInterfaces(ClassBuilder classBuilder) {
        var originalInterfaces = clazz.getInterfaceNames().stream()
                                      .map(ClassDesc::of)
                                      .toArray(ClassDesc[]::new);

        var allInterfaces = new ClassDesc[originalInterfaces.length + 1];
        System.arraycopy(originalInterfaces, 0, allInterfaces, 0, originalInterfaces.length);
        allInterfaces[originalInterfaces.length] = ENTITY_REFERENCE_CLASS;

        classBuilder.withInterfaceSymbols(allInterfaces);
    }

    /**
     * Add @Transformed annotation to the class
     */
    private void addTransformedAnnotation(ClassBuilder classBuilder) {
        classBuilder.with(RuntimeVisibleAnnotationsAttribute.of(
            Annotation.of(TRANSFORMED_CLASS,
                          AnnotationElement.of("timestamp", AnnotationValue.ofString(timestamp)))));
    }

    /**
     * Copy constructors from the original class
     */
    private void copyConstructors(ClassBuilder classBuilder, ClassModel originalClass) {
        for (MethodModel methodModel : originalClass.methods()) {
            if (methodModel.methodName().stringValue().equals("<init>")) {
                classBuilder.with(methodModel);
            }
        }
    }

    /**
     * Generate event wrapper methods and their corresponding $event methods in pairs
     */
    private void generateEventMethodPairs(ClassBuilder classBuilder, ClassModel originalClass) {
        for (MethodModel methodModel : originalClass.methods()) {
            var methodName = methodModel.methodName().stringValue();
            if (!methodName.equals("<init>")) {
                var methodMetadata = findMethodMetadata(methodModel);
                if (methodMetadata != null && remappedMethods.contains(methodMetadata)) {
                    generateEventWrapperMethod(classBuilder, methodMetadata, methodModel);
                    generateRemappedEventMethod(classBuilder, methodModel, methodName);
                }
            }
        }
    }

    /**
     * Generate the remapped $event method with API transformations
     */
    private void generateRemappedEventMethod(ClassBuilder classBuilder, MethodModel methodModel, String methodName) {
        var eventMethodName = REMAPPED_TEMPLATE.formatted(methodName);
        var eventMethodFlags = ClassFile.ACC_PROTECTED;

        classBuilder.withMethod(eventMethodName, methodModel.methodTypeSymbol(), eventMethodFlags,
                                methodBuilder -> {
                                    MethodTransform methodTransform = (mb, me) -> {
                                        if (!(me instanceof AccessFlags)) {
                                            API_REMAPPER.asMethodTransform().accept(mb, me);
                                        }
                                    };

                                    methodBuilder.transform(methodModel, methodTransform);
                                });
    }

    /**
     * Copy regular non-event methods from the original class
     */
    private void copyRegularMethods(ClassBuilder classBuilder, ClassModel originalClass) {
        for (MethodModel methodModel : originalClass.methods()) {
            var methodName = methodModel.methodName().stringValue();
            if (!methodName.equals("<init>")) {
                var methodMetadata = findMethodMetadata(methodModel);
                if (methodMetadata == null || !remappedMethods.contains(methodMetadata)) {
                    classBuilder.with(methodModel);
                }
            }
        }
    }

    /**
     * Generate EntityReference interface methods (__invoke and __signatureFor)
     */
    private void generateEntityReferenceMethods(ClassBuilder classBuilder) {
        generateInvokeMethod(classBuilder);
        generateSignatureForMethod(classBuilder);
    }

    /**
     * Find MethodMetadata that matches a MethodModel using O(1) HashMap lookup
     */
    private MethodMetadata findMethodMetadata(MethodModel methodModel) {
        var name = methodModel.methodName().stringValue();
        var descriptor = methodModel.methodTypeSymbol().descriptorString();
        return methodIndex.get(name + ":" + descriptor);
    }

    /**
     * Generate an event wrapper method with the original method name
     */
    private void generateEventWrapperMethod(ClassBuilder classBuilder, MethodMetadata originalMethod,
                                            MethodModel methodModel) {
        // Use the original method name for the event wrapper
        String wrapperMethodName = originalMethod.getName();

        // Get access flags from the MethodModel to match ClassFile API format
        int accessFlags = methodModel.flags().flagsMask();

        classBuilder.withMethodBody(wrapperMethodName,
                                    MethodTypeDesc.ofDescriptor(originalMethod.getDescriptor()), accessFlags,
                                    codeBuilder -> {
                                        // Get method index
                                        Integer methodIdx = methodToIndex.get(originalMethod);
                                        if (methodIdx == null) {
                                            throw new IllegalStateException(
                                                "[EntityGenerator] No event index found for method: " +
                                                originalMethod.getName() + originalMethod.getDescriptor() +
                                                " in class " + className);
                                        }

                                        // Call Framework.getController()
                                        codeBuilder.invokestatic(FRAMEWORK_CLASS, "getController",
                                                                 GET_CONTROLLER_METHOD_TYPE);

                                        // Load 'this' for posting the event
                                        codeBuilder.aload(0);

                                        // Load method index
                                        codeBuilder.ldc(methodIdx);

                                        // Create and populate arguments array
                                        int paramCount = originalMethod.getParameterCount();
                                        codeBuilder.ldc(paramCount);
                                        codeBuilder.anewarray(OBJECT_CLASS);

                                        // Store each parameter in the array
                                        var paramTypes = originalMethod.getParameters();
                                        for (int i = 0; i < paramTypes.size(); i++) {
                                            codeBuilder.dup();
                                            codeBuilder.ldc(i);
                                            loadParameter(codeBuilder, i + 1, paramTypes.get(i));
                                            boxPrimitiveIfNeeded(codeBuilder, paramTypes.get(i));
                                            codeBuilder.aastore();
                                        }

                                        // Determine which post method to call based on blocking status
                                        if (blockingMethods.contains(originalMethod)) {
                                            codeBuilder.invokevirtual(DEVI_CLASS, "postContinuingEvent",
                                                                      POST_CONTINUING_EVENT_METHOD_TYPE);
                                            // postContinuingEvent returns Object, so pop it off the stack
                                            codeBuilder.pop();
                                        } else {
                                            codeBuilder.invokevirtual(DEVI_CLASS, "postEvent", POST_EVENT_METHOD_TYPE);
                                            // postEvent returns void, so nothing to pop
                                        }

                                        // Return appropriate value
                                        returnDefaultValue(codeBuilder, originalMethod);
                                    });
    }

    /**
     * Generate a single case in the __invoke switch statement
     */
    private void generateInvokeCase(CodeBuilder codeBuilder, int methodIdx) {
        MethodMetadata methodMetadata = indexToMethod.get(methodIdx);
        if (methodMetadata == null) {
            throw new IllegalArgumentException(
                "[EntityGenerator] No method found for event index " + methodIdx +
                " in class " + className);
        }

        // Load 'this'
        codeBuilder.aload(0);

        // Load and unbox parameters from Object array
        var paramTypes = methodMetadata.getParameters();
        for (int i = 0; i < paramTypes.size(); i++) {
            codeBuilder.aload(2); // Load args array
            codeBuilder.ldc(i);   // Load index
            codeBuilder.aaload(); // Get args[i]
            unboxParameter(codeBuilder, paramTypes.get(i));
        }

        // Call the appropriate method
        String methodName = REMAPPED_TEMPLATE.formatted(methodMetadata.getName());
        if (remappedMethods.contains(methodMetadata)) {
            // Call the event wrapper method on this class
            codeBuilder.invokevirtual(classDesc, methodName,
                                      MethodTypeDesc.ofDescriptor(methodMetadata.getDescriptor()));
        } else {
            // Call the method on superclass
            var superClassName = clazz.getSuperclassName();
            if (superClassName != null) {
                codeBuilder.invokespecial(ClassDesc.of(superClassName), methodName,
                                          MethodTypeDesc.ofDescriptor(methodMetadata.getDescriptor()));
            } else {
                throw new IllegalStateException(
                    "[EntityGenerator] Cannot invoke non-remapped method " +
                    methodMetadata.getName() + " in class " + clazz.getName() +
                    " - no superclass found");
            }
        }

        // Handle return value
        var returnType = methodMetadata.getReturnType();
        if (returnType.isVoid()) {
            codeBuilder.aload(0); // Return 'this' for void methods
        } else {
            boxReturnValue(codeBuilder, returnType.getDescriptor());
        }
        codeBuilder.areturn();
    }

    /**
     * Get sorted array of method indices for switch table generation
     */
    private int[] getSortedMethodKeys() {
        return indexToMethod.keySet().stream().mapToInt(Integer::intValue).sorted().toArray();
    }

    /**
     * Generate the __invoke method for EntityReference interface using ClassFile API
     */
    private void generateInvokeMethod(ClassBuilder classBuilder) {
        if (indexToMethod.isEmpty()) {
            return;
        }

        classBuilder.withMethodBody(INVOKE, INVOKE_METHOD_TYPE, ClassFile.ACC_PUBLIC, codeBuilder -> {
            // Create switch table for event dispatching
            int[] keys = getSortedMethodKeys();

            codeBuilder.iload(1); // Load event index parameter

            // Use tableswitch for efficient dispatch
            Label defaultLabel = codeBuilder.newLabel();
            java.util.List<SwitchCase> cases = new java.util.ArrayList<>();

            // Create labels and cases for each method
            for (int key : keys) {
                Label caseLabel = codeBuilder.newLabel();
                cases.add(SwitchCase.of(key, caseLabel));
            }

            codeBuilder.tableswitch(keys[0], keys[keys.length - 1], defaultLabel, cases);

            // Generate each case
            for (int i = 0; i < keys.length; i++) {
                int key = keys[i];
                Label caseLabel = cases.get(i).target();
                codeBuilder.labelBinding(caseLabel);
                generateInvokeCase(codeBuilder, key);
            }

            // Generate default case
            codeBuilder.labelBinding(defaultLabel);
            codeBuilder.new_(ClassDesc.of("java.lang.IllegalArgumentException"))
                       .dup()
                       .ldc("[EntityGenerator] Unknown event index for class " + className)
                       .invokespecial(ClassDesc.of("java.lang.IllegalArgumentException"), "<init>",
                                      MethodTypeDesc.of(ConstantDescs.CD_void, STRING_CLASS))
                       .athrow();
        });
    }

    /**
     * Generate a single case in the __signatureFor switch statement
     */
    private void generateSignatureCase(CodeBuilder codeBuilder, int methodIdx) {
        MethodMetadata methodMetadata = indexToMethod.get(methodIdx);
        if (methodMetadata == null) {
            codeBuilder.ldc("");
        } else {
            String signature = buildMethodSignature(methodMetadata);
            codeBuilder.ldc(signature);
        }
        codeBuilder.areturn();
    }

    /**
     * Generate the __signatureFor method for EntityReference interface using ClassFile API
     */
    private void generateSignatureForMethod(ClassBuilder classBuilder) {
        if (indexToMethod.isEmpty()) {
            return;
        }

        classBuilder.withMethodBody(SIGNATURE_FOR, SIGNATURE_FOR_METHOD_TYPE, ClassFile.ACC_PUBLIC, codeBuilder -> {
            // Create switch table for signature lookup
            int[] keys = getSortedMethodKeys();

            codeBuilder.iload(1); // Load method index parameter

            // Use tableswitch for efficient dispatch
            Label defaultLabel = codeBuilder.newLabel();
            java.util.List<SwitchCase> cases = new java.util.ArrayList<>();

            // Create labels and cases for each method
            for (int key : keys) {
                Label caseLabel = codeBuilder.newLabel();
                cases.add(SwitchCase.of(key, caseLabel));
            }

            codeBuilder.tableswitch(keys[0], keys[keys.length - 1], defaultLabel, cases);

            // Generate each case
            for (int i = 0; i < keys.length; i++) {
                int key = keys[i];
                Label caseLabel = cases.get(i).target();
                codeBuilder.labelBinding(caseLabel);
                generateSignatureCase(codeBuilder, key);
            }

            // Generate default case
            codeBuilder.labelBinding(defaultLabel);
            codeBuilder.new_(ClassDesc.of("java.lang.IllegalArgumentException"))
                       .dup()
                       .ldc("[EntityGenerator] Unknown event index for class " + className)
                       .invokespecial(ClassDesc.of("java.lang.IllegalArgumentException"), "<init>",
                                      MethodTypeDesc.of(ConstantDescs.CD_void, STRING_CLASS))
                       .athrow();
        });
    }

    /**
     * Initialize event mappings and determine which methods are blocking/remapped
     */
    private void initializeEventMappings(Set<MethodMetadata> eventMethods) {
        var key = 0;
        for (var mi : eventMethods.stream().sorted(METHOD_ORDER).toList()) {
            indexToMethod.put(key, mi);
            methodToIndex.put(mi, key++);

            // Build O(1) lookup index: "name:descriptor" -> MethodMetadata
            var methodKey = mi.getName() + ":" + mi.getDescriptor();
            methodIndex.put(methodKey, mi);

            // Determine if this is a blocking event
            if (isBlockingEvent(mi)) {
                blockingMethods.add(mi);
            }

            // Determine if this method should be remapped
            if (isDeclaredInClass(mi)) {
                remappedMethods.add(mi);
            }
        }
    }

    private boolean isBlockingEvent(MethodMetadata mi) {
        return !mi.isVoid() || mi.hasAnnotation(Blocking.class);
    }

    private boolean isDeclaredInClass(MethodMetadata mi) {
        return clazz.getDeclaredMethods(mi.getName()).stream()
                    .anyMatch(m -> mi.equals(m));
    }

    /**
     * Check if a class name represents a wrapper class using O(1) Set lookup
     */
    private boolean isWrapperClass(String className) {
        return WRAPPER_CLASS_NAMES.contains(className);
    }

    /**
     * Load a parameter onto the stack with the correct load instruction
     */
    private void loadParameter(CodeBuilder codeBuilder, int index, ParameterMetadata param) {
        var type = param.getType();
        if (type.isPrimitive()) {
            loadPrimitive(codeBuilder, index, type.getPrimitiveChar());
        } else {
            // Load reference types (objects, arrays, etc.)
            codeBuilder.aload(index);
        }
    }

    /**
     * Load a primitive type with the correct instruction
     */
    private void loadPrimitive(CodeBuilder codeBuilder, int index, char primitiveType) {
        switch (primitiveType) {
            case 'I', 'B', 'C', 'S', 'Z' -> codeBuilder.iload(index);
            case 'J' -> codeBuilder.lload(index);
            case 'F' -> codeBuilder.fload(index);
            case 'D' -> codeBuilder.dload(index);
            default -> throw new IllegalArgumentException(
                "[EntityGenerator] Unknown primitive type for load: '" + primitiveType +
                "' in class " + className);
        }
    }

    /**
     * Return the appropriate default value based on method return type
     */
    private void returnDefaultValue(CodeBuilder codeBuilder, MethodMetadata method) {
        var returnType = method.getReturnType();

        if (returnType.isVoid()) {
            codeBuilder.return_();
            return;
        }

        if (returnType.isPrimitive()) {
            returnPrimitiveDefault(codeBuilder, returnType.getPrimitiveChar());
        } else {
            // Return null for reference types
            codeBuilder.aconst_null();
            codeBuilder.areturn();
        }
    }

    /**
     * Return the default value for a primitive type
     */
    private void returnPrimitiveDefault(CodeBuilder codeBuilder, char primitiveType) {
        switch (primitiveType) {
            case 'I', 'B', 'C', 'S', 'Z' -> {
                codeBuilder.iconst_0();
                codeBuilder.ireturn();
            }
            case 'J' -> {
                codeBuilder.lconst_0();
                codeBuilder.lreturn();
            }
            case 'F' -> {
                codeBuilder.fconst_0();
                codeBuilder.freturn();
            }
            case 'D' -> {
                codeBuilder.dconst_0();
                codeBuilder.dreturn();
            }
            default -> throw new IllegalArgumentException(
                "[EntityGenerator] Unknown primitive type for return: '" + primitiveType +
                "' in class " + className);
        }
    }

    /**
     * Convert a parameter from Object to the target type (primitive or reference)
     */
    private void unboxParameter(CodeBuilder codeBuilder, ParameterMetadata param) {
        var type = param.getType();

        if (type.isPrimitive()) {
            // Handle primitive types - unbox from wrapper objects
            unboxPrimitive(codeBuilder, type.getPrimitiveChar());
        } else if (type.isArray()) {
            // Handle array types - cast to specific array type
            codeBuilder.checkcast(ClassDesc.ofDescriptor(type.getDescriptor()));
        } else {
            // Handle reference types
            codeBuilder.checkcast(ClassDesc.of(type.getClassName()));
        }
    }

    /**
     * Unbox a primitive type from its wrapper object using lookup tables
     */
    private void unboxPrimitive(CodeBuilder codeBuilder, char primitiveType) {
        var wrapperClass = WRAPPER_CLASSES.get(primitiveType);
        var methodName = UNBOX_METHOD_NAMES.get(primitiveType);
        var methodType = UNBOXING_METHOD_TYPES.get(primitiveType);

        if (wrapperClass == null || methodName == null || methodType == null) {
            throw new IllegalArgumentException(
                "[EntityGenerator] Unknown primitive type for unboxing: '" + primitiveType +
                "' in class " + className);
        }

        codeBuilder.checkcast(wrapperClass);
        codeBuilder.invokevirtual(wrapperClass, methodName, methodType);
    }
}
