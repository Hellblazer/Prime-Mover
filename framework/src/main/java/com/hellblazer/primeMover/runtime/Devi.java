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
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.primeMover.runtime;

import com.hellblazer.primeMover.api.Controller;
import com.hellblazer.primeMover.api.Event;
import com.hellblazer.primeMover.api.SimulationException;
import com.hellblazer.primeMover.api.EntityReference;
import com.hellblazer.primeMover.ControllerReport;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The processor of events, the continuation of time. This is the central
 * control interface of PrimeMover.
 * <p>
 * <b>Thread Safety Model: Single Event Processing with Virtual Thread Execution</b>
 * <p>
 * Devi enforces single-threaded event processing semantics using a {@link Semaphore}
 * to serialize event evaluation. Each event executes in its own virtual thread, but
 * only one event evaluates at a time. This provides:
 * <ul>
 *   <li>Predictable, deterministic event ordering</li>
 *   <li>Safe access to controller state during event processing</li>
 *   <li>Efficient blocking via virtual thread continuations</li>
 *   <li>No need for synchronization in entity code</li>
 * </ul>
 * <p>
 * <b>Virtual Thread Execution Model</b>
 * <p>
 * Events execute in virtual threads created by {@link Executors#newVirtualThreadPerTaskExecutor()}.
 * Virtual threads are lightweight (thousands can run concurrently) and use cooperative
 * scheduling. When an event blocks (e.g., {@code Kronos.blockingSleep()}), the virtual
 * thread yields without consuming an OS thread, allowing other events to run efficiently.
 * <p>
 * The {@code serializer} semaphore ensures that despite concurrent virtual thread execution,
 * only one event is actively evaluating at any moment. This maintains simulation determinism
 * while leveraging virtual threads for efficient blocking operations.
 * <p>
 * <b>Thread-Safe Operations</b>
 * <table border="1">
 *   <tr><th>Operation</th><th>Thread-Safe?</th><th>Notes</th></tr>
 *   <tr><td>{@link #postEvent(EntityReference, int, Object...)}</td><td>Implementation-dependent</td><td>Subclasses override {@code post()} with their own thread-safety model</td></tr>
 *   <tr><td>{@link #getCurrentTime()}</td><td>Yes (volatile read)</td><td>Safe to call from any thread, but value may change between calls</td></tr>
 *   <tr><td>{@link #getCurrentEvent()}</td><td>No</td><td>Only safe during event processing from the current event's virtual thread</td></tr>
 *   <tr><td>{@link #setCurrentTime(long)}</td><td>No</td><td>Must be called before simulation starts or from event processing</td></tr>
 *   <tr><td>{@link #advance(long)}</td><td>No</td><td>Only safe from the event processing context</td></tr>
 *   <tr><td>{@link #clear()}</td><td>No</td><td>Only safe when simulation is not running</td></tr>
 * </table>
 * <p>
 * <b>Event Processing Flow</b>
 * <ol>
 *   <li>Event posted via {@code post(EventImpl)} (thread-safety depends on subclass)</li>
 *   <li>{@code evaluate(EventImpl)} acquires the {@code serializer} semaphore</li>
 *   <li>Event invoked in a virtual thread via {@code executor.execute()}</li>
 *   <li>Virtual thread executes entity method, may block and yield</li>
 *   <li>Event completes, releases semaphore, posts continuation/result events</li>
 * </ol>
 * <p>
 * <b>Blocking Event Continuations</b>
 * <p>
 * When an event calls {@code Kronos.blockingSleep()} or similar blocking operations:
 * <ol>
 *   <li>{@link #postContinuingEvent} creates a blocking event and a continuation event</li>
 *   <li>The continuation event captures the current virtual thread continuation state</li>
 *   <li>The blocking event is posted and evaluated normally</li>
 *   <li>When the blocking event completes, the continuation event is posted</li>
 *   <li>The continuation resumes the original virtual thread from its park point</li>
 * </ol>
 * Virtual threads make this efficient because parking doesn't block OS threads.
 * <p>
 * <b>External Synchronization Required For:</b>
 * <ul>
 *   <li>Modifying controller configuration during simulation ({@code setCurrentTime}, {@code setDebugEvents}, etc.)</li>
 *   <li>Accessing statistics or state from threads outside event processing</li>
 *   <li>Multiple threads posting events (depends on subclass {@code post()} implementation)</li>
 * </ul>
 * <p>
 * <b>Subclass Thread-Safety Responsibility</b>
 * <p>
 * Subclasses must implement {@code post(EventImpl)} with their own thread-safety model:
 * <ul>
 *   <li>{@link com.hellblazer.primeMover.controllers.SimulationController}: Single-threaded, no locking</li>
 *   <li>{@link com.hellblazer.primeMover.controllers.RealTimeController}: Thread-safe with {@code ReentrantLock}</li>
 *   <li>{@link com.hellblazer.primeMover.controllers.SteppingController}: Single-threaded, no locking</li>
 * </ul>
 * <p>
 * <b>Example Usage: Single-Threaded Event Posting</b>
 * <pre>{@code
 * var controller = new SimulationController();
 *
 * // Configuration must happen before simulation starts
 * controller.setCurrentTime(0);
 * controller.setEndTime(1_000_000);
 *
 * // Post initial events (single-threaded)
 * entity.someMethod(); // Transformed to post event
 *
 * // Run simulation (blocks until completion)
 * controller.eventLoop();
 *
 * // Read results after completion (safe)
 * long endTime = controller.getCurrentTime();
 * }</pre>
 * <p>
 * <b>Example Usage: Multi-Threaded Event Posting</b>
 * <pre>{@code
 * var controller = new RealTimeController("My Simulation");
 *
 * // Start animation thread
 * controller.start();
 *
 * // Multiple threads can post events concurrently
 * executor.submit(() -> entity1.action());
 * executor.submit(() -> entity2.action());
 *
 * // Stop and read statistics
 * controller.stop();
 * int events = controller.getTotalEvents();
 * }</pre>
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
abstract public class Devi implements Controller, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(Devi.class);
    private final    ExecutorService                     executor;
    private final    Semaphore                           serializer        = new Semaphore(1);
    private volatile EventImpl                           caller;
    private volatile EventImpl                           currentEvent;
    private volatile long                                currentTime       = 0;
    private          boolean                             debugEvents       = false;
    private          Logger                              eventLog;
    private volatile CompletableFuture<EvaluationResult> futureSailor;
    private          boolean                             trackEventSources = false;

    // Statistics tracking infrastructure (subclasses can override for thread-safety)
    protected String               name            = "Simulation";
    protected long                 simulationStart = 0;
    protected long                 simulationEnd   = 0;
    protected int                  totalEvents     = 0;
    protected Map<String, Integer> spectrum        = new HashMap<>();
    protected boolean              trackSpectrum   = false;

    public Devi() {
        executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Advance the current time of the controller
     *
     * @param duration
     */
    @Override
    public void advance(long duration) {
        final var current = currentTime;
        currentTime = current + duration;
    }

    /**
     * Reinitialize the state of the controller
     */
    @Override
    public void clear() {
        currentTime = 0;
        caller = null;
        currentEvent = null;
        futureSailor = null;
    }

    @Override
    public void close() throws Exception {
        executor.close();
    }

    /**
     * Answer the current event of the controller
     *
     * @return
     */
    @Override
    public Event getCurrentEvent() {
        return currentEvent;
    }

    /**
     * Answer the current instant in time of the controller
     *
     * @return
     */
    @Override
    public long getCurrentTime() {
        return currentTime;
    }

    /**
     * Set the current time of the controller
     *
     * @param time
     */
    @Override
    public void setCurrentTime(long time) {
        currentTime = time;
    }

    /**
     * @return true if the controller is collecting debug information as to the
     *         source of where the event was raised
     */
    @Override
    public boolean isDebugEvents() {
        return debugEvents;
    }

    /**
     * Configure the collecting of debug information for raised events. When debug
     * is enabled, the controller will record the source location where an event was
     * raised. The collection of debug information for events is expensive and
     * significantly impacts the performance of the simulation event processing.
     *
     * @param debug - true to trigger the collecting of event debug information
     */
    @Override
    public void setDebugEvents(boolean debug) {
        debugEvents = debug;
    }

    /**
     * @return true if the controller is tracking event sources
     */
    @Override
    public boolean isTrackEventSources() {
        return trackEventSources;
    }

    /**
     * Configure whether the controller will track the source event of a raised
     * event. Tracking event sources has garbage collection implications, as event
     * chains prevent the elimantion of previous events which have already been
     * processed.
     *
     * @param track - true to track event sources
     */
    @Override
    public void setTrackEventSources(boolean track) {
        trackEventSources = track;
    }

    /**
     * Post the event to be evaluated. The event is blocking, meaning that it will
     * cause the caller to block execution until the event is processed, continuing
     * with the result of the blocking event
     *
     * @param entity    - the target of the event
     * @param event     - the event
     * @param arguments - the arguments to the event
     * @return
     * @throws Throwable
     */
    @Override
    public Object postContinuingEvent(EntityReference entity, int event, Object... arguments) throws Throwable {
        final var current = currentEvent;
        final var sailorMoon = futureSailor;

        assert current != null : "no current event";
        assert sailorMoon != null : "No future to signal";
        assert !sailorMoon.isDone() : "Future sailor is already done";

        final var ct = currentTime;
        final var continuingEvent = current.clone(ct);
        var blockingEvent = createEvent(ct, entity, event, arguments);
        return continuingEvent.park(sailorMoon, new EvaluationResult(blockingEvent, continuingEvent));
    }

    /**
     * Post the event to be evaluated
     *
     * @param entity    - the target of the event
     * @param event     - the event event
     * @param arguments - the arguments to the event
     */
    @Override
    public void postEvent(EntityReference entity, int event, Object... arguments) {
        post(createEvent(currentTime, entity, event, arguments));
    }

    /**
     * Post the event to be evaluated at the specified instant in time
     *
     * @param time      - the instant in time the event is to be processed
     * @param entity    - the target of the event
     * @param event     - the event event
     * @param arguments - the arguments to the event
     */
    @Override
    public void postEvent(long time, EntityReference entity, int event, Object... arguments) {
        post(createEvent(time, entity, event, arguments));
    }

    /**
     * Configure the logger for tracing all event processing
     *
     * @param eventLog
     */
    @Override
    public void setEventLogger(Logger eventLog) {
        this.eventLog = eventLog;
    }


    protected EventImpl createEvent(long time, EntityReference entity, int event, Object... arguments) {
        Event sourceEvent = trackEventSources ? currentEvent : null;

        if (debugEvents) {
            // Use getName() instead of getCanonicalName() for reliable matching
            // getCanonicalName() can return null for anonymous/local classes
            final var entityClassName = entity.getClass().getName();
            var frame = StackWalker.getInstance()
                                   .walk(stream -> stream.dropWhile(f -> !f.getClassName().equals(entityClassName))
                                                         .skip(1)
                                                         .findFirst()
                                                         .map(StackWalker.StackFrame::toStackTraceElement)
                                                         .orElse(null));
            if (frame != null) {
                return new EventImpl(frame.toString(), time, sourceEvent, entity, event, arguments);
            }
        }
        return new EventImpl(time, sourceEvent, entity, event, arguments);
    }

    /**
     * The heart of the event processing loop. This is where the events are
     * evaluated.
     *
     * @param next - the event to evaluate.
     * @throws SimulationException - if an exception occurs during the evaluation of
     *                             the event.
     */
    protected final void evaluate(EventImpl next) throws SimulationException {
        try {
            serializer.acquire();
            assert caller == null;
            assert futureSailor == null;
            assert currentEvent == null;
            evaluation(next);
        } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
        } finally {
            caller = null;
            serializer.release();
        }
    }

    /**
     * Post the event to be evaluated
     *
     * @param event
     */
    /**
     * Post the event to be evaluated. Made public to allow blocking primitives
     * in other packages to schedule continuation events.
     */
    abstract public void post(EventImpl event);

    /**
     * Swaps the current event caller for continuation management in blocking primitives.
     * <p>
     * <b>Internal SPI Method - Do Not Use Directly</b>
     * <p>
     * This method is part of the blocking primitives Service Provider Interface (SPI) and is used
     * exclusively by blocking primitive implementations (SimSignal, SimCondition, SimQueue, etc.)
     * to manage event continuations across primitive boundaries. Direct use outside the blocking
     * primitives SPI is not supported and will cause undefined behavior.
     * <p>
     * <b>Purpose</b>
     * <p>
     * Blocking primitives must temporarily capture and restore the caller context when:
     * <ul>
     *   <li>An entity blocks waiting on a resource (queue, condition, signal)</li>
     *   <li>The primitive needs to defer continuation until resource availability</li>
     *   <li>Multiple entities may wait and resume in FIFO/priority order</li>
     * </ul>
     * The primitive calls {@code swapCaller(null)} to capture the current caller,
     * stores it internally, then later restores it via {@code swapCaller(savedCaller)}
     * when resuming the blocked entity.
     * <p>
     * <b>Threading Model</b>
     * <p>
     * This method is <b>not inherently thread-safe</b>. It relies on Devi's single-threaded
     * event processing guarantee enforced by the {@code serializer} semaphore. Only one event
     * evaluates at a time, so {@code caller} access is serialized. The method must only be
     * called from within event processing context (inside an {@code @Entity} method or blocking
     * primitive SPI implementation).
     * <p>
     * <b>Example Usage (Internal SPI)</b>
     * <pre>{@code
     * // In blocking primitive implementation (e.g., SimQueue.enqueue)
     * public void passivate(ProcessEntity entity) {
     *     // Capture current caller for later resumption
     *     EventImpl savedCaller = swapCaller(null);
     *
     *     // Store in wait queue
     *     waitQueue.add(new QueueEntry(entity, savedCaller));
     *
     *     // When resource available, restore caller and post continuation
     *     QueueEntry next = waitQueue.poll();
     *     swapCaller(next.caller);
     *     // Primitive schedules entity.activate() event
     * }
     * }</pre>
     * <p>
     * <b>Related SPI Documentation</b>
     * <ul>
     *   <li>{@link #post(EventImpl)} - Posts events for continuation scheduling</li>
     *   <li>{@link EventImpl#getContinuation()} - Accesses continuation state</li>
     *   <li>{@link EventImpl#setTime(long)} - Adjusts event timing for deferred resume</li>
     *   <li>BEAD-10 (thread safety) - Complete threading model documentation</li>
     * </ul>
     * <p>
     * Visibility changed from protected to public in BEAD-06 to support blocking primitives
     * refactored into the desmoj-ish module while maintaining runtime module encapsulation.
     *
     * @param newCaller the new caller to set, or null to clear current caller for capture
     * @return the previous caller event before the swap (for capture/restore pattern)
     */
    public EventImpl swapCaller(EventImpl newCaller) {
        var tmp = caller;
        caller = newCaller;
        return tmp;
    }

    /**
     * Answer the name of this controller.
     * Subclasses must implement to provide controller identification.
     *
     * @return the controller name
     */
    public abstract String getName();

    /**
     * Answer the simulation clock at the beginning of the simulation.
     * Subclasses must implement to track simulation start time.
     *
     * @return the simulation start time
     */
    public abstract long getSimulationStart();

    /**
     * Answer the simulation clock at the end of the simulation.
     * Subclasses must implement to track simulation end time.
     *
     * @return the simulation end time
     */
    public abstract long getSimulationEnd();

    /**
     * Answer the spectrum of events.
     * Subclasses must implement to provide event signature tracking.
     *
     * @return a Map where the key is the signature of the event, and the value
     *         is the number of times the event was invoked
     */
    public abstract Map<String, Integer> getSpectrum();

    /**
     * Answer the total number of events processed during the simulation.
     * Subclasses must implement to track event count.
     *
     * @return the total number of events processed
     */
    public abstract int getTotalEvents();

    /**
     * Helper method for recording event execution.
     * Subclasses can override for different thread-safety models.
     *
     * @param event the event being recorded
     */
    protected void recordEvent(EventImpl event) {
        totalEvents++;
        if (trackSpectrum) {
            spectrum.merge(event.getSignature(), 1, Integer::sum);
        }
    }

    /**
     * Generate a report of the simulation statistics.
     * Uses abstract methods to gather statistics from subclass implementations.
     *
     * @return a ControllerReport containing all statistics
     */
    public ControllerReport report() {
        return new ControllerReport(
            getName(),
            getSimulationStart(),
            getSimulationEnd(),
            getTotalEvents(),
            getSpectrum()
        );
    }

    private Runnable eval(EventImpl event) {
        return () -> {
            Devi prev = Framework.getCurrentController();
            try {
                Framework.setController(this);
                if (eventLog != null) {
                    eventLog.info(event.toString());
                }
                final var result = event.invoke();
                if (futureSailor.isDone()) {
                    logger.error("[Devi] Event continuation already completed at time {}: {}",
                                currentTime, event.getSignature());
                }
                futureSailor.complete(new EvaluationResult(result));
            } catch (SimulationEnd e) {
                logger.info("[Devi] Simulation ended at time {}", currentTime);
                futureSailor.completeExceptionally(e);
                return;
            } catch (Throwable e) {
                futureSailor.completeExceptionally(e);
            } finally {
                Framework.setController(prev);
            }
        };
    }

    private void evaluation(EventImpl next) throws SimulationException {
        logger.trace("evaluating: {}", next);
        final var sailorMoon = futureSailor = new CompletableFuture<>();
        currentEvent = next;
        currentTime = next.getTime();
        caller = next.getCaller();
        if (next.isContinuation()) {
            next.proceed();
        } else {
            executor.execute(eval(next));
        }
        EvaluationResult result = null;
        try {
            result = sailorMoon.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        } catch (ExecutionException e) {
            if (e.getCause() instanceof SimulationEnd se) {
                throw se;
            }
            if (e.getCause() instanceof SimulationException se) {
                throw se;
            }
            var entityName = next.getReference() != null
                             ? next.getReference().getClass().getSimpleName()
                             : "unknown";
            throw new SimulationException(
                "[Devi] Event evaluation failed for entity " + entityName +
                " at time " + currentTime + ": " + next.getSignature(),
                e.getCause());
        } finally {
            futureSailor = null;
            currentEvent = null;
        }

        assert result != null;

        if (result.t != null) {
            var entityName = next.getReference() != null
                             ? next.getReference().getClass().getSimpleName()
                             : "unknown";
            logger.error("[Devi] Event evaluation failed for entity {} at time {}: {}",
                        entityName, currentTime, next.getSignature(), result.t);
            if (result.t instanceof SimulationException se) {
                throw se;
            }
            if (result.t instanceof SimulationEnd se) {
                throw se;
            }
            throw new SimulationException(
                "[Devi] Event evaluation failed for entity " + entityName +
                " at time " + currentTime + ": " + next.getSignature(),
                result.t);
        }

        final var cc = caller;
        if (result.blockingEvent != null) {
            result.continuingEvent.setCaller(cc);
            result.blockingEvent.setCaller(result.continuingEvent);
            post(result.blockingEvent);
        } else if (cc != null) {
            final var ct = currentTime;
            post(cc.resume(ct, result.result, result.t));
        }
    }

    record EvaluationResult(Throwable t, Object result, EventImpl blockingEvent, EventImpl continuingEvent) {

        EvaluationResult(Object o) {
            this(null, o, null, null);
        }

        public EvaluationResult(Throwable t) {
            this(t, null, null, null);
        }

        EvaluationResult(EventImpl blocking, EventImpl continuing) {
            this(null, null, blocking, continuing);
        }
    }
}
