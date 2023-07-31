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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
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

    private static final String $ENTITY$GEN = "$entity$gen";
    private static final Logger logger      = Logger.getLogger(Devi.class.getCanonicalName());

    private volatile EventImpl                           caller;
    private volatile EventImpl                           currentEvent;
    private volatile long                                currentTime       = 0;
    private boolean                                      debugEvents       = false;
    private Logger                                       eventLog;
    private volatile CompletableFuture<EvaluationResult> futureSailor;
    private final Semaphore                              serializer        = new Semaphore(1);
    private final ThreadFactory                          tf;
    private boolean                                      trackEventSources = false;

    public Devi() {
        tf = Thread.ofVirtual().uncaughtExceptionHandler((thread, t) -> {
            logger.log(Level.SEVERE, "unhandled exception in: " + thread, t);
        }).name("Event Execution: ", 0).factory();
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
        logger.finer("Advancing time from: %s to: %s".formatted(current, currentTime));
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
        final var current = currentEvent;
        final var sailorMoon = futureSailor;

        assert current != null : "no current event";
        assert sailorMoon != null : "No future to signal";
        assert !sailorMoon.isDone() : "Future sailure is done";

        final var ct = currentTime;
        final var continuingEvent = current.clone(ct);
        var blockingEvent = createEvent(ct, entity, event, arguments);
        logger.finer("Blocking: %s on: %s; continuation: %s".formatted(Thread.currentThread(), blockingEvent,
                                                                       continuingEvent));
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
    abstract protected void post(EventImpl event);

    /**
     * Swap the calling event for the current caller
     * 
     * @param newCaller
     * @return
     */
    protected EventImpl swapCaller(EventImpl newCaller) {
        var tmp = caller;
        caller = newCaller;
        logger.finer("Swap caller: %s for: %s".formatted(tmp, newCaller));
        return tmp;
    }

    private Runnable eval(EventImpl event) {
        return () -> {
            try {
                if (eventLog != null) {
                    eventLog.info(event.toString());
                }
                final var result = event.invoke();
                if (futureSailor.isDone()) {
                    logger.severe("Future sailor already done");
                }
                futureSailor.complete(new EvaluationResult(result));
            } catch (Throwable e) {
                futureSailor.complete(new EvaluationResult(e));
            }
        };
    }

    private void evaluation(EventImpl next) throws SimulationException {
        logger.finer("evaluating: %s".formatted(next));
        final var sailorMoon = futureSailor = new CompletableFuture<>();
        currentEvent = next;
        currentTime = next.getTime();
        caller = next.getCaller();
        if (next.isContinuation()) {
            next.proceed();
        } else {
            tf.newThread(eval(next)).start();
        }
        EvaluationResult result = null;
        try {
            result = sailorMoon.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        } catch (ExecutionException e) {
            throw new SimulationException(e.getCause());
        } finally {
            futureSailor = null;
            currentEvent = null;
        }

        assert result != null;

        final var cc = caller;
        if (result.blockingEvent != null) {
            result.continuingEvent.setCaller(cc);
            result.blockingEvent.setCaller(result.continuingEvent);
            post(result.blockingEvent);
        } else if (cc != null) {
            final var ct = currentTime;
            post(cc.resume(ct, result, result.t));
        }
    }
}
