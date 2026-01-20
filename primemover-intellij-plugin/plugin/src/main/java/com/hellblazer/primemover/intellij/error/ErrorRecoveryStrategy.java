package com.hellblazer.primemover.intellij.error;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Strategy pattern for defining error recovery approaches.
 * Each strategy knows how to recover from specific error categories.
 */
public interface ErrorRecoveryStrategy {

    /**
     * Check if this strategy can handle the given error.
     */
    boolean canHandle(@NotNull PrimeMoverError error);

    /**
     * Attempt to recover from the error.
     * @return true if recovery succeeded, false otherwise
     */
    boolean attemptRecovery(@NotNull PrimeMoverError error);

    /**
     * Get recovery actions that users can take manually.
     */
    @NotNull
    List<RecoveryAction> getRecoveryActions(@NotNull PrimeMoverError error);

    /**
     * Registry for managing multiple recovery strategies.
     */
    class Registry {
        private final List<ErrorRecoveryStrategy> strategies = new ArrayList<>();

        public void register(@NotNull ErrorRecoveryStrategy strategy) {
            strategies.add(strategy);
        }

        public @NotNull List<RecoveryAction> getRecoveryActions(@NotNull PrimeMoverError error) {
            var actions = new ArrayList<RecoveryAction>();

            for (var strategy : strategies) {
                if (strategy.canHandle(error)) {
                    actions.addAll(strategy.getRecoveryActions(error));
                }
            }

            return actions;
        }

        public boolean attemptAutomaticRecovery(@NotNull PrimeMoverError error) {
            for (var strategy : strategies) {
                if (strategy.canHandle(error)) {
                    try {
                        if (strategy.attemptRecovery(error)) {
                            return true;
                        }
                    } catch (Exception e) {
                        // Strategy failed, try next one
                    }
                }
            }
            return false;
        }
    }
}
