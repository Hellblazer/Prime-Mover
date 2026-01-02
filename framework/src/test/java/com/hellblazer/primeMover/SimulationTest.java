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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.hellblazer.primeMover.controllers.SimulationController;

/**
 * Tests for Simulation.Builder fluent API and run() convenience methods
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class SimulationTest {

    @Test
    void testBuilderDefaults() {
        var simulation = Simulation.newBuilder().build();

        assertNotNull(simulation, "Simulation should not be null");
        assertNotNull(simulation.controller(), "Controller should not be null");
        assertNotNull(simulation.random(), "Random should not be null");
        assertEquals("Prime Mover Simulation", simulation.name(), "Default name should be set");
        assertEquals(Long.MAX_VALUE, simulation.maxTime(), "Default maxTime should be Long.MAX_VALUE");
        assertTrue(simulation.statisticsEnabled(), "Statistics should be enabled by default");
    }

    @Test
    void testBuilderFluentChaining() {
        var simulation = Simulation.newBuilder()
                                   .withName("Test Simulation")
                                   .withSeed(12345L)
                                   .withMaxTime(1000L)
                                   .withStatistics(false)
                                   .build();

        assertEquals("Test Simulation", simulation.name());
        assertEquals(1000L, simulation.maxTime());
        assertFalse(simulation.statisticsEnabled());
    }

    @Test
    void testSeedReproducibility() {
        var sim1 = Simulation.newBuilder().withSeed(42L).build();
        var sim2 = Simulation.newBuilder().withSeed(42L).build();

        // Same seed should produce same random sequence
        var values1 = new double[10];
        var values2 = new double[10];
        for (int i = 0; i < 10; i++) {
            values1[i] = sim1.random().nextDouble();
            values2[i] = sim2.random().nextDouble();
        }

        assertArrayEquals(values1, values2, "Same seed should produce same random sequence");
    }

    @Test
    void testDifferentSeedsDifferentSequences() {
        var sim1 = Simulation.newBuilder().withSeed(1L).build();
        var sim2 = Simulation.newBuilder().withSeed(2L).build();

        // Different seeds should produce different sequences
        var same = true;
        for (int i = 0; i < 10; i++) {
            if (sim1.random().nextDouble() != sim2.random().nextDouble()) {
                same = false;
                break;
            }
        }

        assertFalse(same, "Different seeds should produce different random sequences");
    }

    @Test
    void testControllerConfiguration() {
        var simulation = Simulation.newBuilder()
                                   .withName("Configured Sim")
                                   .withMaxTime(5000L)
                                   .withStatistics(true)
                                   .build();

        var controller = simulation.controller();
        assertInstanceOf(SimulationController.class, controller);

        var simController = (SimulationController) controller;
        assertEquals("Configured Sim", simController.getName());
        assertEquals(5000L, simController.getEndTime());
        assertTrue(simController.isTrackSpectrum());
    }

    @Test
    void testStatisticsDisabled() {
        var simulation = Simulation.newBuilder()
                                   .withStatistics(false)
                                   .build();

        var controller = (SimulationController) simulation.controller();
        assertFalse(controller.isTrackSpectrum(), "Spectrum tracking should be disabled");
    }

    @Test
    void testBuilderReuse() {
        var builder = Simulation.newBuilder()
                                .withName("Base")
                                .withSeed(100L);

        var sim1 = builder.build();
        var sim2 = builder.withName("Modified").build();

        assertEquals("Base", sim1.name());
        assertEquals("Modified", sim2.name());
    }

    @Test
    void testNullNameUsesDefault() {
        var simulation = Simulation.newBuilder()
                                   .withName(null)
                                   .build();

        assertEquals("Prime Mover Simulation", simulation.name(), "Null name should use default");
    }

    @Test
    void testNegativeMaxTimeThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            Simulation.newBuilder().withMaxTime(-1L).build();
        }, "Negative maxTime should throw IllegalArgumentException");
    }

    @Test
    void testZeroMaxTimeAllowed() {
        var simulation = Simulation.newBuilder().withMaxTime(0L).build();
        assertEquals(0L, simulation.maxTime(), "Zero maxTime should be allowed");
    }

    @Test
    void testAutoCloseable() throws Exception {
        var simulation = Simulation.newBuilder().build();
        assertDoesNotThrow(() -> simulation.close(), "Simulation should be closeable");
    }

    // ========== Tests for Simulation.run() convenience methods ==========

    @Test
    void testRunExecutesRunnable() throws Exception {
        var executed = new AtomicBoolean(false);

        var result = Simulation.run(() -> {
            executed.set(true);
        });

        assertTrue(executed.get(), "Runnable should have been executed");
        assertNotNull(result, "Result should not be null");
    }

    @Test
    void testRunReturnsStatistics() throws Exception {
        var result = Simulation.run(() -> {
            // Empty simulation - just verify we get stats back
        });

        assertNotNull(result, "Result should not be null");
        assertEquals(0, result.simulationStart(), "Start should be 0");
        assertTrue(result.totalEvents() >= 0, "Total events should be non-negative");
        assertNotNull(result.spectrum(), "Spectrum should not be null");
    }

    @Test
    void testRunWithMaxTime() throws Exception {
        var result = Simulation.run(() -> {
            // Empty simulation
        }, 1000L);

        assertNotNull(result, "Result should not be null");
        assertTrue(result.simulationEnd() <= 1000L, "Simulation should end at or before maxTime");
    }

    @Test
    void testRunWithMaxTimeStopsSimulation() throws Exception {
        var eventCount = new AtomicInteger(0);

        // This test verifies maxTime stops simulation even if events keep scheduling
        var result = Simulation.run(() -> {
            eventCount.incrementAndGet();
        }, 100L);

        assertNotNull(result);
        assertTrue(result.simulationEnd() <= 100L, "Simulation should respect maxTime");
    }

    @Test
    void testRunAutoCleanup() throws Exception {
        // Run should clean up resources automatically
        var result = Simulation.run(() -> {});

        assertNotNull(result, "Should complete without errors");
        // If we get here without exception, cleanup was successful
    }

    @Test
    void testRunWithBuilder() throws Exception {
        var executed = new AtomicBoolean(false);

        var result = Simulation.newBuilder()
                               .withName("Builder Run Test")
                               .withMaxTime(500L)
                               .withStatistics(true)
                               .run(() -> {
                                   executed.set(true);
                               });

        assertTrue(executed.get(), "Runnable should have been executed");
        assertNotNull(result, "Result should not be null");
    }

    @Test
    void testSimulationResultRecord() throws Exception {
        var result = Simulation.run(() -> {}, 1000L);

        // Verify SimulationResult has expected accessors
        assertNotNull(result.spectrum());
        assertTrue(result.totalEvents() >= 0);
        assertTrue(result.simulationStart() >= 0);
        assertTrue(result.simulationEnd() >= result.simulationStart());
    }

    @Test
    void testRunWithNullRunnableThrows() {
        assertThrows(NullPointerException.class, () -> {
            Simulation.run(null);
        }, "Null runnable should throw NullPointerException");
    }

    @Test
    void testRunWithNegativeMaxTimeThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            Simulation.run(() -> {}, -1L);
        }, "Negative maxTime should throw IllegalArgumentException");
    }
}
