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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.hellblazer.primeMover.controllers.SimulationController;

/**
 * Tests for SimulationVerifier - fluent API for simulation verification.
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class SimulationVerifierTest {

    @Nested
    @DisplayName("Creation tests")
    class CreationTests {

        @Test
        @DisplayName("creates from controller")
        void createsFromController() {
            var controller = new SimulationController();
            var verifier = SimulationVerifier.create(controller);
            assertNotNull(verifier, "Should create verifier from controller");
        }
    }

    @Nested
    @DisplayName("Time verification tests")
    class TimeVerificationTests {

        @Test
        @DisplayName("expectTimeEquals passes when time matches")
        void expectTimeEqualsPasses() {
            var controller = new SimulationController();
            assertDoesNotThrow(() ->
                SimulationVerifier.create(controller)
                    .expectTimeEquals(0)
                    .verify());
        }

        @Test
        @DisplayName("expectTimeEquals fails when time mismatch")
        void expectTimeEqualsFails() {
            var controller = new SimulationController();
            assertThrows(AssertionError.class, () ->
                SimulationVerifier.create(controller)
                    .expectTimeEquals(100)
                    .verify());
        }

        @Test
        @DisplayName("expectTimeAtLeast passes when time >= expected")
        void expectTimeAtLeastPasses() {
            var controller = new SimulationController();
            assertDoesNotThrow(() ->
                SimulationVerifier.create(controller)
                    .expectTimeAtLeast(0)
                    .verify());
        }

        @Test
        @DisplayName("expectTimeAtLeast fails when time < expected")
        void expectTimeAtLeastFails() {
            var controller = new SimulationController();
            assertThrows(AssertionError.class, () ->
                SimulationVerifier.create(controller)
                    .expectTimeAtLeast(100)
                    .verify());
        }

        @Test
        @DisplayName("expectTimeInRange passes when in range")
        void expectTimeInRangePasses() {
            var controller = new SimulationController();
            assertDoesNotThrow(() ->
                SimulationVerifier.create(controller)
                    .expectTimeInRange(0, 100)
                    .verify());
        }

        @Test
        @DisplayName("expectTimeInRange fails when out of range")
        void expectTimeInRangeFails() {
            var controller = new SimulationController();
            assertThrows(AssertionError.class, () ->
                SimulationVerifier.create(controller)
                    .expectTimeInRange(50, 100)
                    .verify());
        }
    }

    @Nested
    @DisplayName("Event count verification tests")
    class EventCountTests {

        @Test
        @DisplayName("expectEventCount passes when count matches")
        void expectEventCountPasses() {
            var controller = new SimulationController();
            assertDoesNotThrow(() ->
                SimulationVerifier.create(controller)
                    .expectEventCount(0)
                    .verify());
        }

        @Test
        @DisplayName("expectEventCount fails when count mismatch")
        void expectEventCountFails() {
            var controller = new SimulationController();
            assertThrows(AssertionError.class, () ->
                SimulationVerifier.create(controller)
                    .expectEventCount(10)
                    .verify());
        }

        @Test
        @DisplayName("expectEventCountAtLeast passes when count sufficient")
        void expectEventCountAtLeastPasses() {
            var controller = new SimulationController();
            assertDoesNotThrow(() ->
                SimulationVerifier.create(controller)
                    .expectEventCountAtLeast(0)
                    .verify());
        }

        @Test
        @DisplayName("expectEventCountAtLeast fails when count insufficient")
        void expectEventCountAtLeastFails() {
            var controller = new SimulationController();
            assertThrows(AssertionError.class, () ->
                SimulationVerifier.create(controller)
                    .expectEventCountAtLeast(5)
                    .verify());
        }
    }

    @Nested
    @DisplayName("Chaining tests")
    class ChainingTests {

        @Test
        @DisplayName("multiple expectations can be chained")
        void multipleChainingWorks() {
            var controller = new SimulationController();
            assertDoesNotThrow(() ->
                SimulationVerifier.create(controller)
                    .expectTimeEquals(0)
                    .expectEventCount(0)
                    .expectTimeInRange(0, 1000)
                    .verify());
        }

        @Test
        @DisplayName("fails on first failing expectation")
        void failsOnFirst() {
            var controller = new SimulationController();
            var error = assertThrows(AssertionError.class, () ->
                SimulationVerifier.create(controller)
                    .expectTimeEquals(100)  // This should fail first
                    .expectEventCount(0)
                    .verify());
            assertTrue(error.getMessage().contains("time"),
                "Error should mention time");
        }
    }

    @Nested
    @DisplayName("verifyComplete tests")
    class VerifyCompleteTests {

        @SimulationTest
        @DisplayName("verifyComplete verifies simulation ran to completion")
        void verifyCompleteChecksCompletion(SimulationController controller) throws Exception {
            controller.eventLoop();
            assertDoesNotThrow(() ->
                SimulationVerifier.create(controller)
                    .verifyComplete());
        }
    }

    @Nested
    @DisplayName("Description tests")
    class DescriptionTests {

        @Test
        @DisplayName("as() adds description to failure message")
        void asAddsDescription() {
            var controller = new SimulationController();
            var error = assertThrows(AssertionError.class, () ->
                SimulationVerifier.create(controller)
                    .as("Expected simulation to reach time 100")
                    .expectTimeEquals(100)
                    .verify());
            assertTrue(error.getMessage().contains("Expected simulation to reach time 100"),
                "Error should contain description");
        }
    }

    @Nested
    @DisplayName("Integration tests")
    class IntegrationTests {

        @SimulationTest
        @DisplayName("full verification workflow")
        void fullVerificationWorkflow(SimulationController controller) throws Exception {
            controller.eventLoop();

            SimulationVerifier.create(controller)
                .as("After event loop")
                .expectTimeAtLeast(0)
                .expectEventCount(0)
                .verifyComplete();
        }
    }
}
