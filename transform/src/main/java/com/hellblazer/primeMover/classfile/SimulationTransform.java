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

import com.hellblazer.primeMover.annotations.*;
import com.hellblazer.primeMover.api.Kronos;
import com.hellblazer.primeMover.classfile.OpenAddressingSet.OpenSet;
import com.hellblazer.primeMover.runtime.Kairos;

import java.io.Closeable;
import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.constant.ClassDesc;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * ClassFile API implementation of SimulationTransform that provides entity transformation using Java 25 ClassFile API
 * (JEP 484) without external dependencies like ClassGraph.
 * <p>
 * This class scans classpath entries and transforms entity classes for Prime Mover simulation.
 *
 * @author hal.hildebrand
 */
public class SimulationTransform implements Closeable {

    /** Filter that excludes already transformed classes */
    public static final Predicate<ClassMetadata> EXCLUDE_TRANSFORMED_FILTER =
        cm -> !cm.hasAnnotation(Transformed.class);

    /** Filter that accepts all classes */
    public static final Predicate<ClassMetadata> ACCEPT_ALL_FILTER = cm -> true;

    /** Marker annotation that indicates all public methods should be treated as events */
    private static final String ALL_METHODS_MARKER = AllMethodsMarker.class.getCanonicalName();

    /** Cached ClassFile instance for bytecode transformations */
    private static final ClassFile CLASS_FILE = ClassFile.of();

    private static final Logger log = Logger.getLogger(SimulationTransform.class.getName());

    private final ClassScanner scanner;
    private final ClassRemapper apiRemapper;
    private final AtomicReference<String> transformTimestamp = new AtomicReference<>();

    /**
     * Creates a new SimulationTransform that will scan the given classpath entry.
     *
     * @param classpathEntry Path to directory or JAR file to scan
     */
    public SimulationTransform(Path classpathEntry) throws IOException {
        this.scanner = new ClassScanner().addClasspathEntry(classpathEntry).scan();
        this.apiRemapper = createApiRemapper();
        this.transformTimestamp.set(java.time.Instant.now().toString());
    }

    /**
     * Creates a new SimulationTransform with a pre-configured scanner.
     *
     * @param scanner Pre-configured and scanned ClassScanner
     */
    public SimulationTransform(ClassScanner scanner) {
        this.scanner = scanner;
        this.apiRemapper = createApiRemapper();
        this.transformTimestamp.set(java.time.Instant.now().toString());
    }

    private static ClassRemapper createApiRemapper() {
        return new ClassRemapper(classDesc -> {
            var pkg = classDesc.packageName();
            var className = pkg.isEmpty() ? classDesc.displayName() : pkg + "." + classDesc.displayName();
            if (className.equals(Kronos.class.getCanonicalName())) {
                return ClassDesc.of(Kairos.class.getCanonicalName());
            }
            return classDesc;
        });
    }

    /**
     * Extracts entity interfaces from the @Entity annotation value.
     * If no explicit value is provided in the annotation, returns AllMethodsMarker
     * (which is the default value for the @Entity annotation).
     *
     * @param entityClass The entity class to analyze
     * @return Set of interface names marked as entity interfaces
     */
    public static Set<String> getEntityInterfaceNames(ClassMetadata entityClass) {
        var entityAnnotation = entityClass.getAnnotation(Entity.class);
        if (entityAnnotation == null) {
            return Set.of();
        }

        var classValues = entityAnnotation.getClassValues();
        // When @Entity has no explicit value, the default is AllMethodsMarker.class
        // The bytecode doesn't store default values, so an empty list means use default
        if (classValues.isEmpty()) {
            return Set.of(ALL_METHODS_MARKER);
        }
        return new HashSet<>(classValues);
    }

    /**
     * Get entity interfaces resolved to ClassMetadata where available.
     */
    public Set<ClassMetadata> getEntityInterfaces(ClassMetadata entityClass) {
        var interfaceNames = getEntityInterfaceNames(entityClass);
        var result = new OpenSet<ClassMetadata>();

        for (var name : interfaceNames) {
            var cm = scanner.getClass(name);
            if (cm != null) {
                result.add(cm);
            }
        }

        // Also check superclass @Entity annotations
        for (var superClass : entityClass.getSuperclasses()) {
            if (superClass.hasAnnotation(Entity.class)) {
                var superInterfaceNames = getEntityInterfaceNames(superClass);
                for (var name : superInterfaceNames) {
                    var cm = scanner.getClass(name);
                    if (cm != null) {
                        result.add(cm);
                    }
                }
            }
        }

        return result;
    }

    @Override
    public void close() throws IOException {
        if (scanner != null) {
            scanner.close();
        }
    }

    /**
     * Creates an EntityGenerator for the specified class name.
     *
     * @param classname Fully qualified class name of the entity
     * @return EntityGenerator instance
     * @throws IllegalArgumentException if class not found
     */
    public EntityGenerator generatorOf(String classname) {
        return generatorOf(classname, ACCEPT_ALL_FILTER);
    }

    /**
     * Creates an EntityGenerator for the specified class name with filtering.
     *
     * @param classname Fully qualified class name of the entity
     * @param selector  Filter to apply to entity classes
     * @return EntityGenerator instance
     */
    public EntityGenerator generatorOf(String classname, Predicate<ClassMetadata> selector) {
        Objects.requireNonNull(classname, "classname cannot be null");
        if (classname.trim().isEmpty()) {
            throw new IllegalArgumentException("classname cannot be empty");
        }

        var entities = findEntityClasses(selector);
        var entity = entities.stream()
                             .filter(cm -> cm.getName().equals(classname))
                             .findFirst()
                             .orElseThrow(() -> new IllegalArgumentException(
                                 "Entity class not found: " + classname +
                                 ". Available entities: " + entities.stream()
                                                                    .map(ClassMetadata::getName)
                                                                    .collect(Collectors.toSet())));
        return createEntityGenerator(entity);
    }

    /**
     * Creates EntityGenerator instances for all entity classes.
     *
     * @return Map of ClassMetadata to EntityGenerator for all entities
     */
    public Map<ClassMetadata, EntityGenerator> generators() {
        return generators(ACCEPT_ALL_FILTER);
    }

    /**
     * Creates EntityGenerator instances for filtered entity classes.
     *
     * @param selector Filter to apply to entity classes
     * @return Map of ClassMetadata to EntityGenerator for filtered entities
     */
    public Map<ClassMetadata, EntityGenerator> generators(Predicate<ClassMetadata> selector) {
        var entities = findEntityClasses(selector);
        return generators(entities);
    }

    /**
     * Creates EntityGenerator instances for the provided entity class list.
     *
     * @param entities List of entity ClassMetadata objects
     * @return Map of ClassMetadata to EntityGenerator
     */
    public Map<ClassMetadata, EntityGenerator> generators(List<ClassMetadata> entities) {
        return entities.stream()
                       .filter(EXCLUDE_TRANSFORMED_FILTER)
                       .collect(Collectors.toMap(cm -> cm, this::createEntityGenerator));
    }

    /**
     * Gets the current transform timestamp.
     */
    public String getTransformTimestamp() {
        return transformTimestamp.get();
    }

    /**
     * Sets the timestamp to be used in @Transformed annotations.
     */
    public void setTransformTimestamp(String timestamp) {
        this.transformTimestamp.set(timestamp);
    }

    /**
     * Generates transformed bytecode for all classes requiring transformation.
     *
     * @return Map of ClassMetadata to transformed bytecode
     */
    public Map<ClassMetadata, byte[]> transformed() {
        return transformed(ACCEPT_ALL_FILTER);
    }

    /**
     * Generates transformed bytecode for filtered classes requiring transformation.
     *
     * @param selector Filter to apply to classes
     * @return Map of ClassMetadata to transformed bytecode
     */
    public Map<ClassMetadata, byte[]> transformed(Predicate<ClassMetadata> selector) {
        var entities = findEntityClasses(selector);
        var transformed = new ConcurrentHashMap<ClassMetadata, byte[]>();

        // Transform non-entity classes that depend on Kronos (parallel)
        transformDependentClasses(selector, entities, transformed);

        // Transform entity classes (parallel)
        transformEntityClasses(entities, transformed);

        return transformed;
    }

    /**
     * Get the underlying scanner for direct class access
     */
    public ClassScanner getScanner() {
        return scanner;
    }

    /**
     * Collects all event methods for an entity class.
     */
    private Set<MethodMetadata> collectEventMethods(ClassMetadata entityClass, Set<ClassMetadata> implementedInterfaces,
                                                    boolean hasAllMethodsMarker) {
        var eventMethods = new OpenSet<MethodMetadata>();

        // Add methods from implemented entity interfaces
        for (var intf : implementedInterfaces) {
            for (var interfaceMethod : intf.getMethods()) {
                var classMethod = findMatchingMethod(entityClass, interfaceMethod);
                if (classMethod != null && !classMethod.hasAnnotation(NonEvent.class)) {
                    eventMethods.add(classMethod);
                }
            }
        }

        // Add methods from the class itself
        for (var method : entityClass.getDeclaredMethods()) {
            if (!method.isStatic() && !method.hasAnnotation(NonEvent.class)) {
                if (shouldIncludeMethod(method, hasAllMethodsMarker)) {
                    eventMethods.add(method);
                }
            }
        }

        return eventMethods;
    }

    /**
     * Creates an EntityGenerator for the given entity class.
     */
    private EntityGenerator createEntityGenerator(ClassMetadata entityClass) {
        var entityInterfaceNames = getEntityInterfaceNames(entityClass);
        var hasAllMethodsMarker = entityInterfaceNames.contains(ALL_METHODS_MARKER);

        // Get resolved entity interfaces (excluding AllMethodsMarker which isn't a real interface)
        var entityInterfaces = getEntityInterfaces(entityClass);

        // Find implemented entity interfaces
        var implementedInterfaces = findImplementedEntityInterfaces(entityClass, entityInterfaces);

        if (entityInterfaces.isEmpty() && !hasAllMethodsMarker) {
            var msg = "Entity class " + entityClass.getName() +
                      " has no entity interfaces and no AllMethodsMarker annotation";
            log.severe(msg);
            throw new IllegalStateException(msg);
        }

        // Collect event methods from interfaces and annotations
        var eventMethods = collectEventMethods(entityClass, implementedInterfaces, hasAllMethodsMarker);

        try {
            return new EntityGenerator(entityClass, eventMethods, transformTimestamp.get());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create EntityGenerator for " + entityClass.getName(), e);
        }
    }

    /**
     * Finds all classes annotated with @Entity and applies the given filter.
     */
    private List<ClassMetadata> findEntityClasses(Predicate<ClassMetadata> selector) {
        return scanner.getClassesWithAnnotation(Entity.class.getCanonicalName(), selector);
    }

    /**
     * Finds entity interfaces that are actually implemented by the given class.
     */
    private Set<ClassMetadata> findImplementedEntityInterfaces(ClassMetadata entityClass,
                                                               Set<ClassMetadata> entityInterfaces) {
        var classInterfaceNames = new HashSet<>(entityClass.getInterfaceNames());
        var implementedInterfaces = new OpenSet<ClassMetadata>();

        for (var entityInterface : entityInterfaces) {
            if (classInterfaceNames.contains(entityInterface.getName())) {
                implementedInterfaces.add(entityInterface);
            }
        }

        return implementedInterfaces;
    }

    /**
     * Finds a method in the entity class that matches the given interface method.
     */
    private MethodMetadata findMatchingMethod(ClassMetadata entityClass, MethodMetadata interfaceMethod) {
        // First check declared methods
        for (var method : entityClass.getMethods(interfaceMethod.getName())) {
            if (method.getDescriptor().equals(interfaceMethod.getDescriptor())) {
                return method;
            }
        }

        // Then check superclass methods
        for (var superClass : entityClass.getSuperclasses()) {
            for (var method : superClass.getMethods(interfaceMethod.getName())) {
                if (method.getDescriptor().equals(interfaceMethod.getDescriptor())) {
                    return method;
                }
            }
        }

        return null;
    }

    /**
     * Remaps a class's bytecode to replace Kronos with Kairos.
     */
    private byte[] remapClassWithClassFileAPI(ClassMetadata classMetadata) {
        var originalBytes = classMetadata.getOriginalBytes();
        if (originalBytes == null) {
            log.warning("No bytes found for class: " + classMetadata.getName() + " - skipping transformation");
            return null;
        }

        var classModel = classMetadata.getClassModel();
        byte[] transformedBytes = CLASS_FILE.build(classModel.thisClass().asSymbol(),
                                                   classBuilder -> classBuilder.transform(classModel, apiRemapper));

        // Only return transformed bytes if they differ from original
        return Arrays.equals(originalBytes, transformedBytes) ? null : transformedBytes;
    }

    /**
     * Remaps generated entity class bytecode.
     */
    private byte[] remapGeneratedClassWithClassFileAPI(byte[] generatedBytes) {
        var classModel = CLASS_FILE.parse(generatedBytes);
        return CLASS_FILE.build(classModel.thisClass().asSymbol(),
                                classBuilder -> classBuilder.transform(classModel, apiRemapper));
    }

    /**
     * Determines whether a method should be included as an event method.
     */
    private boolean shouldIncludeMethod(MethodMetadata method, boolean hasAllMethodsMarker) {
        if (hasAllMethodsMarker) {
            return method.isPublic();
        }
        return method.hasAnnotation(Blocking.class) || method.hasAnnotation(Event.class);
    }

    /**
     * Determines whether a class should be considered for transformation.
     */
    private boolean shouldTransformClass(ClassMetadata classMetadata) {
        var packageName = classMetadata.getPackageName();

        return !packageName.equals(Kairos.class.getPackageName())
               && !packageName.equals(Kronos.class.getPackageName())
               && !packageName.equals(Entity.class.getPackageName())
               && !packageName.equals(EntityGenerator.class.getPackageName())
               && !packageName.startsWith("org.objectweb.asm")
               && !packageName.startsWith("org.junit");
    }

    /**
     * Transforms non-entity classes that depend on Kronos by remapping API calls.
     */
    private void transformDependentClasses(Predicate<ClassMetadata> selector, List<ClassMetadata> entities,
                                           Map<ClassMetadata, byte[]> transformed) {
        var kronosClassName = Kronos.class.getCanonicalName();
        var entitySet = new HashSet<>(entities);

        scanner.stream(selector)
               .filter(this::shouldTransformClass)
               .filter(cm -> !entitySet.contains(cm))
               .filter(EXCLUDE_TRANSFORMED_FILTER)
               .filter(cm -> cm.getDependencies().contains(kronosClassName))
               .parallel()
               .forEach(classMetadata -> {
                   try {
                       var transformedBytes = remapClassWithClassFileAPI(classMetadata);
                       if (transformedBytes != null) {
                           transformed.put(classMetadata, transformedBytes);
                       }
                   } catch (Exception e) {
                       log.severe("Failed to transform dependent class: " + classMetadata.getName() +
                                  " - " + e.getMessage());
                       throw new IllegalStateException(
                           "Failed to transform dependent class: " + classMetadata.getName(), e);
                   }
               });
    }

    /**
     * Transforms entity classes using EntityGenerator and API remapping.
     */
    private void transformEntityClasses(List<ClassMetadata> entities, Map<ClassMetadata, byte[]> transformed) {
        generators(entities).entrySet().parallelStream().forEach(entry -> {
            var entityName = entry.getKey().getName();
            try {
                var generatedBytes = entry.getValue().generate();
                var remappedBytes = remapGeneratedClassWithClassFileAPI(generatedBytes);
                transformed.put(entry.getKey(), remappedBytes);
            } catch (Exception e) {
                log.severe("Failed to transform entity class: " + entityName + " - " + e.getMessage());
                throw new IllegalStateException("Failed to transform entity: " + entityName, e);
            }
        });
    }
}
