package com.hellblazer.primemover.intellij.error;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents an action that can be taken to recover from an error.
 * Each action has a description for users and optionally executable code.
 */
public class RecoveryAction {
    private final String description;
    private final String technicalNote;
    private final Runnable action;
    private final RecoveryActionType type;

    private RecoveryAction(Builder builder) {
        this.description = Objects.requireNonNull(builder.description, "description cannot be null");
        this.technicalNote = builder.technicalNote;
        this.action = builder.action;
        this.type = builder.type != null ? builder.type : RecoveryActionType.MANUAL;
    }

    public String getDescription() {
        return description;
    }

    public String getTechnicalNote() {
        return technicalNote;
    }

    public RecoveryActionType getType() {
        return type;
    }

    public boolean isExecutable() {
        return action != null && type == RecoveryActionType.AUTOMATIC;
    }

    /**
     * Execute this recovery action.
     * @throws IllegalStateException if action is not executable
     */
    public void execute() {
        if (!isExecutable()) {
            throw new IllegalStateException("Recovery action is not executable");
        }
        action.run();
    }

    public static Builder builder(String description) {
        return new Builder(description);
    }

    public static class Builder {
        private final String description;
        private String technicalNote;
        private Runnable action;
        private RecoveryActionType type;

        private Builder(String description) {
            this.description = description;
        }

        public Builder technicalNote(String technicalNote) {
            this.technicalNote = technicalNote;
            return this;
        }

        public Builder action(Runnable action) {
            this.action = action;
            return this;
        }

        public Builder type(RecoveryActionType type) {
            this.type = type;
            return this;
        }

        public RecoveryAction build() {
            return new RecoveryAction(this);
        }
    }

    public enum RecoveryActionType {
        /**
         * Action requires manual user intervention.
         */
        MANUAL,

        /**
         * Action can be executed automatically by the plugin.
         */
        AUTOMATIC,

        /**
         * Action opens IDE settings or configuration.
         */
        CONFIGURATION,

        /**
         * Action navigates to relevant code or documentation.
         */
        NAVIGATION
    }
}
