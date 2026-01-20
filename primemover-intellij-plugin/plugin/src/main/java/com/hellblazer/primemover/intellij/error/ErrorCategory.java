package com.hellblazer.primemover.intellij.error;

/**
 * Categories of errors that can occur during Prime Mover transformations.
 * Each category has different severity levels and recovery strategies.
 */
public enum ErrorCategory {
    /**
     * Errors during annotation scanning phase.
     * Examples: Missing annotations, invalid annotation usage, classpath issues.
     */
    ANNOTATION_SCANNING("Annotation Scanning", ErrorSeverity.MEDIUM, true),

    /**
     * Errors during entity transformation phase.
     * Examples: Invalid bytecode, unsupported language features, transformation failures.
     */
    ENTITY_TRANSFORMATION("Entity Transformation", ErrorSeverity.HIGH, true),

    /**
     * ClassFile API specific errors.
     * Examples: Malformed class files, version incompatibilities, API exceptions.
     */
    CLASSFILE_API("ClassFile API", ErrorSeverity.HIGH, false),

    /**
     * Compilation stage errors.
     * Examples: Generated code doesn't compile, missing dependencies, type errors.
     */
    COMPILATION("Compilation", ErrorSeverity.HIGH, true),

    /**
     * IDE sandbox violations.
     * Examples: File system access denied, network restrictions, permission issues.
     */
    SANDBOX_VIOLATION("Sandbox Violation", ErrorSeverity.MEDIUM, true),

    /**
     * Memory or performance related issues.
     * Examples: Out of memory, timeouts, excessive processing time.
     */
    PERFORMANCE("Performance", ErrorSeverity.MEDIUM, true),

    /**
     * Configuration errors.
     * Examples: Invalid settings, missing configuration, incompatible options.
     */
    CONFIGURATION("Configuration", ErrorSeverity.LOW, true),

    /**
     * Unknown or unexpected errors.
     * Examples: Uncategorized exceptions, internal plugin errors.
     */
    UNKNOWN("Unknown", ErrorSeverity.HIGH, false);

    private final String displayName;
    private final ErrorSeverity defaultSeverity;
    private final boolean recoverable;

    ErrorCategory(String displayName, ErrorSeverity defaultSeverity, boolean recoverable) {
        this.displayName = displayName;
        this.defaultSeverity = defaultSeverity;
        this.recoverable = recoverable;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ErrorSeverity getDefaultSeverity() {
        return defaultSeverity;
    }

    public boolean isRecoverable() {
        return recoverable;
    }
}
