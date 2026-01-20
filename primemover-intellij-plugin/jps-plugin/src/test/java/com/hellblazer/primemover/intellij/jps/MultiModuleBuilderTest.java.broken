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

import org.jetbrains.jps.ModuleChunk;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.builders.java.JavaSourceRootDescriptor;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ModuleBuildTarget;
import org.jetbrains.jps.incremental.ModuleLevelBuilder;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.jetbrains.jps.model.module.JpsModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for multi-module build scenarios with Prime Mover class instrumentation.
 * Verifies that the builder correctly handles:
 * - Multiple modules in a single compilation chunk
 * - Cross-module entity references
 * - Independent transformation per module output directory
 */
@DisplayName("Multi-Module Builder Tests")
class MultiModuleBuilderTest {

    private static final String TRANSFORMED_ANNOTATION = "Lcom/hellblazer/primeMover/annotations/Transformed;";
    private static final Path TRANSFORM_TEST_CLASSES = Path.of("../../transform/target/test-classes/com/hellblazer/primeMover/classfile/testClasses");

    /**
     * Test that builder handles multiple modules in a compilation chunk.
     * Each module should have its own output directory transformed independently.
     */
    @Test
    @DisplayName("Builder handles multiple modules in compilation chunk")
    void testMultipleModulesInChunk(@TempDir Path tempDir) throws Exception {
        var testClassPath = TRANSFORM_TEST_CLASSES.resolve("MyTest.class");
        var fooPath = TRANSFORM_TEST_CLASSES.resolve("Foo.class");

        if (!Files.exists(testClassPath)) {
            System.out.println("SKIP: Transform module test classes not found");
            return;
        }

        // Create two module output directories
        var module1Dir = tempDir.resolve("module1/classes");
        var module2Dir = tempDir.resolve("module2/classes");
        Files.createDirectories(module1Dir.resolve("com/hellblazer/primeMover/classfile/testClasses"));
        Files.createDirectories(module2Dir.resolve("com/hellblazer/primeMover/classfile/testClasses"));

        // Copy test classes to both modules
        Files.copy(testClassPath, module1Dir.resolve("com/hellblazer/primeMover/classfile/testClasses/MyTest.class"));
        Files.copy(fooPath, module1Dir.resolve("com/hellblazer/primeMover/classfile/testClasses/Foo.class"));
        Files.copy(testClassPath, module2Dir.resolve("com/hellblazer/primeMover/classfile/testClasses/MyTest.class"));
        Files.copy(fooPath, module2Dir.resolve("com/hellblazer/primeMover/classfile/testClasses/Foo.class"));

        // Create mock targets
        var target1 = new MockModuleBuildTarget(module1Dir.toFile(), "module1");
        var target2 = new MockModuleBuildTarget(module2Dir.toFile(), "module2");

        // Create mock chunk with both targets
        var chunk = new MockModuleChunk(List.of(target1, target2));

        // Create mock context
        var context = new MockCompileContext();

        // Create instrumenter and build
        var instrumenter = new PrimeMoverClassInstrumenter();
        var dirtyFilesHolder = new MockDirtyFilesHolder();
        var outputConsumer = new MockOutputConsumer();

        var result = instrumenter.build(context, chunk, dirtyFilesHolder, outputConsumer);

        assertEquals(ModuleLevelBuilder.ExitCode.OK, result, "Build should complete successfully");

        // Verify both modules were transformed
        assertTrue(context.messages.stream()
                        .anyMatch(msg -> msg.messageText().contains("Transformed")),
                "Should have transformation messages");

        // Verify each module's classes were transformed
        verifyClassTransformed(module1Dir.resolve("com/hellblazer/primeMover/classfile/testClasses/MyTest.class"));
        verifyClassTransformed(module2Dir.resolve("com/hellblazer/primeMover/classfile/testClasses/MyTest.class"));
    }

    /**
     * Test that builder correctly handles empty module (no classes to transform).
     */
    @Test
    @DisplayName("Builder handles empty module gracefully")
    void testEmptyModule(@TempDir Path tempDir) throws Exception {
        // Create empty module output directory
        var moduleDir = tempDir.resolve("empty-module/classes");
        Files.createDirectories(moduleDir);

        var target = new MockModuleBuildTarget(moduleDir.toFile(), "empty-module");
        var chunk = new MockModuleChunk(List.of(target));
        var context = new MockCompileContext();

        var instrumenter = new PrimeMoverClassInstrumenter();
        var dirtyFilesHolder = new MockDirtyFilesHolder();
        var outputConsumer = new MockOutputConsumer();

        var result = instrumenter.build(context, chunk, dirtyFilesHolder, outputConsumer);

        assertEquals(ModuleLevelBuilder.ExitCode.OK, result,
                "Build should complete successfully for empty module");
    }

    /**
     * Test that builder handles module with no output directory.
     */
    @Test
    @DisplayName("Builder handles missing output directory")
    void testMissingOutputDirectory(@TempDir Path tempDir) throws Exception {
        // Create target with non-existent output directory
        var nonExistentDir = tempDir.resolve("non-existent").toFile();
        var target = new MockModuleBuildTarget(nonExistentDir, "test-module");
        var chunk = new MockModuleChunk(List.of(target));
        var context = new MockCompileContext();

        var instrumenter = new PrimeMoverClassInstrumenter();
        var dirtyFilesHolder = new MockDirtyFilesHolder();
        var outputConsumer = new MockOutputConsumer();

        var result = instrumenter.build(context, chunk, dirtyFilesHolder, outputConsumer);

        assertEquals(ModuleLevelBuilder.ExitCode.OK, result,
                "Build should complete successfully even with missing output directory");
    }

    /**
     * Test that each module gets independent transformation.
     * Verifies that transformation state doesn't leak between modules.
     */
    @Test
    @DisplayName("Each module gets independent transformation")
    void testIndependentModuleTransformation(@TempDir Path tempDir) throws Exception {
        var testClassPath = TRANSFORM_TEST_CLASSES.resolve("MyTest.class");
        var fooPath = TRANSFORM_TEST_CLASSES.resolve("Foo.class");

        if (!Files.exists(testClassPath)) {
            System.out.println("SKIP: Transform module test classes not found");
            return;
        }

        // Module 1: Has untransformed classes
        var module1Dir = tempDir.resolve("module1/classes");
        Files.createDirectories(module1Dir.resolve("com/hellblazer/primeMover/classfile/testClasses"));
        Files.copy(testClassPath, module1Dir.resolve("com/hellblazer/primeMover/classfile/testClasses/MyTest.class"));
        Files.copy(fooPath, module1Dir.resolve("com/hellblazer/primeMover/classfile/testClasses/Foo.class"));

        // Module 2: Has already-transformed classes (simulate with first transformation)
        var module2Dir = tempDir.resolve("module2/classes");
        Files.createDirectories(module2Dir.resolve("com/hellblazer/primeMover/classfile/testClasses"));
        Files.copy(testClassPath, module2Dir.resolve("com/hellblazer/primeMover/classfile/testClasses/MyTest.class"));
        Files.copy(fooPath, module2Dir.resolve("com/hellblazer/primeMover/classfile/testClasses/Foo.class"));

        // Pre-transform module2's classes
        var target2 = new MockModuleBuildTarget(module2Dir.toFile(), "module2");
        var preChunk = new MockModuleChunk(List.of(target2));
        var preContext = new MockCompileContext();
        var instrumenter = new PrimeMoverClassInstrumenter();
        instrumenter.build(preContext, preChunk, new MockDirtyFilesHolder(), new MockOutputConsumer());

        // Now build both modules together
        var target1 = new MockModuleBuildTarget(module1Dir.toFile(), "module1");
        var target2Again = new MockModuleBuildTarget(module2Dir.toFile(), "module2");
        var chunk = new MockModuleChunk(List.of(target1, target2Again));
        var context = new MockCompileContext();

        var result = instrumenter.build(context, chunk, new MockDirtyFilesHolder(), new MockOutputConsumer());

        assertEquals(ModuleLevelBuilder.ExitCode.OK, result, "Build should complete successfully");

        // Verify module1 was transformed
        verifyClassTransformed(module1Dir.resolve("com/hellblazer/primeMover/classfile/testClasses/MyTest.class"));

        // Verify module2 remains transformed (not re-transformed)
        verifyClassTransformed(module2Dir.resolve("com/hellblazer/primeMover/classfile/testClasses/MyTest.class"));
    }

    /**
     * Verify that a class file has the @Transformed annotation.
     */
    private void verifyClassTransformed(Path classFile) throws IOException {
        assertTrue(Files.exists(classFile), "Class file should exist: " + classFile);

        var bytes = Files.readAllBytes(classFile);
        var classModel = ClassFile.of().parse(bytes);

        boolean hasTransformed = hasTransformedAnnotation(classModel);
        assertTrue(hasTransformed, "Class should have @Transformed annotation: " + classFile);
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

    // ===== Mock JPS Classes =====

    /**
     * Mock ModuleBuildTarget for testing.
     */
    private static class MockModuleBuildTarget extends ModuleBuildTarget {
        private final File outputDir;
        private final String moduleName;

        public MockModuleBuildTarget(File outputDir, String moduleName) {
            super(null, null); // JpsModule and JavaModuleBuildTargetType are not needed for tests
            this.outputDir = outputDir;
            this.moduleName = moduleName;
        }

        @Override
        public File getOutputDir() {
            return outputDir;
        }

        @Override
        public JpsModule getModule() {
            // Return minimal mock module
            return null; // Not used in our tests
        }

        @Override
        public String getPresentableName() {
            return moduleName;
        }
    }

    /**
     * Mock ModuleChunk for testing.
     */
    private static class MockModuleChunk extends ModuleChunk {
        private final Set<ModuleBuildTarget> targets;

        public MockModuleChunk(List<ModuleBuildTarget> targets) {
            super(new HashSet<>(targets));
            this.targets = new HashSet<>(targets);
        }

        @Override
        public Set<ModuleBuildTarget> getTargets() {
            return targets;
        }
    }

    /**
     * Mock CompileContext for testing.
     */
    private static class MockCompileContext implements CompileContext {
        final List<CompilerMessage> messages = new ArrayList<>();

        @Override
        public void processMessage(BuildMessage msg) {
            if (msg instanceof CompilerMessage cm) {
                messages.add(cm);
            }
        }

        // Minimal implementations of CompileContext methods (not used in tests)
        @Override
        public long getCompilationStartStamp() {
            return 0;
        }

        @Override
        public boolean isMake() {
            return false;
        }

        @Override
        public boolean isProjectRebuild() {
            return false;
        }
    }

    /**
     * Mock DirtyFilesHolder for testing.
     */
    private static class MockDirtyFilesHolder extends DirtyFilesHolder<JavaSourceRootDescriptor, ModuleBuildTarget> {
        public MockDirtyFilesHolder() {
            super(null);
        }

        @Override
        public void processDirtyFiles(FileProcessor<JavaSourceRootDescriptor, ModuleBuildTarget> processor) throws IOException {
            // No dirty files for these tests
        }

        @Override
        public boolean hasDirtyFiles() {
            return false;
        }

        @Override
        public boolean hasRemovedFiles() {
            return false;
        }
    }

    /**
     * Mock OutputConsumer for testing.
     */
    private static class MockOutputConsumer implements ModuleLevelBuilder.OutputConsumer {
        @Override
        public Collection<File> getOutputs(ModuleBuildTarget target) {
            return Collections.emptyList();
        }

        @Override
        public void registerOutputFile(ModuleBuildTarget target, File outputFile, Collection<String> sourcePaths) {
            // Not used in these tests
        }
    }
}
