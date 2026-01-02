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

import com.hellblazer.primeMover.controllers.SimulationController;

/**
 * Static assertion methods for simulation testing.
 * Provides convenient assertions with clear error messages for validating
 * simulation state.
 *
 * <p>Example usage:
 * <pre>{@code
 * import static com.hellblazer.primeMover.testing.SimulationAssertions.*;
 *
 * @SimulationTest
 * void myTest(SimulationController controller) {
 *     controller.eventLoop();
 *     assertTimeEquals(1000, controller);
 *     assertEventCountAtLeast(10, controller);
 *     assertSimulationComplete(controller);
 * }
 * }</pre>
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public final class SimulationAssertions {

    private SimulationAssertions() {
        // Utility class - no instantiation
    }

    /**
     * Asserts that the simulation time equals the expected value.
     *
     * @param expected the expected simulation time
     * @param controller the simulation controller to check
     * @throws AssertionError if the time does not match
     */
    public static void assertTimeEquals(long expected, SimulationController controller) {
        var actual = controller.getCurrentTime();
        if (actual != expected) {
            throw new AssertionError(String.format(
                "Simulation time mismatch: expected <%d> but was <%d>",
                expected, actual));
        }
    }

    /**
     * Asserts that the simulation time equals the expected value with a custom message.
     *
     * @param expected the expected simulation time
     * @param controller the simulation controller to check
     * @param message custom message for failure
     * @throws AssertionError if the time does not match
     */
    public static void assertTimeEquals(long expected, SimulationController controller, String message) {
        var actual = controller.getCurrentTime();
        if (actual != expected) {
            throw new AssertionError(String.format(
                "%s: Simulation time mismatch: expected <%d> but was <%d>",
                message, expected, actual));
        }
    }

    /**
     * Asserts that the total event count equals the expected value.
     *
     * @param expected the expected event count
     * @param controller the simulation controller to check
     * @throws AssertionError if the count does not match
     */
    public static void assertEventCountEquals(int expected, SimulationController controller) {
        var actual = controller.getTotalEvents();
        if (actual != expected) {
            throw new AssertionError(String.format(
                "Event count mismatch: expected <%d> events but was <%d>",
                expected, actual));
        }
    }

    /**
     * Asserts that the total event count equals the expected value with a custom message.
     *
     * @param expected the expected event count
     * @param controller the simulation controller to check
     * @param message custom message for failure
     * @throws AssertionError if the count does not match
     */
    public static void assertEventCountEquals(int expected, SimulationController controller, String message) {
        var actual = controller.getTotalEvents();
        if (actual != expected) {
            throw new AssertionError(String.format(
                "%s: Event count mismatch: expected <%d> events but was <%d>",
                message, expected, actual));
        }
    }

    /**
     * Asserts that the total event count is at least the specified minimum.
     *
     * @param minimum the minimum expected event count
     * @param controller the simulation controller to check
     * @throws AssertionError if the count is below the minimum
     */
    public static void assertEventCountAtLeast(int minimum, SimulationController controller) {
        var actual = controller.getTotalEvents();
        if (actual < minimum) {
            throw new AssertionError(String.format(
                "Event count too low: expected at least <%d> events but was <%d>",
                minimum, actual));
        }
    }

    /**
     * Asserts that the simulation time is within the specified range (inclusive).
     *
     * @param minTime the minimum expected time (inclusive)
     * @param maxTime the maximum expected time (inclusive)
     * @param controller the simulation controller to check
     * @throws AssertionError if the time is outside the range
     */
    public static void assertTimeInRange(long minTime, long maxTime, SimulationController controller) {
        var actual = controller.getCurrentTime();
        if (actual < minTime || actual > maxTime) {
            throw new AssertionError(String.format(
                "Simulation time out of range: expected in [%d, %d] but was <%d>",
                minTime, maxTime, actual));
        }
    }

    /**
     * Asserts that the simulation has completed (event loop has finished).
     * A simulation is considered complete when it has processed all events
     * or reached its end time.
     *
     * @param controller the simulation controller to check
     * @throws AssertionError if the simulation is not complete
     */
    public static void assertSimulationComplete(SimulationController controller) {
        // A simulation is complete if its end time has been set (> 0) after running
        var endTime = controller.getSimulationEnd();
        if (endTime < 0) {
            throw new AssertionError(
                "Simulation not complete: event loop has not been run or has not finished");
        }
    }

    /**
     * Asserts that the simulation duration (end time - start time) equals the expected value.
     *
     * @param expected the expected duration
     * @param controller the simulation controller to check
     * @throws AssertionError if the duration does not match
     */
    public static void assertDurationEquals(long expected, SimulationController controller) {
        var start = controller.getSimulationStart();
        var end = controller.getSimulationEnd();
        var actual = end - start;
        if (actual != expected) {
            throw new AssertionError(String.format(
                "Simulation duration mismatch: expected <%d> but was <%d> (start=%d, end=%d)",
                expected, actual, start, end));
        }
    }

    /**
     * Asserts that a specific event type was executed during the simulation.
     * Uses the event spectrum to check if the event signature exists.
     *
     * @param eventSignature the event signature to look for (e.g., "MyEntity.myMethod")
     * @param controller the simulation controller to check
     * @throws AssertionError if the event was not found
     */
    public static void assertEventOccurred(String eventSignature, SimulationController controller) {
        var spectrum = controller.getSpectrum();
        if (!spectrum.containsKey(eventSignature)) {
            throw new AssertionError(String.format(
                "Expected event '%s' did not occur. Events that occurred: %s",
                eventSignature, spectrum.keySet()));
        }
    }

    /**
     * Asserts that a specific event type was executed at least the specified number of times.
     *
     * @param eventSignature the event signature to look for
     * @param minCount the minimum number of occurrences
     * @param controller the simulation controller to check
     * @throws AssertionError if the event count is below the minimum
     */
    public static void assertEventOccurredAtLeast(String eventSignature, int minCount,
                                                   SimulationController controller) {
        var spectrum = controller.getSpectrum();
        var actual = spectrum.getOrDefault(eventSignature, 0);
        if (actual < minCount) {
            throw new AssertionError(String.format(
                "Event '%s' occurred %d times, expected at least %d",
                eventSignature, actual, minCount));
        }
    }
}
