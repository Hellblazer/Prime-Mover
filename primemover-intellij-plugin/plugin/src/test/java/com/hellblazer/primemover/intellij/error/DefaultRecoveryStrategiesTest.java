package com.hellblazer.primemover.intellij.error;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DefaultRecoveryStrategies.
 */
class DefaultRecoveryStrategiesTest {

    @Test
    void testAnnotationScanningStrategy() {
        var strategy = new DefaultRecoveryStrategies.AnnotationScanningStrategy();
        var error = PrimeMoverError.builder(ErrorCategory.ANNOTATION_SCANNING)
            .message("Missing annotation")
            .build();

        assertTrue(strategy.canHandle(error));
        assertFalse(strategy.attemptRecovery(error)); // Not automatically recoverable

        var actions = strategy.getRecoveryActions(error);
        assertFalse(actions.isEmpty());
        assertTrue(actions.stream()
            .anyMatch(a -> a.getDescription().contains("@Entity")));
    }

    @Test
    void testTransformationStrategy() {
        var strategy = new DefaultRecoveryStrategies.TransformationStrategy();
        var error = PrimeMoverError.builder(ErrorCategory.ENTITY_TRANSFORMATION)
            .message("Transformation failed")
            .build();

        assertTrue(strategy.canHandle(error));
        assertFalse(strategy.attemptRecovery(error));

        var actions = strategy.getRecoveryActions(error);
        assertTrue(actions.stream()
            .anyMatch(a -> a.getDescription().contains("valid entity")));
    }

    @Test
    void testClassFileApiStrategy() {
        var strategy = new DefaultRecoveryStrategies.ClassFileApiStrategy();
        var error = PrimeMoverError.builder(ErrorCategory.CLASSFILE_API)
            .message("ClassFile error")
            .build();

        assertTrue(strategy.canHandle(error));

        var actions = strategy.getRecoveryActions(error);
        assertTrue(actions.stream()
            .anyMatch(a -> a.getDescription().contains("Java 25")));
    }

    @Test
    void testCompilationStrategy() {
        var strategy = new DefaultRecoveryStrategies.CompilationStrategy();
        var error = PrimeMoverError.builder(ErrorCategory.COMPILATION)
            .message("Compilation failed")
            .build();

        assertTrue(strategy.canHandle(error));

        var actions = strategy.getRecoveryActions(error);
        assertTrue(actions.stream()
            .anyMatch(a -> a.getDescription().toLowerCase().contains("rebuild")));
    }

    @Test
    void testSandboxViolationStrategy() {
        var strategy = new DefaultRecoveryStrategies.SandboxViolationStrategy();
        var error = PrimeMoverError.builder(ErrorCategory.SANDBOX_VIOLATION)
            .message("Access denied")
            .build();

        assertTrue(strategy.canHandle(error));

        var actions = strategy.getRecoveryActions(error);
        assertTrue(actions.stream()
            .anyMatch(a -> a.getDescription().contains("permissions")));
    }

    @Test
    void testPerformanceStrategy() {
        var strategy = new DefaultRecoveryStrategies.PerformanceStrategy();
        var error = PrimeMoverError.builder(ErrorCategory.PERFORMANCE)
            .message("Out of memory")
            .build();

        assertTrue(strategy.canHandle(error));

        var actions = strategy.getRecoveryActions(error);
        assertTrue(actions.stream()
            .anyMatch(a -> a.getDescription().contains("memory")));
    }

    @Test
    void testStrategyRegistry() {
        var registry = new ErrorRecoveryStrategy.Registry();
        DefaultRecoveryStrategies.registerAll(registry);

        var error = PrimeMoverError.builder(ErrorCategory.ANNOTATION_SCANNING)
            .message("Test error")
            .build();

        var actions = registry.getRecoveryActions(error);
        assertFalse(actions.isEmpty());
    }

    @Test
    void testRegistryAutomaticRecovery() {
        var registry = new ErrorRecoveryStrategy.Registry();

        // Register a custom strategy that can auto-recover
        registry.register(new ErrorRecoveryStrategy() {
            @Override
            public boolean canHandle(PrimeMoverError error) {
                return error.getCategory() == ErrorCategory.CONFIGURATION;
            }

            @Override
            public boolean attemptRecovery(PrimeMoverError error) {
                return true; // Simulated successful recovery
            }

            @Override
            public java.util.List<RecoveryAction> getRecoveryActions(PrimeMoverError error) {
                return java.util.List.of();
            }
        });

        var error = PrimeMoverError.builder(ErrorCategory.CONFIGURATION)
            .message("Config error")
            .build();

        assertTrue(registry.attemptAutomaticRecovery(error));
    }
}
