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

import com.hellblazer.primeMover.classfile.OpenAddressingSet.OpenSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for OpenAddressingSet.clone() method.
 * Validates Issue 1.3: Proper type casting in clone() method.
 *
 * @author hal.hildebrand
 */
class OpenAddressingSetCloneTest {

    /**
     * Test that OpenSet.clone() returns the correct type.
     */
    @Test
    void testOpenSetClone() {
        var original = new OpenSet<String>();
        original.add("one");
        original.add("two");
        original.add("three");

        var cloned = (OpenSet<String>) original.clone();

        assertNotNull(cloned, "Cloned set should not be null");
        assertNotSame(original, cloned, "Cloned set should be a different instance");
        assertEquals(original.size(), cloned.size(), "Cloned set should have same size");
        assertTrue(cloned.contains("one"), "Cloned set should contain 'one'");
        assertTrue(cloned.contains("two"), "Cloned set should contain 'two'");
        assertTrue(cloned.contains("three"), "Cloned set should contain 'three'");

        // Verify independence - modifying clone shouldn't affect original
        cloned.add("four");
        assertFalse(original.contains("four"), "Original should not contain element added to clone");
        assertEquals(3, original.size(), "Original size should remain unchanged");
        assertEquals(4, cloned.size(), "Cloned size should reflect new element");
    }

    /**
     * Test that IdentitySet.clone() returns the correct type.
     */
    @Test
    void testIdentitySetClone() {
        var obj1 = new Object();
        var obj2 = new Object();
        var obj3 = new Object();

        var original = new IdentitySet<Object>();
        original.add(obj1);
        original.add(obj2);
        original.add(obj3);

        var cloned = (IdentitySet<Object>) original.clone();

        assertNotNull(cloned, "Cloned set should not be null");
        assertNotSame(original, cloned, "Cloned set should be a different instance");
        assertEquals(original.size(), cloned.size(), "Cloned set should have same size");
        assertTrue(cloned.contains(obj1), "Cloned set should contain obj1");
        assertTrue(cloned.contains(obj2), "Cloned set should contain obj2");
        assertTrue(cloned.contains(obj3), "Cloned set should contain obj3");

        // Verify independence
        var obj4 = new Object();
        cloned.add(obj4);
        assertFalse(original.contains(obj4), "Original should not contain element added to clone");
    }

    /**
     * Test cloning an empty set.
     */
    @Test
    void testCloneEmptySet() {
        var original = new OpenSet<String>();
        var cloned = (OpenSet<String>) original.clone();

        assertNotNull(cloned, "Cloned empty set should not be null");
        assertNotSame(original, cloned, "Cloned set should be a different instance");
        assertEquals(0, cloned.size(), "Cloned empty set should have size 0");
        assertTrue(cloned.isEmpty(), "Cloned set should be empty");
    }

    /**
     * Test that table array is properly copied (deep copy of array, shallow copy of elements).
     */
    @Test
    void testTableArrayIndependence() {
        var original = new OpenSet<String>();
        original.add("alpha");
        original.add("beta");

        var cloned = (OpenSet<String>) original.clone();

        // Remove from original
        original.remove("alpha");

        // Cloned should still contain the removed element
        assertTrue(cloned.contains("alpha"), "Cloned set should still contain removed element");
        assertEquals(1, original.size(), "Original should have size 1 after removal");
        assertEquals(2, cloned.size(), "Cloned should maintain size 2");
    }

    /**
     * Test cloning with various set sizes to ensure rehashing scenarios work.
     */
    @Test
    void testCloneWithVariousSizes() {
        for (int size : new int[]{1, 5, 10, 50, 100}) {
            var original = new OpenSet<Integer>();
            for (int i = 0; i < size; i++) {
                original.add(i);
            }

            var cloned = (OpenSet<Integer>) original.clone();

            assertEquals(size, cloned.size(), "Cloned set should have size " + size);
            for (int i = 0; i < size; i++) {
                assertTrue(cloned.contains(i), "Cloned set should contain " + i);
            }
        }
    }
}
