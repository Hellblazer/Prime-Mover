/**
 * Core API for the Prime Mover discrete event simulation framework. This package provides the
 * public interfaces and classes that simulation code uses to interact with the simulation kernel.
 *
 * <h2>Primary Entry Points</h2>
 * <ul>
 *   <li>{@link com.hellblazer.primeMover.api.Kronos} - Static API facade for simulation operations
 *       (time advancement, channel creation, simulation control)</li>
 *   <li>{@link com.hellblazer.primeMover.api.Controller} - Core interface for managing simulation
 *       state, event scheduling, and time management</li>
 *   <li>{@link com.hellblazer.primeMover.api.EntityReference} - Interface for simulation entities
 *       that enables event-based method dispatch</li>
 *   <li>{@link com.hellblazer.primeMover.api.Event} - Interface representing scheduled events with
 *       metadata for debugging and introspection</li>
 * </ul>
 *
 * <h2>Framework Architecture</h2>
 * <p>Prime Mover is an event-driven simulation framework that converts regular Java code into
 * discrete event simulations through bytecode transformation. Classes annotated with
 * {@link com.hellblazer.primeMover.annotations.Entity @Entity} have their method calls automatically
 * transformed into events scheduled through a {@link com.hellblazer.primeMover.api.Controller}.
 *
 * <h3>Key Concepts</h3>
 * <dl>
 *   <dt><b>Simulation Time</b></dt>
 *   <dd>A logical clock that advances discretely as events are processed. Multiple events may
 *       execute at the same simulation time instant, representing concurrent activities in the
 *       simulated system. Simulation time is distinct from wall-clock time.</dd>
 *
 *   <dt><b>Events</b></dt>
 *   <dd>Method invocations scheduled at specific simulation times. Events are processed in
 *       time-ordered sequence by the controller.</dd>
 *
 *   <dt><b>Entities</b></dt>
 *   <dd>Simulation participants marked with {@code @Entity}. Entity method calls become events
 *       that execute in virtual threads managed by the framework.</dd>
 *
 *   <dt><b>Blocking Operations</b></dt>
 *   <dd>Methods marked with {@code @Blocking} suspend the calling virtual thread using
 *       continuations, allowing other events to execute while waiting. Used for synchronous
 *       communication patterns like channel operations.</dd>
 * </dl>
 *
 * <h2>Basic Usage</h2>
 * <pre>{@code
 * // Define a simulation entity
 * @Entity
 * public class Server {
 *     public void processRequest(int id) {
 *         // Non-blocking delay - schedules continuation
 *         Kronos.sleep(100);
 *
 *         System.out.println("Processing request " + id);
 *
 *         // Blocking delay - suspends this event
 *         Kronos.blockingSleep(50);
 *
 *         System.out.println("Completed request " + id);
 *     }
 * }
 *
 * // Run the simulation
 * public class Main {
 *     public static void main(String[] args) {
 *         SimulationController controller = new SimulationController();
 *         Kronos.setController(controller);
 *
 *         Server server = new Server();
 *         server.processRequest(1);  // Scheduled as event
 *         server.processRequest(2);  // Scheduled as event
 *
 *         controller.run();  // Execute until no more events
 *     }
 * }
 * }</pre>
 *
 * <h2>Bytecode Transformation</h2>
 * <p>At compile time (or runtime with the Java agent), the framework transforms:
 * <ul>
 *   <li>{@code Kronos} method calls → {@code Kairos} thread-local controller calls</li>
 *   <li>Entity method calls → Event scheduling via {@code Controller.postEvent()}</li>
 *   <li>{@code @Blocking} methods → Continuation-based suspension points</li>
 * </ul>
 *
 * <h2>Thread Model</h2>
 * <p>Prime Mover uses Java Virtual Threads (Project Loom) to execute events. Each event runs in
 * a virtual thread, enabling blocking operations through continuations without consuming platform
 * threads. This allows simulations with millions of concurrent entities.
 *
 * <h2>Channel Communication</h2>
 * <p>The framework provides CSP-style channels via {@link com.hellblazer.primeMover.api.SynchronousQueue}
 * created through {@link com.hellblazer.primeMover.api.Kronos#createChannel(Class)}. Channels enable
 * synchronous message passing between entities, with blocking semantics that respect simulation time.
 *
 * @see com.hellblazer.primeMover.annotations
 * @see com.hellblazer.primeMover.api.Kronos
 * @see com.hellblazer.primeMover.api.Controller
 */
package com.hellblazer.primeMover.api;
