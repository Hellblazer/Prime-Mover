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

import java.util.Random;

import java.util.Optional;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestWatcher;

import com.hellblazer.primeMover.Simulation;
import com.hellblazer.primeMover.controllers.SimulationController;

/**
 * JUnit 5 extension for simulation testing. Provides:
 * <ul>
 *   <li>Automatic simulation lifecycle management (setup/teardown)</li>
 *   <li>Parameter injection for Simulation, SimulationController, and Random</li>
 *   <li>Support for @Seed annotation for reproducible tests</li>
 *   <li>Automatic seed reporting on test failure</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * @Test
 * @ExtendWith(SimulationExtension.class)
 * void myTest(Simulation simulation, SimulationController controller) {
 *     // simulation and controller are automatically injected and managed
 * }
 * }</pre>
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * @see Simulation
 * @see SimulationController
 */
public class SimulationExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver, TestWatcher {

    private static final Namespace NAMESPACE = Namespace.create(SimulationExtension.class);
    private static final String SIMULATION_KEY = "simulation";
    private static final String SEED_KEY = "seed";

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        var seed = determineSeed(context);
        var builder = Simulation.newBuilder()
                                .withName(generateTestName(context));

        if (seed != null) {
            builder.withSeed(seed);
        }

        var simulation = builder.build();
        getStore(context).put(SIMULATION_KEY, simulation);
        getStore(context).put(SEED_KEY, seed);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        var simulation = getStore(context).remove(SIMULATION_KEY, Simulation.class);
        if (simulation != null) {
            simulation.close();
        }
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        var seed = getSeed(context);
        if (seed != null) {
            var testName = context.getDisplayName();
            System.err.println();
            System.err.println("=== Simulation Test Failure ===");
            System.err.println("Test: " + testName);
            System.err.println("Seed: " + seed);
            System.err.println("To reproduce, add: @Seed(" + seed + "L)");
            System.err.println("===============================");
            System.err.println();
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        var type = parameterContext.getParameter().getType();
        return type == Simulation.class
            || type == SimulationController.class
            || type == Random.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        var simulation = getSimulation(extensionContext);
        var type = parameterContext.getParameter().getType();

        if (type == Simulation.class) {
            return simulation;
        } else if (type == SimulationController.class) {
            return simulation.controller();
        } else if (type == Random.class) {
            return simulation.random();
        }

        throw new ParameterResolutionException("Cannot resolve parameter of type: " + type);
    }

    /**
     * Gets the current simulation from the extension context.
     *
     * @param context the extension context
     * @return the current simulation
     * @throws IllegalStateException if no simulation is available
     */
    protected Simulation getSimulation(ExtensionContext context) {
        var simulation = getStore(context).get(SIMULATION_KEY, Simulation.class);
        if (simulation == null) {
            throw new IllegalStateException("No simulation available. Ensure @ExtendWith(SimulationExtension.class) is present.");
        }
        return simulation;
    }

    /**
     * Gets the seed used for the current test, if any.
     *
     * @param context the extension context
     * @return the seed, or null if no seed was set
     */
    protected Long getSeed(ExtensionContext context) {
        return getStore(context).get(SEED_KEY, Long.class);
    }

    /**
     * Determines the seed to use for this test.
     * Checks for @Seed annotation on the test method first, then class level.
     * If no @Seed annotation is found, generates a random seed for reproducibility tracking.
     *
     * @param context the extension context
     * @return the seed to use (never null for tracking purposes)
     */
    protected Long determineSeed(ExtensionContext context) {
        // Check method-level @Seed first
        var methodSeed = context.getRequiredTestMethod().getAnnotation(Seed.class);
        if (methodSeed != null) {
            return methodSeed.value();
        }

        // Check class-level @Seed
        var classSeed = context.getRequiredTestClass().getAnnotation(Seed.class);
        if (classSeed != null) {
            return classSeed.value();
        }

        // Generate a random seed for tracking purposes
        return System.nanoTime();
    }

    /**
     * Generates a test name from the extension context.
     *
     * @param context the extension context
     * @return a descriptive test name
     */
    private String generateTestName(ExtensionContext context) {
        return context.getDisplayName() + " [" + context.getUniqueId() + "]";
    }

    /**
     * Gets the extension store for storing simulation state.
     *
     * @param context the extension context
     * @return the store
     */
    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(NAMESPACE);
    }
}
