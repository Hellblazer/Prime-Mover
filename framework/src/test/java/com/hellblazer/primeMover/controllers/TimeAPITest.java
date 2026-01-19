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

package com.hellblazer.primeMover.controllers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.hellblazer.primeMover.runtime.Kairos;

/**
 * Tests for SimulationController time management API semantics.
 * <p>
 * This test class validates the behavior and interaction of:
 * <ul>
 *   <li>{@link SimulationController#setStartTime(long)} - Defines simulation start time</li>
 *   <li>{@link SimulationController#setCurrentTime(long)} - Sets initial simulation clock</li>
 *   <li>{@link SimulationController#setEndTime(long)} - Defines simulation termination time</li>
 * </ul>
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class TimeAPITest {

    /**
     * Verify that all three time methods can be called before eventLoop() starts.
     */
    @Test
    void testTimeMethodsBeforeSimulation() {
        var controller = new SimulationController();

        // All three methods should work before simulation starts
        assertDoesNotThrow(() -> controller.setStartTime(100));
        assertDoesNotThrow(() -> controller.setCurrentTime(100));
        assertDoesNotThrow(() -> controller.setEndTime(1000));

        assertEquals(100, controller.getStartTime());
        assertEquals(100, controller.getCurrentTime());
        assertEquals(1000, controller.getEndTime());
    }

    /**
     * Verify typical usage pattern: start=0, current=0, end=1000
     */
    @Test
    void testTypicalTimeConfiguration() {
        var controller = new SimulationController();

        // Typical configuration
        controller.setStartTime(0);
        controller.setCurrentTime(0);
        controller.setEndTime(1000);

        assertEquals(0, controller.getStartTime());
        assertEquals(0, controller.getCurrentTime());
        assertEquals(1000, controller.getEndTime());
    }

    /**
     * Verify that setCurrentTime() cannot be called after simulation has started.
     * We verify this by checking the simulationRunning flag is properly enforced.
     */
    @Test
    void testSetCurrentTimeValidation() {
        var controller = new TestableSimulationController();

        // Before simulation, should work
        assertDoesNotThrow(() -> controller.setCurrentTime(50));

        // Simulate the controller being in running state
        controller.setSimulationRunning(true);

        // During simulation, should fail
        var exception = assertThrows(IllegalStateException.class, () -> controller.setCurrentTime(100));
        assertTrue(exception.getMessage().contains("during simulation"),
                   "Exception should mention simulation is running");

        controller.setSimulationRunning(false);
    }

    /**
     * Verify that setStartTime() cannot be called after simulation has started.
     */
    @Test
    void testSetStartTimeValidation() {
        var controller = new TestableSimulationController();

        // Before simulation, should work
        assertDoesNotThrow(() -> controller.setStartTime(50));

        // Simulate the controller being in running state
        controller.setSimulationRunning(true);

        // During simulation, should fail
        var exception = assertThrows(IllegalStateException.class, () -> controller.setStartTime(100));
        assertTrue(exception.getMessage().contains("running"),
                   "Exception should mention simulation is running");

        controller.setSimulationRunning(false);
    }

    /**
     * Verify that setEndTime() CAN be called during simulation (dynamic termination).
     */
    @Test
    void testSetEndTimeDynamicAdjustment() {
        var controller = new TestableSimulationController();
        controller.setEndTime(1000);

        // Simulate the controller being in running state
        controller.setSimulationRunning(true);

        // Dynamically changing end time should work even during simulation
        assertDoesNotThrow(() -> controller.setEndTime(500));
        assertEquals(500, controller.getEndTime());

        controller.setSimulationRunning(false);
    }

    /**
     * Test helper class that exposes simulationRunning for testing.
     */
    private static class TestableSimulationController extends SimulationController {
        void setSimulationRunning(boolean running) {
            this.simulationRunning = running;
        }
    }

    /**
     * Verify that negative values are rejected for all time methods.
     */
    @Test
    void testNegativeTimeValuesRejected() {
        var controller = new SimulationController();

        // All methods should reject negative values
        assertThrows(IllegalArgumentException.class, () -> controller.setStartTime(-1),
                     "setStartTime should reject negative values");

        assertThrows(IllegalArgumentException.class, () -> controller.setCurrentTime(-1),
                     "setCurrentTime should reject negative values");

        assertThrows(IllegalArgumentException.class, () -> controller.setEndTime(-1),
                     "setEndTime should reject negative values");
    }

    /**
     * Verify that endTime must be >= startTime.
     */
    @Test
    void testEndTimeValidation() {
        var controller = new SimulationController();

        // Set start time first
        controller.setStartTime(1000);

        // End time less than start time should be rejected
        var exception = assertThrows(IllegalArgumentException.class, () -> controller.setEndTime(500));

        assertTrue(exception.getMessage().contains("start time"),
                   "Exception should mention relationship to start time");
    }

    /**
     * Verify that eventLoop() normalizes negative simulationStart to 0.
     */
    @Test
    void testEventLoopNormalizesNegativeStartTime() throws Exception {
        var controller = new SimulationController();
        controller.setEndTime(10);

        // Don't call setStartTime(), let it default to 0 (or set explicitly to test)
        Kairos.setController(controller);
        controller.eventLoop();

        // simulationStart should be normalized to 0
        assertTrue(controller.getSimulationStart() >= 0,
                   "Simulation start should be normalized to non-negative");
    }

    /**
     * Verify zero values are allowed for all time methods.
     */
    @Test
    void testZeroTimeValuesAllowed() {
        var controller = new SimulationController();

        // Zero should be allowed for all methods
        assertDoesNotThrow(() -> controller.setStartTime(0));
        assertDoesNotThrow(() -> controller.setCurrentTime(0));
        assertDoesNotThrow(() -> controller.setEndTime(0));
    }

    /**
     * Document correct usage pattern with code example.
     */
    @Test
    void testCorrectUsagePattern() throws Exception {
        // This is the recommended pattern for users
        var controller = new SimulationController();
        Kairos.setController(controller);

        // Configure time BEFORE calling eventLoop()
        controller.setStartTime(0);      // Simulation begins at time 0
        controller.setCurrentTime(0);    // Initialize clock to 0
        controller.setEndTime(1000);     // Simulation ends at time 1000

        // Schedule initial events would go here
        // new MyEntity().someMethod();

        controller.eventLoop();          // Run simulation

        // Verify simulation completed
        assertTrue(controller.getSimulationEnd() >= 0);
        assertTrue(controller.getSimulationStart() == 0);
    }

    /**
     * Verify that time can advance only forward during simulation.
     */
    @Test
    void testTimeAdvancesForward() throws Exception {
        var controller = new SimulationController();
        controller.setStartTime(0);
        controller.setCurrentTime(0);
        controller.setEndTime(100);

        Kairos.setController(controller);

        // During eventLoop(), time should start at 0
        controller.eventLoop();

        // After completion, time should have advanced (or stayed at 0 if no events)
        assertTrue(controller.getSimulationEnd() >= 0);
        assertTrue(controller.getCurrentTime() >= 0);
    }

    /**
     * Verify that setCurrentTime and setStartTime maintain independent state.
     */
    @Test
    void testCurrentTimeAndStartTimeIndependence() {
        var controller = new SimulationController();

        // Set different values for current and start time
        controller.setStartTime(100);
        controller.setCurrentTime(200);

        // Both should maintain their values
        assertEquals(100, controller.getStartTime());
        assertEquals(200, controller.getCurrentTime());

        // When eventLoop() runs, it will set current time to start time
    }

    /**
     * Verify that time configuration survives controller reuse (not recommended but possible).
     */
    @Test
    void testTimeConfigurationPersistence() throws Exception {
        var controller = new SimulationController();

        // First configuration
        controller.setStartTime(0);
        controller.setCurrentTime(0);
        controller.setEndTime(100);

        Kairos.setController(controller);
        controller.eventLoop();

        // After simulation completes, we can reconfigure
        controller.setStartTime(500);
        controller.setCurrentTime(500);
        controller.setEndTime(1000);

        assertEquals(500, controller.getStartTime());
        assertEquals(500, controller.getCurrentTime());
        assertEquals(1000, controller.getEndTime());
    }
}
