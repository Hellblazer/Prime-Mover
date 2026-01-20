package com.hellblazer.primemover.intellij.error;

/**
 * Severity levels for Prime Mover errors.
 * Determines notification style and recovery strategies.
 */
public enum ErrorSeverity {
    /**
     * Low severity - informational errors that don't block work.
     * Examples: Configuration warnings, optional feature failures.
     */
    LOW("Information", false),

    /**
     * Medium severity - errors that may impact functionality but have workarounds.
     * Examples: Non-critical transformation failures, performance degradation.
     */
    MEDIUM("Warning", true),

    /**
     * High severity - critical errors that prevent core functionality.
     * Examples: Build failures, transformation errors preventing compilation.
     */
    HIGH("Error", true);

    private final String displayName;
    private final boolean requiresNotification;

    ErrorSeverity(String displayName, boolean requiresNotification) {
        this.displayName = displayName;
        this.requiresNotification = requiresNotification;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean requiresNotification() {
        return requiresNotification;
    }
}
