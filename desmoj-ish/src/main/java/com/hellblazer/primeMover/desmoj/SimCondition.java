/**
 * Copyright (C) 2024 Hal Hildebrand. All rights reserved.
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

package com.hellblazer.primeMover.desmoj;

import com.hellblazer.primeMover.runtime.EventImpl;

import com.hellblazer.primeMover.runtime.Devi;

import java.util.ArrayDeque;
import java.util.Deque;

import com.hellblazer.primeMover.annotations.Blocking;
import com.hellblazer.primeMover.annotations.Entity;
import com.hellblazer.primeMover.annotations.Transformed;
import com.hellblazer.primeMover.api.EntityReference;

/**
 * A blocking condition variable that passes a value when signaling.
 * Generalization of SimSignal that allows data to be passed from signaler to waiter.
 * 
 * <p>This is a fundamental blocking primitive for simulation. Waiters block until
 * signaled, and receive a value of type T from the signaler.</p>
 * 
 * @param <T> the type of value passed when signaling
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
@Entity
@Transformed(comment = "Hand written", date = "2024", value = "Hand")
public class SimCondition<T> {

    /**
     * The simulated entity implementation.
     */
    @Transformed(comment = "Hand written", date = "2024", value = "Hand")
    public static class entity<T> extends SimCondition<T> implements EntityReference {
        private static final int AWAIT = 0;

        public entity(Devi controller) {
            this.controller = controller;
        }

        @Override
        public Object __invoke(int event, Object[] arguments) throws Throwable {
            return switch (event) {
                case AWAIT -> super.await();  // Returns the value
                default -> throw new IllegalArgumentException("Unknown event: " + event);
            };
        }

        @Override
        public String __signatureFor(int event) {
            return switch (event) {
                case AWAIT -> "<SimCondition: Object await()>";
                default -> throw new IllegalArgumentException("Unknown event: " + event);
            };
        }

        @Override
        @SuppressWarnings("unchecked")
        public T await() {
            try {
                return (T) controller.postContinuingEvent(this, AWAIT);
            } catch (Throwable e) {
                throw new IllegalStateException("Exception in await", e);
            }
        }
    }

    protected Devi                  controller;
    private final Deque<EventImpl> waiters = new ArrayDeque<>();
    /**
     * Queue of pending values that arrived before waiters were ready.
     * Acts like a semaphore to handle timing windows where signal() is called before await().
     */
    private final Deque<T> pendingValues = new ArrayDeque<>();

    /**
     * Protected constructor for entity inner class to use
     */
    protected SimCondition() {
    }

    /**
     * Block until signaled, returning the signaled value.
     * The return value is set by the signaler via signal(T value).
     *
     * <p>If a signal(value) was called before this await() (pending value),
     * the await returns immediately with that value without blocking.
     *
     * @return the value passed by the signaler
     */
    @Blocking
    public T await() {
        // If there's a pending value, consume it and return immediately
        if (!pendingValues.isEmpty()) {
            return pendingValues.removeFirst();
        }
        var waiter = controller.swapCaller(null);
        waiters.addLast(waiter);
        return null;  // Actual value set by Continuation.setReturnValue()
    }

    /**
     * Check if there are any waiting events.
     * 
     * @return true if there are waiters
     */
    public boolean hasWaiters() {
        return !waiters.isEmpty();
    }

    /**
     * Signal the first waiter with a value.
     * The waiter's await() will return this value.
     *
     * <p>If no waiter is currently blocked, the value is stored as pending
     * and will be consumed by the next await() call (semaphore semantics).
     *
     * @param value the value to pass to the waiter
     */
    public void signal(T value) {
        if (!waiters.isEmpty()) {
            var waiter = waiters.removeFirst();
            waiter.setTime(controller.getCurrentTime());
            waiter.getContinuation().setReturnValue(value);
            controller.post(waiter);
        } else {
            // No waiter yet - store as pending value
            pendingValues.addLast(value);
        }
    }

    /**
     * Signal all waiters with the same value.
     * All waiters resume at the current simulation time with the same value.
     * 
     * @param value the value to pass to all waiters
     */
    public void signalAll(T value) {
        while (!waiters.isEmpty()) {
            signal(value);
        }
    }

    /**
     * Get the count of waiting events.
     *
     * @return number of waiters
     */
    public int waiterCount() {
        return waiters.size();
    }

    /**
     * Get the count of pending values (values that arrived before waiters).
     *
     * @return number of pending values
     */
    public int pendingValueCount() {
        return pendingValues.size();
    }
}
