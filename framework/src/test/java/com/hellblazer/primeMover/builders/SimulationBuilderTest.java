/**
 * Copyright (C) 2008 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.primeMover.builders;

import com.hellblazer.primeMover.controllers.RealTimeController;
import com.hellblazer.primeMover.controllers.SimulationController;
import com.hellblazer.primeMover.controllers.StatisticalController;
import com.hellblazer.primeMover.controllers.SteppingController;
import com.hellblazer.primeMover.runtime.Devi;
import com.hellblazer.primeMover.runtime.Kairos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SimulationBuilder fluent API.
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class SimulationBuilderTest {

    @AfterEach
    public void cleanup() {
        // Clean up thread-local controller
        Kairos.setController(null);
    }

    @Test
    public void testDefaultBuild() {
        var controller = SimulationBuilder.builder().build();

        assertNotNull(controller);
        assertTrue(controller instanceof SimulationController);
        assertEquals(0L, controller.getCurrentTime());

        // Verify Kronos is set up
        assertNotNull(Kairos.queryController());
        assertEquals(controller, Kairos.queryController());
    }

    @Test
    public void testEndTime() {
        var controller = SimulationBuilder.builder().endTime(1000).build();

        assertTrue(controller instanceof SimulationController);
        var simController = (SimulationController) controller;
        assertEquals(1000, simController.getEndTime());
    }

    @Test
    public void testTrackSpectrum() {
        var controller = SimulationBuilder.builder().trackSpectrum(true).build();

        assertTrue(controller instanceof StatisticalController);
        var statController = (StatisticalController) controller;
        assertTrue(((SimulationController) statController).isTrackSpectrum());
    }

    @Test
    public void testTrackEventSources() {
        var controller = SimulationBuilder.builder().trackEventSources(true).build();

        assertNotNull(controller);
        assertTrue(controller.isTrackEventSources());
    }

    @Test
    public void testDebugEvents() {
        var controller = SimulationBuilder.builder().debugEvents(true).build();

        assertNotNull(controller);
        assertTrue(controller.isDebugEvents());
    }

    @Test
    public void testRealTimeControllerType() {
        var controller = SimulationBuilder.builder()
                                          .controllerType(RealTimeController.class)
                                          .name("Test Real-Time")
                                          .build();

        assertTrue(controller instanceof RealTimeController);
        var rtController = (RealTimeController) controller;
        assertEquals("Test Real-Time", rtController.getName());
    }

    @Test
    public void testSteppingControllerType() {
        var controller = SimulationBuilder.builder().controllerType(SteppingController.class).build();

        assertTrue(controller instanceof SteppingController);
    }

    @Test
    public void testFluentChaining() {
        var controller = SimulationBuilder.builder()
                                          .endTime(5000)
                                          .trackSpectrum(true)
                                          .trackEventSources(false)
                                          .debugEvents(false)
                                          .build();

        assertNotNull(controller);
        assertTrue(controller instanceof SimulationController);
        var simController = (SimulationController) controller;
        assertEquals(5000, simController.getEndTime());
        assertTrue(simController.isTrackSpectrum());
        assertFalse(controller.isTrackEventSources());
        assertFalse(controller.isDebugEvents());
    }

    @Test
    public void testName() {
        var controller = SimulationBuilder.builder().name("My Simulation").build();

        assertTrue(controller instanceof SimulationController);
        var simController = (SimulationController) controller;
        assertEquals("My Simulation", simController.getName());
    }

    @Test
    public void testDefaultsAreSensible() {
        var controller = SimulationBuilder.builder().build();

        assertTrue(controller instanceof SimulationController);
        var simController = (SimulationController) controller;

        // Default values should work for most use cases
        assertEquals(Long.MAX_VALUE, simController.getEndTime());
        assertTrue(simController.isTrackSpectrum()); // Enable by default for demos
        assertFalse(controller.isTrackEventSources()); // Expensive, off by default
        assertFalse(controller.isDebugEvents()); // Very expensive, off by default
    }

    @Test
    public void testMultipleBuildsAreIndependent() {
        var controller1 = SimulationBuilder.builder().endTime(100).build();
        var controller2 = SimulationBuilder.builder().endTime(200).build();

        assertTrue(controller1 instanceof SimulationController);
        assertTrue(controller2 instanceof SimulationController);

        var sim1 = (SimulationController) controller1;
        var sim2 = (SimulationController) controller2;

        assertEquals(100, sim1.getEndTime());
        assertEquals(200, sim2.getEndTime());
    }

    @Test
    public void testControllerTypeFailsGracefullyWithInvalidType() {
        // Test that specifying an invalid controller type handles the error
        assertThrows(IllegalArgumentException.class, () -> {
            SimulationBuilder.builder().controllerType(Devi.class).build();
        });
    }

    @Test
    public void testStartTime() {
        var controller = SimulationBuilder.builder().startTime(100).build();

        assertTrue(controller instanceof SimulationController);
        var simController = (SimulationController) controller;
        assertEquals(100, simController.getStartTime());
    }
}
