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

import com.hellblazer.primeMover.runtime.Devi;

import java.util.ArrayDeque;
import java.util.Deque;

import com.hellblazer.primeMover.annotations.Blocking;
import com.hellblazer.primeMover.annotations.Entity;
import com.hellblazer.primeMover.annotations.Transformed;
import com.hellblazer.primeMover.api.EntityReference;

/**
 * A resource pool with blocking acquire semantics.
 * Entities can acquire and release resources, blocking when insufficient resources are available.
 * 
 * <p>This demonstrates the critical pattern of @Blocking methods using SimSignal internally.
 * The acquire() method blocks using SimSignal.await() until resources become available.</p>
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
@Entity
@Transformed(comment = "Hand written", date = "2024", value = "Hand")
public class Resource {
    
    /**
     * Request record tracking waiter information.
     */
    public record Request(int count, long entryTime) {}
    
    /**
     * The simulated entity implementation.
     */
    @Transformed(comment = "Hand written", date = "2024", value = "Hand")
    public static class entity extends Resource implements EntityReference {
        private static final int ACQUIRE = 0;
        private static final int ACQUIRE_COUNT = 1;
        private static final int LOAN = 2;
        private static final int LOAN_COUNT = 3;
        
        public entity(Devi controller, int capacity) {
            this.controller = controller;
            this.capacity = capacity;
            this.available = capacity;
            this.stats = new ResourceStatistics(capacity);
            this.waitSignal = new SimSignal.entity(controller);
        }
        
        @Override
        public Object __invoke(int event, Object[] arguments) throws Throwable {
            return switch (event) {
                case ACQUIRE -> super.acquire();
                case ACQUIRE_COUNT -> super.acquire((Integer) arguments[0]);
                case LOAN -> super.loan();
                case LOAN_COUNT -> super.loan((Integer) arguments[0]);
                default -> throw new IllegalArgumentException("Unknown event: " + event);
            };
        }
        
        @Override
        public String __signatureFor(int event) {
            return switch (event) {
                case ACQUIRE -> "<Resource: ResourceToken acquire()>";
                case ACQUIRE_COUNT -> "<Resource: ResourceToken acquire(int)>";
                case LOAN -> "<Resource: Loan loan()>";
                case LOAN_COUNT -> "<Resource: Loan loan(int)>";
                default -> throw new IllegalArgumentException("Unknown event: " + event);
            };
        }
        
        @Override
        public ResourceToken acquire() {
            try {
                return (ResourceToken) controller.postContinuingEvent(this, ACQUIRE);
            } catch (Throwable e) {
                throw new IllegalStateException("Exception in acquire", e);
            }
        }
        
        @Override
        public ResourceToken acquire(int count) {
            try {
                return (ResourceToken) controller.postContinuingEvent(this, ACQUIRE_COUNT, count);
            } catch (Throwable e) {
                throw new IllegalStateException("Exception in acquire(count)", e);
            }
        }
        
        @Override
        public Loan loan() {
            try {
                return (Loan) controller.postContinuingEvent(this, LOAN);
            } catch (Throwable e) {
                throw new IllegalStateException("Exception in loan", e);
            }
        }
        
        @Override
        public Loan loan(int count) {
            try {
                return (Loan) controller.postContinuingEvent(this, LOAN_COUNT, count);
            } catch (Throwable e) {
                throw new IllegalStateException("Exception in loan(count)", e);
            }
        }
    }
    
    protected Devi controller;
    protected int capacity;
    protected int available;
    protected ResourceStatistics stats;
    protected SimSignal waitSignal;  // Internal signal for blocking
    
    private final Deque<Request> waitQueue = new ArrayDeque<>();
    
    /**
     * Protected constructor for entity inner class to use
     */
    protected Resource() {}
    
    /**
     * Acquire a single resource, blocking if none available.
     * 
     * @return token representing the acquired resource
     */
    @Blocking
    public ResourceToken acquire() {
        return acquire(1);
    }
    
    /**
     * Acquire multiple resources, blocking if insufficient available.
     * 
     * <p>This is the critical @Blocking pattern: it uses SimSignal.await() internally
     * to suspend the calling event until resources become available.</p>
     * 
     * @param count number of resources to acquire
     * @return token representing the acquired resources
     */
    @Blocking
    public ResourceToken acquire(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }
        if (count > capacity) {
            throw new IllegalArgumentException("Cannot acquire " + count + " from pool of " + capacity);
        }
        
        var entryTime = controller.getCurrentTime();
        
        // Block using SimSignal until resources available
        while (available < count) {
            waitQueue.addLast(new Request(count, entryTime));
            waitSignal.await();  // <-- BLOCKING call to SimSignal
            // When we resume, check again in case someone else got them first
        }
        
        // Got the resources
        available -= count;
        var waitTime = controller.getCurrentTime() - entryTime;
        stats.recordAcquire(count, controller.getCurrentTime(), waitTime);
        return new ResourceToken(this, count);
    }
    
    /**
     * Release resources using a token.
     * 
     * @param token the token from acquire()
     */
    public void release(ResourceToken token) {
        if (token.resource() != this) {
            throw new IllegalArgumentException("Token is from a different resource pool");
        }
        release(token.count());
    }
    
    /**
     * Release a number of resources back to the pool.
     * 
     * @param count number of resources to release
     */
    public void release(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }
        if (available + count > capacity) {
            throw new IllegalStateException("Releasing more resources than capacity");
        }
        
        available += count;
        stats.recordRelease(count, controller.getCurrentTime());
        wakeWaiters();
    }
    
    /**
     * Wake waiters who can now acquire resources.
     * Signals waiters in FIFO order if their request can be satisfied.
     */
    private void wakeWaiters() {
        while (!waitQueue.isEmpty() && available > 0) {
            var request = waitQueue.peekFirst();
            if (request.count() <= available) {
                waitQueue.removeFirst();
                waitSignal.signal();  // Wake one waiter
            } else {
                break;  // First waiter can't proceed, stop
            }
        }
    }
    
    /**
     * Acquire a single resource with automatic release on close.
     * 
     * @return loan that will auto-release when closed
     */
    @Blocking
    public Loan loan() {
        return new Loan(acquire(1));
    }
    
    /**
     * Acquire multiple resources with automatic release on close.
     * 
     * @param count number of resources to acquire
     * @return loan that will auto-release when closed
     */
    @Blocking
    public Loan loan(int count) {
        return new Loan(acquire(count));
    }
    
    /**
     * Get the number of available resources.
     */
    public int available() {
        return available;
    }
    
    /**
     * Get the capacity of the resource pool.
     */
    public int capacity() {
        return capacity;
    }
    
    /**
     * Get the statistics tracker.
     */
    public ResourceStatistics statistics() {
        return stats;
    }
    
    /**
     * Check if there are any waiting requests.
     */
    public boolean hasWaiters() {
        return !waitQueue.isEmpty();
    }
    
    /**
     * Get the number of waiting requests.
     */
    public int waiterCount() {
        return waitQueue.size();
    }
}
