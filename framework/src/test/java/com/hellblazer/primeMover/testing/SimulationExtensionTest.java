/**
 * Copyright (C) 2008 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.primeMover.testing;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.hellblazer.primeMover.Simulation;
import com.hellblazer.primeMover.controllers.SimulationController;

/**
 * Tests for SimulationExtension - JUnit 5 extension for simulation testing.
 * These tests verify that the extension properly manages simulation lifecycle
 * and parameter injection.
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class SimulationExtensionTest {

    // ========== Parameter Injection Tests ==========

    @Test
    @ExtendWith(SimulationExtension.class)
    void testInjectsSimulation(Simulation simulation) {
        assertNotNull(simulation, "Simulation should be injected");
        assertNotNull(simulation.controller(), "Simulation should have controller");
        assertNotNull(simulation.random(), "Simulation should have random");
    }

    @Test
    @ExtendWith(SimulationExtension.class)
    void testInjectsController(SimulationController controller) {
        assertNotNull(controller, "Controller should be injected");
    }

    @Test
    @ExtendWith(SimulationExtension.class)
    void testInjectsRandom(Random random) {
        assertNotNull(random, "Random should be injected");
        // Verify it produces values
        var value = random.nextDouble();
        assertTrue(value >= 0 && value < 1, "Random should produce valid doubles");
    }

    @Test
    @ExtendWith(SimulationExtension.class)
    void testInjectsMultipleParameters(Simulation simulation, SimulationController controller, Random random) {
        assertNotNull(simulation, "Simulation should be injected");
        assertNotNull(controller, "Controller should be injected");
        assertNotNull(random, "Random should be injected");

        // Verify they're from the same simulation
        assertSame(simulation.controller(), controller, "Controller should be from the same simulation");
        assertSame(simulation.random(), random, "Random should be from the same simulation");
    }

    // ========== Simulation Configuration Tests ==========

    @Test
    @ExtendWith(SimulationExtension.class)
    void testDefaultConfiguration(Simulation simulation) {
        // Default configuration should have reasonable values
        assertNotNull(simulation.name());
        assertTrue(simulation.maxTime() > 0, "MaxTime should be positive");
    }

    @Test
    @ExtendWith(SimulationExtension.class)
    void testControllerIsReady(SimulationController controller) {
        // Controller should be usable
        assertEquals(0, controller.getCurrentTime(), "Controller should start at time 0");
    }

    // ========== Isolation Tests ==========

    private static Simulation lastSimulation = null;
    private static SimulationController lastController = null;

    @Test
    @ExtendWith(SimulationExtension.class)
    void testIsolation_First(Simulation simulation, SimulationController controller) {
        lastSimulation = simulation;
        lastController = controller;
        assertNotNull(simulation);
        assertNotNull(controller);
    }

    @Test
    @ExtendWith(SimulationExtension.class)
    void testIsolation_Second(Simulation simulation, SimulationController controller) {
        // Each test should get its own simulation instance
        if (lastSimulation != null) {
            assertNotSame(lastSimulation, simulation, "Each test should get a new Simulation");
        }
        if (lastController != null) {
            assertNotSame(lastController, controller, "Each test should get a new Controller");
        }
    }

    // ========== Edge Case Tests ==========

    @Test
    @ExtendWith(SimulationExtension.class)
    void testNoParameterRequired() {
        // Extension should work even if no simulation parameters are requested
        assertTrue(true, "Test should run without simulation parameters");
    }

    @Test
    @ExtendWith(SimulationExtension.class)
    void testSimulationCanRunEventLoop(SimulationController controller) {
        // Should be able to run the event loop
        assertDoesNotThrow(() -> controller.eventLoop());
    }
}
