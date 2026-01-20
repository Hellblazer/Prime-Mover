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
import java.lang.ref.WeakReference;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hellblazer.primeMover.api.Event;
import com.hellblazer.primeMover.api.EntityReference;
import com.hellblazer.primeMover.runtime.Devi.EvaluationResult;

/**
 * Represents the simulated event.
 * <p>
 * Event source tracking uses weak references to prevent memory leaks in long-running simulations.
 * When {@link Devi#setTrackEventSources(boolean)} is enabled, each event stores a weak reference
 * to its source event. This allows the garbage collector to reclaim completed events even when
 * newer events reference them in the source chain. As a result, {@link #printTrace()} may show
 * incomplete event chains if intermediate events have been garbage collected.
 * </p>
 * <p>
 * Event source tracking is intended for debugging and development purposes only and should not
 * be relied upon in production environments where complete event traces are required.
 * </p>
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 *
 */
public class EventImpl implements Cloneable, Serializable, Comparable<EventImpl>, Event {
    private static final Logger       logger           = LoggerFactory.getLogger(EventImpl.class);
    /**
     * Serial version UID for serialization compatibility.
     * Used to ensure that serialized EventImpl objects can be deserialized across
     * different versions of the Prime Mover framework.
     */
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
    private final     int             event;
    /**
     * The entity which is the target of the event
     */
    private transient EntityReference reference;

    /**
     * Cached signature for the event, computed lazily on first access.
     * Cached to allow signature access even after clearReferences() is called.
     */
    private transient String cachedSignature;

    /**
     * The event that was the source of this event. Uses WeakReference to prevent memory
     * leaks in long event chains. Marked transient because WeakReference is not serializable.
     */
    private transient final WeakReference<Event> sourceRef;

    /**
     * The instant in time when this event was raised
     */
    private volatile long time;

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
        sourceRef = sourceEvent == null ? null : new WeakReference<>(sourceEvent);
    }

    public EventImpl clone(long time) {
        EventImpl clone;
        try {
            clone = (EventImpl) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("[EventImpl] Clone operation not supported for event: " + this, e);
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
        if (cachedSignature == null && reference != null) {
            cachedSignature = reference.__signatureFor(event);
        }
        return cachedSignature != null ? cachedSignature : "<unknown>";
    }

    @Override
    public Event getSource() {
        return sourceRef == null ? null : sourceRef.get();
    }

    @Override
    public long getTime() {
        return time;
    }

    public Object park(CompletableFuture<EvaluationResult> sailorMoon, EvaluationResult result) throws Throwable {
        final var newCont = new Continuation();
        continuation = newCont;
        final var parked = newCont.park(sailorMoon, result);
        return parked;
    }

    @Override
    public void printTrace() {
        printTrace(System.err);
    }

    @Override
    public void printTrace(PrintStream s) {
        // PrintStream is already thread-safe for concurrent writes
        s.println(this);
        var eventSource = getSource();
        while (eventSource != null) {
            s.println("\tat " + eventSource);
            eventSource = eventSource.getSource();
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

    /**
     * Get the continuation associated with this event. Made public to allow
     * blocking primitives in other packages to set return values.
     */
    public Continuation getContinuation() {
        return continuation;
    }

    Object invoke() throws Throwable {

        final var result = reference.__invoke(event, arguments);

        return result;
    }

    boolean isContinuation() {
        final var cont = continuation;
        return cont != null;
    }

    /**
     * Clears fields that hold strong references to allow garbage collection.
     * <p>
     * This method should be called by controller implementations after event processing is complete
     * and the event is no longer needed. It nulls out the {@code reference} (entity) and {@code caller}
     * fields to break strong reference chains that would otherwise prevent garbage collection.
     * <p>
     * <b>When to call:</b>
     * <ul>
     *   <li>After {@link #invoke()} completes successfully</li>
     *   <li>After {@link Devi#recordEvent(EventImpl)} finishes</li>
     *   <li>Before discarding the event object from controller state</li>
     * </ul>
     * <p>
     * <b>Why this matters:</b> In long-running simulations, completed events can accumulate
     * if they remain reachable through controller data structures. Even if the event itself
     * becomes unreachable, strong references to entities and caller chains prevent garbage
     * collection. Calling this method breaks those chains, allowing the GC to reclaim memory.
     * <p>
     * <b>Note:</b> This is an internal cleanup method intended for controller implementations.
     * Do not call this method if the event may be used again (e.g., for retry or replay).
     */
    public void clearReferences() {
        reference = null;
        caller = null;
    }

    void proceed() {
        final var cont = continuation;
//        assert (cont != null &&
//                cont.isParked()) : "continuation != null: %s parked: %s".formatted(cont != null,
//                                                                                   cont == null ? false
//                                                                                                : cont.isParked());
        continuation = null;
        logger.trace("Continuing: {}", this);
        cont.resume();
    }

    EventImpl resume(long currentTime, Object result, Throwable exception) {
        time = currentTime;
        final var current = continuation;
        if (current != null) {
            current.setReturnState(result, exception);
        }
        return this;
    }

    /**
     * Set the time for this event. Made public to allow blocking primitives
     * in other packages to schedule events at specific times.
     */
    public void setTime(long time) {
        this.time = time;
    }
}
