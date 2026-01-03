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

package com.hellblazer.primeMover;

import com.hellblazer.primeMover.api.SimulationException;
import com.hellblazer.primeMover.controllers.SimulationController;
import com.hellblazer.primeMover.runtime.Kairos;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * A simulation instance with fluent configuration via the Builder pattern.
 * Provides a convenient API for configuring and running discrete event simulations.
 * <p>
 * Example usage:
 * <pre>{@code
 * try (var simulation = Simulation.newBuilder()
 *         .withName("My Simulation")
 *         .withSeed(12345L)
 *         .withMaxTime(10000L)
 *         .withStatistics(true)
 *         .build()) {
 *     // Use simulation.controller() to schedule events
 *     // Use simulation.random() for reproducible randomness
 * }
 * }</pre>
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class Simulation implements AutoCloseable {

    private static final String DEFAULT_NAME = "Prime Mover Simulation";

    private final SimulationController controller;
    private final Random               random;
    private final String               name;
    private final long                 maxTime;
    private final boolean              statisticsEnabled;

    private Simulation(Builder builder) {
        this.name = builder.name != null ? builder.name : DEFAULT_NAME;
        this.maxTime = builder.maxTime;
        this.statisticsEnabled = builder.statisticsEnabled;
        this.random = builder.seed != null ? new Random(builder.seed) : new Random();

        this.controller = new SimulationController();
        this.controller.setName(this.name);
        this.controller.setEndTime(this.maxTime);
        this.controller.setTrackSpectrum(this.statisticsEnabled);
    }

    /**
     * Creates a new Builder instance for configuring a Simulation.
     *
     * @return a new Builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Runs a simulation with default settings.
     * The runnable is executed to set up initial events, then the event loop runs
     * until completion. Resources are automatically cleaned up.
     *
     * @param initializer the runnable to set up initial simulation state
     * @return the simulation result containing statistics
     * @throws SimulationException if an error occurs during simulation
     * @throws NullPointerException if initializer is null
     */
    public static SimulationResult run(Runnable initializer) throws SimulationException {
        return newBuilder().run(initializer);
    }

    /**
     * Runs a simulation with a maximum time limit.
     * The runnable is executed to set up initial events, then the event loop runs
     * until the maxTime is reached or no more events remain.
     *
     * @param initializer the runnable to set up initial simulation state
     * @param maxTime the maximum simulation time
     * @return the simulation result containing statistics
     * @throws SimulationException if an error occurs during simulation
     * @throws NullPointerException if initializer is null
     * @throws IllegalArgumentException if maxTime is negative
     */
    public static SimulationResult run(Runnable initializer, long maxTime) throws SimulationException {
        return newBuilder().withMaxTime(maxTime).run(initializer);
    }

    /**
     * Returns the underlying simulation controller.
     *
     * @return the SimulationController
     */
    public SimulationController controller() {
        return controller;
    }

    /**
     * Returns the Random instance for this simulation.
     * If a seed was provided via the builder, this Random will produce
     * a reproducible sequence of values.
     *
     * @return the Random instance
     */
    public Random random() {
        return random;
    }

    /**
     * Returns the name of this simulation.
     *
     * @return the simulation name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the maximum simulation time.
     *
     * @return the max time
     */
    public long maxTime() {
        return maxTime;
    }

    /**
     * Returns whether statistics collection is enabled.
     *
     * @return true if statistics are enabled
     */
    public boolean statisticsEnabled() {
        return statisticsEnabled;
    }

    @Override
    public void close() throws Exception {
        controller.close();
    }

    /**
     * Builder for creating Simulation instances with fluent configuration.
     */
    public static class Builder {
        private String  name              = null;
        private Long    seed              = null;
        private long    maxTime           = Long.MAX_VALUE;
        private boolean statisticsEnabled = true;

        private Builder() {
        }

        /**
         * Sets the name of the simulation.
         *
         * @param name the simulation name, or null to use the default
         * @return this builder for chaining
         */
        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the random seed for reproducible simulations.
         *
         * @param seed the random seed
         * @return this builder for chaining
         */
        public Builder withSeed(long seed) {
            this.seed = seed;
            return this;
        }

        /**
         * Sets the maximum simulation time. The simulation will stop
         * when this time is reached.
         *
         * @param maxTime the maximum time, must be non-negative
         * @return this builder for chaining
         * @throws IllegalArgumentException if maxTime is negative
         */
        public Builder withMaxTime(long maxTime) {
            if (maxTime < 0) {
                throw new IllegalArgumentException("maxTime must be non-negative: " + maxTime);
            }
            this.maxTime = maxTime;
            return this;
        }

        /**
         * Enables or disables statistics collection (event spectrum tracking).
         *
         * @param enabled true to enable statistics, false to disable
         * @return this builder for chaining
         */
        public Builder withStatistics(boolean enabled) {
            this.statisticsEnabled = enabled;
            return this;
        }

        /**
         * Builds a new Simulation instance with the configured settings.
         *
         * @return a new Simulation
         */
        public Simulation build() {
            return new Simulation(this);
        }

        /**
         * Builds and runs a simulation with the configured settings.
         * The initializer runnable is executed to set up initial events,
         * then the event loop runs until completion.
         *
         * @param initializer the runnable to set up initial simulation state
         * @return the simulation result containing statistics
         * @throws SimulationException if an error occurs during simulation
         * @throws NullPointerException if initializer is null
         */
        public SimulationResult run(Runnable initializer) throws SimulationException {
            Objects.requireNonNull(initializer, "initializer must not be null");

            try (var simulation = build()) {
                var controller = simulation.controller();
                Kairos.setController(controller);
                try {
                    initializer.run();
                    controller.eventLoop();
                } finally {
                    Kairos.setController(null);
                }

                return new SimulationResult(
                    controller.getSimulationStart(),
                    controller.getSimulationEnd(),
                    controller.getTotalEvents(),
                    Map.copyOf(controller.getSpectrum())
                );
            } catch (SimulationException e) {
                throw e;
            } catch (Exception e) {
                throw new SimulationException("Error during simulation", e);
            }
        }
    }

    /**
     * Result of a simulation run containing statistics and timing information.
     *
     * @param simulationStart the simulation time at the start
     * @param simulationEnd the simulation time at the end
     * @param totalEvents the total number of events processed
     * @param spectrum map of event signatures to invocation counts
     */
    public record SimulationResult(
        long simulationStart,
        long simulationEnd,
        int totalEvents,
        Map<String, Integer> spectrum
    ) {
        /**
         * Returns the simulation duration (end time - start time).
         *
         * @return the duration in simulation time units
         */
        public long duration() {
            return simulationEnd - simulationStart;
        }
    }
}
