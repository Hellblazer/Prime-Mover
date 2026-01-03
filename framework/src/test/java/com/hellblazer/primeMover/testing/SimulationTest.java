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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Meta-annotation that combines {@code @Test} with {@code @ExtendWith(SimulationExtension.class)}.
 * Methods annotated with {@code @SimulationTest} are automatically registered as JUnit tests
 * and have access to simulation parameter injection.
 *
 * <p>This annotation simplifies simulation testing by eliminating the need to specify
 * both annotations separately:
 *
 * <pre>{@code
 * // Instead of:
 * @Test
 * @ExtendWith(SimulationExtension.class)
 * void myTest(Simulation sim) { ... }
 *
 * // Use:
 * @SimulationTest
 * void myTest(Simulation sim) { ... }
 * }</pre>
 *
 * <p>Injected parameters:
 * <ul>
 *   <li>{@link com.hellblazer.primeMover.Simulation} - the simulation instance</li>
 *   <li>{@link com.hellblazer.primeMover.controllers.SimulationController} - the controller</li>
 *   <li>{@link java.util.Random} - the random number generator</li>
 * </ul>
 *
 * <p>For reproducible tests, combine with {@link Seed}:
 * <pre>{@code
 * @SimulationTest
 * @Seed(42L)
 * void reproducibleTest(Simulation sim, Random random) {
 *     // Same seed = same random sequence
 * }
 * }</pre>
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * @see SimulationExtension
 * @see Seed
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Test
@ExtendWith(SimulationExtension.class)
public @interface SimulationTest {
}
