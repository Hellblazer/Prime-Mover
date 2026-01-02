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
import java.lang.classfile.ClassModel;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PrimeMoverClassInstrumenter transformation logic.
 * Tests the integration with SimulationTransform using the ClassFile API.
 */
public class PrimeMoverClassInstrumenterTest {

    private static final String TRANSFORMED_ANNOTATION = "Lcom/hellblazer/primeMover/annotations/Transformed;";

    // Path to transform module test classes (relative to jps-plugin directory)
    // jps-plugin -> primemover-intellij-plugin -> Prime-Mover -> transform
    private static final Path TRANSFORM_TEST_CLASSES = Path.of("../../transform/target/test-classes/com/hellblazer/primeMover/classfile/testClasses");

    @Test
    void testSimulationTransformFindsEntityClasses(@TempDir Path tempDir) throws Exception {
        // Copy test class from the transform module's test classes
        var testClassPath = TRANSFORM_TEST_CLASSES.resolve("MyTest.class");
        var fooPath = TRANSFORM_TEST_CLASSES.resolve("Foo.class");

        if (!Files.exists(testClassPath)) {
            // Skip if transform module hasn't been built with tests
            System.out.println("SKIP: Transform module test classes not found at " + testClassPath.toAbsolutePath());
            return;
        }

        // Create package directory structure in temp
        var packageDir = tempDir.resolve("com/hellblazer/primeMover/classfile/testClasses");
        Files.createDirectories(packageDir);

        // Copy test class files
        Files.copy(testClassPath, packageDir.resolve("MyTest.class"));
        Files.copy(fooPath, packageDir.resolve("Foo.class"));

        // Run SimulationTransform on temp directory
        try (var transform = new SimulationTransform(tempDir)) {
            Map<ClassMetadata, byte[]> transformed = transform.transformed();

            // Should find and transform MyTest class
            assertFalse(transformed.isEmpty(), "Should transform at least one entity class");

            // Verify transformation produced valid bytecode
            for (var entry : transformed.entrySet()) {
                var metadata = entry.getKey();
                var bytecode = entry.getValue();

                assertTrue(bytecode.length > 0, "Transformed bytecode should not be empty");

                // Parse with ClassFile API to verify validity
                ClassModel cm = ClassFile.of().parse(bytecode);
                assertNotNull(cm, "Should parse transformed bytecode");

                // Verify @Transformed annotation was added
                boolean hasTransformed = hasTransformedAnnotation(cm);
                assertTrue(hasTransformed, "Transformed class should have @Transformed annotation: " + metadata.getName());

                System.out.println("Verified transformation of: " + metadata.getName());
            }
        }
    }

    @Test
    void testTransformSkipsAlreadyTransformedClasses(@TempDir Path tempDir) throws Exception {
        var testClassPath = TRANSFORM_TEST_CLASSES.resolve("MyTest.class");
        var fooPath = TRANSFORM_TEST_CLASSES.resolve("Foo.class");

        if (!Files.exists(testClassPath)) {
            System.out.println("SKIP: Transform module test classes not found at " + testClassPath.toAbsolutePath());
            return;
        }

        var packageDir = tempDir.resolve("com/hellblazer/primeMover/classfile/testClasses");
        Files.createDirectories(packageDir);
        Files.copy(testClassPath, packageDir.resolve("MyTest.class"));
        Files.copy(fooPath, packageDir.resolve("Foo.class"));

        // First transformation
        int firstCount;
        try (var transform = new SimulationTransform(tempDir)) {
            Map<ClassMetadata, byte[]> transformed = transform.transformed();
            firstCount = transformed.size();

            // Write transformed bytecode back
            for (var entry : transformed.entrySet()) {
                var classPath = getClassFilePath(tempDir, entry.getKey().getName());
                Files.write(classPath, entry.getValue());
            }
        }

        // Second transformation should skip already transformed classes
        try (var transform = new SimulationTransform(tempDir)) {
            Map<ClassMetadata, byte[]> transformed = transform.transformed();

            // Should find no classes to transform (all have @Transformed now)
            assertEquals(0, transformed.size(),
                "Should skip already transformed classes (first run transformed " + firstCount + " classes)");
        }
    }

    @Test
    void testTransformWritesBackCorrectly(@TempDir Path tempDir) throws Exception {
        var testClassPath = TRANSFORM_TEST_CLASSES.resolve("MyTest.class");
        var fooPath = TRANSFORM_TEST_CLASSES.resolve("Foo.class");

        if (!Files.exists(testClassPath)) {
            System.out.println("SKIP: Transform module test classes not found at " + testClassPath.toAbsolutePath());
            return;
        }

        var packageDir = tempDir.resolve("com/hellblazer/primeMover/classfile/testClasses");
        Files.createDirectories(packageDir);
        var destFile = packageDir.resolve("MyTest.class");
        Files.copy(testClassPath, destFile);
        Files.copy(fooPath, packageDir.resolve("Foo.class"));

        // Get original file size
        long originalSize = Files.size(destFile);

        // Transform
        try (var transform = new SimulationTransform(tempDir)) {
            Map<ClassMetadata, byte[]> transformed = transform.transformed();

            for (var entry : transformed.entrySet()) {
                var classPath = getClassFilePath(tempDir, entry.getKey().getName());
                Files.write(classPath, entry.getValue());
            }
        }

        // Verify file was updated
        long newSize = Files.size(destFile);
        assertNotEquals(originalSize, newSize, "Transformed file should have different size");

        // Verify the written file is valid
        byte[] written = Files.readAllBytes(destFile);
        ClassModel cm = ClassFile.of().parse(written);

        boolean hasTransformed = hasTransformedAnnotation(cm);
        assertTrue(hasTransformed, "Written file should have @Transformed annotation");
    }

    /**
     * Check if a ClassModel has the @Transformed annotation.
     */
    private boolean hasTransformedAnnotation(ClassModel cm) {
        for (var attr : cm.attributes()) {
            if (attr instanceof RuntimeVisibleAnnotationsAttribute rva) {
                for (var ann : rva.annotations()) {
                    if (ann.className().stringValue().equals(TRANSFORMED_ANNOTATION)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Convert a fully qualified class name to its .class file path.
     */
    private Path getClassFilePath(Path outputDir, String className) {
        var relativePath = className.replace('.', '/') + ".class";
        return outputDir.resolve(relativePath);
    }
}
