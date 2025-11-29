package com.hellblazer.primeMover.desmoj.report;

import java.util.Map;

/**
 * Interface for generating reports from simulation components.
 */
public interface Reporter {
    /**
     * Get the component name.
     */
    String getName();
    
    /**
     * Get the component type (e.g., "Resource", "Queue", "Distribution").
     */
    String getType();
    
    /**
     * Get the statistics as a map of name -> value.
     * Values should be Number, String, or Boolean.
     */
    Map<String, Object> getStatistics();
}
