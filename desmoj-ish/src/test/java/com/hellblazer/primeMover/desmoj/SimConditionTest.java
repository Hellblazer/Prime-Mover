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
 * Test for SimCondition<T> blocking primitive - Wave 1b implementation.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class SimConditionTest {

    /**
     * Test record for complex type testing
     */
    public record Message(String content, int priority, long timestamp) {
    }

    /**
     * Hand-written test entity that waits on a condition and returns the value
     */
    @Entity
    @Transformed(comment = "Hand written", date = "2024", value = "Hand")
    public static class ValueWaiter<T> {
        @Transformed(comment = "Hand written", date = "2024", value = "Hand")
        public static class entity<T> extends ValueWaiter<T> implements EntityReference {
            private static final int WAIT_FOR_VALUE = 0;

            public entity(Devi controller, SimCondition<T> condition, List<String> results) {
                this.controller = controller;
                this.condition = condition;
                this.results = results;
            }

            @Override
            public Object __invoke(int event, Object[] arguments) throws Throwable {
                return switch (event) {
                    case WAIT_FOR_VALUE -> super.waitForValue();
                    default -> throw new IllegalArgumentException("Unknown event: " + event);
                };
            }

            @Override
            public String __signatureFor(int event) {
                return switch (event) {
                    case WAIT_FOR_VALUE -> "<ValueWaiter: Object waitForValue()>";
                    default -> throw new IllegalArgumentException("Unknown event: " + event);
                };
            }

            @Override
            @SuppressWarnings("unchecked")
            public T waitForValue() {
                try {
                    return (T) controller.postContinuingEvent(this, WAIT_FOR_VALUE);
                } catch (Throwable e) {
                    throw new IllegalStateException("Exception in waitForValue", e);
                }
            }
        }

        protected Devi                controller;
        protected SimCondition<T>     condition;
        protected List<String>        results;

        protected ValueWaiter() {
            this.condition = null;
            this.results = null;
        }

        public ValueWaiter(SimCondition<T> condition, List<String> results) {
            this.condition = condition;
            this.results = results;
        }

        @Blocking
        public T waitForValue() {
            results.add("waiter:before:" + controller.getCurrentTime());
            var value = condition.await();
            results.add("waiter:after:" + controller.getCurrentTime() + ":value=" + value);
            return value;
        }
    }

    /**
     * Hand-written test entity that signals with a value
     */
    @Entity
    @Transformed(comment = "Hand written", date = "2024", value = "Hand")
    public static class ValueSignaler<T> {
        @Transformed(comment = "Hand written", date = "2024", value = "Hand")
        public static class entity<T> extends ValueSignaler<T> implements EntityReference {
            private static final int DO_SIGNAL = 0;

            public entity(Devi controller, SimCondition<T> condition, List<String> results, T value) {
                this.controller = controller;
                this.condition = condition;
                this.results = results;
                this.value = value;
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
                    case DO_SIGNAL -> "<ValueSignaler: void doSignal()>";
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

        protected Devi              controller;
        protected SimCondition<T>   condition;
        protected List<String>      results;
        protected T                 value;

        protected ValueSignaler() {
            this.condition = null;
            this.results = null;
            this.value = null;
        }

        public ValueSignaler(SimCondition<T> condition, List<String> results, T value) {
            this.condition = condition;
            this.results = results;
            this.value = value;
        }

        public void doSignal() {
            results.add("signaler:" + controller.getCurrentTime() + ":value=" + value);
            condition.signal(value);
        }
    }

    /**
     * Hand-written test entity that calls signalAll with a value
     */
    @Entity
    @Transformed(comment = "Hand written", date = "2024", value = "Hand")
    public static class SignalAllCaller<T> {
        @Transformed(comment = "Hand written", date = "2024", value = "Hand")
        public static class entity<T> extends SignalAllCaller<T> implements EntityReference {
            private static final int DO_SIGNAL_ALL = 0;

            public entity(Devi controller, SimCondition<T> condition, List<String> results, T value) {
                this.controller = controller;
                this.condition = condition;
                this.results = results;
                this.value = value;
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

        protected Devi              controller;
        protected SimCondition<T>   condition;
        protected List<String>      results;
        protected T                 value;

        protected SignalAllCaller() {
            this.condition = null;
            this.results = null;
            this.value = null;
        }

        public SignalAllCaller(SimCondition<T> condition, List<String> results, T value) {
            this.condition = condition;
            this.results = results;
            this.value = value;
        }

        public void doSignalAll() {
            results.add("signalAll:" + controller.getCurrentTime() + ":value=" + value);
            condition.signalAll(value);
        }
    }

    @Test
    public void testBasicCondition() throws Exception {
        try (var controller = new SimulationController()) {
            var results = new ArrayList<String>();
            var condition = new SimCondition.entity<String>(controller);

            var waiter = new ValueWaiter.entity<String>(controller, condition, results);
            var signaler = new ValueSignaler.entity<>(controller, condition, results, "hello");

            // Schedule waiter at time 0
            controller.postEvent(0, waiter, ValueWaiter.entity.WAIT_FOR_VALUE);

            // Schedule signaler at time 100
            controller.postEvent(100, signaler, ValueSignaler.entity.DO_SIGNAL);

            // Run simulation
            controller.eventLoop();

            // Verify the order, timing, and value
            assertEquals(List.of(
                "waiter:before:0",
                "signaler:100:value=hello",
                "waiter:after:100:value=hello"
            ), results, "Waiter should block at time 0 and resume at time 100 with value 'hello'");
        }
    }

    @Test
    public void testMultipleWaitersWithDifferentValues() throws Exception {
        try (var controller = new SimulationController()) {
            var results = new ArrayList<String>();
            var condition = new SimCondition.entity<Integer>(controller);

            // Create 3 waiters
            var waiter1 = new ValueWaiter.entity<Integer>(controller, condition, results);
            var waiter2 = new ValueWaiter.entity<Integer>(controller, condition, results);
            var waiter3 = new ValueWaiter.entity<Integer>(controller, condition, results);

            // Schedule all waiters at different times
            controller.postEvent(0, waiter1, ValueWaiter.entity.WAIT_FOR_VALUE);
            controller.postEvent(10, waiter2, ValueWaiter.entity.WAIT_FOR_VALUE);
            controller.postEvent(20, waiter3, ValueWaiter.entity.WAIT_FOR_VALUE);

            // Create 3 signalers with different values
            var signaler1 = new ValueSignaler.entity<>(controller, condition, results, 42);
            var signaler2 = new ValueSignaler.entity<>(controller, condition, results, 99);
            var signaler3 = new ValueSignaler.entity<>(controller, condition, results, 777);

            controller.postEvent(100, signaler1, ValueSignaler.entity.DO_SIGNAL);
            controller.postEvent(200, signaler2, ValueSignaler.entity.DO_SIGNAL);
            controller.postEvent(300, signaler3, ValueSignaler.entity.DO_SIGNAL);

            controller.eventLoop();

            // Verify FIFO ordering and that each waiter gets the correct value
            assertEquals(List.of(
                "waiter:before:0",
                "waiter:before:10",
                "waiter:before:20",
                "signaler:100:value=42",
                "waiter:after:100:value=42",   // waiter1 gets 42
                "signaler:200:value=99",
                "waiter:after:200:value=99",   // waiter2 gets 99
                "signaler:300:value=777",
                "waiter:after:300:value=777"   // waiter3 gets 777
            ), results, "Each waiter should receive the correct value in FIFO order");
        }
    }

    @Test
    public void testSignalAllWithSameValue() throws Exception {
        try (var controller = new SimulationController()) {
            var results = new ArrayList<String>();
            var condition = new SimCondition.entity<String>(controller);

            // Create 3 waiters
            var waiter1 = new ValueWaiter.entity<String>(controller, condition, results);
            var waiter2 = new ValueWaiter.entity<String>(controller, condition, results);
            var waiter3 = new ValueWaiter.entity<String>(controller, condition, results);

            controller.postEvent(0, waiter1, ValueWaiter.entity.WAIT_FOR_VALUE);
            controller.postEvent(10, waiter2, ValueWaiter.entity.WAIT_FOR_VALUE);
            controller.postEvent(20, waiter3, ValueWaiter.entity.WAIT_FOR_VALUE);

            // Use signalAll to wake all waiters with the same value
            var signalAllCaller = new SignalAllCaller.entity<>(controller, condition, results, "broadcast");
            controller.postEvent(100, signalAllCaller, SignalAllCaller.entity.DO_SIGNAL_ALL);

            controller.eventLoop();

            // All three waiters should resume at time 100 with the same value
            assertEquals(List.of(
                "waiter:before:0",
                "waiter:before:10",
                "waiter:before:20",
                "signalAll:100:value=broadcast",
                "waiter:after:100:value=broadcast",
                "waiter:after:100:value=broadcast",
                "waiter:after:100:value=broadcast"
            ), results, "All waiters should resume at time 100 with value 'broadcast'");
        }
    }

    @Test
    public void testWithComplexType() throws Exception {
        try (var controller = new SimulationController()) {
            var results = new ArrayList<String>();
            var condition = new SimCondition.entity<Message>(controller);

            var waiter = new ValueWaiter.entity<Message>(controller, condition, results);
            var message = new Message("urgent", 1, 12345L);
            var signaler = new ValueSignaler.entity<>(controller, condition, results, message);

            controller.postEvent(0, waiter, ValueWaiter.entity.WAIT_FOR_VALUE);
            controller.postEvent(50, signaler, ValueSignaler.entity.DO_SIGNAL);

            controller.eventLoop();

            // Verify the record was passed correctly
            assertEquals(List.of(
                "waiter:before:0",
                "signaler:50:value=" + message,
                "waiter:after:50:value=" + message
            ), results, "Complex record type should be passed correctly");

            // The message should be the exact same instance (reference equality)
            assertTrue(results.get(2).contains("Message[content=urgent, priority=1, timestamp=12345]"),
                      "Record should contain expected values");
        }
    }

    @Test
    public void testSignalWithNoWaiters() throws Exception {
        try (var controller = new SimulationController()) {
            var results = new ArrayList<String>();
            var condition = new SimCondition.entity<String>(controller);

            var signaler = new ValueSignaler.entity<>(controller, condition, results, "ignored");

            // Signal with no waiters - should be a no-op
            controller.postEvent(100, signaler, ValueSignaler.entity.DO_SIGNAL);

            controller.eventLoop();

            assertEquals(List.of("signaler:100:value=ignored"), results, 
                        "Signaling with no waiters should be a no-op");
            assertFalse(condition.hasWaiters(), "Should have no waiters");
        }
    }

    @Test
    public void testWaiterCount() throws Exception {
        try (var controller = new SimulationController()) {
            var results = new ArrayList<String>();
            var condition = new SimCondition.entity<Double>(controller);

            assertEquals(0, condition.waiterCount(), "Initially should have 0 waiters");

            var waiter1 = new ValueWaiter.entity<Double>(controller, condition, results);
            var waiter2 = new ValueWaiter.entity<Double>(controller, condition, results);

            controller.postEvent(0, waiter1, ValueWaiter.entity.WAIT_FOR_VALUE);
            controller.postEvent(10, waiter2, ValueWaiter.entity.WAIT_FOR_VALUE);

            // Run until time 15 (both waiters should be waiting)
            controller.setEndTime(15);
            controller.eventLoop();

            assertEquals(2, condition.waiterCount(), "Should have 2 waiters at time 15");
            assertTrue(condition.hasWaiters(), "Should have waiters");

            // Signal both waiters with different values
            var signaler1 = new ValueSignaler.entity<>(controller, condition, results, 3.14);
            var signaler2 = new ValueSignaler.entity<>(controller, condition, results, 2.71);
            controller.postEvent(20, signaler1, ValueSignaler.entity.DO_SIGNAL);
            controller.postEvent(30, signaler2, ValueSignaler.entity.DO_SIGNAL);

            controller.setEndTime(Long.MAX_VALUE);
            controller.eventLoop();

            assertEquals(0, condition.waiterCount(), "Should have 0 waiters after all signaled");
            assertFalse(condition.hasWaiters(), "Should have no waiters");
        }
    }

    @Test
    public void testNullValue() throws Exception {
        try (var controller = new SimulationController()) {
            var results = new ArrayList<String>();
            var condition = new SimCondition.entity<String>(controller);

            var waiter = new ValueWaiter.entity<String>(controller, condition, results);
            var signaler = new ValueSignaler.entity<>(controller, condition, results, null);

            controller.postEvent(0, waiter, ValueWaiter.entity.WAIT_FOR_VALUE);
            controller.postEvent(100, signaler, ValueSignaler.entity.DO_SIGNAL);

            controller.eventLoop();

            // Verify null value can be passed
            assertEquals(List.of(
                "waiter:before:0",
                "signaler:100:value=null",
                "waiter:after:100:value=null"
            ), results, "Null value should be passed correctly");
        }
    }
}
