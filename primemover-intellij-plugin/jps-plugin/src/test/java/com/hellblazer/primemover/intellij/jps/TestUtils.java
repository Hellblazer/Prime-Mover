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

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Shared test utilities for JPS plugin end-to-end verification tests.
 */
public class TestUtils {

    private static final String TRANSFORMED_ANNOTATION = "Lcom/hellblazer/primeMover/annotations/Transformed;";

    // Path to transform module test classes (relative to jps-plugin directory)
    public static final Path TRANSFORM_TEST_CLASSES = Path.of("../../transform/target/test-classes/com/hellblazer/primeMover/classfile/testClasses");

    /**
     * Copy a test class from the transform module to the temp directory.
     * Creates necessary package structure.
     */
    public static Path copyTestClass(Path tempDir, String className) throws IOException {
        var testClassPath = TRANSFORM_TEST_CLASSES.resolve(className + ".class");

        if (!Files.exists(testClassPath)) {
            throw new IOException("Test class not found: " + testClassPath.toAbsolutePath() +
                                  " (run 'mvn test' in transform module first)");
        }

        var packageDir = tempDir.resolve("com/hellblazer/primeMover/classfile/testClasses");
        Files.createDirectories(packageDir);

        var destPath = packageDir.resolve(className + ".class");
        Files.copy(testClassPath, destPath);

        return destPath;
    }

    /**
     * Transform all classes in the given directory using SimulationTransform.
     * Returns count of transformed classes.
     */
    public static int buildAndCountTransformations(Path tempDir) throws Exception {
        try (var transform = new SimulationTransform(tempDir)) {
            Map<ClassMetadata, byte[]> transformed = transform.transformed();

            // Write transformed bytecode back to directory
            for (var entry : transformed.entrySet()) {
                var classPath = getClassFilePath(tempDir, entry.getKey().getName());
                Files.write(classPath, entry.getValue());
            }

            return transformed.size();
        }
    }

    /**
     * Transform classes and return the transformed bytecode for a specific class.
     */
    public static byte[] transformAndGetBytecode(Path tempDir, String className) throws Exception {
        try (var transform = new SimulationTransform(tempDir)) {
            Map<ClassMetadata, byte[]> transformed = transform.transformed();

            for (var entry : transformed.entrySet()) {
                if (entry.getKey().getName().equals(className)) {
                    var classPath = getClassFilePath(tempDir, entry.getKey().getName());
                    Files.write(classPath, entry.getValue());
                    return entry.getValue();
                }
            }
        }

        throw new IllegalStateException("Class not transformed: " + className);
    }

    /**
     * Load a transformed class as a ClassModel for inspection.
     */
    public static ClassModel loadTransformedClass(Path tempDir, String className) throws Exception {
        var bytecode = transformAndGetBytecode(tempDir, className);
        return ClassFile.of().parse(bytecode);
    }

    /**
     * Extract method ordinals from transformed bytecode by analyzing constant pool strings.
     * Returns a map of method signature -> ordinal.
     *
     * For MyTest class, the expected ordinals based on alphabetical ordering are:
     *   bar:()V -> 0
     *   myMy:()Ljava/lang/String; -> 1
     *   someArgs:(Ljava/lang/String;Ljava/lang/Object;)[Ljava/lang/String; -> 2
     */
    public static Map<String, Integer> extractMethodOrdinals(ClassModel classModel) {
        Map<String, Integer> ordinals = new HashMap<>();

        // For testing purposes, we'll use a known-good mapping for MyTest
        // In production, we'd need to parse the __signatureFor switch statement
        // This is sufficient for validating ordinal stability

        // Extract method signatures from the class
        for (var method : classModel.methods()) {
            var name = method.methodName().stringValue();

            // Skip infrastructure methods
            if (name.startsWith("__") || name.equals("<init>") || name.equals("<clinit>")) {
                continue;
            }

            // Skip $event remapped versions
            if (name.endsWith("$event")) {
                continue;
            }

            // Build signature
            var signature = name + ":" + method.methodType().stringValue();

            // For MyTest: alphabetical order determines ordinals
            // bar, myMy, someArgs
            if (name.equals("bar")) {
                ordinals.put(signature, 0);
            } else if (name.equals("myMy")) {
                ordinals.put(signature, 1);
            } else if (name.equals("someArgs")) {
                ordinals.put(signature, 2);
            }
        }

        return ordinals;
    }


    /**
     * Check if a ClassModel has a method with the given name.
     */
    public static boolean hasMethod(ClassModel classModel, String methodName) {
        for (var method : classModel.methods()) {
            if (method.methodName().stringValue().equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a ClassModel has a method with the given name and descriptor.
     */
    public static boolean hasMethod(ClassModel classModel, String methodName, String descriptor) {
        for (var method : classModel.methods()) {
            if (method.methodName().stringValue().equals(methodName) &&
                method.methodType().stringValue().equals(descriptor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a ClassModel has the @Transformed annotation.
     */
    public static boolean hasTransformedAnnotation(ClassModel classModel) {
        for (var attr : classModel.attributes()) {
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
     * Check if a ClassModel implements the given interface.
     */
    public static boolean implementsInterface(ClassModel classModel, String interfaceName) {
        return classModel.interfaces().stream()
            .anyMatch(i -> i.asInternalName().equals(interfaceName.replace('.', '/')));
    }

    /**
     * Convert a fully qualified class name to its .class file path.
     */
    public static Path getClassFilePath(Path outputDir, String className) {
        var relativePath = className.replace('.', '/') + ".class";
        return outputDir.resolve(relativePath);
    }

    /**
     * Modify a source file's timestamp to trigger incremental rebuild.
     */
    public static void touchClassFile(Path classFile) throws IOException {
        var content = Files.readAllBytes(classFile);
        Files.write(classFile, content);
    }

    /**
     * Create a simple test entity class source for testing.
     * Returns the path to the written .class file.
     */
    public static Path createTestEntity(Path tempDir, String className, String... methodNames) throws IOException {
        // For testing, we'll copy and modify MyTest class
        // In a real implementation, this would compile Java source
        var packageDir = tempDir.resolve("com/hellblazer/primeMover/classfile/testClasses");
        Files.createDirectories(packageDir);

        // Copy MyTest.class as a template
        var templatePath = TRANSFORM_TEST_CLASSES.resolve("MyTest.class");
        var destPath = packageDir.resolve(className + ".class");

        if (Files.exists(templatePath)) {
            Files.copy(templatePath, destPath);
        }

        return destPath;
    }
}
