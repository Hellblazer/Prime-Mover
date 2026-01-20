/**
 * Copyright (C) 2008 Hal Hildebrand. All rights reserved.
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

/**
 * Exception thrown when an error occurs during simulation execution. SimulationException serves
 * as the base exception type for all Prime Mover simulation-related errors, including event
 * processing failures, continuation errors, and simulation control issues.
 *
 * <p><b>Common Causes:</b>
 * <ul>
 *   <li>Entity method threw an unhandled exception during event processing</li>
 *   <li>Blocking operation failed or was interrupted (e.g., {@link Kronos#blockingSleep(long)})</li>
 *   <li>Invalid simulation state or configuration</li>
 *   <li>Continuation/virtual thread management errors</li>
 * </ul>
 *
 * <p><b>Recovery Guidance:</b>
 * When catching SimulationException, examine the cause chain using {@link #getCause()} to identify
 * the root problem. For event processing errors, enable debug mode with
 * {@link Controller#setDebugEvents(boolean)} and event source tracking with
 * {@link Controller#setTrackEventSources(boolean)} to trace the event chain leading to the failure.
 *
 * <p><b>Example:</b>
 * <pre>{@code
 * try {
 *     controller.runSimulation();
 * } catch (SimulationException e) {
 *     logger.error("Simulation failed at time {}", controller.getCurrentTime(), e);
 *     if (e.getCause() != null) {
 *         logger.error("Root cause: {}", e.getCause().getMessage());
 *     }
 *     // Examine event that caused the failure
 *     Event failedEvent = controller.getCurrentEvent();
 *     if (failedEvent != null) {
 *         failedEvent.printTrace();
 *     }
 * }
 * }</pre>
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * @see Controller
 * @see Event
 * @see Kronos
 */
public class SimulationException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new simulation exception with no detail message.
     */
    public SimulationException() {
        super();
    }

    /**
     * Constructs a new simulation exception with the specified detail message.
     *
     * @param message the detail message describing the error
     */
    public SimulationException(String message) {
        super(message);
    }

    /**
     * Constructs a new simulation exception with the specified detail message and cause.
     * This constructor is typically used when wrapping lower-level exceptions that occurred
     * during simulation execution.
     *
     * @param message the detail message describing the error
     * @param cause the underlying cause of the simulation failure (may be null)
     */
    public SimulationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new simulation exception with the specified cause. The detail message
     * is derived from the cause's message if available.
     *
     * @param cause the underlying cause of the simulation failure (may be null)
     */
    public SimulationException(Throwable cause) {
        super(cause);
    }
}
