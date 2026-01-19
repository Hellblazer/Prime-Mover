/**
 * Copyright (C) 2008 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.primeMover.runtime;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.hellblazer.primeMover.annotations.Entity;
import com.hellblazer.primeMover.api.SimulationException;
import com.hellblazer.primeMover.controllers.SimulationController;

/**
 * Tests for exception handling in Devi.eval() method.
 * Verifies that specific exception types are preserved through the event processing stack.
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class DeviExceptionHandlingTest {

    /**
     * Test entity that throws various types of exceptions
     */
    @Entity
    public static class ExceptionThrowingEntity {

        public void throwSimulationEnd() {
            throw new SimulationEnd("Test simulation end");
        }

        public void throwSimulationException() throws SimulationException {
            throw new SimulationException("Test simulation exception");
        }

        public void throwError() {
            throw new OutOfMemoryError("Test error");
        }

        public void throwRuntimeException() {
            throw new IllegalStateException("Test runtime exception");
        }

        public void throwCheckedException() throws Exception {
            throw new Exception("Test checked exception");
        }
    }

    @Test
    void testSimulationEndPropagates() {
        var controller = new SimulationController();
        var entity = new ExceptionThrowingEntity();

        // Schedule event that throws SimulationEnd
        entity.throwSimulationEnd();

        // SimulationEnd should propagate without wrapping
        var exception = assertThrows(SimulationEnd.class, controller::eventLoop);
        assertEquals("Test simulation end", exception.getMessage());
        assertNull(exception.getCause(), "SimulationEnd should not have a cause wrapper");
    }

    @Test
    void testSimulationExceptionPropagates() {
        var controller = new SimulationController();
        var entity = new ExceptionThrowingEntity();

        // Schedule event that throws SimulationException
        entity.throwSimulationException();

        // SimulationException should propagate without extra wrapping
        var exception = assertThrows(SimulationException.class, controller::eventLoop);
        assertEquals("Test simulation exception", exception.getMessage());
        assertNull(exception.getCause(), "SimulationException should not have a cause wrapper");
    }

    @Test
    void testErrorPropagates() {
        var controller = new SimulationController();
        var entity = new ExceptionThrowingEntity();

        // Schedule event that throws Error
        entity.throwError();

        // Error should propagate without wrapping
        var exception = assertThrows(OutOfMemoryError.class, controller::eventLoop);
        assertEquals("Test error", exception.getMessage());
        assertNull(exception.getCause(), "Error should not have a cause wrapper");
    }

    @Test
    void testRuntimeExceptionWrappedInSimulationException() {
        var controller = new SimulationController();
        var entity = new ExceptionThrowingEntity();

        // Schedule event that throws RuntimeException
        entity.throwRuntimeException();

        // RuntimeException should be wrapped in SimulationException
        var exception = assertThrows(SimulationException.class, controller::eventLoop);
        assertTrue(exception.getMessage().contains("Event evaluation failed"),
                   "Should indicate event evaluation failure");
        assertNotNull(exception.getCause(), "Should preserve cause chain");
        assertInstanceOf(IllegalStateException.class, exception.getCause(),
                         "Original exception should be preserved as cause");
        assertEquals("Test runtime exception", exception.getCause().getMessage());
    }

    @Test
    void testCheckedExceptionWrappedInSimulationException() {
        var controller = new SimulationController();
        var entity = new ExceptionThrowingEntity();

        // Schedule event that throws checked Exception
        entity.throwCheckedException();

        // Checked exception should be wrapped in SimulationException
        var exception = assertThrows(SimulationException.class, controller::eventLoop);
        assertTrue(exception.getMessage().contains("Event evaluation failed"),
                   "Should indicate event evaluation failure");
        assertNotNull(exception.getCause(), "Should preserve cause chain");
        assertInstanceOf(Exception.class, exception.getCause(),
                         "Original exception should be preserved as cause");
        assertEquals("Test checked exception", exception.getCause().getMessage());
    }

    @Test
    void testExceptionChainPreserved() {
        var controller = new SimulationController();
        var entity = new ExceptionThrowingEntity();

        // Schedule event that throws RuntimeException
        entity.throwRuntimeException();

        // Verify complete exception chain
        var exception = assertThrows(SimulationException.class, controller::eventLoop);

        // Should have exactly one level of wrapping
        var cause = exception.getCause();
        assertNotNull(cause, "Should have cause");
        assertInstanceOf(IllegalStateException.class, cause, "Cause should be original exception");
        assertNull(cause.getCause(), "Should not have nested wrapping");
    }
}
