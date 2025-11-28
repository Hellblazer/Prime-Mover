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

import io.github.classgraph.ClassGraph;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for input validation in SimulationTransform public APIs.
 * Validates Issue 1.4: Input validation on public APIs.
 *
 * @author hal.hildebrand
 */
class SimulationTransformInputValidationTest {

    private SimulationTransform transform;

    @BeforeEach
    void setUp() {
        var graph = new ClassGraph().acceptPackages("com.hellblazer.primeMover");
        transform = new SimulationTransform(graph);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (transform != null) {
            transform.close();
        }
    }

    /**
     * Test that generatorOf throws NullPointerException for null classname.
     */
    @Test
    void testGeneratorOfNullClassname() {
        assertThrows(NullPointerException.class, () -> {
            transform.generatorOf(null);
        }, "Should throw NullPointerException for null classname");
    }

    /**
     * Test that generatorOf throws IllegalArgumentException for empty classname.
     */
    @Test
    void testGeneratorOfEmptyClassname() {
        assertThrows(IllegalArgumentException.class, () -> {
            transform.generatorOf("");
        }, "Should throw IllegalArgumentException for empty classname");
    }

    /**
     * Test that generatorOf throws IllegalArgumentException for non-existent class.
     */
    @Test
    void testGeneratorOfNonExistentClass() {
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            transform.generatorOf("com.example.NonExistentClass");
        }, "Should throw IllegalArgumentException for non-existent class");

        assertTrue(exception.getMessage().contains("Entity class not found"),
                   "Error message should indicate class not found");
        assertTrue(exception.getMessage().contains("com.example.NonExistentClass"),
                   "Error message should include the class name");
    }

    /**
     * Test that generatorOf with selector throws NullPointerException for null classname.
     */
    @Test
    void testGeneratorOfWithSelectorNullClassname() {
        assertThrows(NullPointerException.class, () -> {
            transform.generatorOf(null, classInfo -> true);
        }, "Should throw NullPointerException for null classname with selector");
    }

    /**
     * Test that generatorOf with selector throws IllegalArgumentException for empty classname.
     */
    @Test
    void testGeneratorOfWithSelectorEmptyClassname() {
        assertThrows(IllegalArgumentException.class, () -> {
            transform.generatorOf("", classInfo -> true);
        }, "Should throw IllegalArgumentException for empty classname with selector");
    }

    /**
     * Test that generatorOf with selector throws IllegalArgumentException for non-existent class.
     */
    @Test
    void testGeneratorOfWithSelectorNonExistentClass() {
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            transform.generatorOf("com.example.AnotherNonExistentClass", classInfo -> true);
        }, "Should throw IllegalArgumentException for non-existent class with selector");

        assertTrue(exception.getMessage().contains("Entity class not found"),
                   "Error message should indicate class not found");
    }

    /**
     * Test that generatorOf throws exception for class filtered out by selector.
     */
    @Test
    void testGeneratorOfFilteredBySelector() {
        // This tests that filtering works correctly - should throw exception when filtered out
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            transform.generatorOf("com.hellblazer.primeMover.runtime.Framework",
                                  classInfo -> false);
        }, "Should throw IllegalArgumentException for class filtered out by selector");

        assertTrue(exception.getMessage().contains("Entity class not found"),
                   "Error message should indicate class not found");
    }

    /**
     * Test validation with whitespace-only classname.
     */
    @Test
    void testGeneratorOfWhitespaceClassname() {
        assertThrows(IllegalArgumentException.class, () -> {
            transform.generatorOf("   ");
        }, "Should throw IllegalArgumentException for whitespace-only classname");
    }
}
