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
 * A simple blocking signal primitive for simulation. Provides basic condition
 * variable semantics where waiters block until signaled.
 * 
 * This is the foundational blocking primitive demonstrating the PrimeMover
 * continuation pattern for Phase 0 proof-of-concept.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
@Entity
@Transformed(comment = "Hand written", date = "2024", value = "Hand")
public class SimSignal {

    /**
     * The simulated entity implementation.
     */
    @Transformed(comment = "Hand written", date = "2024", value = "Hand")
    public static class entity extends SimSignal implements EntityReference {
        private static final int AWAIT = 0;

        public entity(Devi controller) {
            this.controller = controller;
        }

        @Override
        public Object __invoke(int event, Object[] arguments) throws Throwable {
            return switch (event) {
                case AWAIT -> {
                    super.await();
                    yield null;
                }
                default -> throw new IllegalArgumentException("Unknown event: " + event);
            };
        }

        @Override
        public String __signatureFor(int event) {
            return switch (event) {
                case AWAIT -> "<SimSignal: void await()>";
                default -> throw new IllegalArgumentException("Unknown event: " + event);
            };
        }

        @Override
        public void await() {
            try {
                controller.postContinuingEvent(this, AWAIT);
            } catch (Throwable e) {
                throw new IllegalStateException("Exception in await", e);
            }
        }
    }

    protected Devi                  controller;
    private final Deque<EventImpl> waiters = new ArrayDeque<>();
    /**
     * Count of pending signals that arrived before waiters were ready.
     * Acts like a semaphore permit count to handle timing windows.
     */
    private int pendingSignals = 0;

    /**
     * Protected constructor for entity inner class to use
     */
    protected SimSignal() {
    }

    /**
     * Block until signaled. This is a blocking method that suspends the calling
     * event until signal() is called.
     *
     * <p>If a signal() was called before this await() (pending signal),
     * the await returns immediately without blocking.
     */
    @Blocking
    public void await() {
        // If there's a pending signal, consume it and return immediately
        if (pendingSignals > 0) {
            pendingSignals--;
            return;
        }
        var waiter = controller.swapCaller(null);
        waiters.addLast(waiter);
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
     * Signal the first waiter (if any). The waiter resumes at the current
     * simulation time.
     *
     * <p>If no waiter is currently blocked, the signal is stored as pending
     * and will be consumed by the next await() call (semaphore semantics).
     */
    public void signal() {
        if (!waiters.isEmpty()) {
            var waiter = waiters.removeFirst();
            waiter.setTime(controller.getCurrentTime());
            waiter.getContinuation().setReturnValue(null);
            controller.post(waiter);
        } else {
            // No waiter yet - store as pending signal
            pendingSignals++;
        }
    }

    /**
     * Signal all waiters. All waiters resume at the current simulation time.
     */
    public void signalAll() {
        while (!waiters.isEmpty()) {
            signal();
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
     * Get the count of pending signals (signals that arrived before waiters).
     *
     * @return number of pending signals
     */
    public int pendingSignalCount() {
        return pendingSignals;
    }
}
