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

package com.hellblazer.primeMover;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Report containing simulation statistics and timing information from a controller.
 * Provides both human-readable text and JSON output formats.
 * <p>
 * Named ControllerReport to avoid conflicts with desmoj-ish SimulationReport.
 *
 * @param name        the name of the simulation
 * @param startTime   the simulation time at the start
 * @param endTime     the simulation time at the end
 * @param totalEvents the total number of events processed
 * @param spectrum    map of event signatures to invocation counts (immutable)
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public record ControllerReport(
    String name,
    long startTime,
    long endTime,
    int totalEvents,
    Map<String, Integer> spectrum
) {

    /**
     * Creates a ControllerReport with an immutable spectrum map.
     */
    public ControllerReport {
        spectrum = spectrum != null ? Map.copyOf(spectrum) : Map.of();
    }

    /**
     * Returns the simulation duration (end time - start time).
     *
     * @return the duration in simulation time units
     */
    public long duration() {
        return endTime - startTime;
    }

    /**
     * Returns a human-readable text representation of the report.
     *
     * @return formatted text report
     */
    public String toText() {
        var sb = new StringBuilder();
        sb.append("=== Simulation Report: ").append(name).append(" ===\n");
        sb.append("Start Time:   ").append(startTime).append("\n");
        sb.append("End Time:     ").append(endTime).append("\n");
        sb.append("Duration:     ").append(duration()).append("\n");
        sb.append("Total Events: ").append(totalEvents).append("\n");

        if (!spectrum.isEmpty()) {
            sb.append("\nEvent Spectrum:\n");
            spectrum.entrySet()
                    .stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(e -> sb.append("  ")
                                    .append(String.format("%8d", e.getValue()))
                                    .append(" : ")
                                    .append(e.getKey())
                                    .append("\n"));
        }

        return sb.toString();
    }

    /**
     * Returns a JSON representation of the report.
     * Uses simple string formatting to avoid external dependencies.
     *
     * @return JSON string
     */
    public String toJson() {
        var spectrumJson = spectrum.entrySet()
                                   .stream()
                                   .map(e -> String.format("    \"%s\": %d", escapeJson(e.getKey()), e.getValue()))
                                   .collect(Collectors.joining(",\n"));

        return String.format("""
            {
              "name": "%s",
              "startTime": %d,
              "endTime": %d,
              "duration": %d,
              "totalEvents": %d,
              "spectrum": {
            %s
              }
            }""",
            escapeJson(name),
            startTime,
            endTime,
            duration(),
            totalEvents,
            spectrumJson
        );
    }

    private static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
