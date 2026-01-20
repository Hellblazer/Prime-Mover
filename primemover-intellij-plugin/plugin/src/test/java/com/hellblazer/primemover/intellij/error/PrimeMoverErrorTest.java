package com.hellblazer.primemover.intellij.error;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PrimeMoverError class.
 */
class PrimeMoverErrorTest {

    @Test
    void testErrorCreation() {
        var error = PrimeMoverError.builder(ErrorCategory.ENTITY_TRANSFORMATION)
            .message("Test error message")
            .filePath("/path/to/Test.java")
            .technicalDetails("Stack trace here")
            .build();

        assertEquals(ErrorCategory.ENTITY_TRANSFORMATION, error.getCategory());
        assertEquals(ErrorSeverity.HIGH, error.getSeverity()); // default for category
        assertEquals("Test error message", error.getMessage());
        assertEquals("/path/to/Test.java", error.getFilePath());
        assertEquals("Stack trace here", error.getTechnicalDetails());
        assertNotNull(error.getTimestamp());
    }

    @Test
    void testErrorWithCustomSeverity() {
        var error = PrimeMoverError.builder(ErrorCategory.ENTITY_TRANSFORMATION)
            .message("Test error")
            .severity(ErrorSeverity.MEDIUM)
            .build();

        assertEquals(ErrorSeverity.MEDIUM, error.getSeverity());
    }

    @Test
    void testErrorWithRecoveryActions() {
        var recoveryActions = List.of(
            RecoveryAction.builder("Fix annotation import").build(),
            RecoveryAction.builder("Rebuild project").build()
        );

        var error = PrimeMoverError.builder(ErrorCategory.ANNOTATION_SCANNING)
            .message("Missing annotation")
            .recoveryActions(recoveryActions)
            .build();

        assertTrue(error.isRecoverable());
        assertEquals(2, error.getRecoveryActions().size());
    }

    @Test
    void testNonRecoverableError() {
        var error = PrimeMoverError.builder(ErrorCategory.CLASSFILE_API)
            .message("Malformed class file")
            .build();

        assertFalse(error.isRecoverable()); // Category is non-recoverable
    }

    @Test
    void testUserMessageGeneration() {
        var error = PrimeMoverError.builder(ErrorCategory.COMPILATION)
            .message("Compilation failed")
            .filePath("/src/Test.java")
            .recoveryActions(List.of(
                RecoveryAction.builder("Check dependencies").build()
            ))
            .build();

        var userMessage = error.getUserMessage();

        assertTrue(userMessage.contains("Error:"));
        assertTrue(userMessage.contains("Compilation failed"));
        assertTrue(userMessage.contains("/src/Test.java"));
        assertTrue(userMessage.contains("Check dependencies"));
    }

    @Test
    void testFullDetailsGeneration() {
        var cause = new RuntimeException("Test exception");
        var error = PrimeMoverError.builder(ErrorCategory.ENTITY_TRANSFORMATION)
            .message("Transformation failed")
            .cause(cause)
            .technicalDetails("Bytecode error at line 42")
            .build();

        var details = error.getFullDetails();

        assertTrue(details.contains("Entity Transformation"));
        assertTrue(details.contains("Transformation failed"));
        assertTrue(details.contains("Bytecode error at line 42"));
        assertTrue(details.contains("RuntimeException"));
    }

    @Test
    void testBuilderValidation() {
        assertThrows(NullPointerException.class, () ->
            PrimeMoverError.builder(null)
                .message("Test")
                .build()
        );

        assertThrows(NullPointerException.class, () ->
            PrimeMoverError.builder(ErrorCategory.COMPILATION)
                .build() // missing message
        );
    }
}
