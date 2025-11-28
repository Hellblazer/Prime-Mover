/*
 * Copyright (C) 2023 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.hellblazer.primeMover.classfile;

import io.github.classgraph.ClassGraph;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for thread-safe concurrent access to SimulationTransform.
 * Validates Issue 1.1: Thread-safe transformTimestamp using AtomicReference.
 *
 * @author hal.hildebrand
 */
class ConcurrentTransformationTest {

    /**
     * Test that multiple threads can safely access and modify transformTimestamp concurrently.
     */
    @Test
    void testConcurrentTimestampAccess() throws Exception {
        var graph = new ClassGraph().acceptPackages("com.hellblazer.primeMover");
        var transform = new SimulationTransform(graph);

        var timestamp = "2024-01-01T12:00:00Z";
        transform.setTransformTimestamp(timestamp);

        var numThreads = 10;
        var executor = Executors.newFixedThreadPool(numThreads);
        var barrier = new CyclicBarrier(numThreads);
        var errors = new AtomicReference<Exception>();

        try {
            for (int i = 0; i < numThreads; i++) {
                executor.submit(() -> {
                    try {
                        barrier.await(); // Ensure all threads start at the same time
                        var retrieved = transform.getTransformTimestamp();
                        assertEquals(timestamp, retrieved, "Timestamp should be consistent across threads");
                    } catch (Exception e) {
                        errors.set(e);
                    }
                });
            }

            executor.shutdown();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS), "Threads should complete within timeout");
            assertNull(errors.get(), "No exceptions should occur during concurrent access");
        } finally {
            transform.close();
        }
    }

    /**
     * Test that concurrent setTransformTimestamp operations are thread-safe.
     */
    @Test
    void testConcurrentTimestampModification() throws Exception {
        var graph = new ClassGraph().acceptPackages("com.hellblazer.primeMover");
        var transform = new SimulationTransform(graph);

        var numThreads = 10;
        var executor = Executors.newFixedThreadPool(numThreads);
        var latch = new CountDownLatch(numThreads);
        var errors = new AtomicReference<Exception>();

        try {
            for (int i = 0; i < numThreads; i++) {
                var threadNum = i;
                executor.submit(() -> {
                    try {
                        var timestamp = "thread-" + threadNum;
                        transform.setTransformTimestamp(timestamp);
                        latch.countDown();
                    } catch (Exception e) {
                        errors.set(e);
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(10, TimeUnit.SECONDS), "All threads should complete");
            assertNull(errors.get(), "No exceptions should occur during concurrent modification");

            // The final timestamp should be one of the thread timestamps
            var finalTimestamp = transform.getTransformTimestamp();
            assertNotNull(finalTimestamp, "Final timestamp should not be null");
            assertTrue(finalTimestamp.startsWith("thread-"), "Final timestamp should be from one of the threads");

            executor.shutdown();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS),
                       "Executor should shutdown cleanly within timeout");
        } finally {
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
            transform.close();
        }
    }

    /**
     * Test that transformTimestamp is properly initialized and accessible.
     */
    @Test
    void testTimestampInitialization() throws Exception {
        var graph = new ClassGraph().acceptPackages("com.hellblazer.primeMover");
        var transform = new SimulationTransform(graph);

        try {
            var timestamp = transform.getTransformTimestamp();
            assertNotNull(timestamp, "Timestamp should be initialized");
            assertFalse(timestamp.isEmpty(), "Timestamp should not be empty");
        } finally {
            transform.close();
        }
    }
}
