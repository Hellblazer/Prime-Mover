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

import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.hellblazer.primeMover.api.EntityReference;
import com.hellblazer.primeMover.controllers.SimulationController;

/**
 * Tests for SimulationVisualizer - exports simulation data to various formats.
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
class SimulationVisualizerTest {

    private SimulationController controller;
    private SimulationVisualizer visualizer;
    private TestEntity entity;
    private TestEntity2 entity2;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        controller = new SimulationController();
        controller.setTrackSpectrum(true);
        entity = new TestEntity();
        entity2 = new TestEntity2();
    }

    // ========== JSON Export Tests ==========

    @Test
    void exportToJSON_emptySimulation() {
        visualizer = SimulationVisualizer.from(controller);

        var json = visualizer.exportToJSON();

        assertTrue(json.contains("\"events\""));
        assertTrue(json.contains("\"statistics\""));
    }

    @Test
    void exportToJSON_afterSimulation() throws Exception {
        controller.postEvent(100, entity, 0);
        controller.postEvent(200, entity2, 0);
        controller.setEndTime(300);
        controller.eventLoop();

        visualizer = SimulationVisualizer.from(controller);
        var json = visualizer.exportToJSON();

        assertTrue(json.contains("TestEntity.test"));
        assertTrue(json.contains("TestEntity2.test2"));
        assertTrue(json.contains("\"totalEvents\""));
        assertTrue(json.contains("\"simulationStart\""));
        assertTrue(json.contains("\"simulationEnd\""));
    }

    @Test
    void exportToJSON_toWriter() throws Exception {
        controller.postEvent(100, entity, 0);
        controller.setEndTime(200);
        controller.eventLoop();

        visualizer = SimulationVisualizer.from(controller);
        var writer = new StringWriter();
        visualizer.exportToJSON(writer);

        var json = writer.toString();
        assertTrue(json.contains("TestEntity.test"));
    }

    @Test
    void exportToJSON_toFile(@TempDir Path tempDir) throws Exception {
        controller.postEvent(100, entity, 0);
        controller.setEndTime(200);
        controller.eventLoop();

        visualizer = SimulationVisualizer.from(controller);
        var path = tempDir.resolve("simulation.json");
        visualizer.exportToJSON(path);

        assertTrue(Files.exists(path));
        var content = Files.readString(path);
        assertTrue(content.contains("TestEntity.test"));
    }

    // ========== Mermaid Export Tests ==========

    @Test
    void exportToMermaid_emptySimulation() {
        visualizer = SimulationVisualizer.from(controller);

        var mermaid = visualizer.exportToMermaid();

        assertTrue(mermaid.contains("sequenceDiagram"));
    }

    @Test
    void exportToMermaid_afterSimulation() throws Exception {
        controller.postEvent(100, entity, 0);
        controller.postEvent(200, entity2, 0);
        controller.setEndTime(300);
        controller.eventLoop();

        visualizer = SimulationVisualizer.from(controller);
        var mermaid = visualizer.exportToMermaid();

        assertTrue(mermaid.contains("sequenceDiagram"));
        assertTrue(mermaid.contains("TestEntity"));
        assertTrue(mermaid.contains("TestEntity2"));
    }

    @Test
    void exportToMermaid_toFile(@TempDir Path tempDir) throws Exception {
        controller.postEvent(100, entity, 0);
        controller.setEndTime(200);
        controller.eventLoop();

        visualizer = SimulationVisualizer.from(controller);
        var path = tempDir.resolve("sequence.md");
        visualizer.exportToMermaid(path);

        assertTrue(Files.exists(path));
        var content = Files.readString(path);
        assertTrue(content.contains("sequenceDiagram"));
    }

    // ========== Event Graph Export Tests ==========

    @Test
    void exportEventGraph_emptySimulation() {
        visualizer = SimulationVisualizer.from(controller);

        var graph = visualizer.exportEventGraph();

        assertTrue(graph.contains("graph"));
    }

    @Test
    void exportEventGraph_afterSimulation() throws Exception {
        controller.postEvent(100, entity, 0);
        controller.postEvent(100, entity2, 0);
        controller.setEndTime(200);
        controller.eventLoop();

        visualizer = SimulationVisualizer.from(controller);
        var graph = visualizer.exportEventGraph();

        assertTrue(graph.contains("graph"));
        // Should contain node definitions
        assertTrue(graph.contains("TestEntity") || graph.contains("TestEntity2"));
    }

    @Test
    void exportEventGraph_toFile(@TempDir Path tempDir) throws Exception {
        controller.postEvent(100, entity, 0);
        controller.setEndTime(200);
        controller.eventLoop();

        visualizer = SimulationVisualizer.from(controller);
        var path = tempDir.resolve("graph.md");
        visualizer.exportEventGraph(path);

        assertTrue(Files.exists(path));
        var content = Files.readString(path);
        assertTrue(content.contains("graph"));
    }

    // ========== Summary Export Test ==========

    @Test
    void exportSummary_containsAllSections() throws Exception {
        controller.postEvent(100, entity, 0);
        controller.postEvent(200, entity2, 0);
        controller.setEndTime(300);
        controller.eventLoop();

        visualizer = SimulationVisualizer.from(controller);
        var summary = visualizer.exportSummary();

        assertTrue(summary.contains("Simulation Summary"));
        assertTrue(summary.contains("Event Spectrum"));
    }

    // ========== Helper Classes ==========

    private static class TestEntity implements EntityReference {
        @Override
        public Object __invoke(int event, Object[] arguments) throws Throwable {
            return null;
        }

        @Override
        public String __signatureFor(int event) {
            return "TestEntity.test";
        }
    }

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
}
