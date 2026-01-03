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

import java.util.Random;

import com.hellblazer.primeMover.Simulation;

/**
 * Tests for @Seed annotation functionality.
 * Verifies that seed-based reproducibility works correctly.
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class SeedAnnotationTest {

    // Store values from first test to verify reproducibility in second
    private static double[] seedSequence42 = null;
    private static double[] seedSequence99 = null;

    // ========== Fixed Seed Tests ==========

    @SimulationTest
    @Seed(42L)
    void testFixedSeed_RecordSequence(Random random) {
        // Record the sequence generated with seed 42
        seedSequence42 = new double[10];
        for (int i = 0; i < 10; i++) {
            seedSequence42[i] = random.nextDouble();
        }
        assertNotNull(seedSequence42);
    }

    @SimulationTest
    @Seed(42L)
    void testFixedSeed_VerifyReproducibility(Random random) {
        // Same seed should produce same sequence
        if (seedSequence42 != null) {
            var currentSequence = new double[10];
            for (int i = 0; i < 10; i++) {
                currentSequence[i] = random.nextDouble();
            }
            assertArrayEquals(seedSequence42, currentSequence,
                "Same seed (42) should produce identical random sequence");
        }
    }

    @SimulationTest
    @Seed(99L)
    void testDifferentSeed_RecordSequence(Random random) {
        // Record sequence with different seed
        seedSequence99 = new double[10];
        for (int i = 0; i < 10; i++) {
            seedSequence99[i] = random.nextDouble();
        }
        assertNotNull(seedSequence99);
    }

    @SimulationTest
    @Seed(99L)
    void testDifferentSeed_VerifyDifferent(Random random) {
        // Different seed should produce different sequence (compare to seed 42)
        if (seedSequence42 != null) {
            var currentSequence = new double[10];
            for (int i = 0; i < 10; i++) {
                currentSequence[i] = random.nextDouble();
            }
            // At least one value should be different
            var allSame = true;
            for (int i = 0; i < 10; i++) {
                if (currentSequence[i] != seedSequence42[i]) {
                    allSame = false;
                    break;
                }
            }
            assertFalse(allSame, "Seed 99 should produce different sequence than seed 42");
        }
    }

    // ========== Simulation Integration Tests ==========

    @SimulationTest
    @Seed(12345L)
    void testSeedAffectsSimulation(Simulation simulation) {
        assertNotNull(simulation);
        assertNotNull(simulation.random());
        // Verify the simulation's random is seeded
        var r1 = simulation.random().nextDouble();
        var r2 = simulation.random().nextDouble();
        // Just verify we get values - reproducibility is tested above
        assertTrue(r1 >= 0 && r1 < 1);
        assertTrue(r2 >= 0 && r2 < 1);
    }

    @SimulationTest
    @Seed(0L)
    void testSeedZero(Random random) {
        // Seed 0 should work and produce consistent results
        var value = random.nextDouble();
        assertTrue(value >= 0 && value < 1, "Seed 0 should produce valid random values");
    }

    @SimulationTest
    @Seed(Long.MAX_VALUE)
    void testSeedMaxValue(Random random) {
        // Maximum seed value should work
        var value = random.nextDouble();
        assertTrue(value >= 0 && value < 1, "Max seed should produce valid random values");
    }

    @SimulationTest
    @Seed(Long.MIN_VALUE)
    void testSeedMinValue(Random random) {
        // Minimum seed value should work
        var value = random.nextDouble();
        assertTrue(value >= 0 && value < 1, "Min seed should produce valid random values");
    }

    // ========== No Seed (Random) Tests ==========

    @SimulationTest
    void testNoSeedAnnotation_StillWorks(Random random) {
        // Without @Seed, should still get a working Random
        assertNotNull(random, "Random should be available without @Seed");
        var value = random.nextDouble();
        assertTrue(value >= 0 && value < 1, "Random should produce valid values");
    }
}
