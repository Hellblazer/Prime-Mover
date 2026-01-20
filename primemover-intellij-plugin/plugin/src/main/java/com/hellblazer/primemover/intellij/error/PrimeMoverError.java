package com.hellblazer.primemover.intellij.error;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Prime Mover transformation or compilation error.
 * Includes context, severity, category, and recovery information.
 */
public class PrimeMoverError {
    private final ErrorCategory category;
    private final ErrorSeverity severity;
    private final String message;
    private final String technicalDetails;
    private final Throwable cause;
    private final String filePath;
    private final Instant timestamp;
    private final List<RecoveryAction> recoveryActions;

    private PrimeMoverError(Builder builder) {
        this.category = Objects.requireNonNull(builder.category, "category cannot be null");
        this.severity = builder.severity != null ? builder.severity : category.getDefaultSeverity();
        this.message = Objects.requireNonNull(builder.message, "message cannot be null");
        this.technicalDetails = builder.technicalDetails;
        this.cause = builder.cause;
        this.filePath = builder.filePath;
        this.timestamp = Instant.now();
        this.recoveryActions = builder.recoveryActions != null
            ? List.copyOf(builder.recoveryActions)
            : Collections.emptyList();
    }

    public ErrorCategory getCategory() {
        return category;
    }

    public ErrorSeverity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public String getTechnicalDetails() {
        return technicalDetails;
    }

    public Throwable getCause() {
        return cause;
    }

    public String getFilePath() {
        return filePath;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public List<RecoveryAction> getRecoveryActions() {
        return recoveryActions;
    }

    public boolean isRecoverable() {
        return category.isRecoverable() && !recoveryActions.isEmpty();
    }

    /**
     * Get a user-friendly error message suitable for display.
     */
    public String getUserMessage() {
        var sb = new StringBuilder();
        sb.append(severity.getDisplayName()).append(": ").append(message);

        if (filePath != null) {
            sb.append("\nFile: ").append(filePath);
        }

        if (!recoveryActions.isEmpty()) {
            sb.append("\n\nSuggested actions:");
            for (int i = 0; i < recoveryActions.size(); i++) {
                sb.append("\n").append(i + 1).append(". ").append(recoveryActions.get(i).getDescription());
            }
        }

        return sb.toString();
    }

    /**
     * Get full error details for logging and diagnostics.
     */
    public String getFullDetails() {
        var sb = new StringBuilder();
        sb.append("Prime Mover Error\n");
        sb.append("Category: ").append(category.getDisplayName()).append("\n");
        sb.append("Severity: ").append(severity.getDisplayName()).append("\n");
        sb.append("Time: ").append(timestamp).append("\n");
        sb.append("Message: ").append(message).append("\n");

        if (filePath != null) {
            sb.append("File: ").append(filePath).append("\n");
        }

        if (technicalDetails != null) {
            sb.append("\nTechnical Details:\n").append(technicalDetails).append("\n");
        }

        if (cause != null) {
            sb.append("\nException: ").append(cause.getClass().getName())
              .append(": ").append(cause.getMessage()).append("\n");
        }

        if (!recoveryActions.isEmpty()) {
            sb.append("\nRecovery Actions:\n");
            for (int i = 0; i < recoveryActions.size(); i++) {
                var action = recoveryActions.get(i);
                sb.append(i + 1).append(". ").append(action.getDescription());
                if (action.getTechnicalNote() != null) {
                    sb.append(" (").append(action.getTechnicalNote()).append(")");
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    public static Builder builder(ErrorCategory category) {
        return new Builder(category);
    }

    public static class Builder {
        private final ErrorCategory category;
        private ErrorSeverity severity;
        private String message;
        private String technicalDetails;
        private Throwable cause;
        private String filePath;
        private List<RecoveryAction> recoveryActions;

        private Builder(ErrorCategory category) {
            this.category = category;
        }

        public Builder severity(ErrorSeverity severity) {
            this.severity = severity;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder technicalDetails(String technicalDetails) {
            this.technicalDetails = technicalDetails;
            return this;
        }

        public Builder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder recoveryActions(List<RecoveryAction> recoveryActions) {
            this.recoveryActions = recoveryActions;
            return this;
        }

        public PrimeMoverError build() {
            return new PrimeMoverError(this);
        }
    }
}
