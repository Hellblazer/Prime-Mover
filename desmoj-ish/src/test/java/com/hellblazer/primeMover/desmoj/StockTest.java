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

import com.hellblazer.primeMover.controllers.SimulationController;

import com.hellblazer.primeMover.runtime.Devi;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.hellblazer.primeMover.annotations.Blocking;
import com.hellblazer.primeMover.annotations.Entity;
import com.hellblazer.primeMover.annotations.Transformed;
import com.hellblazer.primeMover.api.EntityReference;
import com.hellblazer.primeMover.controllers.SimulationController;

/**
 * Test suite for Stock - countable inventory primitive.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class StockTest {

    /**
     * Test helper for depositing into stock.
     */
    @Entity
    @Transformed(comment = "Hand written", date = "2024", value = "Hand")
    public static class Depositor {
        
        @Transformed(comment = "Hand written", date = "2024", value = "Hand")
        public static class entity extends Depositor implements EntityReference {
            private static final int DEPOSIT = 0;
            
            public entity(Devi controller, Stock stock, int quantity) {
                this.controller = controller;
                this.stock = stock;
                this.quantity = quantity;
            }
            
            @Override
            public Object __invoke(int event, Object[] arguments) throws Throwable {
                return switch (event) {
                    case DEPOSIT -> {
                        super.deposit();
                        yield null;
                    }
                    default -> throw new IllegalArgumentException("Unknown event: " + event);
                };
            }
            
            @Override
            public String __signatureFor(int event) {
                return switch (event) {
                    case DEPOSIT -> "<Depositor: void deposit()>";
                    default -> "Unknown";
                };
            }
            
            @Override
            public void deposit() {
                try {
                    controller.postContinuingEvent(this, DEPOSIT);
                } catch (Throwable e) {
                    throw new IllegalStateException("Exception in deposit", e);
                }
            }
        }
        
        protected Devi controller;
        protected Stock stock;
        protected int quantity;
        
        protected Depositor() {}
        
        @Blocking
        public void deposit() {
            stock.deposit(quantity);
        }
    }
    
    /**
     * Test helper for withdrawing from stock.
     */
    @Entity
    @Transformed(comment = "Hand written", date = "2024", value = "Hand")
    public static class Withdrawer {
        
        @Transformed(comment = "Hand written", date = "2024", value = "Hand")
        public static class entity extends Withdrawer implements EntityReference {
            private static final int WITHDRAW = 0;
            
            public entity(Devi controller, Stock stock, int quantity, AtomicInteger result) {
                this.controller = controller;
                this.stock = stock;
                this.quantity = quantity;
                this.result = result;
            }
            
            @Override
            public Object __invoke(int event, Object[] arguments) throws Throwable {
                return switch (event) {
                    case WITHDRAW -> {
                        super.withdraw();
                        yield null;
                    }
                    default -> throw new IllegalArgumentException("Unknown event: " + event);
                };
            }
            
            @Override
            public String __signatureFor(int event) {
                return switch (event) {
                    case WITHDRAW -> "<Withdrawer: void withdraw()>";
                    default -> "Unknown";
                };
            }
            
            @Override
            public void withdraw() {
                try {
                    controller.postContinuingEvent(this, WITHDRAW);
                } catch (Throwable e) {
                    throw new IllegalStateException("Exception in withdraw", e);
                }
            }
        }
        
        protected Devi controller;
        protected Stock stock;
        protected int quantity;
        protected AtomicInteger result;
        
        protected Withdrawer() {}
        
        @Blocking
        public void withdraw() {
            var amount = stock.withdraw(quantity);
            result.set(amount);
        }
    }

    @Test
    public void testBasicDepositWithdraw() throws Exception {
        try (var controller = new SimulationController()) {
            var stock = new Stock.entity(controller, 100, 0);
            var result = new AtomicInteger(0);
            
            var depositor = new Depositor.entity(controller, stock, 50);
            var withdrawer = new Withdrawer.entity(controller, stock, 30, result);
            
            controller.postEvent(0, depositor, 0);
            controller.postEvent(10, withdrawer, 0);
            
            controller.eventLoop();
            
            assertEquals(30, result.get());
            assertEquals(20, stock.level());
            assertEquals(80, stock.freeSpace());
        }
    }
    
    @Test
    public void testBlockOnEmpty() throws Exception {
        try (var controller = new SimulationController()) {
            var stock = new Stock.entity(controller, 100, 0);
            var result = new AtomicInteger(0);
            
            var withdrawer = new Withdrawer.entity(controller, stock, 50, result);
            var depositor = new Depositor.entity(controller, stock, 50);
            
            controller.postEvent(0, withdrawer, 0);  // Try to withdraw 50 when empty
            controller.postEvent(100, depositor, 0); // Deposit 50 at t=100
            
            controller.eventLoop();
            
            assertEquals(50, result.get());
            assertEquals(0, stock.level());  // 50 deposited - 50 withdrawn
        }
    }
    
    @Test
    public void testBlockOnFull() throws Exception {
        try (var controller = new SimulationController()) {
            var stock = new Stock.entity(controller, 100, 80);
            var result = new AtomicInteger(0);
            
            var depositor = new Depositor.entity(controller, stock, 30);
            var withdrawer = new Withdrawer.entity(controller, stock, 20, result);
            
            controller.postEvent(0, depositor, 0);   // Try to deposit 30 when only 20 space
            controller.postEvent(50, withdrawer, 0); // Withdraw 20 at t=50
            
            controller.eventLoop();
            
            assertEquals(20, result.get());
            assertEquals(90, stock.level());  // 80 initial - 20 withdrawn + 30 deposited
        }
    }
    
    @Test
    public void testPartialWithdraw() throws Exception {
        try (var controller = new SimulationController()) {
            var stock = new Stock.entity(controller, 100, 50);
            var result1 = new AtomicInteger(0);
            var result2 = new AtomicInteger(0);
            var result3 = new AtomicInteger(0);
            
            var withdrawer1 = new Withdrawer.entity(controller, stock, 20, result1);
            var withdrawer2 = new Withdrawer.entity(controller, stock, 15, result2);
            var withdrawer3 = new Withdrawer.entity(controller, stock, 10, result3);
            
            controller.postEvent(0, withdrawer1, 0);
            controller.postEvent(10, withdrawer2, 0);
            controller.postEvent(20, withdrawer3, 0);
            
            controller.eventLoop();
            
            assertEquals(20, result1.get());
            assertEquals(15, result2.get());
            assertEquals(10, result3.get());
            assertEquals(5, stock.level());  // 50 - 20 - 15 - 10 = 5
        }
    }
    
    @Test
    public void testMultipleWithdrawers() throws Exception {
        try (var controller = new SimulationController()) {
            var stock = new Stock.entity(controller, 100, 0);
            var results = new AtomicInteger[5];
            
            // Create 5 withdrawers that will all block
            for (int i = 0; i < 5; i++) {
                results[i] = new AtomicInteger(0);
                var withdrawer = new Withdrawer.entity(controller, stock, 10, results[i]);
                controller.postEvent(0, withdrawer, 0);
            }
            
            // Deposit enough for all at t=100
            var depositor = new Depositor.entity(controller, stock, 50);
            controller.postEvent(100, depositor, 0);
            
            controller.eventLoop();
            
            // All should have gotten their withdrawals
            for (int i = 0; i < 5; i++) {
                assertEquals(10, results[i].get());
            }
            assertEquals(0, stock.level());  // 50 deposited - 50 withdrawn
        }
    }
    
    @Test
    public void testInitialLevel() throws Exception {
        try (var controller = new SimulationController()) {
            var stock = new Stock.entity(controller, 100, 75);
            
            assertEquals(75, stock.level());
            assertEquals(100, stock.capacity());
            assertEquals(25, stock.freeSpace());
        }
    }
    
    @Test
    public void testUnlimitedCapacity() throws Exception {
        // Test stock with very large capacity
        try (var controller = new SimulationController()) {
            var stock = new Stock.entity(controller, Integer.MAX_VALUE, 0);
            var result = new AtomicInteger(0);

            var depositor = new Depositor.entity(controller, stock, 1000000);
            var withdrawer = new Withdrawer.entity(controller, stock, 500000, result);

            controller.postEvent(0, depositor, 0);
            controller.postEvent(10, withdrawer, 0);

            controller.eventLoop();

            assertEquals(500000, result.get());
            assertEquals(500000, stock.level());
        }
    }
}
