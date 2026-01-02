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

package com.hellblazer.primeMover;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.hellblazer.primeMover.controllers.SimulationController;

/**
 * Tests for ControllerReport record and report() method
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class ControllerReportTest {

    @Test
    void testControllerReportCreation() {
        var spectrum = Map.of("event1", 10, "event2", 20);
        var report = new ControllerReport("Test Sim", 0L, 1000L, 30, spectrum);

        assertEquals("Test Sim", report.name());
        assertEquals(0L, report.startTime());
        assertEquals(1000L, report.endTime());
        assertEquals(30, report.totalEvents());
        assertEquals(spectrum, report.spectrum());
    }

    @Test
    void testDuration() {
        var report = new ControllerReport("Test", 100L, 500L, 10, Map.of());
        assertEquals(400L, report.duration(), "Duration should be endTime - startTime");
    }

    @Test
    void testTextOutput() {
        var spectrum = Map.of("MyEntity.doSomething", 5);
        var report = new ControllerReport("My Simulation", 0L, 100L, 5, spectrum);

        var text = report.toText();

        assertNotNull(text);
        assertTrue(text.contains("My Simulation"), "Should contain simulation name");
        assertTrue(text.contains("100"), "Should contain end time");
        assertTrue(text.contains("5"), "Should contain total events");
        assertTrue(text.contains("MyEntity.doSomething"), "Should contain spectrum entry");
    }

    @Test
    void testJsonOutput() {
        var spectrum = Map.of("TestEvent", 3);
        var report = new ControllerReport("JSON Test", 10L, 200L, 3, spectrum);

        var json = report.toJson();

        assertNotNull(json);
        assertTrue(json.contains("\"name\""), "Should have name field");
        assertTrue(json.contains("\"JSON Test\""), "Should contain simulation name");
        assertTrue(json.contains("\"startTime\""), "Should have startTime field");
        assertTrue(json.contains("\"endTime\""), "Should have endTime field");
        assertTrue(json.contains("\"totalEvents\""), "Should have totalEvents field");
        assertTrue(json.contains("\"spectrum\""), "Should have spectrum field");
        assertTrue(json.contains("\"duration\""), "Should have duration field");
    }

    @Test
    void testEmptySpectrum() {
        var report = new ControllerReport("Empty", 0L, 0L, 0, Map.of());

        assertEquals(0, report.totalEvents());
        assertTrue(report.spectrum().isEmpty());

        var text = report.toText();
        assertNotNull(text);

        var json = report.toJson();
        assertNotNull(json);
    }

    @Test
    void testControllerReportMethod() throws Exception {
        try (var controller = new SimulationController()) {
            controller.setName("Report Test");
            controller.setEndTime(1000L);
            controller.eventLoop();

            var report = controller.report();

            assertNotNull(report, "Report should not be null");
            assertEquals("Report Test", report.name());
            assertEquals(0L, report.startTime());
            assertTrue(report.endTime() >= 0);
            assertNotNull(report.spectrum());
        }
    }

    @Test
    void testReportAfterSimulationRun() throws Exception {
        var result = Simulation.newBuilder()
                               .withName("Run Report Test")
                               .withMaxTime(100L)
                               .run(() -> {});

        // SimulationResult should be convertible to report-like data
        assertNotNull(result);
        assertEquals(0L, result.simulationStart());
        assertTrue(result.simulationEnd() >= 0);
    }

    @Test
    void testReportWithSpectrumData() throws Exception {
        try (var controller = new SimulationController()) {
            controller.setName("Spectrum Test");
            controller.setTrackSpectrum(true);
            controller.setEndTime(100L);
            controller.eventLoop();

            var report = controller.report();
            assertNotNull(report.spectrum());
        }
    }

    @Test
    void testReportImmutability() {
        var originalSpectrum = new java.util.HashMap<String, Integer>();
        originalSpectrum.put("event", 1);

        var report = new ControllerReport("Immutable", 0L, 100L, 1, Map.copyOf(originalSpectrum));

        // Modifying original shouldn't affect report
        originalSpectrum.put("event", 999);
        assertEquals(1, report.spectrum().get("event"));

        // Report spectrum should be unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> {
            report.spectrum().put("new", 1);
        });
    }
}
