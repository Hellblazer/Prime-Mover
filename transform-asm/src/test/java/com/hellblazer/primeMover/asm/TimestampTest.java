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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.classgraph.ClassGraph;

/**
 * Test that verifies timestamp control functionality in SimulationTransformOriginal classes.
 * 
 * @author hal.hildebrand
 */
public class TimestampTest {

    @Test
    public void testTimestampControl() throws Exception {
        // Test original SimulationTransformOriginal
        try (var transform = new SimulationTransformOriginal(new ClassGraph().acceptPackages("testClasses"))) {
            var originalTimestamp = transform.getTransformTimestamp();
            
            // Set a custom timestamp
            var customTimestamp = "2023-12-25T10:15:30Z";
            transform.setTransformTimestamp(customTimestamp);
            
            // Verify it was set correctly
            assertEquals(customTimestamp, transform.getTransformTimestamp());
        }
        
        // Test refactored SimulationTransformOriginal
        try (var transform = new SimulationTransformRefactored(new ClassGraph().acceptPackages("testClasses"))) {
            var originalTimestamp = transform.getTransformTimestamp();
            
            // Set a custom timestamp
            var customTimestamp = "2023-12-25T10:15:30Z";
            transform.setTransformTimestamp(customTimestamp);
            
            // Verify it was set correctly
            assertEquals(customTimestamp, transform.getTransformTimestamp());
        }
    }
}
