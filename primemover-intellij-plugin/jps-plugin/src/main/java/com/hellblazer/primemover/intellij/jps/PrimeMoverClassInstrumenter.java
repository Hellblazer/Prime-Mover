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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.ModuleChunk;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.builders.java.JavaSourceRootDescriptor;
import org.jetbrains.jps.incremental.BuilderCategory;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ModuleBuildTarget;
import org.jetbrains.jps.incremental.ModuleLevelBuilder;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JPS ModuleLevelBuilder that transforms Prime Mover @Entity classes
 * using the Java ClassFile API.
 * <p>
 * This builder runs in the CLASS_INSTRUMENTER category, which executes
 * after Java compilation but before packaging.
 * <p>
 * The transformation:
 * 1. Scans compiled classes for @Entity annotation
 * 2. Skips classes already marked with @Transformed
 * 3. Uses EntityGenerator to transform entity classes
 * 4. Remaps Kronos API calls to Kairos runtime
 * 5. Writes transformed bytecode back to output directory
 */
public class PrimeMoverClassInstrumenter extends ModuleLevelBuilder {

    private static final String BUILDER_NAME = "Prime Mover Instrumenter";

    public PrimeMoverClassInstrumenter() {
        super(BuilderCategory.CLASS_INSTRUMENTER);
    }

    @Override
    public @NotNull String getPresentableName() {
        return BUILDER_NAME;
    }

    @Override
    public @NotNull List<String> getCompilableFileExtensions() {
        // CLASS_INSTRUMENTER doesn't compile source files, just transforms .class files
        return Collections.emptyList();
    }

    @Override
    public ExitCode build(
            @NotNull CompileContext context,
            @NotNull ModuleChunk chunk,
            @NotNull DirtyFilesHolder<JavaSourceRootDescriptor, ModuleBuildTarget> dirtyFilesHolder,
            @NotNull OutputConsumer outputConsumer
    ) throws IOException {

        var totalTransformed = 0;
        var totalSkipped = 0;

        for (var target : chunk.getTargets()) {
            var outputDir = target.getOutputDir();
            if (outputDir == null || !outputDir.exists()) {
                continue;
            }

            var result = transformOutputDirectory(outputDir, context);
            totalTransformed += result.transformed;
            totalSkipped += result.skipped;
        }

        if (totalTransformed > 0 || totalSkipped > 0) {
            context.processMessage(new CompilerMessage(
                    BUILDER_NAME,
                    BuildMessage.Kind.INFO,
                    "Prime Mover: " + totalTransformed + " classes transformed, " + totalSkipped + " skipped (already transformed)"
            ));
        }

        return ExitCode.OK;
    }

    /**
     * Transform all entity classes in the given output directory.
     */
    private TransformResult transformOutputDirectory(File outputDir, CompileContext context) {
        var outputPath = outputDir.toPath();
        var transformed = 0;
        var skipped = 0;

        try (var simulationTransform = new SimulationTransform(outputPath)) {
            // Get all classes that need transformation (excludes @Transformed)
            Map<ClassMetadata, byte[]> transformedClasses = simulationTransform.transformed();

            if (transformedClasses.isEmpty()) {
                return new TransformResult(0, 0);
            }

            for (var entry : transformedClasses.entrySet()) {
                var classMetadata = entry.getKey();
                var transformedBytes = entry.getValue();

                // Compute output path for this class
                var classFilePath = getClassFilePath(outputPath, classMetadata.getName());

                try {
                    // Write transformed bytecode
                    Files.write(classFilePath, transformedBytes);
                    transformed++;

                    context.processMessage(new CompilerMessage(
                            BUILDER_NAME,
                            BuildMessage.Kind.INFO,
                            "Transformed: " + classMetadata.getName()
                    ));
                } catch (IOException e) {
                    context.processMessage(new CompilerMessage(
                            BUILDER_NAME,
                            BuildMessage.Kind.ERROR,
                            "Failed to write transformed class " + classMetadata.getName() + ": " + e.getMessage()
                    ));
                }
            }

        } catch (IOException e) {
            context.processMessage(new CompilerMessage(
                    BUILDER_NAME,
                    BuildMessage.Kind.WARNING,
                    "Failed to scan output directory " + outputDir + ": " + e.getMessage()
            ));
        } catch (Exception e) {
            context.processMessage(new CompilerMessage(
                    BUILDER_NAME,
                    BuildMessage.Kind.ERROR,
                    "Transformation failed: " + e.getClass().getSimpleName() + ": " + e.getMessage()
            ));
        }

        return new TransformResult(transformed, skipped);
    }

    /**
     * Convert a fully qualified class name to its .class file path.
     */
    private Path getClassFilePath(Path outputDir, String className) {
        var relativePath = className.replace('.', File.separatorChar) + ".class";
        return outputDir.resolve(relativePath);
    }

    /**
     * Result of transforming a directory.
     */
    private record TransformResult(int transformed, int skipped) {}
}
