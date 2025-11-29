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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.hellblazer.primeMover.annotations.Entity;
import com.hellblazer.primeMover.annotations.Transformed;
import com.hellblazer.primeMover.api.EntityReference;
import com.hellblazer.primeMover.controllers.SimulationController;

/**
 * Tests for ProcessQueue<E> with statistics - Wave 2.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class ProcessQueueTest {

    /**
     * Hand-written test entity that enqueues items
     */
    @Entity
    @Transformed(comment = "Hand written", date = "2024", value = "Hand")
    public static class Enqueuer {
        @Transformed(comment = "Hand written", date = "2024", value = "Hand")
        public static class entity extends Enqueuer implements EntityReference {
            public static final int ENQUEUE = 0;

            public entity(Devi controller, ProcessQueue<String> queue, String item) {
                this.controller = controller;
                this.queue = queue;
                this.item = item;
            }

            @Override
            public Object __invoke(int event, Object[] arguments) throws Throwable {
                return switch (event) {
                    case ENQUEUE -> {
                        super.enqueue();
                        yield null;
                    }
                    default -> throw new IllegalArgumentException("Unknown event: " + event);
                };
            }

            @Override
            public String __signatureFor(int event) {
                return switch (event) {
                    case ENQUEUE -> "<Enqueuer: void enqueue()>";
                    default -> throw new IllegalArgumentException("Unknown event: " + event);
                };
            }

            @Override
            public void enqueue() {
                try {
                    controller.postContinuingEvent(this, ENQUEUE);
                } catch (Throwable e) {
                    throw new IllegalStateException("Exception in enqueue", e);
                }
            }
        }

        protected Devi                  controller;
        protected ProcessQueue<String>  queue;
        protected String                item;

        protected Enqueuer() {
            this.queue = null;
            this.item = null;
        }

        public Enqueuer(ProcessQueue<String> queue, String item) {
            this.queue = queue;
            this.item = item;
        }

        public void enqueue() {
            queue.enqueue(item);
        }
    }

    /**
     * Hand-written test entity that dequeues items
     */
    @Entity
    @Transformed(comment = "Hand written", date = "2024", value = "Hand")
    public static class Dequeuer {
        @Transformed(comment = "Hand written", date = "2024", value = "Hand")
        public static class entity extends Dequeuer implements EntityReference {
            public static final int DEQUEUE = 0;

            public entity(Devi controller, ProcessQueue<String> queue, List<String> results) {
                this.controller = controller;
                this.queue = queue;
                this.results = results;
            }

            @Override
            public Object __invoke(int event, Object[] arguments) throws Throwable {
                return switch (event) {
                    case DEQUEUE -> {
                        super.dequeue();
                        yield null;
                    }
                    default -> throw new IllegalArgumentException("Unknown event: " + event);
                };
            }

            @Override
            public String __signatureFor(int event) {
                return switch (event) {
                    case DEQUEUE -> "<Dequeuer: void dequeue()>";
                    default -> throw new IllegalArgumentException("Unknown event: " + event);
                };
            }

            @Override
            public void dequeue() {
                try {
                    controller.postContinuingEvent(this, DEQUEUE);
                } catch (Throwable e) {
                    throw new IllegalStateException("Exception in dequeue", e);
                }
            }
        }

        protected Devi                  controller;
        protected ProcessQueue<String>  queue;
        protected List<String>          results;

        protected Dequeuer() {
            this.queue = null;
            this.results = null;
        }

        public Dequeuer(ProcessQueue<String> queue, List<String> results) {
            this.queue = queue;
            this.results = results;
        }

        public void dequeue() {
            var item = queue.dequeue();
            results.add(item + ":" + controller.getCurrentTime());
        }
    }

    @Test
    public void testBasicEnqueueDequeue() throws Exception {
        try (var controller = new SimulationController()) {
            var queue = new ProcessQueue<String>(controller);
            var results = new ArrayList<String>();

            var enqueuer = new Enqueuer.entity(controller, queue, "item1");
            var dequeuer = new Dequeuer.entity(controller, queue, results);

            controller.postEvent(0, enqueuer, Enqueuer.entity.ENQUEUE);
            controller.postEvent(10, dequeuer, Dequeuer.entity.DEQUEUE);

            controller.eventLoop();

            assertEquals(List.of("item1:10"), results, "Should dequeue the item at time 10");
            assertTrue(queue.isEmpty(), "Queue should be empty after dequeue");
        }
    }

    @Test
    public void testFIFOOrdering() throws Exception {
        try (var controller = new SimulationController()) {
            var queue = new ProcessQueue<String>(controller);
            var results = new ArrayList<String>();

            // Enqueue three items
            var enqueuer1 = new Enqueuer.entity(controller, queue, "first");
            var enqueuer2 = new Enqueuer.entity(controller, queue, "second");
            var enqueuer3 = new Enqueuer.entity(controller, queue, "third");

            controller.postEvent(0, enqueuer1, Enqueuer.entity.ENQUEUE);
            controller.postEvent(1, enqueuer2, Enqueuer.entity.ENQUEUE);
            controller.postEvent(2, enqueuer3, Enqueuer.entity.ENQUEUE);

            // Dequeue three items
            var dequeuer1 = new Dequeuer.entity(controller, queue, results);
            var dequeuer2 = new Dequeuer.entity(controller, queue, results);
            var dequeuer3 = new Dequeuer.entity(controller, queue, results);

            controller.postEvent(10, dequeuer1, Dequeuer.entity.DEQUEUE);
            controller.postEvent(20, dequeuer2, Dequeuer.entity.DEQUEUE);
            controller.postEvent(30, dequeuer3, Dequeuer.entity.DEQUEUE);

            controller.eventLoop();

            assertEquals(List.of("first:10", "second:20", "third:30"), results,
                         "Items should dequeue in FIFO order");
        }
    }

    @Test
    public void testWaitTimeStatistics() throws Exception {
        try (var controller = new SimulationController()) {
            var queue = new ProcessQueue<String>(controller);
            var results = new ArrayList<String>();

            // Enqueue at time 0
            var enqueuer = new Enqueuer.entity(controller, queue, "item");
            controller.postEvent(0, enqueuer, Enqueuer.entity.ENQUEUE);

            // Dequeue at time 100
            var dequeuer = new Dequeuer.entity(controller, queue, results);
            controller.postEvent(100, dequeuer, Dequeuer.entity.DEQUEUE);

            controller.eventLoop();

            // Wait time should be 100 (dequeue time - enqueue time)
            assertEquals(100.0, queue.getAvgWaitTime(), 0.001, "Average wait time should be 100");
            assertEquals(100, queue.getMaxWaitTime(), "Max wait time should be 100");
            assertEquals(1, queue.getTotalEntries(), "Should have 1 entry");
            assertEquals(1, queue.getTotalExits(), "Should have 1 exit");
        }
    }

    @Test
    public void testQueueLengthStatistics() throws Exception {
        try (var controller = new SimulationController()) {
            var queue = new ProcessQueue<String>(controller);

            // Enqueue three items at different times
            var enqueuer1 = new Enqueuer.entity(controller, queue, "item1");
            var enqueuer2 = new Enqueuer.entity(controller, queue, "item2");
            var enqueuer3 = new Enqueuer.entity(controller, queue, "item3");

            controller.postEvent(0, enqueuer1, Enqueuer.entity.ENQUEUE);
            controller.postEvent(10, enqueuer2, Enqueuer.entity.ENQUEUE);
            controller.postEvent(20, enqueuer3, Enqueuer.entity.ENQUEUE);

            // Run until time 30 to build up queue
            controller.setEndTime(30);
            controller.eventLoop();

            assertEquals(3, queue.getCurrentLength(), "Queue should have 3 items");
            assertEquals(3, queue.getMaxLength(), "Max length should be 3");

            // Dequeue one item
            var dequeuer = new Dequeuer.entity(controller, queue, new ArrayList<>());
            controller.postEvent(40, dequeuer, Dequeuer.entity.DEQUEUE);

            controller.setEndTime(Long.MAX_VALUE);
            controller.eventLoop();

            assertEquals(2, queue.getCurrentLength(), "Queue should have 2 items after dequeue");
            assertEquals(3, queue.getMaxLength(), "Max length should still be 3");
        }
    }

    @Test
    public void testEmptyQueueDequeue() throws Exception {
        try (var controller = new SimulationController()) {
            var queue = new ProcessQueue<String>(controller);

            var item = queue.dequeue();

            assertNull(item, "Dequeuing from empty queue should return null");
        }
    }

    @Test
    public void testMultipleEnqueueDequeue() throws Exception {
        try (var controller = new SimulationController()) {
            var queue = new ProcessQueue<String>(controller);
            var results = new ArrayList<String>();

            // Interleave enqueues and dequeues
            for (int i = 0; i < 10; i++) {
                var enqueuer = new Enqueuer.entity(controller, queue, "item" + i);
                controller.postEvent(i * 10, enqueuer, Enqueuer.entity.ENQUEUE);
            }

            for (int i = 0; i < 10; i++) {
                var dequeuer = new Dequeuer.entity(controller, queue, results);
                controller.postEvent(100 + i * 10, dequeuer, Dequeuer.entity.DEQUEUE);
            }

            controller.eventLoop();

            assertEquals(10, results.size(), "Should have dequeued 10 items");
            assertEquals(10, queue.getTotalEntries(), "Should have 10 entries");
            assertEquals(10, queue.getTotalExits(), "Should have 10 exits");

            // Check FIFO ordering
            for (int i = 0; i < 10; i++) {
                assertTrue(results.get(i).startsWith("item" + i + ":"), "Item " + i + " should be in correct order");
            }
        }
    }

    @Test
    public void testRemoveSpecificElement() throws Exception {
        try (var controller = new SimulationController()) {
            var queue = new ProcessQueue<String>(controller);

            // Enqueue three items
            var enqueuer1 = new Enqueuer.entity(controller, queue, "first");
            var enqueuer2 = new Enqueuer.entity(controller, queue, "second");
            var enqueuer3 = new Enqueuer.entity(controller, queue, "third");

            controller.postEvent(0, enqueuer1, Enqueuer.entity.ENQUEUE);
            controller.postEvent(1, enqueuer2, Enqueuer.entity.ENQUEUE);
            controller.postEvent(2, enqueuer3, Enqueuer.entity.ENQUEUE);

            controller.setEndTime(5);
            controller.eventLoop();

            // Remove middle element
            assertTrue(queue.remove("second"), "Should remove 'second'");
            assertEquals(2, queue.size(), "Queue should have 2 items");

            // Dequeue remaining items
            var results = new ArrayList<String>();
            var dequeuer1 = new Dequeuer.entity(controller, queue, results);
            var dequeuer2 = new Dequeuer.entity(controller, queue, results);

            controller.postEvent(10, dequeuer1, Dequeuer.entity.DEQUEUE);
            controller.postEvent(20, dequeuer2, Dequeuer.entity.DEQUEUE);

            controller.setEndTime(Long.MAX_VALUE);
            controller.eventLoop();

            assertEquals(List.of("first:10", "third:20"), results,
                         "Should dequeue first and third, skipping removed second");
        }
    }

    @Test
    public void testIterator() throws Exception {
        try (var controller = new SimulationController()) {
            var queue = new ProcessQueue<String>(controller);

            // Enqueue three items
            var enqueuer1 = new Enqueuer.entity(controller, queue, "first");
            var enqueuer2 = new Enqueuer.entity(controller, queue, "second");
            var enqueuer3 = new Enqueuer.entity(controller, queue, "third");

            controller.postEvent(0, enqueuer1, Enqueuer.entity.ENQUEUE);
            controller.postEvent(1, enqueuer2, Enqueuer.entity.ENQUEUE);
            controller.postEvent(2, enqueuer3, Enqueuer.entity.ENQUEUE);

            controller.setEndTime(5);
            controller.eventLoop();

            // Iterate over queue
            var items = new ArrayList<String>();
            for (String item : queue) {
                items.add(item);
            }

            assertEquals(List.of("first", "second", "third"), items,
                         "Iterator should return items in FIFO order");
            assertEquals(3, queue.size(), "Iterator should not modify queue");
        }
    }

    @Test
    public void testPeek() throws Exception {
        try (var controller = new SimulationController()) {
            var queue = new ProcessQueue<String>(controller);

            assertNull(queue.peek(), "Peek on empty queue should return null");

            var enqueuer = new Enqueuer.entity(controller, queue, "item");
            controller.postEvent(0, enqueuer, Enqueuer.entity.ENQUEUE);

            controller.setEndTime(5);
            controller.eventLoop();

            assertEquals("item", queue.peek(), "Peek should return first item");
            assertEquals(1, queue.size(), "Peek should not modify queue");
        }
    }

    @Test
    public void testContains() throws Exception {
        try (var controller = new SimulationController()) {
            var queue = new ProcessQueue<String>(controller);

            assertFalse(queue.contains("item"), "Empty queue should not contain item");

            var enqueuer = new Enqueuer.entity(controller, queue, "item");
            controller.postEvent(0, enqueuer, Enqueuer.entity.ENQUEUE);

            controller.setEndTime(5);
            controller.eventLoop();

            assertTrue(queue.contains("item"), "Queue should contain enqueued item");
            assertFalse(queue.contains("other"), "Queue should not contain non-existent item");
        }
    }

    @Test
    public void testClear() throws Exception {
        try (var controller = new SimulationController()) {
            var queue = new ProcessQueue<String>(controller);

            // Enqueue three items
            var enqueuer1 = new Enqueuer.entity(controller, queue, "first");
            var enqueuer2 = new Enqueuer.entity(controller, queue, "second");
            var enqueuer3 = new Enqueuer.entity(controller, queue, "third");

            controller.postEvent(0, enqueuer1, Enqueuer.entity.ENQUEUE);
            controller.postEvent(1, enqueuer2, Enqueuer.entity.ENQUEUE);
            controller.postEvent(2, enqueuer3, Enqueuer.entity.ENQUEUE);

            controller.setEndTime(5);
            controller.eventLoop();

            assertEquals(3, queue.size(), "Queue should have 3 items");

            queue.clear();

            assertTrue(queue.isEmpty(), "Queue should be empty after clear");
            assertEquals(0, queue.size(), "Size should be 0 after clear");
        }
    }

    @Test
    public void testTimeWeightedAverageLength() throws Exception {
        try (var controller = new SimulationController()) {
            var queue = new ProcessQueue<String>(controller);

            // Enqueue 1 item at time 0 (length = 1)
            var enqueuer1 = new Enqueuer.entity(controller, queue, "item1");
            controller.postEvent(0, enqueuer1, Enqueuer.entity.ENQUEUE);

            // Enqueue 2nd item at time 50 (length = 2)
            var enqueuer2 = new Enqueuer.entity(controller, queue, "item2");
            controller.postEvent(50, enqueuer2, Enqueuer.entity.ENQUEUE);

            // Dequeue at time 100 (length = 1)
            var dequeuer = new Dequeuer.entity(controller, queue, new ArrayList<>());
            controller.postEvent(100, dequeuer, Dequeuer.entity.DEQUEUE);

            controller.setEndTime(150);
            controller.eventLoop();

            // Time-weighted average:
            // 0-50: length 1 for 50 time units = 50
            // 50-100: length 2 for 50 time units = 100
            // 100-150: length 1 for 50 time units = 50
            // Total: (50 + 100 + 50) / 150 = 200 / 150 = 1.333...
            
            var avgLength = queue.getAvgLength(150);
            assertEquals(1.333, avgLength, 0.01, "Time-weighted average length should be ~1.333");
        }
    }

    @Test
    public void testStatisticsReset() throws Exception {
        try (var controller = new SimulationController()) {
            var queue = new ProcessQueue<String>(controller);
            var results = new ArrayList<String>();

            // Enqueue and dequeue one item
            var enqueuer = new Enqueuer.entity(controller, queue, "item");
            var dequeuer = new Dequeuer.entity(controller, queue, results);

            controller.postEvent(0, enqueuer, Enqueuer.entity.ENQUEUE);
            controller.postEvent(100, dequeuer, Dequeuer.entity.DEQUEUE);

            controller.eventLoop();

            assertEquals(1, queue.getTotalEntries(), "Should have 1 entry");
            assertEquals(1, queue.getTotalExits(), "Should have 1 exit");

            queue.resetStatistics();

            assertEquals(0, queue.getTotalEntries(), "Entries should be reset to 0");
            assertEquals(0, queue.getTotalExits(), "Exits should be reset to 0");
            assertEquals(0, queue.getMaxLength(), "Max length should be reset to 0");
        }
    }
}
