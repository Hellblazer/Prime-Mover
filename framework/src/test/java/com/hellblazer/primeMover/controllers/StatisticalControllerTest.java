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

/**
 * Tests for StatisticalController implementations across all controller types
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class StatisticalControllerTest {

    @Test
    void testSimulationControllerImplementsStatisticalController() throws Exception {
        try (var controller = new SimulationController()) {
            assertInstanceOf(StatisticalController.class, controller);

            controller.setName("Test Sim");
            controller.eventLoop();

            assertEquals("Test Sim", controller.getName());
            assertNotNull(controller.getSpectrum());
            assertTrue(controller.getTotalEvents() >= 0);
            assertTrue(controller.getSimulationEnd() >= controller.getSimulationStart());

            var report = controller.report();
            assertNotNull(report);
            assertEquals("Test Sim", report.name());
        }
    }

    @Test
    void testSteppingControllerImplementsStatisticalController() throws Exception {
        try (var controller = new SteppingController()) {
            assertInstanceOf(StatisticalController.class, controller);

            controller.step();

            assertNotNull(controller.getSpectrum());
            assertTrue(controller.getTotalEvents() >= 0);
            assertNotNull(controller.getName());

            var report = controller.report();
            assertNotNull(report);
        }
    }

    @Test
    void testRealTimeControllerImplementsStatisticalController() throws Exception {
        try (var controller = new RealTimeController("Test RTC")) {
            assertInstanceOf(StatisticalController.class, controller);

            // Start and immediately stop - just verify interface is implemented
            controller.start();
            Thread.sleep(10);
            controller.stop();

            assertNotNull(controller.getSpectrum());
            assertTrue(controller.getTotalEvents() >= 0);
            assertEquals("Test RTC", controller.getName());

            var report = controller.report();
            assertNotNull(report);
            assertEquals("Test RTC", report.name());
        }
    }

    @Test
    void testSteppingControllerSpectrumTracking() throws Exception {
        try (var controller = new SteppingController()) {
            // Verify spectrum is initialized
            assertNotNull(controller.getSpectrum());
            assertTrue(controller.getSpectrum().isEmpty());

            // Spectrum tracking can be enabled/disabled
            controller.setTrackSpectrum(true);
            assertTrue(controller.isTrackSpectrum());

            controller.setTrackSpectrum(false);
            assertFalse(controller.isTrackSpectrum());
        }
    }

    @Test
    void testRealTimeControllerSpectrumTracking() throws Exception {
        try (var controller = new RealTimeController("Spectrum Test")) {
            // Verify spectrum is initialized
            assertNotNull(controller.getSpectrum());
            assertTrue(controller.getSpectrum().isEmpty());

            // Spectrum tracking can be enabled/disabled
            controller.setTrackSpectrum(true);
            assertTrue(controller.isTrackSpectrum());

            controller.setTrackSpectrum(false);
            assertFalse(controller.isTrackSpectrum());
        }
    }

    @Test
    void testAllControllersHaveReport() throws Exception {
        // SimulationController
        try (var sim = new SimulationController()) {
            sim.setName("Sim");
            sim.eventLoop();
            var report = sim.report();
            assertNotNull(report);
            assertEquals("Sim", report.name());
        }

        // SteppingController
        try (var step = new SteppingController()) {
            step.step();
            var report = step.report();
            assertNotNull(report);
        }

        // RealTimeController
        try (var rtc = new RealTimeController("RTC")) {
            rtc.start();
            Thread.sleep(5);
            rtc.stop();
            var report = rtc.report();
            assertNotNull(report);
            assertEquals("RTC", report.name());
        }
    }
}
