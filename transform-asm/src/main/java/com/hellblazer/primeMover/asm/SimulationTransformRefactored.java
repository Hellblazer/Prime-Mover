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
package com.hellblazer.primeMover.asm;

import com.hellblazer.primeMover.annotations.*;
import com.hellblazer.primeMover.api.Kronos;
import com.hellblazer.primeMover.classfile.OpenAddressingSet.OpenSet;
import com.hellblazer.primeMover.runtime.Kairos;
import io.github.classgraph.*;
import io.github.classgraph.ClassInfoList.ClassInfoFilter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Refactored SimulationTransformOriginal that provides a cleaner, more maintainable interface for transforming entity
 * classes in the Prime Mover simulation framework.
 *
 * This class orchestrates the bytecode transformation process: 1. Scans classpath for @Entity annotated classes 2.
 * Identifies event methods (from interfaces or @Event annotations) 3. Generates simulation-aware bytecode using
 * EntityGeneratorRefactored 4. Handles API remapping (Kronos -> Kairos) for transformed classes
 *
 * @author hal.hildebrand
 */
public class SimulationTransformRefactored implements Closeable {

    /** Marker annotation that indicates all public methods should be treated as events */
    private static final String ALL_METHODS_MARKER = AllMethodsMarker.class.getCanonicalName();

    /** Filter that accepts all classes */
    private static final ClassInfoFilter ACCEPT_ALL_FILTER = classInfo -> true;

    /** Filter that excludes already transformed classes */
    private static final ClassInfoFilter EXCLUDE_TRANSFORMED_FILTER = classInfo -> !classInfo.hasAnnotation(
    Transformed.class);

    private final ScanResult  scan;
    private final ApiRemapper apiRemapper;
    private       String      transformTimestamp;

    /**
     * Creates a new SimulationTransformOriginal with the given ClassGraph configuration.
     *
     * @param graph ClassGraph instance configured for the desired packages
     */
    public SimulationTransformRefactored(ClassGraph graph) {
        this(
        graph.enableAllInfo().enableInterClassDependencies().enableExternalClasses().ignoreMethodVisibility().scan());
    }

    /**
     * Creates a new SimulationTransformOriginal with an existing scan result.
     *
     * @param scan Pre-computed scan result from ClassGraph
     */
    public SimulationTransformRefactored(ScanResult scan) {
        this.scan = scan;
        this.apiRemapper = new ApiRemapper();
        this.transformTimestamp = java.time.Instant.now().toString();
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
     * Creates an EntityGeneratorOriginal for the specified class name.
     *
     * @param classname Fully qualified class name of the entity
     * @return EntityGeneratorOriginal instance or null if class not found
     */
    public EntityGeneratorRefactored generatorOf(String classname) {
        return generatorOf(classname, ACCEPT_ALL_FILTER);
    }

    /**
     * Creates an EntityGeneratorOriginal for the specified class name with filtering.
     *
     * @param classname Fully qualified class name of the entity
     * @param selector  Filter to apply to entity classes
     * @return EntityGeneratorOriginal instance or null if class not found
     */
    public EntityGeneratorRefactored generatorOf(String classname, ClassInfoFilter selector) {
        var entities = findEntityClasses(selector);
        var entity = entities.get(classname);
        if (entity == null) {
            return null;
        }
        return createEntityGenerator(entity);
    }

    /**
     * Creates EntityGenerators for all entity classes.
     *
     * @return Map of ClassInfo to EntityGeneratorOriginal for all entities
     */
    public Map<ClassInfo, EntityGeneratorRefactored> generators() {
        return generators(ACCEPT_ALL_FILTER);
    }

    /**
     * Creates EntityGenerators for filtered entity classes.
     *
     * @param selector Filter to apply to entity classes
     * @return Map of ClassInfo to EntityGeneratorOriginal for filtered entities
     */
    public Map<ClassInfo, EntityGeneratorRefactored> generators(ClassInfoFilter selector) {
        var entities = findEntityClasses(selector);
        return generators(entities);
    }

    /**
     * Creates EntityGenerators for the provided entity class list.
     *
     * @param entities List of entity ClassInfo objects
     * @return Map of ClassInfo to EntityGeneratorOriginal
     */
    public Map<ClassInfo, EntityGeneratorRefactored> generators(ClassInfoList entities) {
        return entities.filter(EXCLUDE_TRANSFORMED_FILTER).stream().collect(
        Collectors.toMap(ci -> ci, this::createEntityGenerator));
    }

    /**
     * Gets the current transform timestamp being used for @Transformed annotations.
     *
     * @return The current timestamp string
     */
    public String getTransformTimestamp() {
        return transformTimestamp;
    }

    /**
     * Sets the timestamp to be used in @Transformed annotations for all generated entities. This allows for
     * deterministic bytecode generation when the same timestamp is used.
     *
     * @param timestamp The timestamp string to use (typically ISO-8601 format)
     */
    public void setTransformTimestamp(String timestamp) {
        this.transformTimestamp = timestamp;
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
        var transformed = new HashMap<ClassInfo, byte[]>();

        // Transform non-entity classes that depend on Kronos
        transformDependentClasses(selector, entities, transformed);

        // Transform entity classes
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
     * Creates an EntityGeneratorOriginal for the given entity class by analyzing its event methods and interfaces.
     *
     * @param entityClass The entity class to create a generator for
     * @return EntityGeneratorRefactored instance for the class
     */
    private EntityGeneratorRefactored createEntityGenerator(ClassInfo entityClass) {
        var entityInterfaces = getEntityInterfaces(entityClass);
        var hasAllMethodsMarker = entityInterfaces.stream().anyMatch(c -> c.getName().equals(ALL_METHODS_MARKER));

        // Find implemented entity interfaces
        var implementedInterfaces = findImplementedEntityInterfaces(entityClass, entityInterfaces);

        if (entityInterfaces.isEmpty() && !hasAllMethodsMarker) {
            throw new IllegalStateException(
            "Entity class " + entityClass.getName() + " has no entity interfaces and no AllMethodsMarker annotation");
        }

        // Collect event methods from interfaces and annotations
        var eventMethods = collectEventMethods(entityClass, implementedInterfaces, hasAllMethodsMarker);

        try {
            return new EntityGeneratorRefactored(entityClass, eventMethods, transformTimestamp);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create EntityGeneratorOriginal for " + entityClass.getName(), e);
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
        EntityGeneratorOriginal.class.getPackageName()) && !packageName.startsWith(ClassVisitor.class.getPackageName())
        && !packageName.startsWith("org.junit") && !packageName.startsWith(ClassInfo.class.getPackageName());
    }

    /**
     * Transforms non-entity classes that depend on Kronos by remapping API calls.
     *
     * @param selector    Filter for classes to consider
     * @param entities    Entity classes to exclude from this transformation
     * @param transformed Map to store transformed bytecode
     */
    private void transformDependentClasses(ClassInfoFilter selector, ClassInfoList entities,
                                           Map<ClassInfo, byte[]> transformed) {
        var kronosClass = findKronosClass();

        scan.getAllClasses()
            .filter(selector)
            .filter(this::shouldTransformClass)
            .filter(ci -> !entities.contains(ci))
            .filter(EXCLUDE_TRANSFORMED_FILTER)
            .filter(ci -> ci.getClassDependencies().contains(kronosClass))
            .forEach(classInfo -> {
                var transformedBytes = apiRemapper.remapClass(classInfo);
                if (transformedBytes != null) {
                    transformed.put(classInfo, transformedBytes);
                }
            });
    }

    /**
     * Transforms entity classes using EntityGeneratorRefactored and API remapping.
     *
     * @param entities    Entity classes to transform
     * @param transformed Map to store transformed bytecode
     */
    private void transformEntityClasses(ClassInfoList entities, Map<ClassInfo, byte[]> transformed) {
        generators(entities).forEach((classInfo, generator) -> {
            try {
                var generatedBytes = generator.generate().toByteArray();
                var remappedBytes = apiRemapper.remapGeneratedClass(generatedBytes);
                transformed.put(classInfo, remappedBytes);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to transform entity: " + classInfo.getName(), e);
            }
        });
    }

    /**
     * Helper class that handles API remapping from Kronos to Kairos.
     */
    private static class ApiRemapper {
        private final SimpleRemapper remapper;

        public ApiRemapper() {
            var mappings = new HashMap<String, String>();
            mappings.put(Kronos.class.getCanonicalName().replace('.', '/'),
                         Kairos.class.getCanonicalName().replace('.', '/'));
            this.remapper = new SimpleRemapper(mappings);
        }

        /**
         * Remaps a class's bytecode to use Kairos instead of Kronos.
         *
         * @param classInfo The class to remap
         * @return Remapped bytecode or null if no changes needed
         */
        public byte[] remapClass(ClassInfo classInfo) {
            if (classInfo.getResource() == null) {
                return null;
            }

            try (var inputStream = classInfo.getResource().open()) {
                var classReader = new ClassReader(inputStream);
                var classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                classReader.accept(new ClassRemapper(classWriter, remapper), ClassReader.EXPAND_FRAMES);

                var transformedBytes = classWriter.toByteArray();

                // Only return transformed bytes if they differ from original
                @SuppressWarnings("deprecation")
                var originalBytes = classReader.b;
                return transformedBytes.equals(originalBytes) ? null : transformedBytes;

            } catch (IOException e) {
                throw new IllegalStateException("Unable to read class bytes: " + classInfo.getName(), e);
            }
        }

        /**
         * Remaps generated entity class bytecode to use Kairos instead of Kronos.
         *
         * @param generatedBytes Bytecode generated by EntityGeneratorRefactored
         * @return Remapped bytecode
         */
        public byte[] remapGeneratedClass(byte[] generatedBytes) {
            var classReader = new ClassReader(generatedBytes);
            var classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classReader.accept(new ClassRemapper(classWriter, remapper), ClassReader.EXPAND_FRAMES);
            return classWriter.toByteArray();
        }
    }
}
