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
package com.hellblazer.primeMover.agent;

import com.hellblazer.primeMover.api.Kronos;
import com.hellblazer.primeMover.classfile.ClassRemapper;
import com.hellblazer.primeMover.classfile.SimulationTransform;
import com.hellblazer.primeMover.runtime.Kairos;
import io.github.classgraph.ClassGraph;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.PoolEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.ClassDesc;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Java agent transformer that performs runtime transformation of simulation classes.
 * <p>
 * Handles two types of transformations:
 * <ul>
 *   <li>Entity classes (@Entity annotated) - full transformation via EntityGenerator</li>
 *   <li>Classes using Kronos API - simple remapping of Kronos references to Kairos</li>
 * </ul>
 * <p>
 * Thread-safe for use with multiple class loaders.
 *
 * @author hal.hildebrand
 */
public class SimulationTransformerClassFileAPI implements ClassFileTransformer {
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(
            SimulationTransformerClassFileAPI.class.getName());

    private static final ClassFile CLASS_FILE = ClassFile.of();

    // Internal name format for Kronos (used in constant pool)
    private static final String KRONOS_INTERNAL_NAME = Kronos.class.getName().replace('.', '/');

    // ClassRemapper for Kronos -> Kairos transformation
    private static final ClassRemapper API_REMAPPER = new ClassRemapper(classDesc -> {
        var pkg = classDesc.packageName();
        var className = pkg.isEmpty() ? classDesc.displayName() : pkg + "." + classDesc.displayName();
        if (className.equals(Kronos.class.getCanonicalName())) {
            return ClassDesc.of(Kairos.class.getCanonicalName());
        }
        return classDesc;
    });

    // Cache of transformed entity classes to avoid re-transformation
    private final Map<String, byte[]> entityCache = new ConcurrentHashMap<>();

    // Packages to skip (framework internals, etc.)
    private static final String[] SKIP_PACKAGES = {
            "com/hellblazer/primeMover/runtime/",
            "com/hellblazer/primeMover/api/",
            "com/hellblazer/primeMover/annotations/",
            "com/hellblazer/primeMover/classfile/",
            "com/hellblazer/primeMover/agent/",
            "java/",
            "javax/",
            "jdk/",
            "sun/",
            "com/sun/",
            "io/github/classgraph/"
    };

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        // Skip null loader (bootstrap classes)
        if (loader == null || className == null) {
            return null;
        }

        // Skip framework internals and JDK classes
        if (shouldSkip(className)) {
            return null;
        }

        try {
            return doTransform(loader, className, classfileBuffer);
        } catch (Throwable t) {
            log.warning("Failed to transform " + className + ": " + t.getMessage());
            return null;
        }
    }

    private byte[] doTransform(ClassLoader loader, String className, byte[] classfileBuffer) {
        var classModel = CLASS_FILE.parse(classfileBuffer);
        var scanner = new AnnotationScanner();
        scanner.scan(classModel);

        // Skip already transformed classes
        if (scanner.isPreviouslyTransformed()) {
            log.fine("Skipping previously transformed: " + className);
            return null;
        }

        // Check if class references Kronos in constant pool
        boolean referencesKronos = referencesKronos(classModel);

        if (scanner.isTransform()) {
            // Entity class - needs full transformation
            return transformEntity(loader, className, classfileBuffer);
        } else if (referencesKronos) {
            // Non-entity class that uses Kronos API - just remap
            return remapKronosReferences(classModel, classfileBuffer);
        }

        // No transformation needed
        return null;
    }

    /**
     * Check if the class references Kronos in its constant pool
     */
    private boolean referencesKronos(java.lang.classfile.ClassModel classModel) {
        for (PoolEntry entry : classModel.constantPool()) {
            if (entry instanceof ClassEntry ce) {
                if (ce.asInternalName().equals(KRONOS_INTERNAL_NAME)) {
                    return true;
                }
            } else if (entry instanceof Utf8Entry ue) {
                // Check for Kronos in method/field descriptors
                if (ue.stringValue().contains(KRONOS_INTERNAL_NAME)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Transform an entity class using ClassGraph and EntityGenerator
     */
    private byte[] transformEntity(ClassLoader loader, String className, byte[] originalBytes) {
        // Convert internal name to canonical name
        var canonicalName = className.replace('/', '.');

        // Check cache first
        var cached = entityCache.get(canonicalName);
        if (cached != null) {
            log.fine("Using cached transformation for: " + canonicalName);
            return cached;
        }

        log.info("Transforming entity: " + canonicalName);

        // Create a ClassGraph that scans only the necessary class
        var graph = new ClassGraph()
                .addClassLoader(loader)
                .acceptClasses(canonicalName)
                .enableAllInfo()
                .enableInterClassDependencies()
                .enableExternalClasses()
                .ignoreMethodVisibility();

        try (var txfm = new SimulationTransform(graph)) {
            var generator = txfm.generatorOf(canonicalName);

            // Generate transformed bytecode
            var generatedBytes = generator.generate();

            // Apply Kronos -> Kairos remapping
            var generatedModel = CLASS_FILE.parse(generatedBytes);
            var remappedBytes = CLASS_FILE.build(
                    generatedModel.thisClass().asSymbol(),
                    classBuilder -> classBuilder.transform(generatedModel, API_REMAPPER)
            );

            // Cache the result
            entityCache.put(canonicalName, remappedBytes);

            log.info("Successfully transformed entity: " + canonicalName);
            return remappedBytes;
        } catch (IOException e) {
            log.severe("Failed to transform entity " + canonicalName + ": " + e.getMessage());
            throw new RuntimeException("Failed to transform entity: " + canonicalName, e);
        } catch (IllegalArgumentException e) {
            // Entity not found - might not be a valid entity after all
            log.warning("Entity not found for transformation: " + canonicalName + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Remap Kronos references to Kairos for non-entity classes
     */
    private byte[] remapKronosReferences(java.lang.classfile.ClassModel classModel, byte[] originalBytes) {
        var remappedBytes = CLASS_FILE.build(
                classModel.thisClass().asSymbol(),
                classBuilder -> classBuilder.transform(classModel, API_REMAPPER)
        );

        // Only return if actually changed
        if (Arrays.equals(originalBytes, remappedBytes)) {
            return null;
        }

        log.fine("Remapped Kronos references in: " + classModel.thisClass().asInternalName());
        return remappedBytes;
    }

    /**
     * Check if class should be skipped (framework internals, JDK, etc.)
     */
    private boolean shouldSkip(String className) {
        for (var prefix : SKIP_PACKAGES) {
            if (className.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
