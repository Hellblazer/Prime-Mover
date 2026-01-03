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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.hellblazer.primeMover.controllers.StatisticalController;

/**
 * Captures and queries event history from a simulation for debugging and verification.
 * Provides methods to query events by signature, entity, and to export data.
 *
 * <p>Example usage:
 * <pre>{@code
 * @SimulationTest
 * void myTest(SimulationController controller) throws Exception {
 *     controller.eventLoop();
 *
 *     var capture = EventHistoryCapture.from(controller);
 *     assertTrue(capture.hasEvent("MyEntity.process"));
 *     assertEquals(10, capture.getEventCountByEntityPrefix("MyEntity"));
 * }
 * }</pre>
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public final class EventHistoryCapture {

    private final Map<String, Integer> spectrum;

    private EventHistoryCapture(Map<String, Integer> spectrum) {
        this.spectrum = Map.copyOf(spectrum);
    }

    /**
     * Creates an EventHistoryCapture from a StatisticalController.
     *
     * @param controller the controller to capture events from
     * @return a new EventHistoryCapture
     */
    public static EventHistoryCapture from(StatisticalController controller) {
        return new EventHistoryCapture(controller.getSpectrum());
    }

    /**
     * Creates an EventHistoryCapture from an existing spectrum map.
     *
     * @param spectrum the event spectrum map (signature -> count)
     * @return a new EventHistoryCapture
     */
    public static EventHistoryCapture fromSpectrum(Map<String, Integer> spectrum) {
        return new EventHistoryCapture(spectrum);
    }

    /**
     * Creates an empty EventHistoryCapture with no events.
     *
     * @return an empty EventHistoryCapture
     */
    public static EventHistoryCapture empty() {
        return new EventHistoryCapture(Collections.emptyMap());
    }

    /**
     * Returns all event signatures captured.
     *
     * @return set of event signatures
     */
    public Set<String> getEventSignatures() {
        return spectrum.keySet();
    }

    /**
     * Returns the count for a specific event signature.
     *
     * @param signature the event signature (e.g., "MyEntity.process")
     * @return the count, or 0 if the event was not found
     */
    public int getEventCount(String signature) {
        return spectrum.getOrDefault(signature, 0);
    }

    /**
     * Returns the total count of all events.
     *
     * @return total event count
     */
    public int getTotalEventCount() {
        return spectrum.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Checks if an event with the given signature occurred.
     *
     * @param signature the event signature
     * @return true if the event occurred at least once
     */
    public boolean hasEvent(String signature) {
        return spectrum.containsKey(signature);
    }

    /**
     * Returns events matching an entity prefix.
     * For example, "UserService" would match "UserService.create", "UserService.update", etc.
     *
     * @param entityPrefix the entity name prefix
     * @return map of matching event signatures to counts
     */
    public Map<String, Integer> getEventsByEntityPrefix(String entityPrefix) {
        return spectrum.entrySet().stream()
            .filter(e -> e.getKey().startsWith(entityPrefix + "."))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Returns the total event count for events matching an entity prefix.
     *
     * @param entityPrefix the entity name prefix
     * @return total count of matching events
     */
    public int getEventCountByEntityPrefix(String entityPrefix) {
        return spectrum.entrySet().stream()
            .filter(e -> e.getKey().startsWith(entityPrefix + "."))
            .mapToInt(Map.Entry::getValue)
            .sum();
    }

    /**
     * Exports the captured events as a list of records.
     *
     * @return list of event records
     */
    public List<EventRecord> exportToList() {
        return spectrum.entrySet().stream()
            .map(e -> new EventRecord(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }

    /**
     * Exports the captured events as an immutable map.
     *
     * @return immutable map of signature to count
     */
    public Map<String, Integer> exportToMap() {
        return spectrum;
    }

    /**
     * Record representing a single event type's statistics.
     *
     * @param signature the event signature (e.g., "MyEntity.process")
     * @param count the number of times this event occurred
     */
    public record EventRecord(String signature, int count) {}
}
