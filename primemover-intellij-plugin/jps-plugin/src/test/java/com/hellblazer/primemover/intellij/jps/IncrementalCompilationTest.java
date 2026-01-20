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

import com.hellblazer.primeMover.classfile.ClassMetadata;
import com.hellblazer.primeMover.classfile.SimulationTransform;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static com.hellblazer.primemover.intellij.jps.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive end-to-end verification tests for incremental compilation behavior.
 * Validates that the transformation pipeline correctly:
 * - Transforms @Entity classes on first pass
 * - Skips @Transformed classes on subsequent passes (idempotent)
 * - Retransforms only modified classes in incremental builds
 * - Handles class additions and deletions correctly
 */
public class IncrementalCompilationTest {

    /**
     * Test that the first compilation transforms @Entity classes.
     */
    @Test
    void testFirstCompilationTransformsEntityClasses(@TempDir Path tempDir) throws Exception {
        // Setup: Copy untransformed @Entity class
        copyTestClass(tempDir, "MyTest");
        copyTestClass(tempDir, "Foo");

        // First compilation pass
        int transformedCount = buildAndCountTransformations(tempDir);

        assertTrue(transformedCount > 0,
            "First compilation should transform @Entity classes");

        // Verify the transformed class has @Transformed annotation
        var classPath = getClassFilePath(tempDir, "com.hellblazer.primeMover.classfile.testClasses.MyTest");
        var bytecode = Files.readAllBytes(classPath);
        var classModel = ClassFile.of().parse(bytecode);

        assertTrue(hasTransformedAnnotation(classModel),
            "Transformed class should have @Transformed annotation");
    }

    /**
     * Test that incremental compilation skips already-transformed classes.
     * Second pass with no changes should transform 0 classes.
     */
    @Test
    void testIncrementalCompilationSkipsAlreadyTransformed(@TempDir Path tempDir) throws Exception {
        // Setup
        copyTestClass(tempDir, "MyTest");
        copyTestClass(tempDir, "Foo");

        // First pass: count transformations
        int firstPass = buildAndCountTransformations(tempDir);
        assertTrue(firstPass > 0, "Should transform classes on first pass");

        // Second pass: no changes, should skip @Transformed classes
        int secondPass = buildAndCountTransformations(tempDir);

        assertEquals(0, secondPass,
            String.format("Should skip @Transformed classes (first pass transformed %d classes)", firstPass));
    }

    /**
     * Test that modifying source retriggers transformation for that class only.
     */
    @Test
    void testModifyingSourceRetriggersTransform(@TempDir Path tempDir) throws Exception {
        // Setup: Multiple entity classes
        copyTestClass(tempDir, "MyTest");
        copyTestClass(tempDir, "Template");
        copyTestClass(tempDir, "Foo");

        // First compilation
        int firstPass = buildAndCountTransformations(tempDir);
        assertTrue(firstPass >= 2, "Should transform multiple classes");

        // Simulate modification: replace MyTest with a "modified" version
        // In a real scenario, the Java compiler would produce a new .class without @Transformed
        // For testing, we'll copy the original (untransformed) version back
        var originalMyTest = TRANSFORM_TEST_CLASSES.resolve("MyTest.class");
        var modifiedMyTest = getClassFilePath(tempDir, "com.hellblazer.primeMover.classfile.testClasses.MyTest");
        Files.delete(modifiedMyTest);
        Files.copy(originalMyTest, modifiedMyTest);

        // Second compilation: only MyTest should be retransformed
        int secondPass = buildAndCountTransformations(tempDir);

        assertEquals(1, secondPass,
            "Should retransform only the modified class (MyTest)");

        // Verify MyTest is transformed again
        var bytecode = Files.readAllBytes(modifiedMyTest);
        var classModel = ClassFile.of().parse(bytecode);
        assertTrue(hasTransformedAnnotation(classModel),
            "Retransformed class should have @Transformed annotation");
    }

    /**
     * Test adding a new @Entity class to an existing project.
     */
    @Test
    void testAddingNewEntityClass(@TempDir Path tempDir) throws Exception {
        // First pass: single entity class
        copyTestClass(tempDir, "MyTest");
        copyTestClass(tempDir, "Foo");

        int firstPass = buildAndCountTransformations(tempDir);
        assertEquals(1, firstPass, "Should transform MyTest");

        // Add a new entity class (Template)
        copyTestClass(tempDir, "Template");

        // Second pass: should transform only the new class
        int secondPass = buildAndCountTransformations(tempDir);

        assertEquals(1, secondPass,
            "Should transform only the newly added class (Template)");
    }

    /**
     * Test removing an entity class from the project.
     */
    @Test
    void testRemovingEntityClass(@TempDir Path tempDir) throws Exception {
        // Setup: Multiple entity classes
        copyTestClass(tempDir, "MyTest");
        copyTestClass(tempDir, "Template");
        copyTestClass(tempDir, "Foo");

        // First pass
        int firstPass = buildAndCountTransformations(tempDir);
        assertTrue(firstPass >= 2, "Should transform multiple classes");

        // Remove Template class
        var templatePath = getClassFilePath(tempDir, "com.hellblazer.primeMover.classfile.testClasses.Template");
        Files.delete(templatePath);

        // Second pass: no new transformations (MyTest already has @Transformed)
        int secondPass = buildAndCountTransformations(tempDir);

        assertEquals(0, secondPass,
            "Should not transform any classes after removing one");
    }

    /**
     * Test that multiple incremental builds maintain idempotency.
     */
    @Test
    void testMultipleIncrementalBuildsAreIdempotent(@TempDir Path tempDir) throws Exception {
        // Setup
        copyTestClass(tempDir, "MyTest");
        copyTestClass(tempDir, "Foo");

        // First pass
        buildAndCountTransformations(tempDir);

        // Perform 10 incremental builds with no changes
        for (int i = 0; i < 10; i++) {
            int count = buildAndCountTransformations(tempDir);
            assertEquals(0, count,
                String.format("Incremental build %d should transform 0 classes", i + 1));
        }
    }

    /**
     * Test that the transformation writes bytecode correctly for subsequent reads.
     */
    @Test
    void testTransformationWritesValidBytecode(@TempDir Path tempDir) throws Exception {
        // Setup
        copyTestClass(tempDir, "MyTest");
        copyTestClass(tempDir, "Foo");

        // Transform
        buildAndCountTransformations(tempDir);

        // Read the transformed class
        var classPath = getClassFilePath(tempDir, "com.hellblazer.primeMover.classfile.testClasses.MyTest");
        var bytecode = Files.readAllBytes(classPath);

        // Verify bytecode is valid
        var classModel = ClassFile.of().parse(bytecode);
        assertNotNull(classModel, "Should parse transformed bytecode");

        // Verify transformation applied
        assertTrue(hasTransformedAnnotation(classModel),
            "Should have @Transformed annotation");

        assertTrue(implementsInterface(classModel, "com.hellblazer.primeMover.api.EntityReference"),
            "Should implement EntityReference interface");

        assertTrue(hasMethod(classModel, "__invoke"),
            "Should have __invoke method");

        assertTrue(hasMethod(classModel, "__signatureFor"),
            "Should have __signatureFor method");
    }

    /**
     * Test handling of already transformed classes during rebuild.
     * Simulates a rebuild scenario where classes already have @Transformed annotation.
     */
    @Test
    void testHandlingPartiallyTransformedClasses(@TempDir Path tempDir) throws Exception {
        // Setup: Copy and transform a class first
        copyTestClass(tempDir, "MyTest");
        copyTestClass(tempDir, "Foo");

        // First transformation - this adds @Transformed annotation
        int firstPass = buildAndCountTransformations(tempDir);
        assertTrue(firstPass > 0, "First pass should transform classes");

        // Second pass: Classes now have @Transformed annotation
        // Should skip all already-transformed classes
        int secondPass = buildAndCountTransformations(tempDir);

        assertEquals(0, secondPass,
            "Should skip classes with @Transformed annotation from previous build");
    }

    /**
     * Test that clean builds (removing output directory) retransform all classes.
     */
    @Test
    void testCleanBuildRetransformsAllClasses(@TempDir Path tempDir) throws Exception {
        // Setup
        copyTestClass(tempDir, "MyTest");
        copyTestClass(tempDir, "Template");
        copyTestClass(tempDir, "Foo");

        // First build
        int firstPass = buildAndCountTransformations(tempDir);
        assertTrue(firstPass >= 2, "Should transform multiple classes");

        // Simulate clean build: delete transformed classes and copy originals back
        var myTestPath = getClassFilePath(tempDir, "com.hellblazer.primeMover.classfile.testClasses.MyTest");
        var templatePath = getClassFilePath(tempDir, "com.hellblazer.primeMover.classfile.testClasses.Template");

        Files.delete(myTestPath);
        Files.delete(templatePath);

        copyTestClass(tempDir, "MyTest");
        copyTestClass(tempDir, "Template");

        // Clean build: should retransform all classes
        int cleanBuild = buildAndCountTransformations(tempDir);

        assertEquals(firstPass, cleanBuild,
            "Clean build should retransform all entity classes");
    }

    // === Helper Methods ===

    /**
     * Simulate a source modification by restoring the untransformed version of a class.
     */
    private void modifySourceFile(Path tempDir, String className) throws IOException {
        var classPath = getClassFilePath(tempDir, "com.hellblazer.primeMover.classfile.testClasses." + className);
        var originalPath = TRANSFORM_TEST_CLASSES.resolve(className + ".class");

        Files.delete(classPath);
        Files.copy(originalPath, classPath);
    }
}
