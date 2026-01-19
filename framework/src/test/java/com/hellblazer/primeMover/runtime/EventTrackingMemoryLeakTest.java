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

import static org.junit.jupiter.api.Assertions.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.hellblazer.primeMover.api.EntityReference;
import com.hellblazer.primeMover.controllers.SimulationController;

/**
 * Tests for event tracking memory leak prevention.
 * <p>
 * This test verifies that completed events can be garbage collected and don't create
 * memory leaks through strong reference chains. The primary concern is the EventImpl.caller
 * field which creates strong references between events in blocking operations.
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class EventTrackingMemoryLeakTest {

    /**
     * Minimal EntityReference implementation for testing
     */
    static class TestEntity implements EntityReference {
        private final String name;

        TestEntity(String name) {
            this.name = name;
        }

        @Override
        public Object __invoke(int event, Object[] args) {
            return null;
        }

        @Override
        public String __signatureFor(int event) {
            return name + ".testEvent" + event;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Nested
    @DisplayName("Event memory leak tests")
    class EventMemoryLeakTests {

        @Test
        @DisplayName("completed events should be garbage collectible")
        void completedEventsAreGarbageCollectible() throws Exception {
            var controller = new SimulationController();
            controller.setTrackEventSources(false);
            controller.setEndTime(200);

            List<WeakReference<EventImpl>> eventRefs = new ArrayList<>();

            // Create and post events, capturing weak references
            var entity = new TestEntity("TestEntity");
            for (int i = 0; i < 100; i++) {
                var event = new EventImpl(i, null, entity, 0);
                eventRefs.add(new WeakReference<>(event));
                controller.post(event);
            }

            // Process all events
            controller.eventLoop();

            // Verify events were processed
            assertEquals(100, controller.getTotalEvents(), "Should process 100 events");

            // Force garbage collection
            for (int i = 0; i < 5; i++) {
                System.gc();
                Thread.sleep(100);
            }

            // Assert - completed events should be garbage collected
            var stillReachable = eventRefs.stream()
                .filter(ref -> ref.get() != null)
                .count();

            // We expect most events to be GC'd. Some might still be reachable due to timing,
            // but if more than 10% are reachable, we likely have a leak
            var leakThreshold = eventRefs.size() * 0.1;
            assertTrue(stillReachable <= leakThreshold,
                String.format("Expected most events to be GC'd (threshold: %.0f), but %d/%d still reachable. " +
                    "This indicates strong references are preventing garbage collection.",
                    leakThreshold, stillReachable, eventRefs.size()));
        }

        @Test
        @DisplayName("events with source tracking should be garbage collectible")
        void eventsWithSourceTrackingAreCollectible() throws Exception {
            var controller = new SimulationController();
            controller.setTrackEventSources(true); // Enable source tracking (uses WeakReference)
            controller.setEndTime(200);

            List<WeakReference<EventImpl>> eventRefs = new ArrayList<>();

            var entity = new TestEntity("TestEntity");
            EventImpl previousEvent = null;
            for (int i = 0; i < 100; i++) {
                var event = new EventImpl(i, previousEvent, entity, 0);
                eventRefs.add(new WeakReference<>(event));
                controller.post(event);
                previousEvent = event;
            }

            controller.eventLoop();

            // Force garbage collection
            previousEvent = null; // Clear the last reference
            for (int i = 0; i < 5; i++) {
                System.gc();
                Thread.sleep(100);
            }

            // Assert - source tracking uses WeakReference, so events should still be GC-able
            var stillReachable = eventRefs.stream()
                .filter(ref -> ref.get() != null)
                .count();

            var leakThreshold = eventRefs.size() * 0.1;
            assertTrue(stillReachable <= leakThreshold,
                String.format("With source tracking (WeakReference), expected most events GC'd, but %d/%d still reachable",
                    stillReachable, eventRefs.size()));
        }

        @Test
        @DisplayName("event caller chains should not prevent GC")
        void eventCallerChainsDoNotPreventGC() throws Exception {
            // This test specifically targets the EventImpl.caller field memory leak
            var controller = new SimulationController();
            controller.setTrackEventSources(false);
            controller.setEndTime(200);

            List<WeakReference<EventImpl>> eventRefs = new ArrayList<>();

            // Create events with caller chains (simulating blocking operations)
            var entity = new TestEntity("TestEntity");
            EventImpl previousCaller = null;
            for (int i = 0; i < 100; i++) {
                var event = new EventImpl(i, null, entity, 0);
                if (previousCaller != null) {
                    event.setCaller(previousCaller);
                }
                eventRefs.add(new WeakReference<>(event));
                controller.post(event);
                previousCaller = event;
            }

            controller.eventLoop();

            // Clear the last caller reference
            previousCaller = null;

            // Force aggressive GC
            for (int i = 0; i < 10; i++) {
                System.gc();
                Thread.sleep(100);
            }

            // Count how many events are still reachable
            var stillReachable = eventRefs.stream()
                .filter(ref -> ref.get() != null)
                .count();

            // Critical test: if EventImpl.caller creates long-lived strong reference chains,
            // completed events won't be GC'd even after processing
            var leakThreshold = eventRefs.size() * 0.1;
            assertTrue(stillReachable <= leakThreshold,
                String.format("Memory leak detected: %d/%d events still reachable (threshold: %.0f). " +
                    "EventImpl.caller chains are likely holding strong references and preventing GC.",
                    stillReachable, eventRefs.size(), leakThreshold));
        }

        @Test
        @DisplayName("entities referenced by events should be GC-able after simulation")
        void entitiesReferencedByEventsAreCollectible() throws Exception {
            var controller = new SimulationController();
            controller.setTrackEventSources(false);
            controller.setEndTime(200);

            List<WeakReference<TestEntity>> entityRefs = new ArrayList<>();

            // Create events referencing entities, capturing weak refs to entities
            {
                for (int i = 0; i < 10; i++) {
                    var entity = new TestEntity("Entity" + i);
                    entityRefs.add(new WeakReference<>(entity));

                    // Post some events for each entity
                    for (int j = 0; j < 10; j++) {
                        controller.post(new EventImpl(i * 10 + j, null, entity, j));
                    }
                }
                // Entities go out of scope here
            }

            controller.eventLoop();

            // Force garbage collection
            for (int i = 0; i < 5; i++) {
                System.gc();
                Thread.sleep(100);
            }

            // Assert - entities should be collectible after simulation completes
            // If events hold strong references to entities indefinitely, this will fail
            var stillReachable = entityRefs.stream()
                .filter(ref -> ref.get() != null)
                .count();

            assertEquals(0, stillReachable,
                String.format("All entities should be GC'd after simulation, but %d/%d still reachable. " +
                    "Completed events are holding strong references to entities via EventImpl.reference field.",
                    stillReachable, entityRefs.size()));
        }

        @Test
        @DisplayName("stress test: many events should not accumulate memory")
        void stressTestManyEventsDoNotAccumulateMemory() throws Exception {
            // This test simulates a long-running simulation to detect gradual memory accumulation
            var controller = new SimulationController();
            controller.setTrackEventSources(false);
            controller.setEndTime(10000);

            var entity = new TestEntity("StressTest");

            // Track memory before
            System.gc();
            Thread.sleep(200);
            var runtime = Runtime.getRuntime();
            var memoryBefore = runtime.totalMemory() - runtime.freeMemory();

            // Process many events
            for (int i = 0; i < 1000; i++) {
                controller.post(new EventImpl(i, null, entity, 0));
            }

            controller.eventLoop();
            assertEquals(1000, controller.getTotalEvents());

            // Force GC and check memory
            for (int i = 0; i < 5; i++) {
                System.gc();
                Thread.sleep(200);
            }

            var memoryAfter = runtime.totalMemory() - runtime.freeMemory();
            var memoryGrowth = memoryAfter - memoryBefore;

            // Memory should not grow significantly (allow some overhead for controller structures)
            // If more than 10MB retained, likely a leak
            var maxAcceptableGrowth = 10 * 1024 * 1024; // 10 MB
            assertTrue(memoryGrowth < maxAcceptableGrowth,
                String.format("Memory grew by %d bytes (%.2f MB), exceeding threshold of %d bytes (%.2f MB). " +
                    "This indicates completed events are not being garbage collected.",
                    memoryGrowth, memoryGrowth / (1024.0 * 1024.0),
                    maxAcceptableGrowth, maxAcceptableGrowth / (1024.0 * 1024.0)));
        }
    }
}
