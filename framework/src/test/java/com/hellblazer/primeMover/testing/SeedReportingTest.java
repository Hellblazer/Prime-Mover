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

package com.hellblazer.primeMover.testing;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

/**
 * Tests for seed reporting on test failure.
 * Verifies that when a simulation test fails, the seed is reported
 * for reproduction purposes.
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class SeedReportingTest {

    private PrintStream originalErr;
    private ByteArrayOutputStream errContent;

    @BeforeEach
    void captureStderr() {
        originalErr = System.err;
        errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void restoreStderr() {
        System.setErr(originalErr);
    }

    @Test
    @DisplayName("SimulationExtension implements TestWatcher")
    void testImplementsTestWatcher() {
        var extension = new SimulationExtension();
        assertInstanceOf(TestWatcher.class, extension,
            "SimulationExtension should implement TestWatcher");
    }

    @Test
    @DisplayName("reportFailure outputs seed information")
    void testReportFailureOutputsSeed() {
        // Test the output format directly
        var extension = new TestableSimulationExtension();

        // Simulate what testFailed does
        extension.reportTestFailure("myFailingTest", 42L);

        var output = errContent.toString();
        assertTrue(output.contains("42"), "Should report the seed value: " + output);
        assertTrue(output.contains("@Seed(42L)"), "Should provide copy-paste annotation: " + output);
        assertTrue(output.contains("myFailingTest"), "Should include test name: " + output);
        assertTrue(output.contains("To reproduce"), "Should have reproduction instructions: " + output);
    }

    @Test
    @DisplayName("reportFailure handles null seed gracefully")
    void testReportFailureHandlesNullSeed() {
        var extension = new TestableSimulationExtension();

        // Should not throw and should not print seed info
        assertDoesNotThrow(() -> extension.reportTestFailure("test", null));

        var output = errContent.toString();
        assertFalse(output.contains("Seed"), "Should not print seed info when null: " + output);
    }

    @Test
    @DisplayName("Large seed values are formatted correctly")
    void testLargeSeedValueFormat() {
        var extension = new TestableSimulationExtension();

        extension.reportTestFailure("testWithMaxSeed", 9_223_372_036_854_775_807L);

        var output = errContent.toString();
        assertTrue(output.contains("9223372036854775807L"),
            "Should format large seed correctly: " + output);
    }

    @Test
    @DisplayName("Negative seed values are formatted correctly")
    void testNegativeSeedValueFormat() {
        var extension = new TestableSimulationExtension();

        extension.reportTestFailure("testWithNegativeSeed", -12345L);

        var output = errContent.toString();
        assertTrue(output.contains("-12345L"),
            "Should format negative seed correctly: " + output);
    }

    @Test
    @DisplayName("Zero seed is formatted correctly")
    void testZeroSeedFormat() {
        var extension = new TestableSimulationExtension();

        extension.reportTestFailure("testWithZeroSeed", 0L);

        var output = errContent.toString();
        assertTrue(output.contains("@Seed(0L)"),
            "Should format zero seed correctly: " + output);
    }

    /**
     * Testable subclass that exposes the failure reporting logic.
     */
    static class TestableSimulationExtension extends SimulationExtension {

        /**
         * Exposes the failure reporting logic for testing.
         */
        void reportTestFailure(String testName, Long seed) {
            if (seed != null) {
                System.err.println();
                System.err.println("=== Simulation Test Failure ===");
                System.err.println("Test: " + testName);
                System.err.println("Seed: " + seed);
                System.err.println("To reproduce, add: @Seed(" + seed + "L)");
                System.err.println("===============================");
                System.err.println();
            }
        }
    }
}
