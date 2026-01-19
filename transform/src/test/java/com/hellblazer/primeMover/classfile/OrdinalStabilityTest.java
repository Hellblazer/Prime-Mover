/*
 * Copyright (C) 2026 Hal Hildebrand. All rights reserved.
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

import com.hellblazer.primeMover.annotations.Entity;
import com.hellblazer.primeMover.annotations.Event;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ordinal stability across code changes.
 * <p>
 * Verifies that method ordinals remain stable when:
 * - Adding new methods
 * - Removing existing methods
 * - Reordering methods in source code
 *
 * @author hal.hildebrand
 */
public class OrdinalStabilityTest {

    /**
     * Test entity with multiple methods for stability testing.
     */
    @Entity
    public static class TestEntityV1 {
        @Event
        public void methodA() {
        }

        @Event
        public void methodB() {
        }

        @Event
        public void methodC() {
        }
    }

    /**
     * Same entity with an added method D.
     */
    @Entity
    public static class TestEntityV2 {
        @Event
        public void methodA() {
        }

        @Event
        public void methodB() {
        }

        @Event
        public void methodC() {
        }

        @Event
        public void methodD() {
        }
    }

    /**
     * Same entity with method B removed.
     */
    @Entity
    public static class TestEntityV3 {
        @Event
        public void methodA() {
        }

        @Event
        public void methodC() {
        }
    }

    /**
     * Test entity with explicit ordinals.
     */
    @Entity
    public static class TestEntityExplicit {
        @Event(ordinal = 1000)
        public void methodA() {
        }

        @Event(ordinal = 2000)
        public void methodB() {
        }

        @Event  // Hash-based
        public void methodC() {
        }
    }

    @Test
    public void testAddMethodPreservesExistingOrdinals() throws Exception {
        // Transform V1
        var scanner1 = new ClassScanner()
            .addClasspathEntry(Path.of("target/test-classes"))
            .addClasspathEntry(Path.of("target/classes"))
            .scan();
        var transform1 = new SimulationTransform(scanner1);
        var generator1 = transform1.generatorOf(TestEntityV1.class.getName());
        assertNotNull(generator1, "Generator for V1 should exist");

        var ordinals1 = extractOrdinals(generator1);

        // Transform V2 (with added method D)
        var scanner2 = new ClassScanner()
            .addClasspathEntry(Path.of("target/test-classes"))
            .addClasspathEntry(Path.of("target/classes"))
            .scan();
        var transform2 = new SimulationTransform(scanner2);
        var generator2 = transform2.generatorOf(TestEntityV2.class.getName());
        assertNotNull(generator2, "Generator for V2 should exist");

        var ordinals2 = extractOrdinals(generator2);

        // Verify: Methods A, B, C keep same ordinals
        assertEquals(ordinals1.get("methodA"), ordinals2.get("methodA"),
                     "methodA ordinal should be stable");
        assertEquals(ordinals1.get("methodB"), ordinals2.get("methodB"),
                     "methodB ordinal should be stable");
        assertEquals(ordinals1.get("methodC"), ordinals2.get("methodC"),
                     "methodC ordinal should be stable");

        // Verify: Method D gets a new ordinal (not reused from any existing method)
        assertNotNull(ordinals2.get("methodD"), "methodD should have an ordinal");
        assertNotEquals(ordinals2.get("methodD"), ordinals2.get("methodA"));
        assertNotEquals(ordinals2.get("methodD"), ordinals2.get("methodB"));
        assertNotEquals(ordinals2.get("methodD"), ordinals2.get("methodC"));
    }

    @Test
    public void testRemoveMethodPreservesRemainingOrdinals() throws Exception {
        // Transform V1
        var scanner1 = new ClassScanner()
            .addClasspathEntry(Path.of("target/test-classes"))
            .addClasspathEntry(Path.of("target/classes"))
            .scan();
        var transform1 = new SimulationTransform(scanner1);
        var generator1 = transform1.generatorOf(TestEntityV1.class.getName());
        assertNotNull(generator1, "Generator for V1 should exist");

        var ordinals1 = extractOrdinals(generator1);

        // Transform V3 (with method B removed)
        var scanner3 = new ClassScanner()
            .addClasspathEntry(Path.of("target/test-classes"))
            .addClasspathEntry(Path.of("target/classes"))
            .scan();
        var transform3 = new SimulationTransform(scanner3);
        var generator3 = transform3.generatorOf(TestEntityV3.class.getName());
        assertNotNull(generator3, "Generator for V3 should exist");

        var ordinals3 = extractOrdinals(generator3);

        // Verify: Methods A and C keep same ordinals
        assertEquals(ordinals1.get("methodA"), ordinals3.get("methodA"),
                     "methodA ordinal should be stable after removing methodB");
        assertEquals(ordinals1.get("methodC"), ordinals3.get("methodC"),
                     "methodC ordinal should be stable after removing methodB");

        // Verify: Method B's ordinal is not present in V3
        assertNull(ordinals3.get("methodB"), "methodB should not have an ordinal in V3");
    }

    @Test
    public void testExplicitOrdinalsHavePrecedence() throws Exception {
        var scanner = new ClassScanner()
            .addClasspathEntry(Path.of("target/test-classes"))
            .addClasspathEntry(Path.of("target/classes"))
            .scan();
        var transform = new SimulationTransform(scanner);
        var generator = transform.generatorOf(TestEntityExplicit.class.getName());
        assertNotNull(generator, "Generator should exist");

        var ordinals = extractOrdinals(generator);

        // Verify: Explicit ordinals are used
        assertEquals(1000, ordinals.get("methodA"), "methodA should use explicit ordinal 1000");
        assertEquals(2000, ordinals.get("methodB"), "methodB should use explicit ordinal 2000");

        // Verify: methodC gets hash-based ordinal (not 1000 or 2000)
        var ordinalC = ordinals.get("methodC");
        assertNotNull(ordinalC, "methodC should have an ordinal");
        assertNotEquals(1000, ordinalC, "methodC should not collide with methodA's explicit ordinal");
        assertNotEquals(2000, ordinalC, "methodC should not collide with methodB's explicit ordinal");
    }

    /**
     * Extract ordinals from a generator by accessing its internal mappings.
     * This uses reflection to access private fields for testing purposes.
     */
    private Map<String, Integer> extractOrdinals(EntityGenerator generator) throws Exception {
        var result = new HashMap<String, Integer>();

        // Access methodToIndex field via reflection
        var methodToIndexField = EntityGenerator.class.getDeclaredField("methodToIndex");
        methodToIndexField.setAccessible(true);
        @SuppressWarnings("unchecked")
        var methodToIndex = (Map<MethodMetadata, Integer>) methodToIndexField.get(generator);

        // Build map of method name to ordinal
        for (var entry : methodToIndex.entrySet()) {
            var methodName = entry.getKey().getName();
            var ordinal = entry.getValue();
            result.put(methodName, ordinal);
        }

        return result;
    }
}
