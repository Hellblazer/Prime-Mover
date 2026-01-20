/**
 * Annotations for marking classes and methods for Prime Mover bytecode transformation. These
 * annotations control how the simulation framework transforms regular Java code into discrete
 * event simulations.
 *
 * <h2>Core Annotations</h2>
 * <dl>
 *   <dt>{@link com.hellblazer.primeMover.annotations.Entity @Entity}</dt>
 *   <dd>Marks a class as a simulation entity. All public methods in the class become events
 *       that are scheduled and executed through the simulation controller. Entity instances
 *       automatically implement {@link com.hellblazer.primeMover.api.EntityReference}.</dd>
 *
 *   <dt>{@link com.hellblazer.primeMover.annotations.Blocking @Blocking}</dt>
 *   <dd>Marks a method that performs blocking operations. Blocking methods suspend the calling
 *       virtual thread using continuations, allowing other events to execute while waiting.
 *       Required for methods that call {@link com.hellblazer.primeMover.api.Kronos#blockingSleep(long)}
 *       or use synchronous channels.</dd>
 *
 *   <dt>{@link com.hellblazer.primeMover.annotations.Event @Event}</dt>
 *   <dd>Explicitly marks a method to be transformed into an event, even if the class-level
 *       policy would exclude it. Useful for selectively including private or protected methods
 *       as events.</dd>
 *
 *   <dt>{@link com.hellblazer.primeMover.annotations.NonEvent @NonEvent}</dt>
 *   <dd>Explicitly excludes a method from event transformation. The method will execute
 *       synchronously in the caller's context rather than being scheduled as an event. Useful
 *       for utility methods, getters, and setters.</dd>
 * </dl>
 *
 * <h2>Transformation Behavior</h2>
 * <p>When a class is marked with {@code @Entity}:
 * <ul>
 *   <li>Public methods become events (scheduled asynchronously) unless marked {@code @NonEvent}</li>
 *   <li>Private/protected methods remain synchronous unless marked {@code @Event}</li>
 *   <li>The class implements {@code EntityReference} for event dispatch</li>
 *   <li>Method calls are rewritten to schedule events through the controller</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Basic Entity</h3>
 * <pre>{@code
 * @Entity
 * public class Server {
 *     // Automatically becomes an event
 *     public void handleRequest(int id) {
 *         processRequest(id);  // Synchronous call to private method
 *     }
 *
 *     // Not an event (private and not marked @Event)
 *     private void processRequest(int id) {
 *         System.out.println("Processing " + id);
 *     }
 *
 *     // Explicitly excluded from events
 *     @NonEvent
 *     public int getActiveConnections() {
 *         return connectionCount;
 *     }
 * }
 * }</pre>
 *
 * <h3>Blocking Operations</h3>
 * <pre>{@code
 * @Entity
 * public class Database {
 *     // Blocking method using continuation
 *     @Blocking
 *     public ResultSet query(String sql) {
 *         // Simulate query latency
 *         Kronos.blockingSleep(databaseLatency);
 *         return executeQuery(sql);
 *     }
 *
 *     // Non-blocking time advance
 *     public void scheduleBackup() {
 *         Kronos.sleep(3600000);  // Schedule 1 hour later
 *         performBackup();
 *     }
 * }
 * }</pre>
 *
 * <h3>Channel Communication</h3>
 * <pre>{@code
 * @Entity
 * public class Producer {
 *     private final SynchronousQueue<Work> channel;
 *
 *     public Producer(SynchronousQueue<Work> channel) {
 *         this.channel = channel;
 *     }
 *
 *     @Blocking
 *     public void produce(Work work) {
 *         // Blocks until consumer receives
 *         channel.put(work);
 *     }
 * }
 *
 * @Entity
 * public class Consumer {
 *     private final SynchronousQueue<Work> channel;
 *
 *     public Consumer(SynchronousQueue<Work> channel) {
 *         this.channel = channel;
 *     }
 *
 *     @Blocking
 *     public void consume() {
 *         // Blocks until producer sends
 *         Work work = channel.take();
 *         processWork(work);
 *     }
 * }
 * }</pre>
 *
 * <h2>Transformation Details</h2>
 * <p>The bytecode transformer (applied via Maven plugin or Java agent):
 * <ol>
 *   <li>Scans classes for {@code @Entity} annotations</li>
 *   <li>Generates {@code EntityReference} implementation with method ordinals</li>
 *   <li>Rewrites method calls to schedule events via {@code Controller.postEvent()}</li>
 *   <li>Inserts continuation points in {@code @Blocking} methods</li>
 *   <li>Replaces {@code Kronos} calls with {@code Kairos} thread-local implementation</li>
 * </ol>
 *
 * <h2>Performance Considerations</h2>
 * <ul>
 *   <li>Use {@code @NonEvent} for frequently called getters/setters to avoid event overhead</li>
 *   <li>Prefer {@link com.hellblazer.primeMover.api.Kronos#sleep(long)} over {@code blockingSleep()}
 *       when continuation is not required (lower overhead)</li>
 *   <li>{@code @Blocking} methods have additional overhead for continuation management</li>
 *   <li>Private helper methods remain synchronous by default (no event scheduling cost)</li>
 * </ul>
 *
 * @see com.hellblazer.primeMover.api
 * @see com.hellblazer.primeMover.api.Kronos
 * @see com.hellblazer.primeMover.api.Controller
 */
package com.hellblazer.primeMover.annotations;
