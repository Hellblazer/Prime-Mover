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

import org.junit.jupiter.api.Test;

import com.hellblazer.primeMover.annotations.Blocking;
import com.hellblazer.primeMover.annotations.Entity;
import com.hellblazer.primeMover.annotations.Transformed;
import com.hellblazer.primeMover.api.EntityReference;
import com.hellblazer.primeMover.controllers.SimulationController;

/**
 * Test for SimSignal blocking primitive - Phase 0 proof of concept.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class SimSignalTest {

    /**
     * Hand-written test entity that waits on a signal
     */
    @Entity
    @Transformed(comment = "Hand written", date = "2024", value = "Hand")
    public static class Waiter {
        @Transformed(comment = "Hand written", date = "2024", value = "Hand")
        public static class entity extends Waiter implements EntityReference {
            private static final int WAIT_FOR_SIGNAL = 0;

            public entity(Devi controller, SimSignal signal, List<String> results) {
                this.controller = controller;
                this.signal = signal;
                this.results = results;
            }

            @Override
            public Object __invoke(int event, Object[] arguments) throws Throwable {
                return switch (event) {
                    case WAIT_FOR_SIGNAL -> {
                        super.waitForSignal();
                        yield null;
                    }
                    default -> throw new IllegalArgumentException("Unknown event: " + event);
                };
            }

            @Override
            public String __signatureFor(int event) {
                return switch (event) {
                    case WAIT_FOR_SIGNAL -> "<Waiter: void waitForSignal()>";
                    default -> throw new IllegalArgumentException("Unknown event: " + event);
                };
            }

            @Override
            public void waitForSignal() {
                try {
                    controller.postContinuingEvent(this, WAIT_FOR_SIGNAL);
                } catch (Throwable e) {
                    throw new IllegalStateException("Exception in waitForSignal", e);
                }
            }
        }

        protected Devi          controller;
        protected SimSignal     signal;
        protected List<String>  results;

        protected Waiter() {
            this.signal = null;
            this.results = null;
        }

        public Waiter(SimSignal signal, List<String> results) {
            this.signal = signal;
            this.results = results;
        }

        @Blocking
        public void waitForSignal() {
            results.add("waiter:before:" + controller.getCurrentTime());
            signal.await();
            results.add("waiter:after:" + controller.getCurrentTime());
        }
    }

    /**
     * Hand-written test entity that signals
     */
    @Entity
    @Transformed(comment = "Hand written", date = "2024", value = "Hand")
    public static class Signaler {
        @Transformed(comment = "Hand written", date = "2024", value = "Hand")
        public static class entity extends Signaler implements EntityReference {
            private static final int DO_SIGNAL = 0;

            public entity(Devi controller, SimSignal signal, List<String> results) {
                this.controller = controller;
                this.signal = signal;
                this.results = results;
            }

            @Override
            public Object __invoke(int event, Object[] arguments) throws Throwable {
                return switch (event) {
                    case DO_SIGNAL -> {
                        super.doSignal();
                        yield null;
                    }
                    default -> throw new IllegalArgumentException("Unknown event: " + event);
                };
            }

            @Override
            public String __signatureFor(int event) {
                return switch (event) {
                    case DO_SIGNAL -> "<Signaler: void doSignal()>";
                    default -> throw new IllegalArgumentException("Unknown event: " + event);
                };
            }

            @Override
            public void doSignal() {
                try {
                    controller.postContinuingEvent(this, DO_SIGNAL);
                } catch (Throwable e) {
                    throw new IllegalStateException("Exception in doSignal", e);
                }
            }
        }

        protected Devi           controller;
        protected SimSignal      signal;
        protected List<String>   results;

        protected Signaler() {
            this.signal = null;
            this.results = null;
        }

        public Signaler(SimSignal signal, List<String> results) {
            this.signal = signal;
            this.results = results;
        }

        public void doSignal() {
            results.add("signaler:" + controller.getCurrentTime());
            signal.signal();
        }
    }

    /**
     * Hand-written test entity that calls signalAll
     */
    @Entity
    @Transformed(comment = "Hand written", date = "2024", value = "Hand")
    public static class SignalAllCaller {
        @Transformed(comment = "Hand written", date = "2024", value = "Hand")
        public static class entity extends SignalAllCaller implements EntityReference {
            private static final int DO_SIGNAL_ALL = 0;

            public entity(Devi controller, SimSignal signal, List<String> results) {
                this.controller = controller;
                this.signal = signal;
                this.results = results;
            }

            @Override
            public Object __invoke(int event, Object[] arguments) throws Throwable {
                return switch (event) {
                    case DO_SIGNAL_ALL -> {
                        super.doSignalAll();
                        yield null;
                    }
                    default -> throw new IllegalArgumentException("Unknown event: " + event);
                };
            }

            @Override
            public String __signatureFor(int event) {
                return switch (event) {
                    case DO_SIGNAL_ALL -> "<SignalAllCaller: void doSignalAll()>";
                    default -> throw new IllegalArgumentException("Unknown event: " + event);
                };
            }

            @Override
            public void doSignalAll() {
                try {
                    controller.postContinuingEvent(this, DO_SIGNAL_ALL);
                } catch (Throwable e) {
                    throw new IllegalStateException("Exception in doSignalAll", e);
                }
            }
        }

        protected Devi          controller;
        protected SimSignal     signal;
        protected List<String>  results;

        protected SignalAllCaller() {
            this.signal = null;
            this.results = null;
        }

        public SignalAllCaller(SimSignal signal, List<String> results) {
            this.signal = signal;
            this.results = results;
        }

        public void doSignalAll() {
            results.add("signalAll:" + controller.getCurrentTime());
            signal.signalAll();
        }
    }

    @Test
    public void testBasicSignal() throws Exception {
        try (var controller = new SimulationController()) {
            var results = new ArrayList<String>();
            var signal = new SimSignal.entity(controller);

            var waiter = new Waiter.entity(controller, signal, results);
            var signaler = new Signaler.entity(controller, signal, results);

            // Schedule waiter at time 0
            controller.postEvent(0, waiter, Waiter.entity.WAIT_FOR_SIGNAL);

            // Schedule signaler at time 100
            controller.postEvent(100, signaler, Signaler.entity.DO_SIGNAL);

            // Run simulation
            controller.eventLoop();

            // Verify the order and timing
            assertEquals(List.of(
                "waiter:before:0",
                "signaler:100",
                "waiter:after:100"
            ), results, "Waiter should block at time 0 and resume at time 100 when signaled");
        }
    }

    @Test
    public void testMultipleWaiters() throws Exception {
        try (var controller = new SimulationController()) {
            var results = new ArrayList<String>();
            var signal = new SimSignal.entity(controller);

            // Create 3 waiters
            var waiter1 = new Waiter.entity(controller, signal, results);
            var waiter2 = new Waiter.entity(controller, signal, results);
            var waiter3 = new Waiter.entity(controller, signal, results);

            // Schedule all waiters at different times
            controller.postEvent(0, waiter1, Waiter.entity.WAIT_FOR_SIGNAL);
            controller.postEvent(10, waiter2, Waiter.entity.WAIT_FOR_SIGNAL);
            controller.postEvent(20, waiter3, Waiter.entity.WAIT_FOR_SIGNAL);

            // Create 3 signalers at later times
            var signaler1 = new Signaler.entity(controller, signal, results);
            var signaler2 = new Signaler.entity(controller, signal, results);
            var signaler3 = new Signaler.entity(controller, signal, results);

            controller.postEvent(100, signaler1, Signaler.entity.DO_SIGNAL);
            controller.postEvent(200, signaler2, Signaler.entity.DO_SIGNAL);
            controller.postEvent(300, signaler3, Signaler.entity.DO_SIGNAL);

            controller.eventLoop();

            // Verify FIFO ordering - first waiter is resumed first
            assertEquals(List.of(
                "waiter:before:0",
                "waiter:before:10",
                "waiter:before:20",
                "signaler:100",
                "waiter:after:100",  // waiter1 resumes at 100
                "signaler:200",
                "waiter:after:200",  // waiter2 resumes at 200
                "signaler:300",
                "waiter:after:300"   // waiter3 resumes at 300
            ), results, "Waiters should resume in FIFO order");
        }
    }

    @Test
    public void testSignalWithNoWaiters() throws Exception {
        try (var controller = new SimulationController()) {
            var results = new ArrayList<String>();
            var signal = new SimSignal.entity(controller);

            var signaler = new Signaler.entity(controller, signal, results);

            // Signal with no waiters - should be a no-op
            controller.postEvent(100, signaler, Signaler.entity.DO_SIGNAL);

            controller.eventLoop();

            assertEquals(List.of("signaler:100"), results, "Signaling with no waiters should be a no-op");
            assertFalse(signal.hasWaiters(), "Should have no waiters");
        }
    }

    @Test
    public void testSignalAll() throws Exception {
        try (var controller = new SimulationController()) {
            var results = new ArrayList<String>();
            var signal = new SimSignal.entity(controller);

            // Create 3 waiters
            var waiter1 = new Waiter.entity(controller, signal, results);
            var waiter2 = new Waiter.entity(controller, signal, results);
            var waiter3 = new Waiter.entity(controller, signal, results);

            controller.postEvent(0, waiter1, Waiter.entity.WAIT_FOR_SIGNAL);
            controller.postEvent(10, waiter2, Waiter.entity.WAIT_FOR_SIGNAL);
            controller.postEvent(20, waiter3, Waiter.entity.WAIT_FOR_SIGNAL);

            // Use a special signaler entity that calls signalAll()
            var signalAllCaller = new SignalAllCaller.entity(controller, signal, results);
            controller.postEvent(100, signalAllCaller, SignalAllCaller.entity.DO_SIGNAL_ALL);

            controller.eventLoop();

            // All three waiters should resume at time 100
            assertEquals(List.of(
                "waiter:before:0",
                "waiter:before:10",
                "waiter:before:20",
                "signalAll:100",
                "waiter:after:100",
                "waiter:after:100",
                "waiter:after:100"
            ), results, "All waiters should resume at time 100");
        }
    }

    @Test
    public void testWaiterCount() throws Exception {
        try (var controller = new SimulationController()) {
            var results = new ArrayList<String>();
            var signal = new SimSignal.entity(controller);

            assertEquals(0, signal.waiterCount(), "Initially should have 0 waiters");

            var waiter1 = new Waiter.entity(controller, signal, results);
            var waiter2 = new Waiter.entity(controller, signal, results);

            controller.postEvent(0, waiter1, Waiter.entity.WAIT_FOR_SIGNAL);
            controller.postEvent(10, waiter2, Waiter.entity.WAIT_FOR_SIGNAL);

            // Run until time 15 (both waiters should be waiting)
            controller.setEndTime(15);
            controller.eventLoop();

            assertEquals(2, signal.waiterCount(), "Should have 2 waiters at time 15");
            assertTrue(signal.hasWaiters(), "Should have waiters");

            // Signal both waiters
            var signaler1 = new Signaler.entity(controller, signal, results);
            var signaler2 = new Signaler.entity(controller, signal, results);
            controller.postEvent(20, signaler1, Signaler.entity.DO_SIGNAL);
            controller.postEvent(30, signaler2, Signaler.entity.DO_SIGNAL);

            controller.setEndTime(Long.MAX_VALUE);
            controller.eventLoop();

            assertEquals(0, signal.waiterCount(), "Should have 0 waiters after all signaled");
            assertFalse(signal.hasWaiters(), "Should have no waiters");
        }
    }
}
