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

import java.io.IOException;
import java.io.Writer;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.hellblazer.primeMover.controllers.StatisticalController;

/**
 * Exports simulation data to various visualization formats including JSON,
 * Mermaid sequence diagrams, and event graphs.
 *
 * <p>Example usage:
 * <pre>{@code
 * var visualizer = SimulationVisualizer.from(controller);
 *
 * // Export to JSON
 * visualizer.exportToJSON(Path.of("simulation.json"));
 *
 * // Export to Mermaid sequence diagram
 * visualizer.exportToMermaid(Path.of("sequence.md"));
 *
 * // Export event graph
 * visualizer.exportEventGraph(Path.of("graph.md"));
 * }</pre>
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public final class SimulationVisualizer {

    private final StatisticalController controller;

    private SimulationVisualizer(StatisticalController controller) {
        this.controller = controller;
    }

    /**
     * Creates a SimulationVisualizer from a StatisticalController.
     *
     * @param controller the controller to visualize
     * @return a new SimulationVisualizer
     */
    public static SimulationVisualizer from(StatisticalController controller) {
        return new SimulationVisualizer(controller);
    }

    // ========== JSON Export ==========

    /**
     * Exports the simulation data as JSON.
     *
     * @return the JSON string
     */
    public String exportToJSON() {
        var writer = new StringWriter();
        try {
            exportToJSON(writer);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected IO error", e);
        }
        return writer.toString();
    }

    /**
     * Exports the simulation data as JSON to a writer.
     *
     * @param writer the writer to write to
     * @throws IOException if writing fails
     */
    public void exportToJSON(Writer writer) throws IOException {
        var spectrum = controller.getSpectrum();
        var sb = new StringBuilder();

        sb.append("{\n");

        // Statistics section
        sb.append("  \"statistics\": {\n");
        sb.append("    \"simulationStart\": ").append(controller.getSimulationStart()).append(",\n");
        sb.append("    \"simulationEnd\": ").append(controller.getSimulationEnd()).append(",\n");
        sb.append("    \"totalEvents\": ").append(controller.getTotalEvents()).append("\n");
        sb.append("  },\n");

        // Events section
        sb.append("  \"events\": {\n");
        var entries = spectrum.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .toList();
        for (var i = 0; i < entries.size(); i++) {
            var entry = entries.get(i);
            sb.append("    \"").append(escapeJson(entry.getKey())).append("\": ").append(entry.getValue());
            if (i < entries.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("  }\n");

        sb.append("}\n");

        writer.write(sb.toString());
    }

    /**
     * Exports the simulation data as JSON to a file.
     *
     * @param path the file path to write to
     * @throws IOException if writing fails
     */
    public void exportToJSON(Path path) throws IOException {
        try (var writer = Files.newBufferedWriter(path)) {
            exportToJSON(writer);
        }
    }

    // ========== Mermaid Sequence Diagram Export ==========

    /**
     * Exports the simulation as a Mermaid sequence diagram.
     *
     * @return the Mermaid diagram string
     */
    public String exportToMermaid() {
        var writer = new StringWriter();
        try {
            exportToMermaid(writer);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected IO error", e);
        }
        return writer.toString();
    }

    /**
     * Exports the simulation as a Mermaid sequence diagram to a writer.
     *
     * @param writer the writer to write to
     * @throws IOException if writing fails
     */
    public void exportToMermaid(Writer writer) throws IOException {
        var spectrum = controller.getSpectrum();
        var sb = new StringBuilder();

        sb.append("```mermaid\n");
        sb.append("sequenceDiagram\n");

        // Extract unique participants (entities)
        var participants = extractParticipants(spectrum);
        for (var participant : participants) {
            sb.append("    participant ").append(participant).append("\n");
        }

        // Add events as notes
        sb.append("\n");
        for (var entry : spectrum.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList()) {
            var signature = entry.getKey();
            var count = entry.getValue();
            var parts = signature.split("\\.", 2);
            var entity = parts[0];
            var method = parts.length > 1 ? parts[1] : "event";

            sb.append("    Note over ").append(entity).append(": ")
              .append(method).append(" (x").append(count).append(")\n");
        }

        sb.append("```\n");

        writer.write(sb.toString());
    }

    /**
     * Exports the simulation as a Mermaid sequence diagram to a file.
     *
     * @param path the file path to write to
     * @throws IOException if writing fails
     */
    public void exportToMermaid(Path path) throws IOException {
        try (var writer = Files.newBufferedWriter(path)) {
            exportToMermaid(writer);
        }
    }

    // ========== Event Graph Export ==========

    /**
     * Exports the simulation as a Mermaid graph diagram.
     *
     * @return the graph diagram string
     */
    public String exportEventGraph() {
        var writer = new StringWriter();
        try {
            exportEventGraph(writer);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected IO error", e);
        }
        return writer.toString();
    }

    /**
     * Exports the simulation as a Mermaid graph diagram to a writer.
     *
     * @param writer the writer to write to
     * @throws IOException if writing fails
     */
    public void exportEventGraph(Writer writer) throws IOException {
        var spectrum = controller.getSpectrum();
        var sb = new StringBuilder();

        sb.append("```mermaid\n");
        sb.append("graph LR\n");

        // Create nodes for each event type
        var nodeId = 0;
        for (var entry : spectrum.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList()) {
            var signature = entry.getKey();
            var count = entry.getValue();
            var safeName = signature.replaceAll("[^a-zA-Z0-9]", "_");

            sb.append("    ").append(safeName)
              .append("[\"").append(signature).append("<br/>count: ").append(count).append("\"]\n");
            nodeId++;
        }

        // Add style
        sb.append("\n");
        for (var entry : spectrum.entrySet()) {
            var signature = entry.getKey();
            var safeName = signature.replaceAll("[^a-zA-Z0-9]", "_");
            sb.append("    style ").append(safeName).append(" fill:#f9f,stroke:#333,stroke-width:2px\n");
        }

        sb.append("```\n");

        writer.write(sb.toString());
    }

    /**
     * Exports the simulation as a Mermaid graph diagram to a file.
     *
     * @param path the file path to write to
     * @throws IOException if writing fails
     */
    public void exportEventGraph(Path path) throws IOException {
        try (var writer = Files.newBufferedWriter(path)) {
            exportEventGraph(writer);
        }
    }

    // ========== Summary Export ==========

    /**
     * Exports a text summary of the simulation.
     *
     * @return the summary string
     */
    public String exportSummary() {
        var sb = new StringBuilder();

        sb.append("# Simulation Summary\n\n");

        sb.append("## Statistics\n");
        sb.append("- Start Time: ").append(controller.getSimulationStart()).append("\n");
        sb.append("- End Time: ").append(controller.getSimulationEnd()).append("\n");
        sb.append("- Duration: ").append(controller.getSimulationEnd() - controller.getSimulationStart()).append("\n");
        sb.append("- Total Events: ").append(controller.getTotalEvents()).append("\n\n");

        sb.append("## Event Spectrum\n");
        sb.append("| Event | Count |\n");
        sb.append("|-------|-------|\n");
        for (var entry : controller.getSpectrum().entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .toList()) {
            sb.append("| ").append(entry.getKey()).append(" | ").append(entry.getValue()).append(" |\n");
        }

        return sb.toString();
    }

    // ========== Helper Methods ==========

    /**
     * Extracts unique participant names from event signatures.
     */
    private Set<String> extractParticipants(Map<String, Integer> spectrum) {
        return spectrum.keySet().stream()
            .map(sig -> sig.split("\\.")[0])
            .collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Escapes special JSON characters.
     */
    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
