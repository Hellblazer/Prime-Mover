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

package com.hellblazer.primeMover.testing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hellblazer.primeMover.api.EntityReference;
import com.hellblazer.primeMover.controllers.SteppingController;

/**
 * Tests for SimulationDebugger - a debugging wrapper around SteppingController.
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
class SimulationDebuggerTest {

    private SteppingController controller;
    private SimulationDebugger debugger;
    private TestEntity entity;

    @BeforeEach
    void setUp() {
        controller = new SteppingController();
        controller.setTrackSpectrum(true);
        debugger = SimulationDebugger.wrap(controller);
        entity = new TestEntity();
    }

    // ========== Basic State Tests ==========

    @Test
    void getState_initialState() {
        var state = debugger.getState();

        assertEquals(0, state.eventCount());
        assertTrue(state.currentTime() >= 0);
        assertFalse(state.hasMoreEvents());
    }

    @Test
    void getState_afterPostingEvent() {
        postEventAt(100);

        var state = debugger.getState();

        assertEquals(0, state.eventCount()); // Not yet processed
        assertTrue(state.hasMoreEvents());
    }

    @Test
    void getState_afterStepping() {
        postEventAt(100);
        postEventAt(200);

        debugger.stepOne();

        var state = debugger.getState();
        assertEquals(1, state.eventCount());
        assertEquals(100, state.currentTime());
        assertTrue(state.hasMoreEvents());
    }

    // ========== stepOne Tests ==========

    @Test
    void stepOne_processesOneEvent() {
        postEventAt(50);
        postEventAt(100);

        var processed = debugger.stepOne();

        assertTrue(processed);
        assertEquals(1, debugger.getState().eventCount());
        assertTrue(debugger.getState().hasMoreEvents());
    }

    @Test
    void stepOne_returnsFalseWhenNoEvents() {
        var processed = debugger.stepOne();

        assertFalse(processed);
    }

    @Test
    void stepOne_advancesSimulationTime() {
        postEventAt(150);

        debugger.stepOne();

        assertEquals(150, debugger.getState().currentTime());
    }

    // ========== stepN Tests ==========

    @Test
    void stepN_processesMultipleEvents() {
        postEventAt(10);
        postEventAt(20);
        postEventAt(30);
        postEventAt(40);

        var count = debugger.stepN(3);

        assertEquals(3, count);
        assertEquals(3, debugger.getState().eventCount());
        assertTrue(debugger.getState().hasMoreEvents());
    }

    @Test
    void stepN_stopsWhenNoMoreEvents() {
        postEventAt(10);
        postEventAt(20);

        var count = debugger.stepN(5);

        assertEquals(2, count);
        assertFalse(debugger.getState().hasMoreEvents());
    }

    @Test
    void stepN_zeroProcessesNothing() {
        postEventAt(10);

        var count = debugger.stepN(0);

        assertEquals(0, count);
        assertEquals(0, debugger.getState().eventCount());
    }

    // ========== stepTo Tests ==========

    @Test
    void stepTo_processesEventsUpToTime() {
        postEventAt(50);
        postEventAt(100);
        postEventAt(150);
        postEventAt(200);

        var count = debugger.stepTo(150);

        assertEquals(3, count);
        assertEquals(150, debugger.getState().currentTime());
        assertTrue(debugger.getState().hasMoreEvents());
    }

    @Test
    void stepTo_inclusiveOfTargetTime() {
        postEventAt(100);
        postEventAt(200);

        var count = debugger.stepTo(100);

        assertEquals(1, count);
        assertEquals(100, debugger.getState().currentTime());
    }

    @Test
    void stepTo_stopsIfNoEventsAtOrBeforeTime() {
        postEventAt(200);

        var count = debugger.stepTo(100);

        assertEquals(0, count);
        assertFalse(debugger.getState().currentTime() > 0);
    }

    // ========== runUntil Tests ==========

    @Test
    void runUntil_stopsWhenPredicateTrue() {
        postEventAt(10);
        postEventAt(20);
        postEventAt(30);
        postEventAt(40);
        postEventAt(50);

        var count = debugger.runUntil(state -> state.eventCount() >= 3);

        assertEquals(3, count);
        assertEquals(3, debugger.getState().eventCount());
    }

    @Test
    void runUntil_runsToEndIfPredicateNeverTrue() {
        postEventAt(10);
        postEventAt(20);

        var count = debugger.runUntil(state -> state.eventCount() > 100);

        assertEquals(2, count);
        assertFalse(debugger.getState().hasMoreEvents());
    }

    @Test
    void runUntil_timeBasedPredicate() {
        postEventAt(100);
        postEventAt(200);
        postEventAt(300);

        var count = debugger.runUntil(state -> state.currentTime() >= 200);

        assertEquals(2, count);
        assertEquals(200, debugger.getState().currentTime());
    }

    // ========== getCurrentEventDetails Tests ==========

    @Test
    void getCurrentEventDetails_emptyWhenNoEvents() {
        var details = debugger.getCurrentEventDetails();

        assertTrue(details.isEmpty());
    }

    @Test
    void getCurrentEventDetails_returnsNextEvent() {
        postEventAt(100);

        var details = debugger.getCurrentEventDetails();

        assertTrue(details.isPresent());
        assertEquals(100, details.get().time());
        assertNotNull(details.get().signature());
    }

    @Test
    void getCurrentEventDetails_updatesAfterStep() {
        postEventAt(100);
        postEventAt(200);

        debugger.stepOne();
        var details = debugger.getCurrentEventDetails();

        assertTrue(details.isPresent());
        assertEquals(200, details.get().time());
    }

    // ========== reset Tests ==========

    @Test
    void reset_clearsState() {
        postEventAt(100);
        debugger.stepOne();

        debugger.reset();

        assertEquals(0, debugger.getState().eventCount());
        assertFalse(debugger.getState().hasMoreEvents());
    }

    // ========== Breakpoint Tests (Task 4.2) ==========

    @Test
    void breakAtTime_stopsAtSpecifiedTime() {
        postEventAt(100);
        postEventAt(200);
        postEventAt(300);
        postEventAt(400);

        debugger.breakAtTime(250);
        var count = debugger.runToBreakpoint();

        assertEquals(2, count);
        assertEquals(200, debugger.getState().currentTime());
        assertTrue(debugger.getState().hasMoreEvents());
    }

    @Test
    void breakAtTime_exactMatch() {
        postEventAt(100);
        postEventAt(200);
        postEventAt(300);

        debugger.breakAtTime(200);
        var count = debugger.runToBreakpoint();

        // Stops BEFORE processing event at 200
        assertEquals(1, count);
        assertEquals(100, debugger.getState().currentTime());
    }

    @Test
    void breakOnEvent_stopsOnMatchingSignature() {
        var entity2 = new TestEntity2();
        controller.postEvent(100, entity, 0);
        controller.postEvent(200, entity2, 0);
        controller.postEvent(300, entity, 0);

        debugger.breakOnEvent("TestEntity2.test2");
        var count = debugger.runToBreakpoint();

        assertEquals(1, count);
        assertEquals(100, debugger.getState().currentTime());

        var next = debugger.getCurrentEventDetails();
        assertTrue(next.isPresent());
        assertEquals("TestEntity2.test2", next.get().signature());
    }

    @Test
    void breakOnEvent_partialMatch() {
        var entity2 = new TestEntity2();
        controller.postEvent(100, entity, 0);
        controller.postEvent(200, entity2, 0);

        debugger.breakOnEvent("TestEntity2");
        var count = debugger.runToBreakpoint();

        assertEquals(1, count);
    }

    @Test
    void breakWhen_stopsOnCondition() {
        postEventAt(100);
        postEventAt(200);
        postEventAt(300);
        postEventAt(400);

        debugger.breakWhen(state -> state.eventCount() >= 2);
        var count = debugger.runToBreakpoint();

        assertEquals(2, count);
        assertEquals(2, debugger.getState().eventCount());
    }

    @Test
    void multipleBreakpoints_stopsAtFirst() {
        postEventAt(100);
        postEventAt(200);
        postEventAt(300);
        postEventAt(400);

        debugger.breakAtTime(350);
        debugger.breakWhen(state -> state.eventCount() >= 2);
        var count = debugger.runToBreakpoint();

        // Condition breakpoint (2 events) triggers before time (350)
        assertEquals(2, count);
    }

    @Test
    void clearBreakpoints_removesAll() {
        postEventAt(100);
        postEventAt(200);
        postEventAt(300);

        debugger.breakAtTime(150);
        debugger.clearBreakpoints();
        var count = debugger.runToBreakpoint();

        // No breakpoints, runs to completion
        assertEquals(3, count);
        assertFalse(debugger.getState().hasMoreEvents());
    }

    @Test
    void runToBreakpoint_withNoBreakpoints_runsToEnd() {
        postEventAt(100);
        postEventAt(200);

        var count = debugger.runToBreakpoint();

        assertEquals(2, count);
        assertFalse(debugger.getState().hasMoreEvents());
    }

    @Test
    void continueAfterBreakpoint() {
        postEventAt(100);
        postEventAt(200);
        postEventAt(300);
        postEventAt(400);

        debugger.breakAtTime(250);
        debugger.runToBreakpoint();
        debugger.clearBreakpoints();
        var count = debugger.runToBreakpoint();

        assertEquals(2, count);
        assertEquals(400, debugger.getState().currentTime());
    }

    @Test
    void getActiveBreakpoints_returnsCount() {
        debugger.breakAtTime(100);
        debugger.breakOnEvent("Test");
        debugger.breakWhen(s -> true);

        assertEquals(3, debugger.getActiveBreakpointCount());

        debugger.clearBreakpoints();
        assertEquals(0, debugger.getActiveBreakpointCount());
    }

    // ========== Helper Methods ==========

    /**
     * Second test entity for breakpoint testing.
     */
    private static class TestEntity2 implements EntityReference {
        @Override
        public Object __invoke(int event, Object[] arguments) throws Throwable {
            return null;
        }

        @Override
        public String __signatureFor(int event) {
            return "TestEntity2.test2";
        }
    }

    private void postEventAt(long time) {
        controller.postEvent(time, entity, 0);
    }

    /**
     * Simple test entity for posting events.
     */
    private static class TestEntity implements EntityReference {
        @Override
        public Object __invoke(int event, Object[] arguments) throws Throwable {
            // No-op for testing
            return null;
        }

        @Override
        public String __signatureFor(int event) {
            return "TestEntity.test";
        }
    }
}
