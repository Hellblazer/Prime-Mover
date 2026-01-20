package com.hellblazer.primemover.intellij.error;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RecoveryAction class.
 */
class RecoveryActionTest {

    @Test
    void testManualRecoveryAction() {
        var action = RecoveryAction.builder("Fix the issue manually")
            .technicalNote("Check line 42")
            .type(RecoveryAction.RecoveryActionType.MANUAL)
            .build();

        assertEquals("Fix the issue manually", action.getDescription());
        assertEquals("Check line 42", action.getTechnicalNote());
        assertEquals(RecoveryAction.RecoveryActionType.MANUAL, action.getType());
        assertFalse(action.isExecutable());
    }

    @Test
    void testAutomaticRecoveryAction() {
        var executed = new AtomicBoolean(false);
        var action = RecoveryAction.builder("Rebuild project")
            .action(() -> executed.set(true))
            .type(RecoveryAction.RecoveryActionType.AUTOMATIC)
            .build();

        assertTrue(action.isExecutable());
        action.execute();
        assertTrue(executed.get());
    }

    @Test
    void testExecuteNonExecutableAction() {
        var action = RecoveryAction.builder("Manual step")
            .type(RecoveryAction.RecoveryActionType.MANUAL)
            .build();

        assertFalse(action.isExecutable());
        assertThrows(IllegalStateException.class, action::execute);
    }

    @Test
    void testConfigurationAction() {
        var action = RecoveryAction.builder("Open settings")
            .type(RecoveryAction.RecoveryActionType.CONFIGURATION)
            .build();

        assertEquals(RecoveryAction.RecoveryActionType.CONFIGURATION, action.getType());
    }

    @Test
    void testNavigationAction() {
        var action = RecoveryAction.builder("Go to documentation")
            .type(RecoveryAction.RecoveryActionType.NAVIGATION)
            .build();

        assertEquals(RecoveryAction.RecoveryActionType.NAVIGATION, action.getType());
    }

    @Test
    void testDefaultType() {
        var action = RecoveryAction.builder("Some action")
            .build();

        assertEquals(RecoveryAction.RecoveryActionType.MANUAL, action.getType());
    }
}
