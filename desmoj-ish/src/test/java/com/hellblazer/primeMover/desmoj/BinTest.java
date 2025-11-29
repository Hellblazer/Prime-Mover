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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.Test;

import com.hellblazer.primeMover.annotations.Blocking;
import com.hellblazer.primeMover.annotations.Entity;
import com.hellblazer.primeMover.annotations.Transformed;
import com.hellblazer.primeMover.api.EntityReference;
import com.hellblazer.primeMover.controllers.SimulationController;

/**
 * Comprehensive tests for Bin<T> bounded buffer.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class BinTest {
    
    /**
     * Test helper entity for producing items.
     */
    @Entity
    @Transformed(comment = "Hand written", date = "2024", value = "Hand")
    public static class Producer<T> {
        
        @Transformed(comment = "Hand written", date = "2024", value = "Hand")
        public static class entity<T> extends Producer<T> implements EntityReference {
            private static final int PRODUCE = 0;
            
            public entity(Devi controller, Bin<T> bin, List<T> items) {
                this.controller = controller;
                this.bin = bin;
                this.items = items;
            }
            
            @Override
            public Object __invoke(int event, Object[] arguments) throws Throwable {
                return switch (event) {
                    case PRODUCE -> {
                        super.produce();
                        yield null;
                    }
                    default -> throw new IllegalArgumentException("Unknown event: " + event);
                };
            }
            
            @Override
            public String __signatureFor(int event) {
                return switch (event) {
                    case PRODUCE -> "<Producer: void produce()>";
                    default -> "Unknown";
                };
            }
            
            @Override
            public void produce() {
                try {
                    controller.postContinuingEvent(this, PRODUCE);
                } catch (Throwable e) {
                    throw new IllegalStateException("Exception in produce", e);
                }
            }
        }
        
        protected Devi controller;
        protected Bin<T> bin;
        protected List<T> items;
        
        protected Producer() {}
        
        @Blocking
        public void produce() {
            for (var item : items) {
                bin.put(item);
            }
        }
    }
    
    /**
     * Test helper entity for consuming items.
     */
    @Entity
    @Transformed(comment = "Hand written", date = "2024", value = "Hand")
    public static class Consumer<T> {
        
        @Transformed(comment = "Hand written", date = "2024", value = "Hand")
        public static class entity<T> extends Consumer<T> implements EntityReference {
            private static final int CONSUME = 0;
            
            public entity(Devi controller, Bin<T> bin, int count, List<T> results) {
                this.controller = controller;
                this.bin = bin;
                this.count = count;
                this.results = results;
            }
            
            @Override
            public Object __invoke(int event, Object[] arguments) throws Throwable {
                return switch (event) {
                    case CONSUME -> {
                        super.consume();
                        yield null;
                    }
                    default -> throw new IllegalArgumentException("Unknown event: " + event);
                };
            }
            
            @Override
            public String __signatureFor(int event) {
                return switch (event) {
                    case CONSUME -> "<Consumer: void consume()>";
                    default -> "Unknown";
                };
            }
            
            @Override
            public void consume() {
                try {
                    controller.postContinuingEvent(this, CONSUME);
                } catch (Throwable e) {
                    throw new IllegalStateException("Exception in consume", e);
                }
            }
        }
        
        protected Devi controller;
        protected Bin<T> bin;
        protected int count;
        protected List<T> results;
        
        protected Consumer() {}
        
        @Blocking
        public void consume() {
            for (int i = 0; i < count; i++) {
                var item = bin.take();
                results.add(item);
            }
        }
    }
    
    /**
     * Test helper for timing-aware production.
     */
    @Entity
    @Transformed(comment = "Hand written", date = "2024", value = "Hand")
    public static class TimingProducer<T> {
        
        @Transformed(comment = "Hand written", date = "2024", value = "Hand")
        public static class entity<T> extends TimingProducer<T> implements EntityReference {
            private static final int PRODUCE = 0;
            
            public entity(Devi controller, Bin<T> bin, List<T> items, List<String> timings) {
                this.controller = controller;
                this.bin = bin;
                this.items = items;
                this.timings = timings;
            }
            
            @Override
            public Object __invoke(int event, Object[] arguments) throws Throwable {
                return switch (event) {
                    case PRODUCE -> {
                        super.produce();
                        yield null;
                    }
                    default -> throw new IllegalArgumentException("Unknown event: " + event);
                };
            }
            
            @Override
            public String __signatureFor(int event) {
                return switch (event) {
                    case PRODUCE -> "<TimingProducer: void produce()>";
                    default -> "Unknown";
                };
            }
            
            @Override
            public void produce() {
                try {
                    controller.postContinuingEvent(this, PRODUCE);
                } catch (Throwable e) {
                    throw new IllegalStateException("Exception in produce", e);
                }
            }
        }
        
        protected Devi controller;
        protected Bin<T> bin;
        protected List<T> items;
        protected List<String> timings;
        
        protected TimingProducer() {}
        
        @Blocking
        public void produce() {
            for (var item : items) {
                timings.add("put:" + item + ":start:" + controller.getCurrentTime());
                bin.put(item);
                timings.add("put:" + item + ":done:" + controller.getCurrentTime());
            }
        }
    }
    
    @Test
    public void testBasicPutTake() throws Exception {
        try (var controller = new SimulationController()) {
            var bin = new Bin.entity<String>(controller, 5);
            var items = List.of("A", "B", "C");
            var results = new ArrayList<String>();
            
            var producer = new Producer.entity(controller, bin, items);
            var consumer = new Consumer.entity(controller, bin, 3, results);
            
            controller.postEvent(0, producer, 0);
            controller.postEvent(0, consumer, 0);
            
            controller.eventLoop();
            
            assertEquals(List.of("A", "B", "C"), results);
            assertTrue(bin.isEmpty());
        }
    }
    
    @Test
    public void testFIFOOrdering() throws Exception {
        try (var controller = new SimulationController()) {
            var bin = new Bin.entity<Integer>(controller, 10);
            var items = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            var results = new ArrayList<Integer>();
            
            var producer = new Producer.entity<Integer>(controller, bin, items);
            var consumer = new Consumer.entity<Integer>(controller, bin, 10, results);
            
            controller.postEvent(0, producer, 0);
            controller.postEvent(0, consumer, 0);
            
            controller.eventLoop();
            
            assertEquals(items, results);
        }
    }
    
    @Test
    public void testBlockOnFull() throws Exception {
        try (var controller = new SimulationController()) {
            var bin = new Bin.entity<String>(controller, 2);
            var timings = new CopyOnWriteArrayList<String>();
            
            var producer = new TimingProducer.entity<String>(controller, bin, List.of("A", "B", "C"), timings);
            var consumer = new Consumer.entity<String>(controller, bin, 1, new ArrayList<>());
            
            controller.postEvent(0, producer, 0);
            controller.postEvent(100, consumer, 0);  // Take one item at t=100
            
            controller.eventLoop();
            
            // Producer should put A and B immediately, then block on C until t=100
            assertEquals("put:A:start:0", timings.get(0));
            assertEquals("put:A:done:0", timings.get(1));
            assertEquals("put:B:start:0", timings.get(2));
            assertEquals("put:B:done:0", timings.get(3));
            assertEquals("put:C:start:0", timings.get(4));
            assertEquals("put:C:done:100", timings.get(5));  // Resumes at t=100
        }
    }
    
    @Test
    public void testBlockOnEmpty() throws Exception {
        try (var controller = new SimulationController()) {
            var bin = new Bin.entity<String>(controller, 5);
            var results = new CopyOnWriteArrayList<String>();
            
            var consumer = new Consumer.entity(controller, bin, 2, results);
            var producer = new Producer.entity(controller, bin, List.of("X", "Y"));
            
            controller.postEvent(0, consumer, 0);  // Consumer starts first, should block
            controller.postEvent(50, producer, 0);  // Producer arrives at t=50
            
            controller.eventLoop();
            
            // Consumer should get items at t=50
            assertEquals(List.of("X", "Y"), results);
        }
    }
    
    @Test
    public void testMultipleProducers() throws Exception {
        try (var controller = new SimulationController()) {
            var bin = new Bin.entity<String>(controller, 10);
            var results = new CopyOnWriteArrayList<String>();
            
            var producer1 = new Producer.entity(controller, bin, List.of("A1", "A2", "A3"));
            var producer2 = new Producer.entity(controller, bin, List.of("B1", "B2", "B3"));
            var consumer = new Consumer.entity(controller, bin, 6, results);
            
            controller.postEvent(0, producer1, 0);
            controller.postEvent(0, producer2, 0);
            controller.postEvent(0, consumer, 0);
            
            controller.eventLoop();
            
            assertEquals(6, results.size());
            assertTrue(results.containsAll(List.of("A1", "A2", "A3", "B1", "B2", "B3")));
        }
    }
    
    @Test
    public void testMultipleConsumers() throws Exception {
        try (var controller = new SimulationController()) {
            var bin = new Bin.entity<String>(controller, 10);
            var results1 = new CopyOnWriteArrayList<String>();
            var results2 = new CopyOnWriteArrayList<String>();
            
            var producer = new Producer.entity(controller, bin, List.of("1", "2", "3", "4", "5", "6"));
            var consumer1 = new Consumer.entity(controller, bin, 3, results1);
            var consumer2 = new Consumer.entity(controller, bin, 3, results2);
            
            controller.postEvent(0, producer, 0);
            controller.postEvent(0, consumer1, 0);
            controller.postEvent(0, consumer2, 0);
            
            controller.eventLoop();
            
            var allResults = new ArrayList<String>();
            allResults.addAll(results1);
            allResults.addAll(results2);
            
            Collections.sort(allResults);
            assertEquals(List.of("1", "2", "3", "4", "5", "6"), allResults);
            
            // Verify no duplicates
            assertEquals(3, results1.size());
            assertEquals(3, results2.size());
        }
    }
    
    @Test
    public void testCapacityLimits() throws Exception {
        try (var controller = new SimulationController()) {
            var bin = new Bin.entity<String>(controller, 3);
            
            assertFalse(bin.isFull());
            assertEquals(0, bin.size());
            assertEquals(3, bin.capacity());
            
            // This test doesn't use blocking - just verifies the bin state
            // We can't directly put/take outside of a simulation event, so we'll
            // verify through entity methods
            var producer = new Producer.entity(controller, bin, List.of("A", "B", "C"));
            controller.postEvent(0, producer, 0);
            controller.eventLoop();
            
            // After producing 3 items into capacity-3 bin, it should be full
            assertTrue(bin.isFull());
            assertEquals(3, bin.size());
            assertFalse(bin.isEmpty());
        }
    }
}
