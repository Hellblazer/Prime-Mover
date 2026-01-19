/**
 * Copyright (C) 2010 Hal Hildebrand. All rights reserved.
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
package com.hellblazer.primeMover.api;

import org.slf4j.Logger;

/**
 * The central control interface for Prime Mover's event-driven simulation framework. A Controller
 * manages the simulation clock, schedules and processes events, and maintains the execution context
 * for simulation entities.
 *
 * <p>Controllers are responsible for:
 * <ul>
 *   <li>Maintaining the simulation clock and advancing logical time</li>
 *   <li>Scheduling events at specific simulation times</li>
 *   <li>Processing events in time-ordered sequence</li>
 *   <li>Managing virtual thread continuations for blocking operations</li>
 *   <li>Providing debugging and tracing capabilities</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Controllers are typically accessed through thread-local storage via
 * {@link Kronos#getController()}. Each virtual thread executing simulation events has its own
 * controller reference.
 *
 * <p><b>Implementations:</b>
 * <ul>
 *   <li>{@code SimulationController} - Standard discrete event simulation with statistics</li>
 *   <li>{@code SteppingController} - Step-through execution for debugging</li>
 *   <li>{@code RealTimeController} - Real-time paced simulation</li>
 * </ul>
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * @see Kronos
 * @see Event
 * @see EntityReference
 */
public interface Controller {

    /**
     * Advances the simulation clock forward by the specified duration. This method updates the
     * current time without processing events—it's typically used for initialization or special
     * time manipulation scenarios.
     *
     * <p><b>Note:</b> Most simulation code should use {@link Kronos#sleep(long)} instead, which
     * properly schedules event continuations.
     *
     * @param duration the number of time units to advance (must be non-negative)
     * @throws IllegalArgumentException if duration is negative
     * @see #setCurrentTime(long)
     * @see Kronos#sleep(long)
     */
    void advance(long duration);

    /**
     * Reinitializes the controller to its initial state, clearing all pending events and resetting
     * the simulation clock. This method is used to prepare for a new simulation run without creating
     * a new controller instance.
     *
     * <p><b>Effects:</b>
     * <ul>
     *   <li>Clears the event queue</li>
     *   <li>Resets simulation time to 0</li>
     *   <li>Clears statistics (if applicable)</li>
     *   <li>Maintains configuration settings (debug flags, loggers)</li>
     * </ul>
     *
     * <p><b>Warning:</b> This does not reset the state of simulation entities. Entity state must
     * be managed separately.
     */
    void clear();

    /**
     * Returns the event currently being processed by this controller. This provides access to
     * event metadata such as scheduled time, signature, and event source chain.
     *
     * <p><b>Usage:</b> Primarily used for debugging, logging, and introspection during event
     * processing.
     *
     * @return the current event being processed, or null if no event is currently executing
     * @see Event
     */
    Event getCurrentEvent();

    /**
     * Returns the current logical time of the simulation clock. This is the time of the event
     * currently being processed, or the time of the next event to be processed if no event is
     * currently executing.
     *
     * <p>The simulation time is a logical clock measured in arbitrary time units (commonly
     * milliseconds or microseconds). It advances discretely as events are processed.
     *
     * @return the current simulation time (non-negative)
     * @see #setCurrentTime(long)
     * @see Kronos#currentTime()
     */
    long getCurrentTime();

    /**
     * Returns whether the controller is collecting detailed debug information about where events
     * are raised. When enabled, the controller records stack traces at event creation points.
     *
     * <p><b>Performance Impact:</b> Debug mode has significant overhead due to stack trace
     * collection and should only be enabled for troubleshooting.
     *
     * @return true if debug information is being collected
     * @see #setDebugEvents(boolean)
     */
    boolean isDebugEvents();

    /**
     * Returns whether the controller is tracking event source chains. When enabled, each event
     * maintains a reference to the event that caused it to be raised.
     *
     * <p><b>Memory Impact:</b> Event source tracking prevents garbage collection of processed
     * events, as chains of events remain reachable. This can lead to increased memory usage in
     * long-running simulations.
     *
     * @return true if event sources are being tracked
     * @see #setTrackEventSources(boolean)
     * @see Event#getSource()
     */
    boolean isTrackEventSources();

    /**
     * Posts a blocking event that suspends the calling virtual thread until the event completes.
     * This method is used to implement continuation-based blocking operations via
     * {@link Kronos#blockingSleep(long)} and synchronous channel operations.
     *
     * <p>The calling thread will be suspended (using virtual thread continuations) and resumed
     * when the event completes execution. This enables CSP-style synchronous communication
     * patterns within the discrete event simulation.
     *
     * <p><b>Thread Model:</b> This method only works correctly when called from a virtual thread
     * managed by the simulation framework. Calling from platform threads will fail.
     *
     * <p><b>Example Use Case:</b> Channel operations where a sender blocks until a receiver is ready.
     *
     * @param entity the target entity for the event (must not be null)
     * @param event the event ordinal identifying which method to invoke
     * @param arguments the arguments to pass to the event method
     * @return the return value from the event method (may be null)
     * @throws Throwable if the event method throws an exception
     * @throws IllegalArgumentException if entity is null or event ordinal is invalid
     * @see #postEvent(EntityReference, int, Object...)
     * @see Kronos#blockingSleep(long)
     */
    Object postContinuingEvent(EntityReference entity, int event, Object... arguments) throws Throwable;

    /**
     * Posts an event to be evaluated at the current simulation time. The event will be processed
     * in the normal event queue order, respecting time-ordering and any concurrent events at the
     * same time instant.
     *
     * <p>This is the standard mechanism for scheduling events. The calling code continues
     * immediately—it does not wait for the event to execute.
     *
     * @param entity the target entity for the event (must not be null)
     * @param event the event ordinal identifying which method to invoke
     * @param arguments the arguments to pass to the event method
     * @throws IllegalArgumentException if entity is null or event ordinal is invalid
     * @see #postEvent(long, EntityReference, int, Object...)
     */
    void postEvent(EntityReference entity, int event, Object... arguments);

    /**
     * Posts an event to be evaluated at the specified absolute simulation time. The event will
     * be inserted into the event queue in time-ordered position.
     *
     * <p><b>Time Semantics:</b> The time parameter is an absolute simulation time, not a relative
     * duration. To schedule an event 100 time units in the future, use
     * {@code getCurrentTime() + 100}.
     *
     * <p><b>Example:</b>
     * <pre>{@code
     * // Schedule an event 1000 time units from now
     * controller.postEvent(
     *     controller.getCurrentTime() + 1000,
     *     entityRef,
     *     methodOrdinal,
     *     arg1, arg2
     * );
     * }</pre>
     *
     * @param time the absolute simulation time at which to process the event
     * @param entity the target entity for the event (must not be null)
     * @param event the event ordinal identifying which method to invoke
     * @param arguments the arguments to pass to the event method
     * @throws IllegalArgumentException if time is negative, entity is null, or event ordinal is invalid
     * @see #postEvent(EntityReference, int, Object...)
     */
    void postEvent(long time, EntityReference entity, int event, Object... arguments);

    /**
     * Sets the simulation clock to the specified absolute time. This method directly manipulates
     * the clock and is typically only used during initialization or for special simulation control
     * scenarios.
     *
     * <p><b>Warning:</b> Setting time arbitrarily can violate event ordering. Ensure the new time
     * does not precede pending events in the queue.
     *
     * @param time the new simulation time (must be non-negative)
     * @throws IllegalArgumentException if time is negative or precedes pending events
     * @see #advance(long)
     * @see #getCurrentTime()
     */
    void setCurrentTime(long time);

    /**
     * Configures whether the controller collects debug information for raised events. When enabled,
     * the controller records stack traces showing where each event was created, which is invaluable
     * for debugging unexpected event scheduling.
     *
     * <p><b>Performance Warning:</b> Debug mode incurs significant overhead due to stack trace
     * capture. A typical simulation may see 10-50x slowdown. Use only for troubleshooting specific
     * issues, not for production runs.
     *
     * <p><b>Recovery Guidance:</b> To diagnose event scheduling issues, enable debug mode, run
     * a minimal reproduction case, then examine event sources using {@link Event#getSource()} and
     * {@link Event#printTrace()}.
     *
     * @param debug true to enable debug information collection, false to disable
     * @see #isDebugEvents()
     * @see Event#printTrace()
     */
    void setDebugEvents(boolean debug);

    /**
     * Configures an SLF4J logger to trace all event processing. When set, the controller logs
     * each event as it is processed, including the event signature, scheduled time, and processing
     * duration.
     *
     * <p><b>Log Format:</b> Logs typically include:
     * <ul>
     *   <li>Event signature (method name and parameters)</li>
     *   <li>Scheduled time and actual execution time</li>
     *   <li>Event source information (if tracking enabled)</li>
     * </ul>
     *
     * <p><b>Example:</b>
     * <pre>{@code
     * Logger eventLog = LoggerFactory.getLogger("simulation.events");
     * controller.setEventLogger(eventLog);
     * }</pre>
     *
     * @param eventLog the logger to use for event tracing, or null to disable logging
     */
    void setEventLogger(Logger eventLog);

    /**
     * Configures whether the controller tracks event source chains. When enabled, each event
     * maintains a reference to the event that caused it to be scheduled, enabling causal analysis
     * via {@link Event#getSource()} and {@link Event#printTrace()}.
     *
     * <p><b>Memory Impact:</b> Event tracking prevents garbage collection of processed events
     * because each new event holds a reference to its source. In long simulations or simulations
     * with high event rates, this can lead to substantial memory growth.
     *
     * <p><b>Recommendation:</b> Enable only for debugging sessions, not for production simulations.
     * Combine with {@link #setDebugEvents(boolean)} for complete event causality traces.
     *
     * <p><b>Recovery Guidance:</b> If experiencing memory issues, disable event source tracking
     * and rely on event logging instead.
     *
     * @param track true to enable event source tracking, false to disable
     * @see #isTrackEventSources()
     * @see Event#getSource()
     * @see Event#printTrace()
     */
    void setTrackEventSources(boolean track);

}
