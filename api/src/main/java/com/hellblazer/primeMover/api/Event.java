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
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.primeMover.api;

import java.io.PrintStream;

/**
 * Represents a scheduled event in the simulation. Events encapsulate method invocations on
 * simulation entities that occur at specific points in simulation time.
 *
 * <p>Events provide metadata about scheduled operations including:
 * <ul>
 *   <li>The method signature being invoked</li>
 *   <li>The scheduled simulation time</li>
 *   <li>The event source chain (if tracking is enabled)</li>
 * </ul>
 *
 * <p><b>Event Causality:</b> When event source tracking is enabled via
 * {@link Controller#setTrackEventSources(boolean)}, events maintain references to the events
 * that caused them to be scheduled. This enables causal analysis and debugging through
 * {@link #getSource()} and {@link #printTrace()}.
 *
 * <p><b>Thread Safety:</b> Event objects are immutable after creation and safe to access from
 * multiple threads.
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * @see Controller
 * @see Controller#getCurrentEvent()
 * @see Controller#setTrackEventSources(boolean)
 */
public interface Event {

    /**
     * Returns the string representation of the method signature for this event. The signature
     * typically includes the class name, method name, and parameter types.
     *
     * <p><b>Example Signature:</b>
     * {@code "com.example.Server.processRequest(int, String)"}
     *
     * <p><b>Usage:</b> Primarily used for logging, debugging, and event introspection.
     *
     * @return the method signature as a string (never null)
     */
    String getSignature();

    /**
     * Returns the event that caused this event to be scheduled. Event sources form a causal
     * chain that can be traversed back to the original triggering event.
     *
     * <p><b>Availability:</b> This method only returns non-null values when event source tracking
     * is enabled via {@link Controller#setTrackEventSources(boolean)}. If tracking is disabled,
     * this method returns null.
     *
     * <p><b>Usage:</b> Use this for debugging causality issues—understanding why a particular
     * event was scheduled. Combined with {@link #printTrace()}, this provides complete causal
     * history.
     *
     * <p><b>Example:</b>
     * <pre>{@code
     * Event current = controller.getCurrentEvent();
     * Event source = current.getSource();
     * while (source != null) {
     *     System.out.println("Caused by: " + source.getSignature());
     *     source = source.getSource();
     * }
     * }</pre>
     *
     * @return the event that scheduled this event, or null if no source is available
     * @see Controller#setTrackEventSources(boolean)
     * @see #printTrace()
     */
    Event getSource();

    /**
     * Returns the simulation time at which this event is scheduled to execute. For events
     * currently executing, this is the time at which execution began.
     *
     * <p>The time is measured in the simulation's logical time units (commonly milliseconds
     * or microseconds), not wall-clock time.
     *
     * @return the scheduled simulation time (non-negative)
     * @see Controller#getCurrentTime()
     */
    long getTime();

    /**
     * Prints the complete causal chain of events that led to this event to {@code System.out}.
     * Each line shows an event's signature and scheduled time, with the most recent event first.
     *
     * <p><b>Availability:</b> Produces meaningful output only when event source tracking is
     * enabled via {@link Controller#setTrackEventSources(boolean)}. Otherwise, only this event
     * is printed.
     *
     * <p><b>Example Output:</b>
     * <pre>
     * Event at time 1500: com.example.Server.sendResponse()
     *   Caused by time 1450: com.example.Server.processRequest(int)
     *   Caused by time 1400: com.example.Client.makeRequest()
     * </pre>
     *
     * @see #printTrace(PrintStream)
     * @see Controller#setTrackEventSources(boolean)
     */
    void printTrace();

    /**
     * Prints the complete causal chain of events that led to this event to the specified
     * print stream. This is identical to {@link #printTrace()} but allows directing output
     * to any print stream.
     *
     * <p><b>Use Cases:</b>
     * <ul>
     *   <li>Logging to files for post-mortem analysis</li>
     *   <li>Capturing traces in test assertions</li>
     *   <li>Redirecting to custom logging frameworks</li>
     * </ul>
     *
     * <p><b>Example:</b>
     * <pre>{@code
     * try (PrintStream file = new PrintStream("event-trace.txt")) {
     *     controller.getCurrentEvent().printTrace(file);
     * }
     * }</pre>
     *
     * @param stream the print stream to write the trace to (must not be null)
     * @throws NullPointerException if stream is null
     * @see #printTrace()
     */
    void printTrace(PrintStream stream);

}
