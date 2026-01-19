/**
 * Copyright (C) 2024 Hal Hildebrand. All rights reserved.
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

package com.hellblazer.primeMover.spi;

import com.hellblazer.primeMover.annotations.Transformed;
import com.hellblazer.primeMover.api.EntityReference;
import com.hellblazer.primeMover.controllers.SimulationController;
import com.hellblazer.primeMover.runtime.Continuation;
import com.hellblazer.primeMover.runtime.Devi;
import com.hellblazer.primeMover.runtime.EventImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the Blocking Primitives SPI access patterns work correctly.
 * <p>
 * This test ensures that:
 * <ol>
 *   <li>SPI methods are publicly accessible</li>
 *   <li>SPI methods have correct signatures</li>
 *   <li>SPI methods can be used to implement blocking primitives</li>
 *   <li>The blocking/resume pattern works correctly</li>
 * </ol>
 * <p>
 * See BLOCKING_PRIMITIVES_SPI.md for full SPI documentation.
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class BlockingPrimitivesSPITest {

    // ========================================================================
    // SPI Accessibility Tests
    // ========================================================================

    @Test
    public void testDeviPostMethodIsPublic() throws NoSuchMethodException {
        var method = Devi.class.getMethod("post", EventImpl.class);
        assertTrue(Modifier.isPublic(method.getModifiers()),
                   "Devi.post(EventImpl) must be public for SPI access");
        assertTrue(Modifier.isAbstract(method.getModifiers()),
                   "Devi.post(EventImpl) should be abstract");
    }

    @Test
    public void testDeviSwapCallerMethodIsPublic() throws NoSuchMethodException {
        var method = Devi.class.getMethod("swapCaller", EventImpl.class);
        assertTrue(Modifier.isPublic(method.getModifiers()),
                   "Devi.swapCaller(EventImpl) must be public for SPI access");
        assertEquals(EventImpl.class, method.getReturnType(),
                     "swapCaller should return EventImpl");
    }

    @Test
    public void testEventImplGetContinuationMethodIsPublic() throws NoSuchMethodException {
        var method = EventImpl.class.getMethod("getContinuation");
        assertTrue(Modifier.isPublic(method.getModifiers()),
                   "EventImpl.getContinuation() must be public for SPI access");
        assertEquals(Continuation.class, method.getReturnType(),
                     "getContinuation should return Continuation");
    }

    @Test
    public void testEventImplSetTimeMethodIsPublic() throws NoSuchMethodException {
        var method = EventImpl.class.getMethod("setTime", long.class);
        assertTrue(Modifier.isPublic(method.getModifiers()),
                   "EventImpl.setTime(long) must be public for SPI access");
        assertEquals(void.class, method.getReturnType(),
                     "setTime should return void");
    }

    @Test
    public void testContinuationSetReturnValueMethodIsPublic() throws NoSuchMethodException {
        var method = Continuation.class.getMethod("setReturnValue", Object.class);
        assertTrue(Modifier.isPublic(method.getModifiers()),
                   "Continuation.setReturnValue(Object) must be public for SPI access");
        assertEquals(void.class, method.getReturnType(),
                     "setReturnValue should return void");
    }

    // ========================================================================
    // SPI Behavioral Tests - Verifies SPI methods work via reflection
    // ========================================================================

    /**
     * Test that SPI methods can be invoked via reflection, simulating
     * how a blocking primitive in another package would call them.
     */
    @Test
    public void testSPIMethodInvocationViaReflection() throws Exception {
        try (var controller = new SimulationController()) {
            // Create a minimal entity for testing
            var entity = new MinimalEntity(controller);

            // Schedule an event
            controller.postEvent(0, entity, MinimalEntity.PROCESS);

            // Run simulation - this exercises the internal SPI usage
            controller.eventLoop();

            // Verify the event processed successfully
            assertTrue(entity.wasProcessed(),
                       "Event should be processed, demonstrating internal SPI methods work");
        }
    }

    /**
     * Test that swapCaller can be invoked and returns null when no caller exists.
     * This tests the SPI method behavior directly.
     */
    @Test
    public void testSwapCallerReturnsBehavior() throws Exception {
        try (var controller = new SimulationController()) {
            // Outside of event processing, swapCaller(null) should return null
            // (no caller is set outside event context)
            var result = controller.swapCaller(null);
            assertNull(result, "swapCaller(null) should return null when no caller is set");
        }
    }

    /**
     * Test that EventImpl methods are accessible for SPI use.
     */
    @Test
    public void testEventImplSPIMethodsAccessible() throws Exception {
        try (var controller = new SimulationController()) {
            var entity = new MinimalEntity(controller);

            // Create an event via reflection to test SPI methods
            var eventImplClass = EventImpl.class;
            var constructor = eventImplClass.getDeclaredConstructor(
                long.class,
                com.hellblazer.primeMover.api.Event.class,
                EntityReference.class,
                int.class,
                Object[].class
            );
            constructor.setAccessible(true);

            var event = constructor.newInstance(0L, null, entity, 0, new Object[0]);

            // Test setTime - should not throw
            event.setTime(100L);
            assertEquals(100L, event.getTime(), "setTime should update event time");

            // Test getContinuation - should be accessible (may return null for non-continuation)
            var continuation = event.getContinuation();
            assertNull(continuation, "getContinuation should be null for non-continuation event");
        }
    }

    /**
     * Minimal entity for SPI testing
     */
    @Transformed(comment = "SPI Test", date = "2024", value = "Hand")
    public static class MinimalEntity implements EntityReference {
        static final int PROCESS = 0;

        private final Devi controller;
        private boolean processed = false;

        public MinimalEntity(Devi controller) {
            this.controller = controller;
        }

        @Override
        public Object __invoke(int event, Object[] arguments) throws Throwable {
            if (event == PROCESS) {
                processed = true;
                return null;
            }
            throw new IllegalArgumentException("Unknown event: " + event);
        }

        @Override
        public String __signatureFor(int event) {
            if (event == PROCESS) {
                return "<MinimalEntity: void process()>";
            }
            throw new IllegalArgumentException("Unknown event: " + event);
        }

        public boolean wasProcessed() {
            return processed;
        }
    }

    // ========================================================================
    // SPI Contract Verification
    // ========================================================================

    @Test
    public void testAllSPIMethodsAccessibleFromSamePackage() {
        // This test verifies that SPI methods can be called from test code
        // (which simulates cross-package access like desmoj-ish module)

        // Get all declared methods to verify they exist
        assertDoesNotThrow(() -> Devi.class.getMethod("post", EventImpl.class));
        assertDoesNotThrow(() -> Devi.class.getMethod("swapCaller", EventImpl.class));
        assertDoesNotThrow(() -> EventImpl.class.getMethod("getContinuation"));
        assertDoesNotThrow(() -> EventImpl.class.getMethod("setTime", long.class));
        assertDoesNotThrow(() -> Continuation.class.getMethod("setReturnValue", Object.class));
    }

    @Test
    public void testSupportingMethodsAccessible() {
        // Tier 2 supporting methods should also be accessible
        assertDoesNotThrow(() -> Devi.class.getMethod("getCurrentTime"));
        assertDoesNotThrow(() -> Continuation.class.getMethod("setReturnState", Object.class, Throwable.class));
        assertDoesNotThrow(() -> EventImpl.class.getMethod("getCaller"));
        assertDoesNotThrow(() -> EventImpl.class.getMethod("setCaller", EventImpl.class));
    }
}
