package com.hellblazer.primeMover.desmoj.report;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.*;

public class ReportingTest {
    
    // Simple test reporter
    static class TestReporter implements Reporter {
        private final String name;
        private final Map<String, Object> stats;
        
        TestReporter(String name, Map<String, Object> stats) {
            this.name = name;
            this.stats = stats;
        }
        
        @Override
        public String getName() { return name; }
        
        @Override
        public String getType() { return "Test"; }
        
        @Override
        public Map<String, Object> getStatistics() { return stats; }
    }
    
    // Simple test reportable
    static class TestComponent implements Reportable {
        private final String name;
        private final Map<String, Object> stats;
        
        TestComponent(String name, Map<String, Object> stats) {
            this.name = name;
            this.stats = stats;
        }
        
        @Override
        public String getName() { return name; }
        
        @Override
        public Reporter createReporter() {
            return new TestReporter(name, stats);
        }
    }
    
    @Test
    void testJsonOutput() {
        var stats = new LinkedHashMap<String, Object>();
        stats.put("count", 100);
        stats.put("average", 42.5);
        stats.put("name", "test");
        
        var reporter = new TestReporter("TestComponent", stats);
        var output = new JsonReportOutput();
        var json = output.writeToString(List.of(reporter));
        
        assertTrue(json.contains("\"name\": \"TestComponent\""));
        assertTrue(json.contains("\"type\": \"Test\""));
        assertTrue(json.contains("\"count\": 100"));
        assertTrue(json.contains("\"average\": 42.5"));
        assertTrue(json.contains("\"name\": \"test\""));
    }
    
    @Test
    void testSimulationReport() {
        var stats1 = Map.<String, Object>of("value", 10);
        var stats2 = Map.<String, Object>of("value", 20);
        
        var comp1 = new TestComponent("Component1", stats1);
        var comp2 = new TestComponent("Component2", stats2);
        
        var report = new SimulationReport();
        report.register(comp1);
        report.register(comp2);
        
        var json = report.toJson();
        
        assertTrue(json.contains("Component1"));
        assertTrue(json.contains("Component2"));
        assertTrue(json.contains("\"value\": 10"));
        assertTrue(json.contains("\"value\": 20"));
    }
    
    @Test
    void testStaticGenerate() {
        var components = List.of(
            new TestComponent("A", Map.of("x", 1)),
            new TestComponent("B", Map.of("y", 2))
        );
        
        var json = SimulationReport.generate(components);
        
        assertTrue(json.contains("\"name\": \"A\""));
        assertTrue(json.contains("\"name\": \"B\""));
    }
    
    @Test
    void testCompactJson() {
        var reporter = new TestReporter("Test", Map.of("v", 1));
        var output = new JsonReportOutput(false);  // No pretty print
        var json = output.writeToString(List.of(reporter));
        
        assertFalse(json.contains("\n"));  // No newlines in compact mode
    }
    
    @Test
    void testSpecialCharactersInJson() {
        var stats = Map.<String, Object>of("message", "Hello \"World\"");
        var reporter = new TestReporter("Test", stats);
        var json = new JsonReportOutput().writeToString(List.of(reporter));
        
        assertTrue(json.contains("\\\"World\\\""));  // Escaped quotes
    }
    
    @Test
    void testNullValue() {
        var stats = new HashMap<String, Object>();
        stats.put("nullable", null);
        var reporter = new TestReporter("Test", stats);
        var json = new JsonReportOutput().writeToString(List.of(reporter));
        
        assertTrue(json.contains("\"nullable\": null"));
    }
}
