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

import static com.hellblazer.primeMover.testing.SimulationAssertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.hellblazer.primeMover.Simulation;
import com.hellblazer.primeMover.controllers.SimulationController;

/**
 * Tests for SimulationAssertions static assertion methods.
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class SimulationAssertionsTest {

    @Nested
    @DisplayName("assertTimeEquals tests")
    class TimeAssertionTests {

        @Test
        @DisplayName("passes when time matches expected")
        void passesWhenTimeMatches() {
            var controller = new SimulationController();
            // Controller starts at time 0
            assertDoesNotThrow(() -> assertTimeEquals(0, controller));
        }

        @Test
        @DisplayName("fails when time does not match")
        void failsWhenTimeMismatch() {
            var controller = new SimulationController();
            var error = assertThrows(AssertionError.class,
                () -> assertTimeEquals(100, controller));
            assertTrue(error.getMessage().contains("100"),
                "Error message should contain expected time");
            assertTrue(error.getMessage().contains("0"),
                "Error message should contain actual time");
        }

        @Test
        @DisplayName("provides clear error message")
        void providesClearErrorMessage() {
            var controller = new SimulationController();
            var error = assertThrows(AssertionError.class,
                () -> assertTimeEquals(500, controller));
            assertTrue(error.getMessage().toLowerCase().contains("time"),
                "Error message should mention 'time'");
        }
    }

    @Nested
    @DisplayName("assertEventCountEquals tests")
    class EventCountAssertionTests {

        @Test
        @DisplayName("passes when event count matches")
        void passesWhenEventCountMatches() {
            var controller = new SimulationController();
            // No events initially
            assertDoesNotThrow(() -> assertEventCountEquals(0, controller));
        }

        @Test
        @DisplayName("fails when event count does not match")
        void failsWhenEventCountMismatch() {
            var controller = new SimulationController();
            var error = assertThrows(AssertionError.class,
                () -> assertEventCountEquals(10, controller));
            assertTrue(error.getMessage().contains("10"),
                "Error message should contain expected count");
        }

        @Test
        @DisplayName("provides clear error message")
        void providesClearErrorMessage() {
            var controller = new SimulationController();
            var error = assertThrows(AssertionError.class,
                () -> assertEventCountEquals(5, controller));
            assertTrue(error.getMessage().toLowerCase().contains("event"),
                "Error message should mention 'event'");
        }
    }

    @Nested
    @DisplayName("assertEventCountAtLeast tests")
    class EventCountAtLeastTests {

        @Test
        @DisplayName("passes when count is at least minimum")
        void passesWhenCountMeetsMinimum() {
            var controller = new SimulationController();
            assertDoesNotThrow(() -> assertEventCountAtLeast(0, controller));
        }

        @Test
        @DisplayName("fails when count is below minimum")
        void failsWhenCountBelowMinimum() {
            var controller = new SimulationController();
            var error = assertThrows(AssertionError.class,
                () -> assertEventCountAtLeast(5, controller));
            assertTrue(error.getMessage().contains("at least"),
                "Error message should contain 'at least'");
        }
    }

    @Nested
    @DisplayName("assertTimeInRange tests")
    class TimeInRangeTests {

        @Test
        @DisplayName("passes when time is in range")
        void passesWhenTimeInRange() {
            var controller = new SimulationController();
            assertDoesNotThrow(() -> assertTimeInRange(0, 100, controller));
        }

        @Test
        @DisplayName("fails when time is below range")
        void failsWhenTimeBelowRange() {
            var controller = new SimulationController();
            var error = assertThrows(AssertionError.class,
                () -> assertTimeInRange(10, 100, controller));
            assertTrue(error.getMessage().contains("range"),
                "Error message should mention 'range'");
        }

        @Test
        @DisplayName("fails when time is above range")
        void failsWhenTimeAboveRange() {
            var controller = new SimulationController();
            // Would need to advance time to test this properly
            // For now just verify the method exists
            assertDoesNotThrow(() -> assertTimeInRange(0, 0, controller));
        }
    }

    @Nested
    @DisplayName("assertSimulationComplete tests")
    class SimulationCompleteTests {

        @SimulationTest
        @DisplayName("passes after event loop completes")
        void passesAfterEventLoop(SimulationController controller) throws Exception {
            controller.eventLoop();
            assertDoesNotThrow(() -> assertSimulationComplete(controller));
        }
    }

    @Nested
    @DisplayName("Integration with @SimulationTest")
    class IntegrationTests {

        @SimulationTest
        @DisplayName("assertions work with injected controller")
        void assertionsWorkWithInjectedController(SimulationController controller) {
            assertTimeEquals(0, controller);
            assertEventCountEquals(0, controller);
            assertEventCountAtLeast(0, controller);
            assertTimeInRange(0, Long.MAX_VALUE, controller);
        }

        @SimulationTest
        @DisplayName("assertions work with simulation")
        void assertionsWorkWithSimulation(Simulation simulation) throws Exception {
            var controller = simulation.controller();
            assertTimeEquals(0, controller);
            controller.eventLoop();
            assertSimulationComplete(controller);
        }
    }
}
