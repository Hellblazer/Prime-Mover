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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a fixed seed for reproducible simulation tests.
 * When a test method is annotated with {@code @Seed}, the simulation's
 * random number generator will be initialized with the specified seed,
 * ensuring reproducible random sequences.
 *
 * <p>Example usage:
 * <pre>{@code
 * @SimulationTest
 * @Seed(42L)
 * void reproducibleTest(Random random) {
 *     // random.nextDouble() will always return the same sequence
 *     var value = random.nextDouble();
 *     assertEquals(0.7275636800328681, value, 1e-15);
 * }
 * }</pre>
 *
 * <p>When a test fails, the {@link SimulationExtension} will report the seed
 * used, enabling reproduction of failures even for tests without explicit seeds.
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * @see SimulationTest
 * @see SimulationExtension
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Seed {
    /**
     * The seed value for the random number generator.
     *
     * @return the seed value
     */
    long value();
}
