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

import static com.hellblazer.primeMover.runtime.ContinuationFrame.BASE;

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
    private static final String $ENTITY$GEN       = "$entity$gen";
    private EventImpl           blockingEvent;
    private EventImpl           continuingEvent;
    private ContinuationFrame   returnFrame;
    private EventImpl           caller;
    private ContinuationFrame   currentFrame;
    private EventImpl           currentEvent;
    private long                currentTime       = 0;
    private boolean             debugEvents       = false;
    private Logger              eventLog;
    private boolean             trackEventSources = false;

    /**
     * Advance the current time of the controller
     * 
     * @param duration
     */
    @Override
    public void advance(long duration) {
        currentTime = currentTime + duration;
    }

    /**
     * Reinitialize the state of the controller
     */
    @Override
    public void clear() {
        currentTime = 0;
        blockingEvent = null;
        continuingEvent = null;
        currentFrame = null;
        caller = null;
        currentEvent = null;
    }

    protected EventImpl createEvent(long time, EntityReference entity,
                                    int event, Object... arguments) {
        Event sourceEvent = trackEventSources ? currentEvent : null;

        if (debugEvents) {
            StackTraceElement[] stackTrace = new Throwable().getStackTrace();
            for (int i = 0; i < stackTrace.length; i++) {
                if (stackTrace[i].getClassName().endsWith($ENTITY$GEN)) {
                    return new EventImpl(stackTrace[i + 1].toString(), time,
                                         sourceEvent, entity, event, arguments);
                }
            }
        }
        return new EventImpl(time, sourceEvent, entity, event, arguments);

    }

    /**
     * The heart of the event processing loop. This is where the events are
     * actually evaluated.
     * 
     * @param next
     *            - the event to evaluate.
     * @throws SimulationException
     *             - if an exception occurs during the evaluation of the event.
     */
    protected void evaluate(EventImpl next) throws SimulationException {
        currentEvent = next;
        currentTime = currentEvent.getTime();
        Continuation continuation = currentEvent.getContinuation();
        if (continuation != null) {
            returnFrame = continuation.getFrame();
            caller = continuation.getCaller();
        }
        Throwable exception = null;
        Object result = null;
        try {
            if (eventLog != null) {
                eventLog.info(currentEvent.toString());
            }
            result = currentEvent.invoke();
        } catch (SimulationEnd e) {
            throw e;
        } catch (Throwable e) {
            if (caller == null || blockingEvent != null) {
                throw new SimulationException(e);
            }
            exception = e;
        } finally {
            currentEvent = null;
        }
        if (blockingEvent != null) {
            continuingEvent.setContinuation(new Continuation(caller,
                                                             currentFrame));
            blockingEvent.setContinuation(new Continuation(continuingEvent));
            post(blockingEvent);
            blockingEvent = null;
            continuingEvent = null;
            currentFrame = null;
        } else if (caller != null) {
            post(caller.resume(currentTime, result, exception));
        }
        caller = null;
        returnFrame = null;
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
     * Pop the frame off the continuation stack
     * 
     * @return
     */
    protected ContinuationFrame popFrame() {
        ContinuationFrame frame = returnFrame;
        returnFrame = frame.next;
        return frame;
    }

    /**
     * Post the event to be evaluated
     * 
     * @param event
     */
    abstract protected void post(EventImpl event);

    /**
     * Post the event to be evaluated. The event is blocking, meaning that it
     * will cause the caller to continue execution until the event is processed.
     * 
     * @param entity
     *            - the target of the event
     * @param event
     *            - the event event
     * @param arguments
     *            - the arguments to the event
     * @return
     * @throws Throwable
     */
    @Override
    public Object postContinuingEvent(EntityReference entity, int event,
                                      Object... arguments) throws Throwable {
        assert blockingEvent == null;
        assert currentEvent != null;
        if (restoreFrame()) {
            returnFrame = null;
            return currentEvent.getContinuation().returnFrom();
        }
        blockingEvent = createEvent(currentTime, entity, event, arguments);
        continuingEvent = currentEvent.clone(currentTime);
        currentFrame = BASE;
        return null;
    }

    /**
     * Post the event to be evaluated
     * 
     * @param entity
     *            - the target of the event
     * @param event
     *            - the event event
     * @param arguments
     *            - the arguments to the event
     */
    @Override
    public void postEvent(EntityReference entity, int event,
                          Object... arguments) {
        post(createEvent(currentTime, entity, event, arguments));
    }

    /**
     * Post the event to be evaluated at the specified instant in time
     * 
     * @param time
     *            - the instant in time the event is to be processed
     * @param entity
     *            - the target of the event
     * @param event
     *            - the event event
     * @param arguments
     *            - the arguments to the event
     */
    @Override
    public void postEvent(long time, EntityReference entity, int event,
                          Object... arguments) {
        post(createEvent(time, entity, event, arguments));
    }

    /**
     * Answer true if the caller is to restore the continuation frame
     * 
     * @return
     */
    protected void pushFrame(ContinuationFrame frame) {
        frame.next = currentFrame;
        currentFrame = frame;
    }

    /**
     * Answer true if the caller is to restore the continuation frame
     * 
     * @return
     */
    protected boolean restoreFrame() {
        return returnFrame != null;
    }

    /**
     * Answer true if the caller is to save the continuation frame
     * 
     * @return
     */
    protected boolean saveFrame() {
        return blockingEvent != null;
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
     * Configure the collecting of debug information for raised events. When
     * debug is enabled, the controller will record the source location where an
     * event was raised. The collection of debug information for events is
     * expensive and significantly impacts the performance of the simulation
     * event processing.
     * 
     * @param debug
     *            - true to trigger the collecting of event debug information
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
     * event. Tracking event sources has garbage collection implications, as
     * event chains prevent the elimantion of previous events which have already
     * been processed.
     * 
     * @param track
     *            - true to track event sources
     */
    @Override
    public void setTrackEventSources(boolean track) {
        trackEventSources = track;
    }

    /**
     * Swap the calling event for the current caller
     * 
     * @param caller
     * @return
     */
    protected EventImpl swapCaller(EventImpl caller) {
        EventImpl tmp = this.caller;
        this.caller = caller;
        return tmp;
    }
}
