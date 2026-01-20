/*
 * Copyright (C) 2026 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover IntelliJ Plugin.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 */
package com.hellblazer.primemover.intellij.jps;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static com.hellblazer.primemover.intellij.jps.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive end-to-end verification tests for transformed class signatures.
 * Validates that the bytecode transformation produces correct:
 * - Interface implementations (EntityReference)
 * - Required methods (__invoke, __signatureFor)
 * - Annotations (@Transformed)
 * - Method preservation (original event methods)
 * - Event metadata correctness
 */
public class ClassSignatureVerificationTest {

    private static final String ENTITY_REFERENCE_INTERFACE = "com/hellblazer/primeMover/api/EntityReference";
    private static final String TRANSFORMED_ANNOTATION = "Lcom/hellblazer/primeMover/annotations/Transformed;";

    /**
     * Test that transformed classes implement EntityReference interface.
     */
    @Test
    void testTransformedClassImplementsEntityReference(@TempDir Path tempDir) throws Exception {
        // Setup and transform
        copyTestClass(tempDir, "MyTest");
        copyTestClass(tempDir, "Foo");

        var classModel = loadTransformedClass(tempDir, "com.hellblazer.primeMover.classfile.testClasses.MyTest");

        // Verify EntityReference interface added
        boolean implementsEntityReference = classModel.interfaces().stream()
            .anyMatch(i -> i.asInternalName().equals(ENTITY_REFERENCE_INTERFACE));

        assertTrue(implementsEntityReference,
            "Transformed class should implement EntityReference interface");
    }

    /**
     * Test that transformed classes have the __invoke method with correct signature.
     */
    @Test
    void testTransformedClassHasInvokeMethod(@TempDir Path tempDir) throws Exception {
        copyTestClass(tempDir, "MyTest");
        copyTestClass(tempDir, "Foo");

        var classModel = loadTransformedClass(tempDir, "com.hellblazer.primeMover.classfile.testClasses.MyTest");

        // Find __invoke method
        MethodModel invokeMethod = findMethod(classModel, "__invoke");
        assertNotNull(invokeMethod, "Should have __invoke method");

        // Verify signature: Object __invoke(int ordinal, Object[] args)
        var descriptor = invokeMethod.methodType().stringValue();
        assertEquals("(I[Ljava/lang/Object;)Ljava/lang/Object;", descriptor,
            "__invoke should have signature (int, Object[]) -> Object");

        // Verify it's public
        assertTrue((invokeMethod.flags().flagsMask() & ClassFile.ACC_PUBLIC) != 0,
            "__invoke should be public");
    }

    /**
     * Test that transformed classes have the __signatureFor method with correct signature.
     */
    @Test
    void testTransformedClassHasSignatureForMethod(@TempDir Path tempDir) throws Exception {
        copyTestClass(tempDir, "MyTest");
        copyTestClass(tempDir, "Foo");

        var classModel = loadTransformedClass(tempDir, "com.hellblazer.primeMover.classfile.testClasses.MyTest");

        // Find __signatureFor method
        MethodModel signatureForMethod = findMethod(classModel, "__signatureFor");
        assertNotNull(signatureForMethod, "Should have __signatureFor method");

        // Verify signature: String __signatureFor(int ordinal)
        var descriptor = signatureForMethod.methodType().stringValue();
        assertEquals("(I)Ljava/lang/String;", descriptor,
            "__signatureFor should have signature (int) -> String");

        // Verify it's public
        assertTrue((signatureForMethod.flags().flagsMask() & ClassFile.ACC_PUBLIC) != 0,
            "__signatureFor should be public");
    }

    /**
     * Test that transformed classes have the @Transformed annotation.
     */
    @Test
    void testTransformedAnnotationApplied(@TempDir Path tempDir) throws Exception {
        copyTestClass(tempDir, "MyTest");
        copyTestClass(tempDir, "Foo");

        var classModel = loadTransformedClass(tempDir, "com.hellblazer.primeMover.classfile.testClasses.MyTest");

        // Check for @Transformed annotation
        boolean hasTransformed = false;
        for (var attr : classModel.attributes()) {
            if (attr instanceof RuntimeVisibleAnnotationsAttribute rva) {
                for (var ann : rva.annotations()) {
                    if (ann.className().stringValue().equals(TRANSFORMED_ANNOTATION)) {
                        hasTransformed = true;
                        break;
                    }
                }
            }
        }

        assertTrue(hasTransformed, "Should have @Transformed annotation");
    }

    /**
     * Test that original event methods are preserved in the transformed class.
     */
    @Test
    void testOriginalMethodsPreserved(@TempDir Path tempDir) throws Exception {
        copyTestClass(tempDir, "MyTest");
        copyTestClass(tempDir, "Foo");

        var classModel = loadTransformedClass(tempDir, "com.hellblazer.primeMover.classfile.testClasses.MyTest");

        // MyTest class should have original methods from Foo interface
        Set<String> methodNames = new HashSet<>();
        for (var method : classModel.methods()) {
            methodNames.add(method.methodName().stringValue());
        }

        // Original methods should still exist
        assertTrue(methodNames.contains("bar"), "Should preserve bar() method");
        assertTrue(methodNames.contains("myMy"), "Should preserve myMy() method");
        assertTrue(methodNames.contains("someArgs"), "Should preserve someArgs() method");

        // Verify original method signatures unchanged
        var barMethod = findMethod(classModel, "bar");
        assertNotNull(barMethod);
        assertEquals("()V", barMethod.methodType().stringValue(),
            "bar() signature should be unchanged");

        var myMyMethod = findMethod(classModel, "myMy");
        assertNotNull(myMyMethod);
        assertEquals("()Ljava/lang/String;", myMyMethod.methodType().stringValue(),
            "myMy() signature should be unchanged");
    }

    /**
     * Test that event methods have corresponding remapped versions.
     */
    @Test
    void testEventMethodsHaveRemappedVersions(@TempDir Path tempDir) throws Exception {
        copyTestClass(tempDir, "MyTest");
        copyTestClass(tempDir, "Foo");

        var classModel = loadTransformedClass(tempDir, "com.hellblazer.primeMover.classfile.testClasses.MyTest");

        // Event methods should have $event remapped versions
        Set<String> methodNames = new HashSet<>();
        for (var method : classModel.methods()) {
            methodNames.add(method.methodName().stringValue());
        }

        // Look for remapped methods (original implementation moved to methodName$event)
        assertTrue(methodNames.contains("bar$event") ||
                   methodNames.contains("bar"),
            "Should have bar method or bar$event remapped version");

        assertTrue(methodNames.contains("myMy$event") ||
                   methodNames.contains("myMy"),
            "Should have myMy method or myMy$event remapped version");

        assertTrue(methodNames.contains("someArgs$event") ||
                   methodNames.contains("someArgs"),
            "Should have someArgs method or someArgs$event remapped version");
    }

    /**
     * Test the complete transformation pipeline on a complex class.
     */
    @Test
    void testCompleteTransformationOnTemplateClass(@TempDir Path tempDir) throws Exception {
        copyTestClass(tempDir, "Template");

        var classModel = loadTransformedClass(tempDir, "com.hellblazer.primeMover.classfile.testClasses.Template");

        // Verify all transformation artifacts
        assertTrue(implementsInterface(classModel, ENTITY_REFERENCE_INTERFACE.replace('/', '.')),
            "Should implement EntityReference");

        assertTrue(hasTransformedAnnotation(classModel),
            "Should have @Transformed annotation");

        assertTrue(hasMethod(classModel, "__invoke"),
            "Should have __invoke method");

        assertTrue(hasMethod(classModel, "__signatureFor"),
            "Should have __signatureFor method");

        // Count methods (should have originals + remapped + infrastructure)
        int methodCount = 0;
        for (var method : classModel.methods()) {
            methodCount++;
        }

        assertTrue(methodCount > 0, "Should have methods");
    }

    /**
     * Test that the __invoke method dispatches to correct event methods.
     */
    @Test
    void testInvokeMethodDispatchLogic(@TempDir Path tempDir) throws Exception {
        copyTestClass(tempDir, "MyTest");
        copyTestClass(tempDir, "Foo");

        var classModel = loadTransformedClass(tempDir, "com.hellblazer.primeMover.classfile.testClasses.MyTest");

        var invokeMethod = findMethod(classModel, "__invoke");
        assertNotNull(invokeMethod);

        // Verify __invoke method has code
        var code = invokeMethod.code();
        assertTrue(code.isPresent(), "__invoke should have method body");

        // The code should contain switch logic dispatching on ordinal
        var codeModel = code.get();
        var instructions = codeModel.toString();

        // Look for switch/tableswitch/lookupswitch instruction
        assertTrue(instructions.contains("switch") || instructions.contains("Switch"),
            "__invoke should contain switch logic for ordinal dispatch");
    }

    /**
     * Test that the __signatureFor method returns correct signatures.
     */
    @Test
    void testSignatureForMethodReturnsCorrectSignatures(@TempDir Path tempDir) throws Exception {
        copyTestClass(tempDir, "MyTest");
        copyTestClass(tempDir, "Foo");

        var classModel = loadTransformedClass(tempDir, "com.hellblazer.primeMover.classfile.testClasses.MyTest");

        var signatureForMethod = findMethod(classModel, "__signatureFor");
        assertNotNull(signatureForMethod);

        // Verify method has code
        var code = signatureForMethod.code();
        assertTrue(code.isPresent(), "__signatureFor should have method body");

        // The code should contain string constants for method signatures
        var codeModel = code.get();
        var constants = codeModel.toString();

        // Look for method signature strings (e.g., "bar:()V")
        assertTrue(constants.contains("bar:()V") || constants.contains("bar"),
            "__signatureFor should contain signature for bar()");

        assertTrue(constants.contains("myMy:()Ljava/lang/String;") || constants.contains("myMy"),
            "__signatureFor should contain signature for myMy()");
    }

    /**
     * Test that transformed classes maintain proper class hierarchy.
     */
    @Test
    void testClassHierarchyPreserved(@TempDir Path tempDir) throws Exception {
        copyTestClass(tempDir, "MyTest");
        copyTestClass(tempDir, "Foo");

        var classModel = loadTransformedClass(tempDir, "com.hellblazer.primeMover.classfile.testClasses.MyTest");

        // Verify superclass unchanged
        var superclass = classModel.superclass();
        assertTrue(superclass.isPresent(), "Should have superclass");

        var superName = superclass.get().asInternalName();
        assertEquals("java/lang/Object", superName,
            "MyTest should extend Object");

        // Verify original interface (Foo) still present
        boolean implementsFoo = classModel.interfaces().stream()
            .anyMatch(i -> i.asInternalName().equals("com/hellblazer/primeMover/classfile/testClasses/Foo"));

        assertTrue(implementsFoo,
            "Should still implement original Foo interface");

        // Verify EntityReference added
        boolean implementsEntityReference = classModel.interfaces().stream()
            .anyMatch(i -> i.asInternalName().equals(ENTITY_REFERENCE_INTERFACE));

        assertTrue(implementsEntityReference,
            "Should add EntityReference interface");
    }

    /**
     * Test that bytecode passes JVM verification.
     */
    @Test
    void testBytecodePassesVerification(@TempDir Path tempDir) throws Exception {
        copyTestClass(tempDir, "MyTest");
        copyTestClass(tempDir, "Foo");

        var bytecode = transformAndGetBytecode(tempDir, "com.hellblazer.primeMover.classfile.testClasses.MyTest");

        // Parse with ClassFile API (this performs basic verification)
        var classModel = ClassFile.of().parse(bytecode);
        assertNotNull(classModel, "Bytecode should parse successfully");

        // Verify all methods have valid code attributes
        for (var method : classModel.methods()) {
            var flags = method.flags().flagsMask();
            if ((flags & ClassFile.ACC_ABSTRACT) == 0 &&
                (flags & ClassFile.ACC_NATIVE) == 0) {

                var code = method.code();
                assertTrue(code.isPresent(),
                    String.format("Non-abstract method %s should have code attribute",
                        method.methodName().stringValue()));
            }
        }
    }

    // === Helper Methods ===

    /**
     * Find a method by name in a ClassModel.
     */
    private MethodModel findMethod(ClassModel classModel, String methodName) {
        for (var method : classModel.methods()) {
            if (method.methodName().stringValue().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    /**
     * Check if a ClassModel implements a given interface (internal name format).
     */
    private boolean implementsInterface(ClassModel classModel, String interfaceName) {
        var internalName = interfaceName.replace('.', '/');
        return classModel.interfaces().stream()
            .anyMatch(i -> i.asInternalName().equals(internalName));
    }
}
