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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.hellblazer.primeMover.controllers.SimulationController;

/**
 * Fluent API for verifying simulation state, inspired by Project Reactor's StepVerifier.
 * Allows chaining multiple expectations and provides clear failure messages.
 *
 * <p>Example usage:
 * <pre>{@code
 * SimulationVerifier.create(controller)
 *     .as("After processing all orders")
 *     .expectTimeAtLeast(1000)
 *     .expectEventCountAtLeast(50)
 *     .expectEvent("OrderService.process")
 *     .verifyComplete();
 * }</pre>
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public final class SimulationVerifier {

    private final SimulationController controller;
    private final List<Expectation> expectations = new ArrayList<>();
    private String description = null;

    private SimulationVerifier(SimulationController controller) {
        this.controller = controller;
    }

    /**
     * Creates a new SimulationVerifier for the given controller.
     *
     * @param controller the controller to verify
     * @return a new SimulationVerifier
     */
    public static SimulationVerifier create(SimulationController controller) {
        return new SimulationVerifier(controller);
    }

    /**
     * Adds a description to be included in failure messages.
     *
     * @param description the description
     * @return this verifier for chaining
     */
    public SimulationVerifier as(String description) {
        this.description = description;
        return this;
    }

    // ========== Time Expectations ==========

    /**
     * Expects the simulation time to equal the given value.
     *
     * @param expectedTime the expected time
     * @return this verifier for chaining
     */
    public SimulationVerifier expectTimeEquals(long expectedTime) {
        expectations.add(new Expectation(
            "time equals " + expectedTime,
            ctrl -> {
                var actual = ctrl.getCurrentTime();
                if (actual != expectedTime) {
                    throw new AssertionError(formatError(
                        "Expected time to be <%d> but was <%d>", expectedTime, actual));
                }
            }
        ));
        return this;
    }

    /**
     * Expects the simulation time to be at least the given value.
     *
     * @param minimumTime the minimum expected time
     * @return this verifier for chaining
     */
    public SimulationVerifier expectTimeAtLeast(long minimumTime) {
        expectations.add(new Expectation(
            "time at least " + minimumTime,
            ctrl -> {
                var actual = ctrl.getCurrentTime();
                if (actual < minimumTime) {
                    throw new AssertionError(formatError(
                        "Expected time to be at least <%d> but was <%d>", minimumTime, actual));
                }
            }
        ));
        return this;
    }

    /**
     * Expects the simulation time to be within the given range (inclusive).
     *
     * @param minTime the minimum time
     * @param maxTime the maximum time
     * @return this verifier for chaining
     */
    public SimulationVerifier expectTimeInRange(long minTime, long maxTime) {
        expectations.add(new Expectation(
            "time in range [" + minTime + ", " + maxTime + "]",
            ctrl -> {
                var actual = ctrl.getCurrentTime();
                if (actual < minTime || actual > maxTime) {
                    throw new AssertionError(formatError(
                        "Expected time in range [%d, %d] but was <%d>", minTime, maxTime, actual));
                }
            }
        ));
        return this;
    }

    // ========== Event Count Expectations ==========

    /**
     * Expects the total event count to equal the given value.
     *
     * @param expectedCount the expected count
     * @return this verifier for chaining
     */
    public SimulationVerifier expectEventCount(int expectedCount) {
        expectations.add(new Expectation(
            "event count equals " + expectedCount,
            ctrl -> {
                var actual = ctrl.getTotalEvents();
                if (actual != expectedCount) {
                    throw new AssertionError(formatError(
                        "Expected event count to be <%d> but was <%d>", expectedCount, actual));
                }
            }
        ));
        return this;
    }

    /**
     * Expects the total event count to be at least the given value.
     *
     * @param minimumCount the minimum expected count
     * @return this verifier for chaining
     */
    public SimulationVerifier expectEventCountAtLeast(int minimumCount) {
        expectations.add(new Expectation(
            "event count at least " + minimumCount,
            ctrl -> {
                var actual = ctrl.getTotalEvents();
                if (actual < minimumCount) {
                    throw new AssertionError(formatError(
                        "Expected at least <%d> events but was <%d>", minimumCount, actual));
                }
            }
        ));
        return this;
    }

    // ========== Event Signature Expectations ==========

    /**
     * Expects an event with the given signature to have occurred.
     *
     * @param signature the event signature (e.g., "MyEntity.process")
     * @return this verifier for chaining
     */
    public SimulationVerifier expectEvent(String signature) {
        expectations.add(new Expectation(
            "event occurred: " + signature,
            ctrl -> {
                var spectrum = ctrl.getSpectrum();
                if (!spectrum.containsKey(signature)) {
                    throw new AssertionError(formatError(
                        "Expected event '%s' to occur but it did not. Events: %s",
                        signature, spectrum.keySet()));
                }
            }
        ));
        return this;
    }

    /**
     * Expects an event with the given signature to have occurred at least n times.
     *
     * @param signature the event signature
     * @param minCount the minimum number of occurrences
     * @return this verifier for chaining
     */
    public SimulationVerifier expectEventAtLeast(String signature, int minCount) {
        expectations.add(new Expectation(
            "event '" + signature + "' at least " + minCount + " times",
            ctrl -> {
                var spectrum = ctrl.getSpectrum();
                var actual = spectrum.getOrDefault(signature, 0);
                if (actual < minCount) {
                    throw new AssertionError(formatError(
                        "Expected event '%s' at least %d times but occurred %d times",
                        signature, minCount, actual));
                }
            }
        ));
        return this;
    }

    /**
     * Expects no event with the given signature to have occurred.
     *
     * @param signature the event signature
     * @return this verifier for chaining
     */
    public SimulationVerifier expectNoEvent(String signature) {
        expectations.add(new Expectation(
            "no event: " + signature,
            ctrl -> {
                var spectrum = ctrl.getSpectrum();
                if (spectrum.containsKey(signature)) {
                    throw new AssertionError(formatError(
                        "Expected no event '%s' but it occurred %d times",
                        signature, spectrum.get(signature)));
                }
            }
        ));
        return this;
    }

    // ========== Custom Expectations ==========

    /**
     * Adds a custom expectation.
     *
     * @param description description for failure messages
     * @param check the check to perform (should throw AssertionError on failure)
     * @return this verifier for chaining
     */
    public SimulationVerifier expectThat(String description, Consumer<SimulationController> check) {
        expectations.add(new Expectation(description, check));
        return this;
    }

    // ========== Verification Methods ==========

    /**
     * Verifies all expectations.
     *
     * @throws AssertionError if any expectation fails
     */
    public void verify() {
        for (var expectation : expectations) {
            try {
                expectation.check().accept(controller);
            } catch (AssertionError e) {
                throw e;
            } catch (Exception e) {
                throw new AssertionError(formatError(
                    "Expectation '%s' threw exception: %s", expectation.name(), e.getMessage()), e);
            }
        }
    }

    /**
     * Verifies all expectations and that the simulation has completed.
     * A simulation is complete when its event loop has finished.
     *
     * @throws AssertionError if any expectation fails or simulation is not complete
     */
    public void verifyComplete() {
        verify();

        // Check simulation completion
        var endTime = controller.getSimulationEnd();
        if (endTime < 0) {
            throw new AssertionError(formatError(
                "Expected simulation to be complete but event loop has not finished"));
        }
    }

    /**
     * Formats an error message, prepending description if present.
     */
    private String formatError(String format, Object... args) {
        var message = String.format(format, args);
        if (description != null) {
            return description + ": " + message;
        }
        return message;
    }

    /**
     * Internal record for storing expectations.
     */
    private record Expectation(String name, Consumer<SimulationController> check) {}
}
