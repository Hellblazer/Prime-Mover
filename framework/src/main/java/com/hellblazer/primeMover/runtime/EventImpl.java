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

import java.io.PrintStream;
import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import com.hellblazer.primeMover.Event;
import com.hellblazer.primeMover.runtime.Devi.EvaluationResult;

/**
 * Represents the simulated event
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public class EventImpl implements Cloneable, Serializable, Comparable<EventImpl>, Event {
    private static final Logger       logger           = Logger.getLogger(EventImpl.class.getCanonicalName());
    private static final long         serialVersionUID = -628833433139964756L;
    /**
     * The arguments for the event
     */
    private final Object[]            arguments;
    /**
     * The caller of an event, if this is a blocking event
     */
    private volatile EventImpl        caller;
    /**
     * The continuation state of the event
     */
    private volatile Continuation     continuation;
    private final String              debugInfo;
    /**
     * The event
     */
    private final int                 event;
    /**
     * The entity which is the target of the event
     */
    private transient EntityReference reference;

    /**
     * The event that was the source of this event
     */
    private final Event source;

    /**
     * The instant in time when this event was raised
     */
    private long time;

    EventImpl(long time, Event sourceEvent, EntityReference reference, int ordinal, Object... arguments) {
        this(null, time, sourceEvent, reference, ordinal, arguments);
    }

    EventImpl(String debugInfo, long time, Event sourceEvent, EntityReference reference, int ordinal,
              Object... arguments) {
        assert reference != null;
        this.debugInfo = debugInfo;
        this.time = time;
        this.reference = reference;
        event = ordinal;
        this.arguments = arguments;
        source = sourceEvent;
    }

    public EventImpl clone(long time) {
        EventImpl clone;
        try {
            clone = (EventImpl) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Clone not supported for Event!", e);
        }

        clone.continuation = null;
        clone.time = time;

        return clone;
    }

    @Override
    public int compareTo(EventImpl event) {
        // cannot do (thisMillis - otherMillis) as can overflow
        if (time == event.time) {
            return 0;
        }
        if (time < event.time) {
            return -1;
        } else {
            return 1;
        }
    }

    public EventImpl getCaller() {
        return caller;
    }

    public EntityReference getReference() {
        return reference;
    }

    @Override
    public String getSignature() {
        return reference.__signatureFor(event);
    }

    @Override
    public Event getSource() {
        return source;
    }

    @Override
    public long getTime() {
        return time;
    }

    public Object park(CompletableFuture<EvaluationResult> sailorMoon, EvaluationResult result) throws Throwable {
        final var newCont = new Continuation();
        continuation = newCont;
        final var parked = newCont.park(sailorMoon, result);
        logger.finer("Continuing: %s event: %s".formatted(Thread.currentThread(), this));
        return parked;
    }

    @Override
    public void printTrace() {
        printTrace(System.err);
    }

    @Override
    public void printTrace(PrintStream s) {
        synchronized (s) {
            s.println(this);
            Event eventSource = source;
            while (eventSource != null) {
                s.println("\tat " + eventSource);
                eventSource = eventSource.getSource();
            }
        }
    }

    /**
     * @param caller
     */
    public void setCaller(EventImpl caller) {
        this.caller = caller;
    }

    @Override
    public String toString() {
        if (debugInfo == null) {
            return String.format("%s : %s%s", time, getSignature(), continuation == null ? "" : " : c");
        } else {
            return String.format("%s : %s%s @ %s", time, getSignature(), debugInfo, continuation == null ? "" : " : c");
        }
    }

    Continuation getContinuation() {
        return continuation;
    }

    Object invoke() throws Throwable {
        logger.finest("Invoking " + this);
        final var result = reference.__invoke(event, arguments);
        logger.finest("Finished invoking " + this);
        return result;
    }

    boolean isContinuation() {
        final var cont = continuation;
        return cont != null;
    }

    void proceed() {
        final var cont = continuation;
//        assert (cont != null &&
//                cont.isParked()) : "continuation != null: %s parked: %s".formatted(cont != null,
//                                                                                   cont == null ? false
//                                                                                                : cont.isParked());
        continuation = null;
        Logger.getLogger(EventImpl.class.getCanonicalName()).finer("Continuing: %s".formatted(this));
        cont.resume();
    }

    EventImpl resume(long currentTime, Object result, Throwable exception) {
        time = currentTime;
        Logger.getLogger(EventImpl.class.getCanonicalName())
              .finer("Resume at: %s r: %s ex: %s".formatted(this, result, exception));
        final var current = continuation;
        if (current != null) {
            current.setReturnState(result, exception);
        }
        return this;
    }

    void setTime(long time) {
        this.time = time;
    }
}
