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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.hellblazer.primeMover.api.EntityReference;
import com.hellblazer.primeMover.runtime.Devi.EvaluationResult;

/**
 * Comprehensive edge case tests for EventImpl.
 * Tests boundary conditions, null handling, time overflow, continuation edge cases,
 * and event source tracking with garbage collection.
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class EventImplEdgeCasesTest {

    /**
     * Simple test implementation of EntityReference
     */
    private static class TestEntityReference implements EntityReference {
        @Override
        public String __signatureFor(int event) {
            return "void testMethod" + event + "()";
        }

        @Override
        public Object __invoke(int event, Object... args) throws Throwable {
            return null;
        }
    }

    /**
     * Test time boundary: Long.MAX_VALUE should not cause overflow in compareTo
     */
    @Test
    void testTimeAtMaxValue() {
        var mockRef = new TestEntityReference();

        var eventMax = new EventImpl(Long.MAX_VALUE, null, mockRef, 0);
        var eventNearMax = new EventImpl(Long.MAX_VALUE - 1, null, mockRef, 0);

        assertTrue(eventNearMax.compareTo(eventMax) < 0,
                   "Event at MAX_VALUE-1 should be before MAX_VALUE");
        assertTrue(eventMax.compareTo(eventNearMax) > 0,
                   "Event at MAX_VALUE should be after MAX_VALUE-1");
        assertEquals(0, eventMax.compareTo(eventMax),
                    "Event should equal itself");
    }

    /**
     * Test time boundaries: zero, negative, and wraparound scenarios
     */
    @ParameterizedTest
    @ValueSource(longs = {0, -1, Long.MIN_VALUE, Long.MAX_VALUE})
    void testTimeBoundaries(long time) {
        var mockRef = new TestEntityReference();

        var event = new EventImpl(time, null, mockRef, 0);

        assertEquals(time, event.getTime(),
                    "Event should preserve time value: " + time);

        // Test clone preserves time boundaries
        var cloned = event.clone(time + 1);
        assertEquals(time + 1, cloned.getTime(),
                    "Cloned event should have new time");
        assertNull(cloned.getContinuation(),
                  "Cloned event should not copy continuation");
    }

    /**
     * Test null source event handling
     */
    @Test
    void testNullSourceEvent() {
        var mockRef = new TestEntityReference();

        var event = new EventImpl(100, null, mockRef, 0);

        assertNull(event.getSource(),
                  "Event with null source should return null");

        // Verify printTrace handles null source gracefully
        var output = new ByteArrayOutputStream();
        event.printTrace(new PrintStream(output));

        var trace = output.toString();
        assertFalse(trace.isEmpty(), "Trace should contain event info");
        assertTrue(trace.contains("100"), "Trace should show time");
    }

    /**
     * Test event source tracking with garbage collection
     * Verifies that weak references allow source events to be GC'd
     */
    @Test
    void testEventSourceWeakReferenceGarbageCollection() {
        var mockRef = new TestEntityReference();

        // Create a chain of events
        var source1 = new EventImpl(100, null, mockRef, 0);
        var source2 = new EventImpl(200, source1, mockRef, 0);
        var event = new EventImpl(300, source2, mockRef, 0);

        // Verify chain initially intact
        assertNotNull(event.getSource(), "Should have source");
        assertEquals(source2, event.getSource());

        // Clear strong references and force GC
        source1 = null;
        source2 = null;
        System.gc();
        System.runFinalization();

        // The weak reference may or may not be cleared depending on GC timing
        // This test just verifies the API handles it gracefully
        var source = event.getSource();
        // source may be null (GC'd) or still present - both are valid
        if (source != null) {
            // If still present, verify it works correctly
            assertEquals(200, source.getTime());
        }
    }

    /**
     * Test printTrace with incomplete event chain due to GC
     */
    @Test
    void testPrintTraceWithPartialChain() {
        var mockRef = new TestEntityReference();

        // Create event chain
        var source = new EventImpl(100, null, mockRef, 0);
        var event = new EventImpl(200, source, mockRef, 0);

        var output = new ByteArrayOutputStream();
        event.printTrace(new PrintStream(output));

        var trace = output.toString();
        assertTrue(trace.contains("200"), "Should show current event time");
        assertTrue(trace.contains("100"), "Should show source event time");
    }

    /**
     * Test continuation edge case: double park
     * Attempting to park twice should replace the continuation
     */
    @Test
    void testDoubleParkReplacesContinuation() throws Throwable {
        var mockRef = new TestEntityReference();
        var event = new EventImpl(100, null, mockRef, 0);

        var future1 = new CompletableFuture<EvaluationResult>();
        var result1 = new EvaluationResult(event, null);

        // First park
        event.park(future1, result1);
        var cont1 = event.getContinuation();
        assertNotNull(cont1, "First park should create continuation");

        // Second park - creates new continuation
        var future2 = new CompletableFuture<EvaluationResult>();
        var result2 = new EvaluationResult(event, null);
        event.park(future2, result2);

        var cont2 = event.getContinuation();
        assertNotNull(cont2, "Second park should create new continuation");
        assertNotSame(cont1, cont2,
                     "Second park should replace first continuation");
    }

    /**
     * Test continuation resume with null result
     */
    @Test
    void testResumeWithNullResult() {
        var mockRef = new TestEntityReference();
        var event = new EventImpl(100, null, mockRef, 0);

        var resumed = event.resume(200, null, null);

        assertSame(event, resumed, "Resume should return same event");
        assertEquals(200, resumed.getTime(), "Time should be updated");
    }

    /**
     * Test continuation resume with exception but no continuation set
     * Should not throw when continuation is null
     */
    @Test
    void testResumeWithExceptionNoContinuation() {
        var mockRef = new TestEntityReference();
        var event = new EventImpl(100, null, mockRef, 0);

        var exception = new RuntimeException("Test exception");

        // Should not throw even though continuation is null
        assertDoesNotThrow(() -> {
            event.resume(200, null, exception);
        }, "Resume should handle null continuation gracefully");
    }

    /**
     * Test event with debug info formatting
     */
    @Test
    void testDebugInfoInToString() {
        var mockRef = new TestEntityReference();
        var eventWithDebug = new EventImpl("TestDebugInfo", 100, null, mockRef, 0);

        var str = eventWithDebug.toString();
        assertTrue(str.contains("TestDebugInfo"),
                  "toString should include debug info");
        assertTrue(str.contains("100"),
                  "toString should include time");
    }

    /**
     * Test event with null arguments
     */
    @Test
    void testEventWithNullArguments() {
        var mockRef = new TestEntityReference();

        // Event with null varargs
        var event1 = new EventImpl(100, null, mockRef, 0, (Object[]) null);
        assertNotNull(event1, "Should create event with null arguments");

        // Event with mixed null arguments
        var event2 = new EventImpl(100, null, mockRef, 0, "test", null, 42);
        assertNotNull(event2, "Should create event with mixed null arguments");
    }

    /**
     * Test caller tracking
     */
    @Test
    void testCallerTracking() {
        var mockRef = new TestEntityReference();

        var caller = new EventImpl(100, null, mockRef, 0);
        var event = new EventImpl(200, caller, mockRef, 0);

        assertNull(event.getCaller(), "Initially no caller set via setCaller");

        event.setCaller(caller);
        assertSame(caller, event.getCaller(), "Should track caller");

        event.setCaller(null);
        assertNull(event.getCaller(), "Should allow clearing caller");
    }

    /**
     * Test setTime edge cases
     */
    @ParameterizedTest
    @ValueSource(longs = {0, -1000, Long.MIN_VALUE, Long.MAX_VALUE})
    void testSetTimeWithBoundaryValues(long newTime) {
        var mockRef = new TestEntityReference();
        var event = new EventImpl(100, null, mockRef, 0);

        event.setTime(newTime);
        assertEquals(newTime, event.getTime(),
                    "setTime should accept boundary value: " + newTime);
    }

    /**
     * Test event comparison with equal times
     */
    @Test
    void testCompareToWithEqualTimes() {
        var mockRef = new TestEntityReference();

        var event1 = new EventImpl(100, null, mockRef, 0);
        var event2 = new EventImpl(100, null, mockRef, 0);

        assertEquals(0, event1.compareTo(event2),
                    "Events with same time should be equal in compareTo");
        assertEquals(0, event2.compareTo(event1),
                    "compareTo should be symmetric");
    }

    /**
     * Test continuation state check
     */
    @Test
    void testIsContinuation() throws Throwable {
        var mockRef = new TestEntityReference();
        var event = new EventImpl(100, null, mockRef, 0);

        assertFalse(event.isContinuation(),
                   "Event without continuation should return false");

        // Create continuation
        var future = new CompletableFuture<EvaluationResult>();
        var result = new EvaluationResult(event, null);
        event.park(future, result);

        assertTrue(event.isContinuation(),
                  "Event with continuation should return true");
    }

    /**
     * Test cloning preserves event ordinal and reference
     */
    @Test
    void testClonePreservesEventDetails() {
        var mockRef = new TestEntityReference();
        var original = new EventImpl(100, null, mockRef, 5, "arg1", 42);

        var cloned = original.clone(200);

        assertEquals(200, cloned.getTime(), "Clone should have new time");
        assertSame(original.getReference(), cloned.getReference(),
                  "Clone should preserve entity reference");
        assertEquals(original.getSignature(), cloned.getSignature(),
                    "Clone should preserve method signature");
        assertNull(cloned.getContinuation(),
                  "Clone should not copy continuation");
    }

    /**
     * Test event toString with continuation marker
     */
    @Test
    void testToStringWithContinuation() throws Throwable {
        var mockRef = new TestEntityReference();
        var event = new EventImpl(100, null, mockRef, 0);

        var beforeCont = event.toString();
        assertFalse(beforeCont.contains(" : c"),
                   "toString should not show continuation marker initially");

        // Add continuation
        var future = new CompletableFuture<EvaluationResult>();
        var result = new EvaluationResult(event, null);
        event.park(future, result);

        var afterCont = event.toString();
        assertTrue(afterCont.contains(" : c"),
                  "toString should show continuation marker");
    }

    /**
     * Test printTrace thread safety
     * PrintStream is already thread-safe, but verify no exceptions
     */
    @Test
    void testPrintTraceConcurrency() throws InterruptedException {
        var mockRef = new TestEntityReference();
        var event = new EventImpl(100, null, mockRef, 0);

        var output = new ByteArrayOutputStream();
        var ps = new PrintStream(output);

        // Multiple threads printing simultaneously
        var thread1 = new Thread(() -> event.printTrace(ps));
        var thread2 = new Thread(() -> event.printTrace(ps));
        var thread3 = new Thread(() -> event.printTrace(ps));

        thread1.start();
        thread2.start();
        thread3.start();

        thread1.join();
        thread2.join();
        thread3.join();

        // Should complete without exceptions
        var result = output.toString();
        assertFalse(result.isEmpty(), "Should produce output");
    }

    /**
     * Test time overflow prevention in compareTo
     * Verifies that (time1 - time2) overflow is avoided
     */
    @Test
    void testCompareToAvoidsOverflow() {
        var mockRef = new TestEntityReference();

        // These times would overflow if we used (time1 - time2)
        var eventMin = new EventImpl(Long.MIN_VALUE, null, mockRef, 0);
        var eventMax = new EventImpl(Long.MAX_VALUE, null, mockRef, 0);

        // Should not throw or produce incorrect results
        assertTrue(eventMin.compareTo(eventMax) < 0,
                  "MIN_VALUE should be before MAX_VALUE");
        assertTrue(eventMax.compareTo(eventMin) > 0,
                  "MAX_VALUE should be after MIN_VALUE");
    }
}
