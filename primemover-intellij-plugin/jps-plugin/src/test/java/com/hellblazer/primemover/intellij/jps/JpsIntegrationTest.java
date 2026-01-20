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

import org.jetbrains.jps.builders.java.JavaSourceRootDescriptor;
import org.jetbrains.jps.incremental.BuilderCategory;
import org.jetbrains.jps.incremental.BuilderService;
import org.jetbrains.jps.incremental.ModuleBuildTarget;
import org.jetbrains.jps.incremental.ModuleLevelBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Prime Mover JPS builder registration and configuration.
 * Tests that the builder properly integrates with JPS build pipeline through SPI.
 */
@DisplayName("JPS Integration Tests")
class JpsIntegrationTest {

    /**
     * Verify that PrimeMoverBuilderService is discoverable through Java's ServiceLoader mechanism.
     * JPS uses ServiceLoader to discover BuilderService implementations at runtime.
     */
    @Test
    @DisplayName("BuilderService discovers PrimeMoverBuilderService via SPI")
    void testBuilderServiceDiscovery() {
        // Use ServiceLoader to discover BuilderService implementations
        ServiceLoader<BuilderService> loader = ServiceLoader.load(
                BuilderService.class,
                getClass().getClassLoader()
        );

        // Collect all discovered services
        List<BuilderService> services = new ArrayList<>();
        loader.forEach(services::add);

        // Should find at least one service
        assertFalse(services.isEmpty(), "ServiceLoader should discover at least one BuilderService");

        // Should find PrimeMoverBuilderService specifically
        boolean foundPrimeMoverService = services.stream()
                .anyMatch(service -> service instanceof PrimeMoverBuilderService);

        assertTrue(foundPrimeMoverService,
                "ServiceLoader should discover PrimeMoverBuilderService. Found services: " +
                        services.stream().map(s -> s.getClass().getName()).toList());
    }

    /**
     * Verify that PrimeMoverBuilderService creates the correct ModuleLevelBuilder.
     */
    @Test
    @DisplayName("BuilderService creates PrimeMoverClassInstrumenter")
    void testBuilderServiceCreatesInstrumenter() {
        var service = new PrimeMoverBuilderService();
        var builders = service.createModuleLevelBuilders();

        assertNotNull(builders, "Builder service should return non-null builder list");
        assertEquals(1, builders.size(), "Builder service should return exactly one builder");

        var builder = builders.getFirst();
        assertInstanceOf(PrimeMoverClassInstrumenter.class, builder,
                "Builder should be instance of PrimeMoverClassInstrumenter");
    }

    /**
     * Verify that the builder is categorized as CLASS_INSTRUMENTER.
     * This ensures it runs after Java compilation but before packaging.
     */
    @Test
    @DisplayName("Builder has correct category for JPS pipeline")
    void testBuilderCategory() {
        var instrumenter = new PrimeMoverClassInstrumenter();

        assertEquals(BuilderCategory.CLASS_INSTRUMENTER, instrumenter.getCategory(),
                "Prime Mover builder should be CLASS_INSTRUMENTER to run after compilation");
    }

    /**
     * Verify that the builder provides a human-readable name for logging and UI.
     */
    @Test
    @DisplayName("Builder has presentable name")
    void testBuilderPresentableName() {
        var instrumenter = new PrimeMoverClassInstrumenter();
        var name = instrumenter.getPresentableName();

        assertNotNull(name, "Builder should have a presentable name");
        assertFalse(name.isBlank(), "Builder name should not be blank");
        assertTrue(name.contains("Prime Mover"), "Builder name should mention Prime Mover");
    }

    /**
     * Verify that the builder returns empty list for compilable file extensions.
     * CLASS_INSTRUMENTER builders work on compiled .class files, not source files.
     */
    @Test
    @DisplayName("Builder does not compile source files")
    void testBuilderCompilesNoSourceExtensions() {
        var instrumenter = new PrimeMoverClassInstrumenter();
        var extensions = instrumenter.getCompilableFileExtensions();

        assertNotNull(extensions, "Compilable extensions list should not be null");
        assertTrue(extensions.isEmpty(),
                "CLASS_INSTRUMENTER should not compile source files (returns empty extensions list)");
    }

    /**
     * Verify that the builder implements the correct build method signature.
     * This ensures it conforms to the ModuleLevelBuilder contract.
     */
    @Test
    @DisplayName("Build method has correct JPS contract")
    void testBuildMethodContract() throws NoSuchMethodException {
        // Verify build method exists with correct signature
        var buildMethod = PrimeMoverClassInstrumenter.class.getDeclaredMethod(
                "build",
                org.jetbrains.jps.incremental.CompileContext.class,
                org.jetbrains.jps.ModuleChunk.class,
                org.jetbrains.jps.builders.DirtyFilesHolder.class,
                ModuleLevelBuilder.OutputConsumer.class
        );

        assertNotNull(buildMethod, "Builder should have build() method with JPS signature");

        // Verify return type
        assertEquals(ModuleLevelBuilder.ExitCode.class, buildMethod.getReturnType(),
                "Build method should return ExitCode");
    }

    /**
     * Verify builder category ordering relative to other builder types.
     * CLASS_INSTRUMENTER should run after source processors and compilers.
     */
    @Test
    @DisplayName("Builder ordering is correct for post-compilation transformation")
    void testBuilderCategoryOrdering() {
        var instrumenterCategory = BuilderCategory.CLASS_INSTRUMENTER;

        // CLASS_INSTRUMENTER runs after these categories
        assertTrue(BuilderCategory.SOURCE_GENERATOR.ordinal() < instrumenterCategory.ordinal(),
                "CLASS_INSTRUMENTER should run after SOURCE_GENERATOR");

        assertTrue(BuilderCategory.SOURCE_PROCESSOR.ordinal() < instrumenterCategory.ordinal(),
                "CLASS_INSTRUMENTER should run after SOURCE_PROCESSOR (annotation processors)");

        // CLASS_INSTRUMENTER runs before these categories
        assertTrue(instrumenterCategory.ordinal() < BuilderCategory.CLASS_POST_PROCESSOR.ordinal(),
                "CLASS_INSTRUMENTER should run before CLASS_POST_PROCESSOR");
    }

    /**
     * Verify that META-INF/services file is properly configured.
     * This is a compile-time verification that the SPI registration file exists.
     */
    @Test
    @DisplayName("META-INF/services file contains correct service class")
    void testMetaInfServicesFile() throws Exception {
        var serviceName = "org.jetbrains.jps.incremental.BuilderService";
        var resourcePath = "META-INF/services/" + serviceName;

        try (var stream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            assertNotNull(stream,
                    "META-INF/services/org.jetbrains.jps.incremental.BuilderService should exist");

            var content = new String(stream.readAllBytes()).trim();
            assertEquals("com.hellblazer.primemover.intellij.jps.PrimeMoverBuilderService", content,
                    "Service file should contain PrimeMoverBuilderService class name");
        }
    }

    /**
     * Verify that builder provides correct categorization for JPS lifecycle.
     */
    @Test
    @DisplayName("Builder integrates with JPS build lifecycle")
    void testJpsBuildLifecycleIntegration() {
        var service = new PrimeMoverBuilderService();
        var builders = service.createModuleLevelBuilders();

        for (var builder : builders) {
            // Verify builder is properly typed
            assertInstanceOf(ModuleLevelBuilder.class, builder,
                    "Builder should implement ModuleLevelBuilder");

            // Verify category is set (not null)
            assertNotNull(builder.getCategory(),
                    "Builder category should be set for JPS pipeline integration");

            // Verify it's the correct category
            assertEquals(BuilderCategory.CLASS_INSTRUMENTER, builder.getCategory(),
                    "Builder should be CLASS_INSTRUMENTER for post-compilation transformation");
        }
    }
}
