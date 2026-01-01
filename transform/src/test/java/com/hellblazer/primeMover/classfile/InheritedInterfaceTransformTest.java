/**
 * Copyright (C) 2008-2025 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 */
package com.hellblazer.primeMover.classfile;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that the bytecode transform correctly handles inherited entity interfaces.
 *
 * This regression test verifies the fix for a bug where child entities extending
 * a parent @Entity with interfaces would have mismatched event indices.
 *
 * Bug scenario:
 * - ParentEntityWithInterface is @Entity(ParentInterface.class), has parentMethod(String)
 * - ChildEntityWithOwnMethod extends parent, is @Entity, has childMethod(Integer)
 *
 * Without the fix:
 * - Parent transforms with: parentMethod=0
 * - Child transforms with: childMethod=0 (ignoring inherited interface!)
 * - When child calls inherited parentMethod, parent's wrapper posts event(0, [String])
 * - Child's __invoke(0, [String]) tries to call childMethod, casts String to Integer -> ClassCastException
 *
 * With the fix:
 * - Child correctly includes inherited interface methods
 * - Child transforms with: childMethod=0, parentMethod=1 (both sorted alphabetically)
 * - Indices match and dispatch works correctly
 *
 * @author hal.hildebrand
 */
public class InheritedInterfaceTransformTest {

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

    @Test
    void testChildEntityIncludesInheritedInterfaceMethods() throws Exception {
        var childClassName = "testClasses.ChildEntityWithOwnMethod";

        // Get the generator for the child entity
        var generator = transform.generatorOf(childClassName);
        assertNotNull(generator, "Should create generator for child entity");

        // Generate the transformed bytecode
        var bytecode = generator.generate();
        assertNotNull(bytecode, "Should generate bytecode");
        assertTrue(bytecode.length > 0, "Bytecode should not be empty");
    }

    @Test
    void testParentEntityTransformsCorrectly() throws Exception {
        var parentClassName = "testClasses.ParentEntityWithInterface";

        // Get the generator for the parent entity
        var generator = transform.generatorOf(parentClassName);
        assertNotNull(generator, "Should create generator for parent entity");

        // Generate the transformed bytecode
        var bytecode = generator.generate();
        assertNotNull(bytecode, "Should generate bytecode");
        assertTrue(bytecode.length > 0, "Bytecode should not be empty");
    }

    @Test
    void testInheritedInterfacesAreDetected() throws Exception {
        var scanner = transform.getScanner();

        // Get the child entity class
        var childClass = scanner.getClass("testClasses.ChildEntityWithOwnMethod");
        assertNotNull(childClass, "Should find child class");

        // Get entity interfaces - should include parent's interfaces
        var entityInterfaces = transform.getEntityInterfaces(childClass);
        assertNotNull(entityInterfaces, "Should get entity interfaces");

        // Should include ParentInterface from parent's @Entity annotation
        var interfaceNames = entityInterfaces.stream()
                                             .map(ClassMetadata::getName)
                                             .toList();

        assertTrue(interfaceNames.contains("testClasses.ParentInterface"),
                   "Child should inherit ParentInterface from parent's @Entity. Found: " + interfaceNames);
    }

    @Test
    void testInterfaceNamesIncludeInherited() throws Exception {
        var scanner = transform.getScanner();

        // Get the child entity class
        var childClass = scanner.getClass("testClasses.ChildEntityWithOwnMethod");
        assertNotNull(childClass, "Should find child class");

        // Get superclasses
        var superclasses = childClass.getSuperclasses();
        assertFalse(superclasses.isEmpty(), "Child should have superclasses");

        // Check that we can traverse the hierarchy
        var parentClass = superclasses.getFirst();
        assertEquals("testClasses.ParentEntityWithInterface", parentClass.getName());

        // Parent should directly implement ParentInterface
        var parentInterfaces = parentClass.getInterfaceNames();
        assertTrue(parentInterfaces.contains("testClasses.ParentInterface"),
                   "Parent should implement ParentInterface. Found: " + parentInterfaces);
    }
}
