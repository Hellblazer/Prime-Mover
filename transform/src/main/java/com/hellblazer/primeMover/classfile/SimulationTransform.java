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
import io.github.classgraph.*;
import io.github.classgraph.ClassInfoList.ClassInfoFilter;

import java.io.Closeable;
import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.constant.ClassDesc;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * ClassFile API implementation of SimulationTransformOriginal that provides entity transformation using Java 25 ClassFile API
 * (JEP 484) instead of ASM.
 *
 * This class provides the same functionality as SimulationTransformRefactored but uses the new ClassFile API for
 * bytecode manipulation, ensuring compatibility with modern Java versions while maintaining identical behavior.
 *
 * @author hal.hildebrand
 */
public class SimulationTransform implements Closeable {

    /** Filter that excludes already transformed classes */
    public static final  ClassInfoFilter EXCLUDE_TRANSFORMED_FILTER = classInfo -> !classInfo.hasAnnotation(
    Transformed.class);
    /** Marker annotation that indicates all public methods should be treated as events */
    private static final String          ALL_METHODS_MARKER         = AllMethodsMarker.class.getCanonicalName();
    /** Cached ClassFile instance for bytecode transformations */
    private static final ClassFile       CLASS_FILE                 = ClassFile.of();
    /** Filter that accepts all classes */
    private static final ClassInfoFilter              ACCEPT_ALL_FILTER   = classInfo -> true;
    private static final Logger                       log                 = Logger.getLogger(SimulationTransform.class.getName());
    private final        ScanResult                   scan;
    private final        ClassRemapper                apiRemapper;
    private final        AtomicReference<String>      transformTimestamp  = new AtomicReference<>();

    /**
     * Creates a new SimulationTransform with the given ClassGraph configuration.
     *
     * @param graph ClassGraph instance configured for the desired packages
     */
    public SimulationTransform(ClassGraph graph) {
        this(
        graph.enableAllInfo().enableInterClassDependencies().enableExternalClasses().ignoreMethodVisibility().scan());
    }

    /**
     * Creates a new SimulationTransform with an existing scan result.
     *
     * @param scan Pre-computed scan result from ClassGraph
     */
    public SimulationTransform(ScanResult scan) {
        this.scan = scan;
        // Create mapping function for ClassFile API ClassRemapper
        this.apiRemapper = new ClassRemapper(classDesc -> {
            var pkg = classDesc.packageName();
            var className = pkg.isEmpty() ? classDesc.displayName() : pkg + "." + classDesc.displayName();
            if (className.equals(Kronos.class.getCanonicalName())) {
                return ClassDesc.of(Kairos.class.getCanonicalName());
            }
            return classDesc;
        });
        this.transformTimestamp.set(java.time.Instant.now().toString());
    }

    /**
     * Extracts entity interfaces from the @Entity annotation value. This includes interfaces from the current class and
     * its superclasses.
     *
     * @param entityClass The entity class to analyze
     * @return Set of interfaces marked as entity interfaces
     */
    public static Set<ClassInfo> getEntityInterfaces(ClassInfo entityClass) {
        var entityAnnotation = entityClass.getAnnotationInfo(Entity.class);
        if (entityAnnotation == null) {
            return new OpenSet<>();
        }

        var interfaceReferences = (Object[]) entityAnnotation.getParameterValues(true).getValue("value");
        if (interfaceReferences == null) {
            interfaceReferences = new Object[0];
        }

        // Collect interfaces from current class annotation
        Set<ClassInfo> entityInterfaces = Arrays.stream(interfaceReferences).map(o -> (AnnotationClassRef) o).map(
        AnnotationClassRef::getClassInfo).collect(Collectors.toCollection(() -> new OpenSet<>()));

        // Collect interfaces from superclass @Entity annotations
        entityClass.getSuperclasses()
                   .stream()
                   .filter(superClass -> superClass.getAnnotationInfo(Entity.class) != null)
                   .flatMap(superClass -> {
                       var superInterfaces = (Object[]) superClass.getAnnotationInfo(Entity.class).getParameterValues(
                       true).getValue("value");
                       return Arrays.stream(superInterfaces != null ? superInterfaces : new Object[0]);
                   })
                   .map(o -> (AnnotationClassRef) o)
                   .map(AnnotationClassRef::getClassInfo)
                   .forEach(entityInterfaces::add);

        return entityInterfaces;
    }

    @Override
    public void close() throws IOException {
        if (scan != null) {
            scan.close();
        }
    }

    /**
     * Creates an EntityGenerator for the specified class name.
     *
     * @param classname Fully qualified class name of the entity
     * @return EntityGenerator instance or null if class not found
     */
    public EntityGenerator generatorOf(String classname) {
        return generatorOf(classname, ACCEPT_ALL_FILTER);
    }

    /**
     * Creates an EntityGenerator for the specified class name with filtering.
     *
     * @param classname Fully qualified class name of the entity
     * @param selector  Filter to apply to entity classes
     * @return EntityGenerator instance or null if class not found
     * @throws NullPointerException if classname is null
     * @throws IllegalArgumentException if classname is empty or class not found
     */
    public EntityGenerator generatorOf(String classname, ClassInfoFilter selector) {
        Objects.requireNonNull(classname, "classname cannot be null");
        if (classname.trim().isEmpty()) {
            throw new IllegalArgumentException("classname cannot be empty");
        }

        var entities = findEntityClasses(selector);
        var entity = entities.get(classname);
        if (entity == null) {
            throw new IllegalArgumentException("Entity class not found: " + classname +
                ". Available entities: " + entities.stream().map(ci -> ci.getName()).collect(Collectors.toSet()));
        }
        return createEntityGenerator(entity);
    }

    /**
     * Creates EntityGenerator instances for all entity classes.
     *
     * @return Map of ClassInfo to EntityGenerator for all entities
     */
    public Map<ClassInfo, EntityGenerator> generators() {
        return generators(ACCEPT_ALL_FILTER);
    }

    /**
     * Creates EntityGenerator instances for filtered entity classes.
     *
     * @param selector Filter to apply to entity classes
     * @return Map of ClassInfo to EntityGenerator for filtered entities
     */
    public Map<ClassInfo, EntityGenerator> generators(ClassInfoFilter selector) {
        var entities = findEntityClasses(selector);
        return generators(entities);
    }

    /**
     * Creates EntityGenerator instances for the provided entity class list.
     *
     * @param entities List of entity ClassInfo objects
     * @return Map of ClassInfo to EntityGenerator
     */
    public Map<ClassInfo, EntityGenerator> generators(ClassInfoList entities) {
        return entities.filter(EXCLUDE_TRANSFORMED_FILTER).stream().collect(
        Collectors.toMap(ci -> ci, this::createEntityGenerator));
    }

    /**
     * Gets the current transform timestamp being used for @Transformed annotations.
     *
     * @return The current timestamp string
     */
    public String getTransformTimestamp() {
        return transformTimestamp.get();
    }

    /**
     * Sets the timestamp to be used in @Transformed annotations for all generated entities. This allows for
     * deterministic bytecode generation when the same timestamp is used.
     *
     * @param timestamp The timestamp string to use (typically ISO-8601 format)
     */
    public void setTransformTimestamp(String timestamp) {
        this.transformTimestamp.set(timestamp);
    }

    /**
     * Generates transformed bytecode for all classes requiring transformation. This includes both entity classes and
     * dependent classes that reference Kronos.
     *
     * @return Map of ClassInfo to transformed bytecode
     */
    public Map<ClassInfo, byte[]> transformed() {
        return transformed(ACCEPT_ALL_FILTER);
    }

    /**
     * Generates transformed bytecode for filtered classes requiring transformation.
     *
     * @param selector Filter to apply to classes
     * @return Map of ClassInfo to transformed bytecode
     */
    public Map<ClassInfo, byte[]> transformed(ClassInfoFilter selector) {
        var entities = findEntityClasses(selector);
        var transformed = new ConcurrentHashMap<ClassInfo, byte[]>();

        // Transform non-entity classes that depend on Kronos (parallel)
        transformDependentClasses(selector, entities, transformed);

        // Transform entity classes (parallel)
        transformEntityClasses(entities, transformed);

        return transformed;
    }

    /**
     * Collects all event methods for an entity class from: 1. Methods declared in implemented entity interfaces 2.
     * Methods explicitly annotated with @Event 3. All public methods if AllMethodsMarker is present
     *
     * @param entityClass           The entity class to analyze
     * @param implementedInterfaces Set of entity interfaces implemented by the class
     * @param hasAllMethodsMarker   Whether the class should treat all public methods as events
     * @return Set of event methods
     */
    private OpenSet<MethodInfo> collectEventMethods(ClassInfo entityClass, OpenSet<ClassInfo> implementedInterfaces,
                                                    boolean hasAllMethodsMarker) {
        var eventMethods = new OpenSet<MethodInfo>();

        // Add methods from implemented entity interfaces
        implementedInterfaces.stream().flatMap(intf -> intf.getMethodInfo().stream()).forEach(interfaceMethod -> {
            var classMethod = findMatchingMethod(entityClass, interfaceMethod);
            if (classMethod != null && !classMethod.hasAnnotation(NonEvent.class)) {
                eventMethods.add(classMethod);
            }
        });

        // Add methods from the class itself
        entityClass.getDeclaredMethodInfo().stream().filter(method -> !method.isStatic()).filter(
        method -> !method.hasAnnotation(NonEvent.class)).filter(
        method -> shouldIncludeMethod(method, hasAllMethodsMarker)).forEach(eventMethods::add);

        return eventMethods;
    }

    /**
     * Creates an EntityGenerator for the given entity class by analyzing its event methods and interfaces.
     *
     * @param entityClass The entity class to create a generator for
     * @return EntityGenerator instance for the class
     */
    private EntityGenerator createEntityGenerator(ClassInfo entityClass) {
        var entityInterfaces = getEntityInterfaces(entityClass);
        var hasAllMethodsMarker = entityInterfaces.stream().anyMatch(c -> c.getName().equals(ALL_METHODS_MARKER));

        // Find implemented entity interfaces
        var implementedInterfaces = findImplementedEntityInterfaces(entityClass, entityInterfaces);

        if (entityInterfaces.isEmpty() && !hasAllMethodsMarker) {
            var msg = "Entity class " + entityClass.getName() + " has no entity interfaces and no AllMethodsMarker annotation";
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
     *
     * @param selector Filter to apply to entity classes
     * @return Filtered list of entity classes
     */
    private ClassInfoList findEntityClasses(ClassInfoFilter selector) {
        return scan.getClassesWithAnnotation(Entity.class.getCanonicalName()).filter(selector);
    }

    /**
     * Finds entity interfaces that are actually implemented by the given class.
     *
     * @param entityClass      The entity class to check
     * @param entityInterfaces Set of entity interfaces from annotations
     * @return Set of implemented entity interfaces
     */
    private OpenSet<ClassInfo> findImplementedEntityInterfaces(ClassInfo entityClass, Set<ClassInfo> entityInterfaces) {
        var classInterfaces = entityClass.getInterfaces();
        var implementedInterfaces = new OpenSet<ClassInfo>();

        entityInterfaces.forEach(entityInterface -> {
            if (classInterfaces.contains(entityInterface)) {
                implementedInterfaces.add(entityInterface);
            }
        });

        return implementedInterfaces;
    }

    /**
     * Finds the Kronos class in the scan results.
     *
     * @return ClassInfo for Kronos class
     * @throws IllegalStateException if Kronos class cannot be found
     */
    private ClassInfo findKronosClass() {
        var kronosClass = scan.getAllClasses().get(Kronos.class.getCanonicalName());
        if (kronosClass == null) {
            throw new IllegalStateException("Cannot find " + Kronos.class.getSimpleName());
        }
        return kronosClass;
    }

    /**
     * Finds a method in the entity class that matches the given interface method. Searches both declared methods and
     * inherited methods from superclasses.
     *
     * @param entityClass     The entity class to search
     * @param interfaceMethod The interface method to match
     * @return Matching method or null if not found
     */
    private MethodInfo findMatchingMethod(ClassInfo entityClass, MethodInfo interfaceMethod) {
        // First check declared methods
        var declaredMethod = entityClass.getMethodInfo(interfaceMethod.getName()).stream().filter(
        m -> m.getTypeDescriptorStr().equals(interfaceMethod.getTypeDescriptorStr())).findFirst().orElse(null);

        if (declaredMethod != null) {
            return declaredMethod;
        }

        // Then check superclass methods
        return entityClass.getSuperclasses().stream().flatMap(
        superClass -> superClass.getMethodInfo(interfaceMethod.getName()).stream()).filter(
        m -> m.getTypeDescriptorStr().equals(interfaceMethod.getTypeDescriptorStr())).findFirst().orElse(null);
    }

    /**
     * Remaps a class's bytecode using ClassFile API ClassRemapper to replace Kronos with Kairos.
     *
     * @param classInfo The class to remap
     * @return Remapped bytecode or null if no changes needed
     */
    private byte[] remapClassWithClassFileAPI(ClassInfo classInfo) {
        if (classInfo.getResource() == null) {
            log.warning("No resource found for class: " + classInfo.getName() + " - skipping transformation");
            return null;
        }

        try (var inputStream = classInfo.getResource().open()) {
            byte[] originalBytes = inputStream.readAllBytes();

            var classModel = CLASS_FILE.parse(originalBytes);

            byte[] transformedBytes = CLASS_FILE.build(classModel.thisClass().asSymbol(),
                                                       classBuilder -> classBuilder.transform(classModel, apiRemapper));

            // Only return transformed bytes if they differ from original
            return java.util.Arrays.equals(originalBytes, transformedBytes) ? null : transformedBytes;

        } catch (IOException e) {
            throw new IllegalStateException("Unable to read class bytes: " + classInfo.getName(), e);
        }
    }

    /**
     * Remaps generated entity class bytecode using ClassFile API ClassRemapper to replace Kronos with Kairos.
     *
     * @param generatedBytes Bytecode generated by EntityGenerator
     * @return Remapped bytecode
     */
    private byte[] remapGeneratedClassWithClassFileAPI(byte[] generatedBytes) {
        var classModel = CLASS_FILE.parse(generatedBytes);

        return CLASS_FILE.build(classModel.thisClass().asSymbol(),
                                classBuilder -> classBuilder.transform(classModel, apiRemapper));
    }

    /**
     * Determines whether a method should be included as an event method.
     *
     * @param method              The method to check
     * @param hasAllMethodsMarker Whether all public methods should be included
     * @return true if the method should be included as an event method
     */
    private boolean shouldIncludeMethod(MethodInfo method, boolean hasAllMethodsMarker) {
        if (hasAllMethodsMarker) {
            return method.isPublic();
        }
        return method.hasAnnotation(Blocking.class) || method.hasAnnotation(Event.class);
    }

    /**
     * Determines whether a class should be considered for transformation. Excludes framework and test classes.
     *
     * @param classInfo The class to check
     * @return true if the class should be transformed
     */
    private boolean shouldTransformClass(ClassInfo classInfo) {
        String packageName = classInfo.getPackageName();

        return !packageName.equals(Kairos.class.getPackageName()) && !packageName.equals(Kronos.class.getPackageName())
        && !packageName.equals(Entity.class.getPackageName()) && !packageName.equals(
        EntityGenerator.class.getPackageName()) && !packageName.startsWith("org.objectweb.asm")
        && !packageName.startsWith("org.junit") && !packageName.startsWith(ClassInfo.class.getPackageName());
    }

    /**
     * Transforms non-entity classes that depend on Kronos by remapping API calls.
     * <p>
     * Uses parallel stream processing for improved throughput. Exceptions during
     * transformation are logged with full context before being re-thrown.
     *
     * @param selector    Filter for classes to consider
     * @param entities    Entity classes to exclude from this transformation
     * @param transformed Map to store transformed bytecode (must be thread-safe)
     */
    private void transformDependentClasses(ClassInfoFilter selector, ClassInfoList entities,
                                           Map<ClassInfo, byte[]> transformed) {
        var kronosClass = findKronosClass();
        var entitySet = new HashSet<>(entities);  // O(1) lookup instead of O(n)

        scan.getAllClasses()
            .filter(selector)
            .filter(this::shouldTransformClass)
            .filter(ci -> !entitySet.contains(ci))
            .filter(EXCLUDE_TRANSFORMED_FILTER)
            .filter(ci -> ci.getClassDependencies().contains(kronosClass))
            .parallelStream()
            .forEach(classInfo -> {
                try {
                    var transformedBytes = remapClassWithClassFileAPI(classInfo);
                    if (transformedBytes != null) {
                        transformed.put(classInfo, transformedBytes);
                    }
                } catch (Exception e) {
                    log.severe("Failed to transform dependent class: " + classInfo.getName() + " - " + e.getMessage());
                    throw new IllegalStateException("Failed to transform dependent class: " + classInfo.getName(), e);
                }
            });
    }

    /**
     * Transforms entity classes using EntityGenerator and API remapping.
     * <p>
     * Uses parallel stream processing for improved throughput. Exceptions during
     * transformation are logged with full context before being re-thrown.
     *
     * @param entities    Entity classes to transform
     * @param transformed Map to store transformed bytecode (must be thread-safe)
     */
    private void transformEntityClasses(ClassInfoList entities, Map<ClassInfo, byte[]> transformed) {
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
