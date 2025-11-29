package com.hellblazer.primeMover.desmoj.report;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Aggregates reportables and generates simulation reports.
 */
public class SimulationReport {
    
    private final List<Reportable> components = new ArrayList<>();
    
    /**
     * Register a component for reporting.
     */
    public void register(Reportable component) {
        components.add(component);
    }
    
    /**
     * Register multiple components.
     */
    public void registerAll(Collection<? extends Reportable> components) {
        this.components.addAll(components);
    }
    
    /**
     * Get all registered components.
     */
    public List<Reportable> getComponents() {
        return List.copyOf(components);
    }
    
    /**
     * Generate report as JSON string.
     */
    public String toJson() {
        return toJson(true);
    }
    
    /**
     * Generate report as JSON string.
     */
    public String toJson(boolean prettyPrint) {
        var reporters = components.stream()
                                  .map(Reportable::createReporter)
                                  .toList();
        return new JsonReportOutput(prettyPrint).writeToString(reporters);
    }
    
    /**
     * Write report to file as JSON.
     */
    public void writeJson(Path path) {
        var reporters = components.stream()
                                  .map(Reportable::createReporter)
                                  .toList();
        new JsonReportOutput().write(reporters, path);
    }
    
    /**
     * Static convenience method.
     */
    public static String generate(Collection<? extends Reportable> components) {
        var report = new SimulationReport();
        report.registerAll(components);
        return report.toJson();
    }
}
