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

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.hellblazer.primeMover.controllers.SimulationController;

/**
 * Tests for EventHistoryCapture - captures and queries event history for debugging.
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class EventHistoryCaptureTest {

    @Nested
    @DisplayName("Construction tests")
    class ConstructionTests {

        @Test
        @DisplayName("creates from controller")
        void createsFromController() {
            var controller = new SimulationController();
            var capture = EventHistoryCapture.from(controller);
            assertNotNull(capture, "Should create capture from controller");
        }

        @Test
        @DisplayName("creates from spectrum map")
        void createsFromSpectrum() {
            var spectrum = Map.of("Entity.method", 5, "Other.call", 3);
            var capture = EventHistoryCapture.fromSpectrum(spectrum);
            assertNotNull(capture, "Should create capture from spectrum");
        }
    }

    @Nested
    @DisplayName("Event query tests")
    class EventQueryTests {

        @Test
        @DisplayName("getEventSignatures returns all signatures")
        void getEventSignaturesReturnsAll() {
            var spectrum = Map.of("A.one", 1, "B.two", 2, "C.three", 3);
            var capture = EventHistoryCapture.fromSpectrum(spectrum);

            var signatures = capture.getEventSignatures();

            assertEquals(3, signatures.size());
            assertTrue(signatures.contains("A.one"));
            assertTrue(signatures.contains("B.two"));
            assertTrue(signatures.contains("C.three"));
        }

        @Test
        @DisplayName("getEventCount returns count for specific signature")
        void getEventCountForSignature() {
            var spectrum = Map.of("Entity.method", 42);
            var capture = EventHistoryCapture.fromSpectrum(spectrum);

            assertEquals(42, capture.getEventCount("Entity.method"));
            assertEquals(0, capture.getEventCount("Unknown.method"));
        }

        @Test
        @DisplayName("getTotalEventCount returns sum of all events")
        void getTotalEventCount() {
            var spectrum = Map.of("A.one", 10, "B.two", 20, "C.three", 30);
            var capture = EventHistoryCapture.fromSpectrum(spectrum);

            assertEquals(60, capture.getTotalEventCount());
        }

        @Test
        @DisplayName("hasEvent returns true for existing event")
        void hasEventReturnsTrue() {
            var spectrum = Map.of("Entity.method", 1);
            var capture = EventHistoryCapture.fromSpectrum(spectrum);

            assertTrue(capture.hasEvent("Entity.method"));
            assertFalse(capture.hasEvent("Unknown.method"));
        }
    }

    @Nested
    @DisplayName("Entity filtering tests")
    class EntityFilteringTests {

        @Test
        @DisplayName("getEventsByEntityPrefix filters by entity name")
        void filtersByEntityPrefix() {
            var spectrum = Map.of(
                "UserService.create", 5,
                "UserService.update", 3,
                "OrderService.process", 10
            );
            var capture = EventHistoryCapture.fromSpectrum(spectrum);

            var userEvents = capture.getEventsByEntityPrefix("UserService");

            assertEquals(2, userEvents.size());
            assertTrue(userEvents.containsKey("UserService.create"));
            assertTrue(userEvents.containsKey("UserService.update"));
            assertFalse(userEvents.containsKey("OrderService.process"));
        }

        @Test
        @DisplayName("getEventCountByEntityPrefix sums events for entity")
        void sumsEventsByEntity() {
            var spectrum = Map.of(
                "UserService.create", 5,
                "UserService.update", 3,
                "OrderService.process", 10
            );
            var capture = EventHistoryCapture.fromSpectrum(spectrum);

            assertEquals(8, capture.getEventCountByEntityPrefix("UserService"));
            assertEquals(10, capture.getEventCountByEntityPrefix("OrderService"));
            assertEquals(0, capture.getEventCountByEntityPrefix("Unknown"));
        }
    }

    @Nested
    @DisplayName("Export tests")
    class ExportTests {

        @Test
        @DisplayName("exportToList returns list of event records")
        void exportToList() {
            var spectrum = Map.of("A.one", 2, "B.two", 1);
            var capture = EventHistoryCapture.fromSpectrum(spectrum);

            var list = capture.exportToList();

            assertEquals(2, list.size());
            assertTrue(list.stream().anyMatch(r ->
                r.signature().equals("A.one") && r.count() == 2));
            assertTrue(list.stream().anyMatch(r ->
                r.signature().equals("B.two") && r.count() == 1));
        }

        @Test
        @DisplayName("exportToMap returns the spectrum map")
        void exportToMap() {
            var spectrum = Map.of("A.one", 1, "B.two", 2);
            var capture = EventHistoryCapture.fromSpectrum(spectrum);

            var map = capture.exportToMap();

            assertEquals(spectrum, map);
        }
    }

    @Nested
    @DisplayName("Integration tests")
    class IntegrationTests {

        @SimulationTest
        @DisplayName("captures events from simulation controller")
        void capturesFromController(SimulationController controller) throws Exception {
            controller.eventLoop();

            var capture = EventHistoryCapture.from(controller);
            assertNotNull(capture);
            // Empty simulation should have no events
            assertEquals(0, capture.getTotalEventCount());
        }
    }

    @Nested
    @DisplayName("Empty capture tests")
    class EmptyCaptureTests {

        @Test
        @DisplayName("empty capture has no events")
        void emptyHasNoEvents() {
            var capture = EventHistoryCapture.empty();

            assertTrue(capture.getEventSignatures().isEmpty());
            assertEquals(0, capture.getTotalEventCount());
            assertFalse(capture.hasEvent("anything"));
        }
    }
}
