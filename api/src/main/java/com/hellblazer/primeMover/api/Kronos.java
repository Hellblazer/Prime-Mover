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

import com.hellblazer.primeMover.annotations.Blocking;

import java.lang.reflect.Method;

/**
 * The static API facade for the Prime Mover simulation kernel. This class provides the primary interface that
 * simulation code uses to interact with the event-driven simulation framework.
 *
 * <p>At compile time, calls to these methods are placeholders that throw {@link UnsupportedOperationException}.
 * During bytecode transformation (either at build time via the Maven plugin or at runtime via the Java agent),
 * these method calls are rewritten to invoke the actual runtime implementation through {@code Kairos}.
 *
 * <p><b>Thread Safety:</b> All methods delegate to thread-local controller instances, making them safe to call
 * from simulation entities running in different virtual threads.
 *
 * <p><b>Usage Example:</b>
 * <pre>{@code
 * @Entity
 * public class SimulatedServer {
 *     public void handleRequest() {
 *         // Non-blocking time advance - schedules continuation at future time
 *         Kronos.sleep(100); // Process after 100 time units
 *
 *         processRequest();
 *
 *         // Blocking time advance - suspends this event's virtual thread
 *         Kronos.blockingSleep(50); // Wait 50 time units before continuing
 *
 *         sendResponse();
 *     }
 * }
 * }</pre>
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * @see Controller
 * @see com.hellblazer.primeMover.annotations.Entity
 * @see com.hellblazer.primeMover.annotations.Blocking
 */
public class Kronos {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Kronos() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Advances simulation time by the specified duration, blocking the calling event until that simulation
     * time is reached. This method suspends the current virtual thread using a continuation, allowing other
     * events to execute while this event waits.
     *
     * <p>This method must be used within an {@link com.hellblazer.primeMover.annotations.Entity @Entity}
     * method that is marked with {@link Blocking @Blocking}, as it requires continuation support.
     *
     * <p><b>Use Cases:</b>
     * <ul>
     *   <li>Simulating blocking I/O operations (database queries, network requests)</li>
     *   <li>Modeling resource acquisition delays where the entity must wait</li>
     *   <li>Implementing synchronous communication patterns between entities</li>
     * </ul>
     *
     * <p><b>Example:</b>
     * <pre>{@code
     * @Blocking
     * public void fetchData() {
     *     // Simulate network round-trip delay
     *     Kronos.blockingSleep(latencyMs);
     *     // Execution resumes here after latencyMs simulation time units
     *     return processData();
     * }
     * }</pre>
     *
     * @param duration the number of simulation time units to advance (must be non-negative)
     * @throws IllegalArgumentException if duration is negative
     * @see #sleep(long)
     * @see Blocking
     */
    @Blocking
    public static void blockingSleep(long duration) {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }

    /**
     * Schedules a static method to be invoked as an event at the specified absolute simulation time.
     * The method will execute when the simulation clock reaches the given time instant.
     *
     * <p><b>Note:</b> For most use cases, entity instance methods are preferred over static methods.
     * Static events are primarily useful for simulation initialization and global coordination.
     *
     * @param time the absolute simulation time at which to invoke the method
     * @param method the static method to invoke (must be static)
     * @param arguments the arguments to pass to the method (must match method signature)
     * @throws IllegalArgumentException if time is negative or method is not static
     * @see #callStatic(Method, Object...)
     */
    public static void callStatic(long time, Method method, Object... arguments) {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }

    /**
     * Schedules a static method to be invoked as an event at the current simulation time.
     * Equivalent to {@code callStatic(currentTime(), method, arguments)}.
     *
     * @param method the static method to invoke (must be static)
     * @param arguments the arguments to pass to the method (must match method signature)
     * @throws IllegalArgumentException if method is not static
     * @see #callStatic(long, Method, Object...)
     */
    public static void callStatic(Method method, Object... arguments) {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }

    /**
     * Creates a synchronous communication channel for inter-entity message passing within the simulation.
     * Channels provide blocking send/receive semantics that respect simulation time.
     *
     * <p>Channels enable CSP-style (Communicating Sequential Processes) coordination patterns between
     * simulation entities. Send operations block until a receiver is ready, and receive operations block
     * until a sender provides data.
     *
     * <p><b>Example:</b>
     * <pre>{@code
     * // Producer entity
     * SynchronousQueue<WorkItem> queue = Kronos.createChannel(WorkItem.class);
     *
     * @Blocking
     * public void produce() {
     *     WorkItem item = createWork();
     *     queue.put(item); // Blocks until consumer receives
     * }
     *
     * // Consumer entity
     * @Blocking
     * public void consume() {
     *     WorkItem item = queue.take(); // Blocks until producer sends
     *     processItem(item);
     * }
     * }</pre>
     *
     * @param <T> the type of elements transferred through the channel
     * @param elementType the class object representing the element type
     * @return a new synchronous queue for simulation-time communication
     * @see SynchronousQueue
     */
    public static <T> SynchronousQueue<T> createChannel(Class<T> elementType) {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }

    /**
     * Returns the current simulation time as maintained by the thread's controller.
     * The simulation time is a logical clock that advances as events are processed.
     *
     * <p><b>Note:</b> Simulation time is distinct from wall-clock time. Multiple events may
     * execute at the same simulation time instant, representing concurrent activities in the
     * simulated system.
     *
     * @return the current simulation time in arbitrary time units (typically milliseconds or microseconds)
     * @see Controller#getCurrentTime()
     */
    public static long currentTime() {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }

    /**
     * Terminates the simulation immediately after processing all events at the current time instant.
     * Equivalent to {@code endSimulationAt(currentTime() + 1)}.
     *
     * <p>All events scheduled for the current time will complete, but no future events will execute.
     *
     * @see #endSimulationAt(long)
     */
    public static void endSimulation() {
        endSimulationAt(currentTime() + 1);
    }

    /**
     * Schedules the simulation to terminate at the specified absolute time. All events scheduled
     * at or before the given time will execute; events scheduled after will be discarded.
     *
     * <p><b>Use Cases:</b>
     * <ul>
     *   <li>Ending simulations after a fixed duration</li>
     *   <li>Implementing timeout conditions</li>
     *   <li>Coordinated shutdown triggered by a specific event</li>
     * </ul>
     *
     * @param time the absolute simulation time at which to end (must not be before current time)
     * @throws IllegalArgumentException if time is before the current simulation time
     */
    public static void endSimulationAt(long time) {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }

    /**
     * Returns the simulation controller associated with the current thread. Each virtual thread
     * executing simulation events has an associated controller that manages event scheduling and
     * time advancement.
     *
     * @return the current thread's controller (never null)
     * @throws IllegalStateException if there is no controller set for the current thread
     *         (typically indicates calling from non-simulation code)
     * @see #queryController()
     * @see Controller
     */
    public static Controller getController() {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }

    /**
     * Returns the simulation controller associated with the current thread, or null if none exists.
     * This is a non-throwing variant of {@link #getController()} for use in contexts where it's
     * acceptable for no controller to be present.
     *
     * @return the current thread's controller, or null if the thread is not executing simulation code
     * @see #getController()
     */
    public static Controller queryController() {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }

    /**
     * Schedules a runnable to execute as an event at the current simulation time. The runnable
     * will be processed in the normal event queue order.
     *
     * <p><b>Use Case:</b> Wrapping non-entity code (lambdas, callbacks) to execute within the
     * simulation time framework.
     *
     * <p><b>Example:</b>
     * <pre>{@code
     * Kronos.run(() -> {
     *     logger.info("Event triggered at time {}", Kronos.currentTime());
     *     notifyObservers();
     * });
     * }</pre>
     *
     * @param r the runnable to schedule (must not be null)
     * @throws NullPointerException if r is null
     * @see #runAt(Runnable, long)
     */
    public static void run(Runnable r) {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }

    /**
     * Schedules a runnable to execute as an event at the specified absolute simulation time.
     *
     * @param r the runnable to schedule (must not be null)
     * @param instant the absolute simulation time at which to execute the runnable
     * @throws NullPointerException if r is null
     * @throws IllegalArgumentException if instant is negative
     * @see #run(Runnable)
     */
    public static void runAt(Runnable r, long instant) {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }

    /**
     * Associates a simulation controller with the current thread. This method is typically called
     * by the simulation framework infrastructure, not by simulation code.
     *
     * <p><b>Note:</b> End users rarely need to call this directly. Controllers are automatically
     * set when events execute in virtual threads managed by the simulation framework.
     *
     * @param controller the controller to associate with the current thread (may be null to clear)
     * @see #getController()
     */
    public static void setController(Controller controller) {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }

    /**
     * Returns whether the simulation is currently active and processing events.
     *
     * @return true if the simulation is running, false if it has ended or not started
     */
    public static boolean simulationIsRunning() {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }

    /**
     * Advances simulation time by the specified duration without blocking the calling event.
     * The current event completes immediately, and any continuation executes at the future time.
     *
     * <p>This is the standard non-blocking time advance mechanism. Unlike {@link #blockingSleep(long)},
     * this method does not suspend the virtual thread—it schedules the continuation of execution
     * for a future simulation time.
     *
     * <p><b>Typical Usage:</b>
     * <pre>{@code
     * public void processRequest() {
     *     // Schedule next processing step 100 time units in the future
     *     Kronos.sleep(100);
     *     // Code here will not execute until simulation time advances by 100
     *     sendResponse();
     * }
     * }</pre>
     *
     * @param duration the number of simulation time units to advance (must be non-negative)
     * @throws IllegalArgumentException if duration is negative
     * @see #blockingSleep(long)
     */
    public static void sleep(long duration) {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }
}
