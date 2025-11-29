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

import com.hellblazer.primeMover.annotations.Blocking;
import com.hellblazer.primeMover.annotations.Entity;
import com.hellblazer.primeMover.annotations.Transformed;
import com.hellblazer.primeMover.api.EntityReference;

/**
 * A countable inventory primitive with blocking semantics.
 * Depositors block when the stock is full, withdrawers block when insufficient stock available.
 * 
 * <p>This is like Bin but for quantities rather than discrete items.
 * Uses two SimSignal instances for coordination: one for stock availability, one for space availability.</p>
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
@Entity
@Transformed(comment = "Hand written", date = "2024", value = "Hand")
public class Stock {
    
    /**
     * The simulated entity implementation.
     */
    @Transformed(comment = "Hand written", date = "2024", value = "Hand")
    public static class entity extends Stock implements EntityReference {
        private static final int DEPOSIT = 0;
        private static final int WITHDRAW = 1;
        
        public entity(Devi controller, int capacity, int initialLevel) {
            if (initialLevel < 0 || initialLevel > capacity) {
                throw new IllegalArgumentException("Initial level must be between 0 and capacity");
            }
            this.controller = controller;
            this.capacity = capacity;
            this.level = initialLevel;
            this.availableSignal = new SimSignal.entity(controller);
            this.spaceSignal = new SimSignal.entity(controller);
        }
        
        @Override
        public Object __invoke(int event, Object[] arguments) throws Throwable {
            return switch (event) {
                case DEPOSIT -> {
                    super.deposit((Integer) arguments[0]);
                    yield null;
                }
                case WITHDRAW -> super.withdraw((Integer) arguments[0]);
                default -> throw new IllegalArgumentException("Unknown event: " + event);
            };
        }
        
        @Override
        public String __signatureFor(int event) {
            return switch (event) {
                case DEPOSIT -> "<Stock: void deposit(int)>";
                case WITHDRAW -> "<Stock: int withdraw(int)>";
                default -> throw new IllegalArgumentException("Unknown event: " + event);
            };
        }
        
        @Override
        public void deposit(int quantity) {
            try {
                controller.postContinuingEvent(this, DEPOSIT, quantity);
            } catch (Throwable e) {
                throw new IllegalStateException("Exception in deposit", e);
            }
        }
        
        @Override
        public int withdraw(int quantity) {
            try {
                return (Integer) controller.postContinuingEvent(this, WITHDRAW, quantity);
            } catch (Throwable e) {
                throw new IllegalStateException("Exception in withdraw", e);
            }
        }
    }
    
    protected Devi controller;
    protected int capacity;
    protected int level;
    
    // Two signals: one for stock availability, one for space availability
    protected SimSignal availableSignal;
    protected SimSignal spaceSignal;
    
    /**
     * Protected constructor for entity inner class to use
     */
    protected Stock() {}
    
    /**
     * Deposit quantity into the stock. Blocks if insufficient space available.
     * 
     * @param quantity the amount to deposit
     */
    @Blocking
    public void deposit(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (quantity > capacity) {
            throw new IllegalArgumentException("Quantity " + quantity + " exceeds capacity " + capacity);
        }

        while (freeSpace() < quantity) {
            spaceSignal.await();  // Wait for space
            // When we resume, check again in case someone else took the space
        }
        level += quantity;
        // Wake all waiters - they'll re-check if they can proceed
        availableSignal.signalAll();
    }
    
    /**
     * Withdraw quantity from the stock. Blocks if insufficient stock available.
     * 
     * @param quantity the amount to withdraw
     * @return the amount withdrawn (always equals quantity)
     */
    @Blocking
    public int withdraw(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (quantity > capacity) {
            throw new IllegalArgumentException("Quantity " + quantity + " exceeds capacity " + capacity);
        }

        while (level < quantity) {
            availableSignal.await();  // Wait for stock
            // When we resume, check again in case someone else took it
        }
        level -= quantity;
        // Wake all depositors - they'll re-check if they can proceed
        spaceSignal.signalAll();
        return quantity;
    }
    
    /**
     * Get the current level of stock.
     * 
     * @return current quantity in stock
     */
    public int level() {
        return level;
    }
    
    /**
     * Get the capacity of the stock.
     * 
     * @return maximum capacity
     */
    public int capacity() {
        return capacity;
    }
    
    /**
     * Get the amount of free space available.
     * 
     * @return capacity - level
     */
    public int freeSpace() {
        return capacity - level;
    }
}
