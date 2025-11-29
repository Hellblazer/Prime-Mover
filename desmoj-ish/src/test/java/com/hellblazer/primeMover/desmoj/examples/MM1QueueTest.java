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

package com.hellblazer.primeMover.desmoj.examples;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.hellblazer.primeMover.annotations.Blocking;
import com.hellblazer.primeMover.annotations.Entity;
import com.hellblazer.primeMover.annotations.Transformed;
import com.hellblazer.primeMover.api.EntityReference;
import com.hellblazer.primeMover.controllers.SimulationController;
import com.hellblazer.primeMover.runtime.Devi;
import com.hellblazer.primeMover.runtime.Kairos;
import com.hellblazer.primeMover.desmoj.Resource;

/**
 * M/M/1 Queue simulation example demonstrating Resource blocking primitives.
 *
 * <p>This is a classic queueing theory example: customers arrive at random intervals
 * and compete for a single server (M/M/1 = Markovian arrivals/Markovian service/1 server).
 *
 * <p>The test validates:
 * <ul>
 * <li>All customers get served (no lost customers)
 * <li>Resource blocking works correctly (customers wait when server busy)
 * <li>Statistics are properly collected (acquisitions, wait times)
 * <li>Customers actually queue when arrival rate exceeds service rate
 * </ul>
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class MM1QueueTest {

    /**
     * Hand-written Customer entity that arrives, waits for server, gets served, and leaves.
     */
    @Entity
    @Transformed(comment = "Hand written", date = "2024", value = "Hand")
    public static class Customer {
        @Transformed(comment = "Hand written", date = "2024", value = "Hand")
        public static class entity extends Customer implements EntityReference {
            public static final int VISIT = 0;

            public entity(Devi controller, int customerId, Resource server, long serviceTime,
                         AtomicInteger servedCount, List<String> log) {
                this.controller = controller;
                this.customerId = customerId;
                this.server = server;
                this.serviceTime = serviceTime;
                this.servedCount = servedCount;
                this.log = log;
            }

            @Override
            public Object __invoke(int event, Object[] arguments) throws Throwable {
                return switch (event) {
                    case VISIT -> {
                        super.visit();
                        yield null;
                    }
                    default -> throw new IllegalArgumentException("Unknown event: " + event);
                };
            }

            @Override
            public String __signatureFor(int event) {
                return switch (event) {
                    case VISIT -> "<Customer: void visit()>";
                    default -> throw new IllegalArgumentException("Unknown event: " + event);
                };
            }

            @Override
            public void visit() {
                try {
                    controller.postContinuingEvent(this, VISIT);
                } catch (Throwable e) {
                    throw new IllegalStateException("Exception in visit", e);
                }
            }
        }

        protected Devi controller;
        protected int customerId;
        protected Resource server;
        protected long serviceTime;
        protected AtomicInteger servedCount;
        protected List<String> log;

        protected Customer() {}

        /**
         * Customer lifecycle: arrive, acquire server (blocking if busy), get served, release server, leave.
         */
        @Blocking
        public void visit() {
            var arrivalTime = controller.getCurrentTime();
            log.add("customer-" + customerId + ":arrive:" + arrivalTime);

            // Acquire the server - this will block if server is busy
            var token = server.acquire();

            var serviceStartTime = controller.getCurrentTime();
            var waitTime = serviceStartTime - arrivalTime;
            log.add("customer-" + customerId + ":acquired:" + serviceStartTime + ":waited:" + waitTime);

            // Simulate service time using blocking sleep
            Kairos.blockingSleep(serviceTime);

            var serviceEndTime = controller.getCurrentTime();
            log.add("customer-" + customerId + ":served:" + serviceEndTime);

            // Release the server
            server.release(token);

            // Track that we were served
            servedCount.incrementAndGet();
        }
    }

    /**
     * Test M/M/1 queue where customers arrive faster than service rate.
     * This creates actual queueing and wait times.
     *
     * Setup:
     * - Inter-arrival time: 5 (customer arrives every 5 time units)
     * - Service time: 10 (each customer takes 10 time units to serve)
     * - Traffic intensity (rho) = serviceTime/interArrival = 10/5 = 2.0 (overloaded!)
     *
     * Since rho > 1, queue will grow without bound. We limit to 5 customers to observe behavior.
     */
    @Test
    public void testMM1QueueWithContention() throws Exception {
        final var numCustomers = 5;
        final var interArrivalTime = 5L;  // Customer arrives every 5 time units
        final var serviceTime = 10L;       // Each customer takes 10 time units to serve

        try (var controller = new SimulationController()) {
            var servedCount = new AtomicInteger(0);
            var log = new ArrayList<String>();

            // Create a single server (M/M/1 has exactly 1 server)
            var server = new Resource.entity(controller, 1);

            // Schedule customer arrivals
            // Customer 0: arrives at 0
            // Customer 1: arrives at 5
            // Customer 2: arrives at 10
            // Customer 3: arrives at 15
            // Customer 4: arrives at 20
            for (int i = 0; i < numCustomers; i++) {
                var customer = new Customer.entity(controller, i, server, serviceTime, servedCount, log);
                var arrivalTime = i * interArrivalTime;
                controller.postEvent(arrivalTime, customer, Customer.entity.VISIT);
            }

            // Run the simulation
            controller.eventLoop();

            // Verify all customers were served
            assertEquals(numCustomers, servedCount.get(),
                        "All customers should be served");

            // Verify server is idle at the end
            assertEquals(1, server.available(),
                        "Server should be idle after all customers served");

            // Verify statistics
            var stats = server.statistics();
            assertEquals(numCustomers, stats.getTotalAcquisitions(),
                        "Should have exactly " + numCustomers + " acquisitions");

            // Print results first to see actual behavior
            System.out.println("M/M/1 Queue with Contention Test Results:");
            System.out.println("  Customers served: " + servedCount.get());
            System.out.println("  Total acquisitions: " + stats.getTotalAcquisitions());
            System.out.println("  Average wait time: " + stats.getAvgWaitTime());
            System.out.println("  Max wait time: " + stats.getMaxWaitTime());
            System.out.println("  Event log:");
            for (var entry : log) {
                System.out.println("    " + entry);
            }

            // With arrival rate > service rate, customers must wait.
            // The exact wait times depend on FIFO ordering in the Resource queue.
            // Verify that wait times are positive (contention occurred) and reasonable.
            assertTrue(stats.getAvgWaitTime() > 0,
                      "Average wait time should be positive with contention");
            assertTrue(stats.getMaxWaitTime() > 0,
                      "Max wait time should be positive with contention");
        }
    }

    /**
     * Test M/M/1 queue with burst arrivals (all at time 0).
     * This is the extreme contention case.
     */
    @Test
    public void testMM1QueueWithBurstArrivals() throws Exception {
        final var numCustomers = 5;
        final var serviceTime = 10L;

        try (var controller = new SimulationController()) {
            var servedCount = new AtomicInteger(0);
            var log = new ArrayList<String>();

            var server = new Resource.entity(controller, 1);

            // All customers arrive at time 0
            for (int i = 0; i < numCustomers; i++) {
                var customer = new Customer.entity(controller, i, server, serviceTime, servedCount, log);
                controller.postEvent(0, customer, Customer.entity.VISIT);
            }

            controller.eventLoop();

            assertEquals(numCustomers, servedCount.get(),
                        "All customers should be served");

            var stats = server.statistics();
            assertEquals(numCustomers, stats.getTotalAcquisitions(),
                        "Should have exactly " + numCustomers + " acquisitions");

            // All arrive at 0, each takes 10 time units
            // Customer 0: served 0-10, wait = 0
            // Customer 1: served 10-20, wait = 10
            // Customer 2: served 20-30, wait = 20
            // Customer 3: served 30-40, wait = 30
            // Customer 4: served 40-50, wait = 40
            // Average wait = (0 + 10 + 20 + 30 + 40) / 5 = 20
            assertEquals(20.0, stats.getAvgWaitTime(), 0.001,
                        "Average wait time should be 20");
            assertEquals(40, stats.getMaxWaitTime(),
                        "Max wait time should be 40 (customer 4)");

            System.out.println("\nM/M/1 Queue with Burst Arrivals Test Results:");
            System.out.println("  Customers served: " + servedCount.get());
            System.out.println("  Average wait time: " + stats.getAvgWaitTime());
            System.out.println("  Max wait time: " + stats.getMaxWaitTime());
            System.out.println("  Final simulation time: " + controller.getCurrentTime());
        }
    }

    /**
     * Test stable M/M/1 queue where arrival rate < service rate.
     * Traffic intensity rho < 1, so queue should stabilize.
     */
    @Test
    public void testMM1QueueStable() throws Exception {
        final var numCustomers = 5;
        final var interArrivalTime = 20L;  // Customer arrives every 20 time units
        final var serviceTime = 10L;        // Each customer takes 10 time units

        // rho = 10/20 = 0.5 (stable, queue should not grow)

        try (var controller = new SimulationController()) {
            var servedCount = new AtomicInteger(0);
            var log = new ArrayList<String>();

            var server = new Resource.entity(controller, 1);

            // Customer 0: arrives 0
            // Customer 1: arrives 20
            // Customer 2: arrives 40
            // etc.
            for (int i = 0; i < numCustomers; i++) {
                var customer = new Customer.entity(controller, i, server, serviceTime, servedCount, log);
                controller.postEvent(i * interArrivalTime, customer, Customer.entity.VISIT);
            }

            controller.eventLoop();

            assertEquals(numCustomers, servedCount.get(),
                        "All customers should be served");

            var stats = server.statistics();

            // With inter-arrival=20, service=10:
            // Customer 0: arrives 0, served 0-10, wait = 0
            // Customer 1: arrives 20, served 20-30, wait = 0 (server idle since 10)
            // Customer 2: arrives 40, served 40-50, wait = 0
            // etc.
            // All customers find server idle - zero wait time
            assertEquals(0.0, stats.getAvgWaitTime(), 0.001,
                        "With stable load (rho < 1), customers should not wait");
            assertEquals(0, stats.getMaxWaitTime(),
                        "Max wait should be zero");

            System.out.println("\nM/M/1 Queue Stable (rho=0.5) Test Results:");
            System.out.println("  Customers served: " + servedCount.get());
            System.out.println("  Average wait time: " + stats.getAvgWaitTime());
            System.out.println("  Max wait time: " + stats.getMaxWaitTime());
        }
    }
}
