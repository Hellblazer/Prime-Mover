package com.hellblazer.primeMover.desmoj.report;

/**
 * Interface for simulation components that can generate reports.
 */
public interface Reportable {
    /**
     * Get the name of this reportable component.
     */
    String getName();
    
    /**
     * Create a reporter for this component.
     */
    Reporter createReporter();
}
