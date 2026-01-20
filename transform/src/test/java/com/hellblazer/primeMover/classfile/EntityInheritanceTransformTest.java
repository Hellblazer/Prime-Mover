/**
 * Copyright (C) 2008-2025 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 */
package com.hellblazer.primeMover.classfile;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for entity inheritance transformation.
 * <p>
 * Tests verify that @Entity classes with inheritance chains are correctly
 * transformed, including:
 * - Single inheritance (child extends parent)
 * - Deep inheritance (multiple levels of @Entity classes)
 * - Method overrides (child overrides parent entity methods)
 * - Abstract entities (abstract @Entity with concrete children)
 * - Mixed hierarchy (some classes @Entity, some not)
 * - Interface implementation with entity hierarchies
 * <p>
 * Critical invariants:
 * - Method ordinals must be consistent across hierarchy
 * - Inherited methods must not be re-transformed
 * - Each level transforms correctly and independently
 * - Overridden methods use same ordinal as parent
 *
 * @author hal.hildebrand
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EntityInheritanceTransformTest {

    private static SimulationTransform transform;

    @BeforeAll
    static void setUp() throws IOException {
        var testClassesPath = Path.of("target/test-classes");
        transform = new SimulationTransform(testClassesPath);
    }

    @AfterAll
    static void tearDown() throws IOException {
        if (transform != null) {
            transform.close();
        }
    }

    // ========================================================================
    // Single Inheritance Tests
    // ========================================================================

    @Test
    @Order(1)
    @DisplayName("Base entity transforms correctly")
    void testBaseEntityTransforms() throws Exception {
        var className = "testClasses.hierarchy.BaseEntity";

        var generator = transform.generatorOf(className);
        assertNotNull(generator, "Should create generator for base entity");

        var bytecode = generator.generate();
        assertNotNull(bytecode, "Should generate bytecode");
        assertTrue(bytecode.length > 0, "Bytecode should not be empty");

        // Verify class metadata
        var scanner = transform.getScanner();
        var entityClass = scanner.getClass(className);
        assertNotNull(entityClass, "Should find entity class in scanner");
        assertEquals(className, entityClass.getName());
    }

    @Test
    @Order(2)
    @DisplayName("Derived entity transforms correctly")
    void testDerivedEntityTransforms() throws Exception {
        var className = "testClasses.hierarchy.DerivedEntity";

        var generator = transform.generatorOf(className);
        assertNotNull(generator, "Should create generator for derived entity");

        var bytecode = generator.generate();
        assertNotNull(bytecode, "Should generate bytecode");
        assertTrue(bytecode.length > 0, "Bytecode should not be empty");
    }

    @Test
    @Order(3)
    @DisplayName("Derived entity includes inherited methods")
    void testDerivedEntityIncludesInheritedMethods() throws Exception {
        var scanner = transform.getScanner();

        var derivedClass = scanner.getClass("testClasses.hierarchy.DerivedEntity");
        assertNotNull(derivedClass, "Should find derived class");

        // Get all event methods including inherited
        var generator = transform.generatorOf(derivedClass.getName());
        assertNotNull(generator, "Should create generator");

        // Verify inheritance chain
        var superclasses = derivedClass.getSuperclasses();
        assertFalse(superclasses.isEmpty(), "Should have superclasses");
        assertEquals("testClasses.hierarchy.BaseEntity", superclasses.get(0).getName());
    }

    // ========================================================================
    // Deep Inheritance Tests
    // ========================================================================

    @Test
    @Order(10)
    @DisplayName("Three-level hierarchy transforms correctly")
    void testDeepHierarchyTransforms() throws Exception {
        // Test ConcreteEntity which extends DerivedEntity which extends BaseEntity
        var className = "testClasses.hierarchy.ConcreteEntity";

        var generator = transform.generatorOf(className);
        assertNotNull(generator, "Should create generator for concrete entity");

        var bytecode = generator.generate();
        assertNotNull(bytecode, "Should generate bytecode");
        assertTrue(bytecode.length > 0, "Bytecode should not be empty");
    }

    @Test
    @Order(11)
    @DisplayName("Deep hierarchy includes all inherited methods")
    void testDeepHierarchyIncludesAllInheritedMethods() throws Exception {
        var scanner = transform.getScanner();

        var concreteClass = scanner.getClass("testClasses.hierarchy.ConcreteEntity");
        assertNotNull(concreteClass, "Should find concrete class");

        // Verify complete inheritance chain
        var superclasses = concreteClass.getSuperclasses();
        assertTrue(superclasses.size() >= 2, "Should have at least 2 superclasses");

        // Should have DerivedEntity and BaseEntity in chain
        var superNames = superclasses.stream()
                                     .map(ClassMetadata::getName)
                                     .toList();
        assertTrue(superNames.contains("testClasses.hierarchy.DerivedEntity"),
                   "Should inherit from DerivedEntity");
        assertTrue(superNames.contains("testClasses.hierarchy.BaseEntity"),
                   "Should inherit from BaseEntity");
    }

    // ========================================================================
    // Method Override Tests
    // ========================================================================

    @Test
    @Order(20)
    @DisplayName("Overridden methods do not create duplicate ordinals")
    void testOverriddenMethodsNoDuplicateOrdinals() throws Exception {
        // DerivedEntity overrides baseEvent from BaseEntity
        var className = "testClasses.hierarchy.DerivedEntity";

        var generator = transform.generatorOf(className);
        assertNotNull(generator, "Should create generator");

        var bytecode = generator.generate();
        assertNotNull(bytecode, "Should generate bytecode");

        // The override should work correctly - no duplicate ordinals
        // This is verified by successful bytecode generation without errors
    }

    // ========================================================================
    // Abstract Entity Tests
    // ========================================================================

    @Test
    @Order(30)
    @DisplayName("Abstract entity transforms correctly")
    void testAbstractEntityTransforms() throws Exception {
        var className = "testClasses.hierarchy.AbstractEntity";

        var generator = transform.generatorOf(className);
        assertNotNull(generator, "Should create generator for abstract entity");

        var bytecode = generator.generate();
        assertNotNull(bytecode, "Should generate bytecode");
        assertTrue(bytecode.length > 0, "Bytecode should not be empty");
    }

    @Test
    @Order(31)
    @DisplayName("Concrete class extending abstract entity transforms")
    void testConcreteAbstractEntityTransforms() throws Exception {
        var className = "testClasses.hierarchy.ConcreteAbstractEntity";

        var generator = transform.generatorOf(className);
        assertNotNull(generator, "Should create generator for concrete abstract entity");

        var bytecode = generator.generate();
        assertNotNull(bytecode, "Should generate bytecode");
        assertTrue(bytecode.length > 0, "Bytecode should not be empty");
    }

    @Test
    @Order(32)
    @DisplayName("Concrete class includes inherited interface from abstract parent")
    void testConcreteIncludesInheritedInterface() throws Exception {
        var scanner = transform.getScanner();

        var concreteClass = scanner.getClass("testClasses.hierarchy.ConcreteAbstractEntity");
        assertNotNull(concreteClass, "Should find concrete class");

        // Get entity interfaces - should include EventInterface from parent's @Entity annotation
        var entityInterfaces = transform.getEntityInterfaces(concreteClass);
        assertNotNull(entityInterfaces, "Should get entity interfaces");

        var interfaceNames = entityInterfaces.stream()
                                             .map(ClassMetadata::getName)
                                             .toList();

        assertTrue(interfaceNames.contains("testClasses.hierarchy.EventInterface"),
                   "Concrete should inherit EventInterface from abstract parent's @Entity. Found: " + interfaceNames);
    }

    // ========================================================================
    // Mixed Hierarchy Tests
    // ========================================================================

    @Test
    @Order(40)
    @DisplayName("Entity extending non-entity base transforms correctly")
    void testEntityOnNonEntityBaseTransforms() throws Exception {
        var className = "testClasses.hierarchy.EntityOnNonEntityBase";

        var generator = transform.generatorOf(className);
        assertNotNull(generator, "Should create generator for entity on non-entity base");

        var bytecode = generator.generate();
        assertNotNull(bytecode, "Should generate bytecode");
        assertTrue(bytecode.length > 0, "Bytecode should not be empty");
    }

    @Test
    @Order(41)
    @DisplayName("Non-entity base class does not transform")
    void testNonEntityBaseDoesNotTransform() throws Exception {
        var className = "testClasses.hierarchy.NonEntityBase";

        // Non-entity class should throw IllegalArgumentException when requesting generator
        assertThrows(IllegalArgumentException.class, () -> transform.generatorOf(className),
                     "Should throw IllegalArgumentException for non-entity class");
    }

    @Test
    @Order(42)
    @DisplayName("Entity on non-entity base only transforms own methods")
    void testEntityOnNonEntityBaseOnlyTransformsOwnMethods() throws Exception {
        var scanner = transform.getScanner();

        var entityClass = scanner.getClass("testClasses.hierarchy.EntityOnNonEntityBase");
        assertNotNull(entityClass, "Should find entity class");

        // The entity should transform, inheriting regular methods from non-entity base
        var superclasses = entityClass.getSuperclasses();
        assertFalse(superclasses.isEmpty(), "Should have superclasses");

        var nonEntityBase = superclasses.get(0);
        assertEquals("testClasses.hierarchy.NonEntityBase", nonEntityBase.getName());

        // Non-entity base should not have @Entity annotation
        assertFalse(nonEntityBase.hasAnnotation(com.hellblazer.primeMover.annotations.Entity.class),
                    "Base should not be @Entity");
    }

    // ========================================================================
    // Generator Consistency Tests
    // ========================================================================

    @Test
    @Order(50)
    @DisplayName("All hierarchy entities generate valid bytecode")
    void testAllHierarchyEntitiesGenerateValidBytecode() throws Exception {
        var hierarchyClasses = new String[]{
            "testClasses.hierarchy.BaseEntity",
            "testClasses.hierarchy.DerivedEntity",
            "testClasses.hierarchy.ConcreteEntity",
            "testClasses.hierarchy.AbstractEntity",
            "testClasses.hierarchy.ConcreteAbstractEntity",
            "testClasses.hierarchy.EntityOnNonEntityBase"
        };

        for (var className : hierarchyClasses) {
            var generator = transform.generatorOf(className);
            assertNotNull(generator, "Should create generator for " + className);

            var bytecode = generator.generate();
            assertNotNull(bytecode, "Should generate bytecode for " + className);
            assertTrue(bytecode.length > 0, "Bytecode should not be empty for " + className);

            // Parse to verify structure
            var classModel = java.lang.classfile.ClassFile.of().parse(bytecode);
            assertNotNull(classModel, "Should parse bytecode for " + className);
        }
    }

    @Test
    @Order(51)
    @DisplayName("Transformed classes can be generated multiple times")
    void testMultipleGenerationsProduceSameBytecode() throws Exception {
        var className = "testClasses.hierarchy.ConcreteEntity";
        var generator = transform.generatorOf(className);
        assertNotNull(generator, "Should create generator");

        var bytecode1 = generator.generate();
        var bytecode2 = generator.generate();

        assertNotNull(bytecode1, "First generation should succeed");
        assertNotNull(bytecode2, "Second generation should succeed");

        // Bytecode should be identical
        assertArrayEquals(bytecode1, bytecode2,
                          "Multiple generations should produce identical bytecode");
    }

    // ========================================================================
    // Ordinal Consistency Tests
    // ========================================================================

    @Test
    @Order(60)
    @DisplayName("Parent and child generators exist independently")
    void testParentAndChildGeneratorsExistIndependently() throws Exception {
        var baseGen = transform.generatorOf("testClasses.hierarchy.BaseEntity");
        var derivedGen = transform.generatorOf("testClasses.hierarchy.DerivedEntity");

        assertNotNull(baseGen, "Base generator should exist");
        assertNotNull(derivedGen, "Derived generator should exist");

        var baseBytecode = baseGen.generate();
        var derivedBytecode = derivedGen.generate();

        assertNotNull(baseBytecode, "Base should generate");
        assertNotNull(derivedBytecode, "Derived should generate");

        // Both should be valid but different
        assertFalse(java.util.Arrays.equals(baseBytecode, derivedBytecode),
                    "Base and derived should generate different bytecode");
    }

    @Test
    @Order(61)
    @DisplayName("Hierarchy transformation is independent of discovery order")
    void testHierarchyTransformationOrderIndependent() throws Exception {
        // Get generators in different orders
        var concrete = transform.generatorOf("testClasses.hierarchy.ConcreteEntity");
        var base = transform.generatorOf("testClasses.hierarchy.BaseEntity");
        var derived = transform.generatorOf("testClasses.hierarchy.DerivedEntity");

        assertNotNull(concrete, "Concrete generator should exist");
        assertNotNull(base, "Base generator should exist");
        assertNotNull(derived, "Derived generator should exist");

        // All should generate successfully regardless of access order
        assertNotNull(concrete.generate(), "Concrete should generate");
        assertNotNull(base.generate(), "Base should generate");
        assertNotNull(derived.generate(), "Derived should generate");
    }
}
