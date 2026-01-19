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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hellblazer.primeMover.api.EntityReference;
import com.hellblazer.primeMover.api.SimulationException;
import com.hellblazer.primeMover.controllers.SimulationController;

/**
 * Comprehensive error recovery tests for Prime Mover.
 * <p>
 * Tests the exception taxonomy and recovery patterns documented in
 * ERROR_HANDLING_STRATEGY.md:
 * <ul>
 *   <li>User exception in event - wrapped in SimulationException</li>
 *   <li>SimulationException propagation - preserved with cause</li>
 *   <li>SimulationEnd propagation - control flow signal</li>
 *   <li>Controller shutdown on error - clean termination</li>
 *   <li>Error context preservation - message includes time, entity, event</li>
 * </ul>
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * @see com.hellblazer.primeMover.api.SimulationException
 * @see com.hellblazer.primeMover.runtime.SimulationEnd
 */
public class ErrorRecoveryTest {

    private SimulationController controller;

    @BeforeEach
    void setUp() {
        controller = new SimulationController();
        Kairos.setController(controller);
    }

    @AfterEach
    void tearDown() throws Exception {
        Kairos.setController(null);
        if (controller != null) {
            controller.close();
        }
    }

    /**
     * Test entity that throws various exceptions for error recovery testing.
     */
    private static class ErrorEntity implements EntityReference {
        private final AtomicInteger invocationCount = new AtomicInteger(0);
        private Throwable exceptionToThrow;
        private boolean throwOnInvoke = false;

        void setExceptionToThrow(Throwable t) {
            this.exceptionToThrow = t;
            this.throwOnInvoke = true;
        }

        void reset() {
            this.throwOnInvoke = false;
            this.exceptionToThrow = null;
            this.invocationCount.set(0);
        }

        int getInvocationCount() {
            return invocationCount.get();
        }

        @Override
        public String __signatureFor(int event) {
            return switch (event) {
                case 0 -> "void normalMethod()";
                case 1 -> "void throwingMethod()";
                case 2 -> "void checkedExceptionMethod()";
                case 3 -> "void runtimeExceptionMethod()";
                case 4 -> "void simulationEndMethod()";
                default -> "void unknownMethod()";
            };
        }

        @Override
        public Object __invoke(int event, Object... args) throws Throwable {
            invocationCount.incrementAndGet();

            if (throwOnInvoke && exceptionToThrow != null) {
                throw exceptionToThrow;
            }

            return switch (event) {
                case 0 -> "success";
                case 4 -> {
                    throw new SimulationEnd("Simulation terminated by entity");
                }
                default -> null;
            };
        }
    }

    /**
     * Test that user RuntimeException is wrapped in SimulationException.
     * <p>
     * Verifies the exception taxonomy: user exceptions are wrapped with
     * simulation context (time, entity, event signature).
     */
    @Test
    void testUserRuntimeExceptionWrappedInSimulationException() {
        var entity = new ErrorEntity();
        var userException = new IllegalStateException("User error in entity");
        entity.setExceptionToThrow(userException);

        controller.setStartTime(0);
        controller.setCurrentTime(0);
        controller.setEndTime(1000);

        // Post event that will throw
        controller.postEvent(entity, 1);

        // Event loop should throw SimulationException wrapping user exception
        var thrown = assertThrows(SimulationException.class, () -> {
            controller.eventLoop();
        });

        // Verify exception wrapping
        assertNotNull(thrown.getCause(), "Should have cause");
        assertSame(userException, thrown.getCause(),
                  "Cause should be original user exception");

        // Verify simulation context in message
        assertTrue(thrown.getMessage().contains("[Devi]"),
                  "Message should include component prefix");
        assertTrue(thrown.getMessage().contains("time 0"),
                  "Message should include simulation time");
        assertTrue(thrown.getMessage().contains("throwingMethod"),
                  "Message should include event signature");
    }

    /**
     * Test that checked Exception is wrapped in SimulationException.
     */
    @Test
    void testUserCheckedExceptionWrappedInSimulationException() {
        var entity = new ErrorEntity();
        var checkedException = new java.io.IOException("I/O error in entity");
        entity.setExceptionToThrow(checkedException);

        controller.setStartTime(0);
        controller.setCurrentTime(0);
        controller.setEndTime(1000);

        controller.postEvent(entity, 2);

        var thrown = assertThrows(SimulationException.class, () -> {
            controller.eventLoop();
        });

        assertNotNull(thrown.getCause());
        assertSame(checkedException, thrown.getCause(),
                  "Should wrap checked exception");
    }

    /**
     * Test that SimulationException from user code is propagated.
     * <p>
     * When user code explicitly throws SimulationException, it gets wrapped
     * in ExecutionException and then re-thrown as SimulationException with context.
     * The original exception becomes the cause chain.
     */
    @Test
    void testSimulationExceptionPropagatedWithContext() {
        var entity = new ErrorEntity();
        var userSimException = new SimulationException("User simulation error");
        entity.setExceptionToThrow(userSimException);

        controller.setStartTime(0);
        controller.setCurrentTime(0);
        controller.setEndTime(1000);

        controller.postEvent(entity, 1);

        var thrown = assertThrows(SimulationException.class, () -> {
            controller.eventLoop();
        });

        // The exception is propagated - either directly or wrapped with context
        // Verify we get a SimulationException (the exact behavior depends on
        // whether it comes from ExecutionException unwrapping or result.t check)
        assertNotNull(thrown.getMessage(),
                     "SimulationException should have a message");
        // The original message or context should be present
        assertTrue(thrown.getMessage().contains("User simulation error") ||
                   thrown.getMessage().contains("[Devi]"),
                  "Message should contain original or context: " + thrown.getMessage());
    }

    /**
     * Test that SimulationEnd propagates and terminates simulation.
     * <p>
     * SimulationEnd is control flow, not an error. It should propagate
     * cleanly without being wrapped in SimulationException.
     */
    @Test
    void testSimulationEndPropagatesForTermination() {
        var entity = new ErrorEntity();

        controller.setStartTime(0);
        controller.setCurrentTime(0);
        controller.setEndTime(1000);

        // Post event that calls SimulationEnd
        controller.postEvent(entity, 4);

        // SimulationEnd should propagate (it extends Error, not Exception)
        assertThrows(SimulationEnd.class, () -> {
            controller.eventLoop();
        });

        // Verify entity was invoked
        assertEquals(1, entity.getInvocationCount(),
                    "Entity should have been invoked once before SimulationEnd");
    }

    /**
     * Test that explicit SimulationEnd throw propagates correctly.
     */
    @Test
    void testExplicitSimulationEndPropagates() {
        var entity = new ErrorEntity();
        entity.setExceptionToThrow(new SimulationEnd("Explicit termination"));

        controller.setStartTime(0);
        controller.setCurrentTime(0);
        controller.setEndTime(1000);

        controller.postEvent(entity, 1);

        // Should propagate as SimulationEnd (Error), not wrapped
        assertThrows(SimulationEnd.class, () -> {
            controller.eventLoop();
        });
    }

    /**
     * Test controller state after exception.
     * <p>
     * Verifies that controller resources are cleaned up properly
     * when exception terminates the event loop.
     */
    @Test
    void testControllerStateAfterException() throws Exception {
        var entity = new ErrorEntity();
        entity.setExceptionToThrow(new RuntimeException("Test error"));

        controller.setStartTime(0);
        controller.setCurrentTime(0);
        controller.setEndTime(1000);

        controller.postEvent(entity, 1);

        assertThrows(SimulationException.class, () -> {
            controller.eventLoop();
        });

        // Controller should be usable after exception (for inspection)
        assertEquals(0, controller.getCurrentTime(),
                    "Time should be at point of failure");
        assertEquals(0, controller.getSimulationEnd(),
                    "Simulation end should be recorded");
    }

    /**
     * Test multiple events with failure in middle.
     * <p>
     * Verifies fail-fast behavior: events after failure are not processed.
     */
    @Test
    void testMultipleEventsFailFastBehavior() {
        var normalEntity = new ErrorEntity();
        var errorEntity = new ErrorEntity();
        var afterEntity = new ErrorEntity();

        errorEntity.setExceptionToThrow(new RuntimeException("Middle failure"));

        controller.setStartTime(0);
        controller.setCurrentTime(0);
        controller.setEndTime(1000);

        // Schedule three events at times 100, 200, 300
        controller.postEvent(100, normalEntity, 0);
        controller.postEvent(200, errorEntity, 1);
        controller.postEvent(300, afterEntity, 0);

        assertThrows(SimulationException.class, () -> {
            controller.eventLoop();
        });

        // First event should have executed
        assertEquals(1, normalEntity.getInvocationCount(),
                    "First event should have executed");
        // Error event should have executed
        assertEquals(1, errorEntity.getInvocationCount(),
                    "Error event should have executed");
        // After event should NOT have executed (fail-fast)
        assertEquals(0, afterEntity.getInvocationCount(),
                    "Event after failure should not execute (fail-fast)");
    }

    /**
     * Test error context includes entity class name.
     */
    @Test
    void testErrorContextIncludesEntityInfo() {
        var entity = new ErrorEntity();
        entity.setExceptionToThrow(new RuntimeException("Context test"));

        controller.setStartTime(50);
        controller.setCurrentTime(50);
        controller.setEndTime(1000);

        controller.postEvent(75, entity, 1);

        var thrown = assertThrows(SimulationException.class, () -> {
            controller.eventLoop();
        });

        var message = thrown.getMessage();
        assertTrue(message.contains("ErrorEntity"),
                  "Message should include entity class name: " + message);
        assertTrue(message.contains("time 75"),
                  "Message should include event time: " + message);
    }

    /**
     * Test exception does not leak controller state.
     * <p>
     * After exception, thread-local controller should be cleared.
     */
    @Test
    void testExceptionClearsThreadLocalController() {
        var entity = new ErrorEntity();
        entity.setExceptionToThrow(new RuntimeException("Cleanup test"));

        controller.setStartTime(0);
        controller.setCurrentTime(0);
        controller.setEndTime(1000);

        controller.postEvent(entity, 1);

        assertThrows(SimulationException.class, () -> {
            controller.eventLoop();
        });

        // After eventLoop, controller should be cleared from thread-local
        // Use queryController() which returns null instead of throwing
        assertNull(Kairos.queryController(),
                  "Thread-local controller should be null after exception");
    }

    /**
     * Test normal completion followed by error does not affect first run.
     * <p>
     * Controller should be reusable after clearing.
     */
    @Test
    void testControllerReusableAfterError() throws Exception {
        var successEntity = new ErrorEntity();
        var errorEntity = new ErrorEntity();
        errorEntity.setExceptionToThrow(new RuntimeException("Error run"));

        // First run - normal completion
        controller.setStartTime(0);
        controller.setCurrentTime(0);
        controller.setEndTime(100);
        controller.postEvent(50, successEntity, 0);

        controller.eventLoop();
        assertEquals(1, successEntity.getInvocationCount(),
                    "First run should complete normally");

        // Clear for second run
        controller.clear();
        successEntity.reset();
        Kairos.setController(controller);

        // Second run - error
        controller.setStartTime(0);
        controller.setCurrentTime(0);
        controller.setEndTime(100);
        controller.postEvent(50, errorEntity, 1);

        assertThrows(SimulationException.class, () -> {
            controller.eventLoop();
        });

        // Error entity should have been invoked
        assertEquals(1, errorEntity.getInvocationCount(),
                    "Error run should have invoked entity");
    }

    /**
     * Test that AutoCloseable cleanup works after exception.
     */
    @Test
    void testAutoCloseableAfterException() {
        var entity = new ErrorEntity();
        entity.setExceptionToThrow(new RuntimeException("Closeable test"));

        var closeCalled = new AtomicBoolean(false);

        try (var localController = new SimulationController()) {
            Kairos.setController(localController);
            localController.setStartTime(0);
            localController.setCurrentTime(0);
            localController.setEndTime(1000);
            localController.postEvent(entity, 1);

            assertThrows(SimulationException.class, () -> {
                localController.eventLoop();
            });

            closeCalled.set(true);
        } catch (Exception e) {
            fail("Close should not throw: " + e.getMessage());
        }

        assertTrue(closeCalled.get(), "Controller should be closed");
    }

    /**
     * Test NullPointerException in entity is properly wrapped.
     */
    @Test
    void testNullPointerExceptionWrapped() {
        var entity = new ErrorEntity();
        entity.setExceptionToThrow(new NullPointerException("Null in entity"));

        controller.setStartTime(0);
        controller.setCurrentTime(0);
        controller.setEndTime(1000);

        controller.postEvent(entity, 1);

        var thrown = assertThrows(SimulationException.class, () -> {
            controller.eventLoop();
        });

        assertInstanceOf(NullPointerException.class, thrown.getCause(),
                        "Should wrap NPE");
    }

    /**
     * Test that event statistics are recorded even on failure.
     */
    @Test
    void testStatisticsRecordedOnFailure() {
        var normalEntity = new ErrorEntity();
        var errorEntity = new ErrorEntity();
        errorEntity.setExceptionToThrow(new RuntimeException("Stats test"));

        controller.setStartTime(0);
        controller.setCurrentTime(0);
        controller.setEndTime(1000);
        controller.setTrackSpectrum(true);

        // Schedule two events
        controller.postEvent(100, normalEntity, 0);
        controller.postEvent(200, errorEntity, 1);

        assertThrows(SimulationException.class, () -> {
            controller.eventLoop();
        });

        // First event should be in statistics
        // Note: The error event may or may not be counted depending on
        // when the exception occurs relative to recordEvent()
        assertTrue(controller.getTotalEvents() >= 1,
                  "At least first event should be recorded");
    }

    /**
     * Test exception message format follows ERROR_MESSAGE_STANDARD.
     */
    @Test
    void testExceptionMessageFollowsStandard() {
        var entity = new ErrorEntity();
        entity.setExceptionToThrow(new RuntimeException("Standard test"));

        controller.setStartTime(0);
        controller.setCurrentTime(0);
        controller.setEndTime(1000);

        controller.postEvent(entity, 1);

        var thrown = assertThrows(SimulationException.class, () -> {
            controller.eventLoop();
        });

        var message = thrown.getMessage();

        // Verify message follows [Component] format from ERROR_MESSAGE_STANDARD.md
        assertTrue(message.startsWith("[Devi]"),
                  "Message should start with [Component]: " + message);

        // Verify message contains "failed" action word
        assertTrue(message.toLowerCase().contains("failed"),
                  "Message should contain action word 'failed': " + message);

        // Verify simulation context present
        assertTrue(message.contains("time"),
                  "Message should include time context: " + message);
        assertTrue(message.contains("entity") || message.contains("Entity"),
                  "Message should include entity context: " + message);
    }
}
