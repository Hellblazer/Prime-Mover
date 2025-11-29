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
 * A bounded producer-consumer buffer with blocking semantics.
 * Producers block when the buffer is full, consumers block when empty.
 * 
 * <p>This is a classic bounded buffer implementation using SimSignal for blocking.
 * Items are stored in FIFO order using a Deque.</p>
 * 
 * @param <T> the type of items stored in the bin
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
@Entity
@Transformed(comment = "Hand written", date = "2024", value = "Hand")
public class Bin<T> {
    
    /**
     * The simulated entity implementation.
     */
    @Transformed(comment = "Hand written", date = "2024", value = "Hand")
    public static class entity<T> extends Bin<T> implements EntityReference {
        private static final int PUT = 0;
        private static final int TAKE = 1;
        
        public entity(Devi controller, int capacity) {
            this.controller = controller;
            this.capacity = capacity;
            this.availableSignal = new SimSignal.entity(controller);
            this.spaceSignal = new SimSignal.entity(controller);
        }
        
        @Override
        public Object __invoke(int event, Object[] arguments) throws Throwable {
            return switch (event) {
                case PUT -> {
                    @SuppressWarnings("unchecked")
                    var item = (T) arguments[0];
                    super.put(item);
                    yield null;
                }
                case TAKE -> super.take();
                default -> throw new IllegalArgumentException("Unknown event: " + event);
            };
        }
        
        @Override
        public String __signatureFor(int event) {
            return switch (event) {
                case PUT -> "<Bin: void put(T)>";
                case TAKE -> "<Bin: T take()>";
                default -> throw new IllegalArgumentException("Unknown event: " + event);
            };
        }
        
        @Override
        public void put(T item) {
            try {
                controller.postContinuingEvent(this, PUT, item);
            } catch (Throwable e) {
                throw new IllegalStateException("Exception in put", e);
            }
        }
        
        @Override
        public T take() {
            try {
                @SuppressWarnings("unchecked")
                var result = (T) controller.postContinuingEvent(this, TAKE);
                return result;
            } catch (Throwable e) {
                throw new IllegalStateException("Exception in take", e);
            }
        }
    }
    
    protected Devi controller;
    protected int capacity;
    protected final Deque<T> buffer = new ArrayDeque<>();
    
    // Two signals: one for item availability, one for space availability
    protected SimSignal availableSignal;
    protected SimSignal spaceSignal;
    
    /**
     * Protected constructor for entity inner class to use
     */
    protected Bin() {}
    
    /**
     * Put an item into the bin. Blocks if buffer is full.
     * 
     * @param item the item to put
     */
    @Blocking
    public void put(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot put null item");
        }
        
        while (buffer.size() >= capacity) {
            spaceSignal.await();  // Wait for space
            // When we resume, check again in case someone else took the space
        }
        buffer.addLast(item);
        if (availableSignal.hasWaiters()) {
            availableSignal.signal();  // Wake a consumer if any are waiting
        }
    }
    
    /**
     * Take an item from the bin. Blocks if buffer is empty.
     * 
     * @return the item taken
     */
    @Blocking
    public T take() {
        while (buffer.isEmpty()) {
            availableSignal.await();  // Wait for item
            // When we resume, check again in case someone else took the item
        }
        var item = buffer.removeFirst();
        if (spaceSignal.hasWaiters()) {
            spaceSignal.signal();  // Wake a producer if any are waiting
        }
        return item;
    }
    
    /**
     * Get the current number of items in the bin.
     */
    public int size() {
        return buffer.size();
    }
    
    /**
     * Get the capacity of the bin.
     */
    public int capacity() {
        return capacity;
    }
    
    /**
     * Check if the bin is empty.
     */
    public boolean isEmpty() {
        return buffer.isEmpty();
    }
    
    /**
     * Check if the bin is full.
     */
    public boolean isFull() {
        return buffer.size() >= capacity;
    }
}
