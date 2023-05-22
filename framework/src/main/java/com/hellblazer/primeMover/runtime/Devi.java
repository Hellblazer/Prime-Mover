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

package com.hellblazer.primeMover.runtime;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Logger;

import com.hellblazer.primeMover.Controller;
import com.hellblazer.primeMover.Event;
import com.hellblazer.primeMover.SimulationException;

/**
 * The processor of events, the continuation of time. This is the central
 * control interface of PrimeMover.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
abstract public class Devi implements Controller {
    private static final String   $ENTITY$GEN     = "$entity$gen";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(1);
    private static final Logger   logger          = Logger.getLogger(Devi.class.getCanonicalName());

    private EventImpl                 blockingEvent;
    private EventImpl                 caller;
    private EventImpl                 continuingEvent;
    private EventImpl                 currentEvent;
    private long                      currentTime       = 0;
    private boolean                   debugEvents       = false;
    private Logger                    eventLog;
    private CompletableFuture<Object> futureSailor;
    private Semaphore                 serializer        = new Semaphore(1);
    private final ExecutorService     threadPool;
    private boolean                   trackEventSources = false;

    public Devi() {
        threadPool = Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Advance the current time of the controller
     * 
     * @param duration
     */
    @Override
    public void advance(long duration) {
        final var current = currentTime;
        currentTime = currentTime + duration;
        logger.info("Advancing time from: %s to: %s".formatted(current, currentTime));
    }

    /**
     * Reinitialize the state of the controller
     */
    @Override
    public void clear() {
        currentTime = 0;
        caller = null;
        currentEvent = null;
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
     * @return true if the controller is collecting debug information as to the
     *         source of where the event was raised
     */
    @Override
    public boolean isDebugEvents() {
        return debugEvents;
    }

    /**
     * @return true if the controller is tracking event sources
     */
    @Override
    public boolean isTrackEventSources() {
        return trackEventSources;
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
        assert currentEvent != null;
        assert blockingEvent == null;
        assert continuingEvent == null;

        final var blocking = createEvent(currentTime, entity, event, arguments);
        blockingEvent = blocking;
        continuingEvent = currentEvent.clone(currentTime);
        final var cont = continuingEvent;
        continuingEvent.setContinuation(new Continuation(caller));
        blockingEvent.setContinuation(new Continuation(continuingEvent));
        post(blockingEvent);
        futureSailor.complete(null);
        serializer.release();
        logger.info("Blocking: %s on: %s; continuation: %s".formatted(Thread.currentThread(), blocking, cont));
        LockSupport.park();
        logger.info("Continuing: %s event: %s; from blocking: %s".formatted(Thread.currentThread(), cont, blocking));
        return blocking.getContinuation().returnFrom();
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
     * Set the current time of the controller
     * 
     * @param time
     */
    @Override
    public void setCurrentTime(long time) {
        currentTime = time;
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
     * Configure the logger for tracing all event processing
     * 
     * @param eventLog
     */
    @Override
    public void setEventLogger(Logger eventLog) {
        this.eventLog = eventLog;
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

    protected EventImpl createEvent(long time, EntityReference entity, int event, Object... arguments) {
        Event sourceEvent = trackEventSources ? currentEvent : null;

        if (debugEvents) {
            StackTraceElement[] stackTrace = new Throwable().getStackTrace();
            for (int i = 0; i < stackTrace.length; i++) {
                if (stackTrace[i].getClassName().endsWith($ENTITY$GEN)) {
                    return new EventImpl(stackTrace[i + 1].toString(), time, sourceEvent, entity, event, arguments);
                }
            }
        }
        return new EventImpl(time, sourceEvent, entity, event, arguments);

    }

    /**
     * The heart of the event processing loop. This is where the events are actually
     * evaluated.
     * 
     * @param next - the event to evaluate.
     * @throws SimulationException - if an exception occurs during the evaluation of
     *                             the event.
     */
    protected final void evaluate(EventImpl next) throws SimulationException {
        evaluate(next, DEFAULT_TIMEOUT);
    }

    /**
     * The heart of the event processing loop. This is where the events are actually
     * evaluated.
     * 
     * @param next    - the event to evaluate.
     * @param timeout - how long to wait for the event evaluation
     * @throws SimulationException - if an exception occurs during the evaluation of
     *                             the event.
     */
    protected final void evaluate(EventImpl next, Duration timeout) throws SimulationException {
        try {
            serializer.acquire();
        } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
            return;
        }

        futureSailor = new CompletableFuture<>();
        threadPool.execute(() -> {
            logger.info("evaluating: %s".formatted(next));
            currentEvent = next;
            currentTime = currentEvent.getTime();
            Continuation continuation = currentEvent.getContinuation();
            if (continuation != null) {
                caller = continuation.getCaller();
                logger.info("continuation caller: %s".formatted(caller));
            }
            var thisCaller = caller;
            Throwable exception = null;
            Object result = null;
            try {
                try {
                    if (eventLog != null) {
                        eventLog.info(currentEvent.toString());
                    }
                    result = currentEvent.invoke();
                } catch (SimulationEnd e) {
                    futureSailor.completeExceptionally(e);
                    return;
                } catch (Throwable e) {
                    if (caller == null) {
                        futureSailor.completeExceptionally(new SimulationException(e));
                    }
                    exception = e;
                }
                if (thisCaller != null) {
                    post(thisCaller.resume(currentTime, result, exception));
                }
            } finally {
                currentEvent = null;
                caller = null;
                blockingEvent = null;
                continuingEvent = null;
                if (!futureSailor.isDone()) {
                    logger.info("Evaluation complete: %s at: %s".formatted(currentEvent, currentTime));
                    futureSailor.complete(null);
                    serializer.release();
                } else {
                    logger.info("Evaluation continued: %s at: %s".formatted(currentEvent, currentTime));
                }
            }
        });
        try {
            futureSailor.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new SimulationException(e.getCause());
        } catch (TimeoutException e) {
            throw new SimulationException(e);
        }
    }

    /**
     * Post the event to be evaluated
     * 
     * @param event
     */
    abstract protected void post(EventImpl event);

    /**
     * Swap the calling event for the current caller
     * 
     * @param caller
     * @return
     */
    protected EventImpl swapCaller(EventImpl caller) {
        EventImpl tmp = this.caller;
        this.caller = caller;
        logger.info("Swap caller: %s for: %s".formatted(tmp, caller));
        return tmp;
    }
}
