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

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.hellblazer.primeMover.api.EntityReference;
import com.hellblazer.primeMover.runtime.Devi.EvaluationResult;

/**
 * Non-vacuity guards for the hang tripwires added after the EventImplEdgeCasesTest
 * CI hang (bead Prime-Mover-d1m). A test that parks the JUnit thread with no resumer
 * used to hang surefire forever; the defense is the default test timeout in
 * junit-platform.properties. These tests fail loudly if that defense silently
 * disappears - a missing or malformed properties file would otherwise skip-pass.
 */
public class TimeoutTripwireTest {

    private static class TestEntityReference implements EntityReference {
        @Override
        public String __signatureFor(int event) {
            return "void testMethod" + event + "()";
        }

        @Override
        public Object __invoke(int event, Object... args) throws Throwable {
            return null;
        }
    }

    /**
     * The default-timeout configuration must exist and parse as a JUnit duration.
     * Guards the typo/deletion class: without this, a broken properties file is
     * indistinguishable from a working one until the next hang burns an hour of CI.
     */
    @Test
    void timeoutDefaultIsConfigured() throws IOException {
        var props = new Properties();
        try (var in = getClass().getResourceAsStream("/junit-platform.properties")) {
            assertNotNull(in, "junit-platform.properties must be on the test classpath");
            props.load(in);
        }
        var value = props.getProperty("junit.jupiter.execution.timeout.default");
        assertNotNull(value, "junit.jupiter.execution.timeout.default must be set");
        assertTrue(value.trim().matches("\\d+\\s*(ns|μs|ms|s|m|h|d)"),
                   "timeout default must be a valid JUnit duration, was: " + value);
    }

    /**
     * park() must actually suspend its caller until resume() - the semantics the
     * tripwire exists to contain. The parked thread is observed still alive with
     * no resumer, then released by resume(); if park() ever stopped suspending,
     * or resume() stopped releasing, this fails within seconds.
     */
    @Test
    void unresumedParkStaysSuspendedUntilResumed() throws Exception {
        var event = new EventImpl(100, null, new TestEntityReference(), 0);
        var future = new CompletableFuture<EvaluationResult>();
        var result = new EvaluationResult(event, null);
        var parker = Thread.ofVirtual().start(() -> {
            try {
                event.park(future, result);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        future.get(5, TimeUnit.SECONDS);
        assertFalse(parker.join(java.time.Duration.ofMillis(200)),
                    "parked thread must stay suspended while un-resumed");

        event.getContinuation().resume();
        assertTrue(parker.join(java.time.Duration.ofSeconds(5)),
                   "resume() must release the parked thread");
    }
}
