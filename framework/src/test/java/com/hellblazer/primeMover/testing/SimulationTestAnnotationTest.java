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

import com.hellblazer.primeMover.Simulation;
import com.hellblazer.primeMover.controllers.SimulationController;

/**
 * Tests for @SimulationTest meta-annotation.
 * Verifies that @SimulationTest properly enables SimulationExtension
 * and works as a drop-in replacement for @Test + @ExtendWith.
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class SimulationTestAnnotationTest {

    // ========== Basic Functionality Tests ==========

    @SimulationTest
    void testAnnotationEnablesExtension(Simulation simulation) {
        assertNotNull(simulation, "@SimulationTest should enable SimulationExtension");
    }

    @SimulationTest
    void testInjectsController(SimulationController controller) {
        assertNotNull(controller, "@SimulationTest should allow controller injection");
    }

    @SimulationTest
    void testInjectsRandom(Random random) {
        assertNotNull(random, "@SimulationTest should allow random injection");
    }

    @SimulationTest
    void testMultipleParameterInjection(Simulation sim, SimulationController controller, Random random) {
        assertNotNull(sim);
        assertNotNull(controller);
        assertNotNull(random);
        assertSame(sim.controller(), controller, "Parameters should come from same simulation");
        assertSame(sim.random(), random, "Parameters should come from same simulation");
    }

    // ========== Test Execution Tests ==========

    @SimulationTest
    void testActsAsTest() {
        // This test simply verifies that @SimulationTest methods are executed as tests
        assertTrue(true, "This test should run");
    }

    @SimulationTest
    void testControllerIsUsable(SimulationController controller) {
        // Verify controller is ready to use
        assertEquals(0, controller.getCurrentTime(), "Controller should start at time 0");
        assertDoesNotThrow(() -> controller.eventLoop(), "Event loop should be runnable");
    }

    // ========== Isolation Tests ==========

    private static SimulationController firstController = null;

    @SimulationTest
    void testIsolation_recordFirst(SimulationController controller) {
        firstController = controller;
        assertNotNull(controller);
    }

    @SimulationTest
    void testIsolation_verifyDifferent(SimulationController controller) {
        if (firstController != null) {
            assertNotSame(firstController, controller, "Each @SimulationTest should get a new controller");
        }
    }
}
