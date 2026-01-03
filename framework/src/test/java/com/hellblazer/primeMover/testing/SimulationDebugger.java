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
import java.util.Optional;
import java.util.function.Predicate;

import com.hellblazer.primeMover.api.SimulationException;
import com.hellblazer.primeMover.controllers.SteppingController;

/**
 * Debugging wrapper around SteppingController that provides fine-grained control
 * over simulation execution. Supports step-by-step debugging, conditional execution,
 * and state inspection.
 *
 * <p>Example usage:
 * <pre>{@code
 * var debugger = SimulationDebugger.wrap(controller);
 *
 * // Step-by-step debugging
 * debugger.stepTo(100);
 * debugger.stepN(5);
 *
 * // Conditional execution
 * debugger.runUntil(state -> state.eventCount() >= 10);
 *
 * // State inspection
 * var state = debugger.getState();
 * var nextEvent = debugger.getCurrentEventDetails();
 * }</pre>
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public final class SimulationDebugger {

    private final SteppingController controller;
    private final List<Breakpoint> breakpoints = new ArrayList<>();

    private SimulationDebugger(SteppingController controller) {
        this.controller = controller;
    }

    /**
     * Wraps a SteppingController for debugging.
     *
     * @param controller the controller to wrap
     * @return a new SimulationDebugger
     */
    public static SimulationDebugger wrap(SteppingController controller) {
        return new SimulationDebugger(controller);
    }

    /**
     * Returns the current state of the simulation.
     *
     * @return the current simulation state
     */
    public DebuggerState getState() {
        return new DebuggerState(
            controller.getTotalEvents(),
            controller.getCurrentTime(),
            controller.hasMoreEvents()
        );
    }

    /**
     * Process a single event from the queue.
     *
     * @return true if an event was processed, false if queue was empty
     */
    public boolean stepOne() {
        try {
            return controller.stepOne();
        } catch (SimulationException e) {
            throw new RuntimeException("Simulation error during step", e);
        }
    }

    /**
     * Process up to n events from the queue.
     *
     * @param n the maximum number of events to process
     * @return the actual number of events processed
     */
    public int stepN(int n) {
        var count = 0;
        for (var i = 0; i < n; i++) {
            if (stepOne()) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    /**
     * Process events until the simulation time reaches the target time (inclusive).
     * Events scheduled at times after the target will not be processed.
     *
     * @param targetTime the target simulation time
     * @return the number of events processed
     */
    public int stepTo(long targetTime) {
        var count = 0;
        while (controller.hasMoreEvents()) {
            var nextEvent = controller.peekNextEvent();
            if (nextEvent == null || nextEvent.getTime() > targetTime) {
                break;
            }
            if (stepOne()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Process events until the predicate returns true for the current state.
     * The predicate is checked after each event is processed.
     *
     * @param condition the condition to stop at
     * @return the number of events processed
     */
    public int runUntil(Predicate<DebuggerState> condition) {
        var count = 0;
        while (controller.hasMoreEvents()) {
            if (stepOne()) {
                count++;
                if (condition.test(getState())) {
                    break;
                }
            }
        }
        return count;
    }

    /**
     * Returns details about the next event to be processed, if any.
     *
     * @return the event details, or empty if no events are pending
     */
    public Optional<EventDetails> getCurrentEventDetails() {
        var next = controller.peekNextEvent();
        if (next == null) {
            return Optional.empty();
        }
        return Optional.of(new EventDetails(
            next.getTime(),
            next.getSignature()
        ));
    }

    /**
     * Resets the debugger state, clearing all pending events and statistics.
     */
    public void reset() {
        controller.reset();
    }

    /**
     * Returns the underlying controller.
     *
     * @return the wrapped controller
     */
    public SteppingController getController() {
        return controller;
    }

    // ========== Breakpoint Support ==========

    /**
     * Adds a time-based breakpoint. Execution stops before any event at or after
     * the specified time is processed.
     *
     * @param time the simulation time to break at
     */
    public void breakAtTime(long time) {
        breakpoints.add(new TimeBreakpoint(time));
    }

    /**
     * Adds an event-based breakpoint. Execution stops before any event matching
     * the signature (prefix match) is processed.
     *
     * @param signaturePrefix the event signature prefix to match (e.g., "Entity.method" or "Entity")
     */
    public void breakOnEvent(String signaturePrefix) {
        breakpoints.add(new EventBreakpoint(signaturePrefix));
    }

    /**
     * Adds a conditional breakpoint. Execution stops when the predicate returns
     * true for the current state.
     *
     * @param condition the condition to check after each event
     */
    public void breakWhen(Predicate<DebuggerState> condition) {
        breakpoints.add(new ConditionalBreakpoint(condition));
    }

    /**
     * Clears all breakpoints.
     */
    public void clearBreakpoints() {
        breakpoints.clear();
    }

    /**
     * Returns the number of active breakpoints.
     *
     * @return the breakpoint count
     */
    public int getActiveBreakpointCount() {
        return breakpoints.size();
    }

    /**
     * Runs the simulation until a breakpoint is hit or no more events remain.
     *
     * @return the number of events processed before stopping
     */
    public int runToBreakpoint() {
        var count = 0;
        while (controller.hasMoreEvents()) {
            // Check breakpoints BEFORE processing the next event
            if (shouldBreak()) {
                return count;
            }

            if (stepOne()) {
                count++;

                // Check conditional breakpoints AFTER processing
                if (shouldBreakAfterStep()) {
                    return count;
                }
            }
        }
        return count;
    }

    /**
     * Checks if any breakpoint should stop execution BEFORE processing next event.
     */
    private boolean shouldBreak() {
        var next = controller.peekNextEvent();
        if (next == null) {
            return false;
        }

        for (var bp : breakpoints) {
            if (bp.shouldBreakBefore(next.getTime(), next.getSignature())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if any conditional breakpoint should stop execution AFTER processing.
     */
    private boolean shouldBreakAfterStep() {
        var state = getState();
        for (var bp : breakpoints) {
            if (bp.shouldBreakAfter(state)) {
                return true;
            }
        }
        return false;
    }

    // ========== Breakpoint Implementations ==========

    private sealed interface Breakpoint {
        default boolean shouldBreakBefore(long eventTime, String signature) {
            return false;
        }
        default boolean shouldBreakAfter(DebuggerState state) {
            return false;
        }
    }

    private record TimeBreakpoint(long breakTime) implements Breakpoint {
        @Override
        public boolean shouldBreakBefore(long eventTime, String signature) {
            return eventTime >= breakTime;
        }
    }

    private record EventBreakpoint(String signaturePrefix) implements Breakpoint {
        @Override
        public boolean shouldBreakBefore(long eventTime, String signature) {
            return signature != null && signature.startsWith(signaturePrefix);
        }
    }

    private record ConditionalBreakpoint(Predicate<DebuggerState> condition) implements Breakpoint {
        @Override
        public boolean shouldBreakAfter(DebuggerState state) {
            return condition.test(state);
        }
    }

    /**
     * Immutable snapshot of the debugger/simulation state.
     *
     * @param eventCount the total number of events processed
     * @param currentTime the current simulation time
     * @param hasMoreEvents whether there are more events in the queue
     */
    public record DebuggerState(int eventCount, long currentTime, boolean hasMoreEvents) {}

    /**
     * Details about a pending event.
     *
     * @param time the scheduled time of the event
     * @param signature the event signature (entity.method)
     */
    public record EventDetails(long time, String signature) {}
}
