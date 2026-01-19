/**
 * Copyright (C) 2008 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.primeMover.builders;

import com.hellblazer.primeMover.controllers.RealTimeController;
import com.hellblazer.primeMover.controllers.SimulationController;
import com.hellblazer.primeMover.controllers.SteppingController;
import com.hellblazer.primeMover.runtime.Devi;
import com.hellblazer.primeMover.runtime.Kairos;

import java.lang.reflect.Constructor;

/**
 * Fluent builder for creating and configuring simulation controllers.
 * Reduces boilerplate setup code by providing sensible defaults and
 * a clean API for customization.
 * <p>
 * Example usage:
 * <pre>{@code
 * var controller = SimulationBuilder.builder()
 *     .endTime(1000)
 *     .trackSpectrum(true)
 *     .debugEvents(false)
 *     .build();
 * }</pre>
 * <p>
 * The builder automatically:
 * <ul>
 *   <li>Creates the controller instance</li>
 *   <li>Sets it as the thread-local controller via {@code Kronos.setController()}</li>
 *   <li>Initializes the current time to 0 (or specified start time)</li>
 *   <li>Applies all configuration options</li>
 * </ul>
 * <p>
 * Default values:
 * <ul>
 *   <li>Controller type: {@link SimulationController}</li>
 *   <li>End time: {@code Long.MAX_VALUE} (unlimited)</li>
 *   <li>Track spectrum: {@code true} (useful for demos and analysis)</li>
 *   <li>Track event sources: {@code false} (GC overhead)</li>
 *   <li>Debug events: {@code false} (expensive)</li>
 *   <li>Start time: {@code 0}</li>
 * </ul>
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class SimulationBuilder {
    private long                      endTime           = Long.MAX_VALUE;
    private long                      startTime         = 0;
    private boolean                   trackSpectrum     = true; // Enable by default for demos
    private boolean                   trackEventSources = false; // Expensive, off by default
    private boolean                   debugEvents       = false; // Very expensive, off by default
    private Class<? extends Devi>     controllerType    = SimulationController.class;
    private String                    name              = null; // Will use controller default

    /**
     * Private constructor - use {@link #builder()} to create instances.
     */
    private SimulationBuilder() {
    }

    /**
     * Create a new SimulationBuilder instance.
     *
     * @return a new builder
     */
    public static SimulationBuilder builder() {
        return new SimulationBuilder();
    }

    /**
     * Set the simulation end time. The simulation will stop when the
     * current time reaches this value.
     *
     * @param time the end time (default: {@code Long.MAX_VALUE})
     * @return this builder for fluent chaining
     */
    public SimulationBuilder endTime(long time) {
        this.endTime = time;
        return this;
    }

    /**
     * Set the simulation start time. This is the initial value of the
     * simulation clock.
     *
     * @param time the start time (default: 0)
     * @return this builder for fluent chaining
     */
    public SimulationBuilder startTime(long time) {
        this.startTime = time;
        return this;
    }

    /**
     * Configure whether to track event spectrum (counts per event type).
     * Tracking spectrum is useful for profiling but has some overhead.
     *
     * @param track true to track spectrum (default: true)
     * @return this builder for fluent chaining
     */
    public SimulationBuilder trackSpectrum(boolean track) {
        this.trackSpectrum = track;
        return this;
    }

    /**
     * Configure whether to track event source chains (which event caused which).
     * Tracking event sources has garbage collection implications as event chains
     * prevent the elimination of previous events.
     *
     * @param track true to track event sources (default: false)
     * @return this builder for fluent chaining
     */
    public SimulationBuilder trackEventSources(boolean track) {
        this.trackEventSources = track;
        return this;
    }

    /**
     * Configure whether to collect debug information (stack traces) for events.
     * Debug event tracking is very expensive and significantly impacts performance.
     *
     * @param debug true to enable debug events (default: false)
     * @return this builder for fluent chaining
     */
    public SimulationBuilder debugEvents(boolean debug) {
        this.debugEvents = debug;
        return this;
    }

    /**
     * Set the controller type to create. Supported types:
     * <ul>
     *   <li>{@link SimulationController} - Standard discrete event simulation</li>
     *   <li>{@link RealTimeController} - Real-time paced simulation</li>
     *   <li>{@link SteppingController} - Step-through simulation for debugging</li>
     * </ul>
     *
     * @param type the controller class (default: {@link SimulationController})
     * @return this builder for fluent chaining
     */
    public SimulationBuilder controllerType(Class<? extends Devi> type) {
        this.controllerType = type;
        return this;
    }

    /**
     * Set the simulation name. Only applies to controller types that support naming
     * (e.g., {@link SimulationController}, {@link RealTimeController}).
     *
     * @param name the simulation name
     * @return this builder for fluent chaining
     */
    public SimulationBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Build and configure the controller. This method:
     * <ol>
     *   <li>Creates the controller instance</li>
     *   <li>Applies all configuration options</li>
     *   <li>Sets the controller as the thread-local via {@code Kronos.setController()}</li>
     *   <li>Initializes the current time</li>
     * </ol>
     *
     * @return the configured controller
     * @throws IllegalArgumentException if the controller type cannot be instantiated
     */
    public Devi build() {
        var controller = createController();

        // Apply common configuration
        controller.setCurrentTime(startTime);
        controller.setTrackEventSources(trackEventSources);
        controller.setDebugEvents(debugEvents);

        // Apply controller-specific configuration
        if (controller instanceof SimulationController simController) {
            if (endTime != Long.MAX_VALUE) {
                simController.setEndTime(endTime);
            }
            simController.setTrackSpectrum(trackSpectrum);
            if (name != null) {
                simController.setName(name);
            }
            if (startTime != 0) {
                simController.setStartTime(startTime);
            }
        } else if (controller instanceof RealTimeController rtController) {
            rtController.setTrackSpectrum(trackSpectrum);
        } else if (controller instanceof SteppingController stepController) {
            stepController.setTrackSpectrum(trackSpectrum);
            if (name != null) {
                stepController.setName(name);
            }
        }

        // Set as thread-local controller
        Kairos.setController(controller);

        return controller;
    }

    /**
     * Create the controller instance based on the configured type.
     *
     * @return a new controller instance
     * @throws IllegalArgumentException if the controller type cannot be instantiated
     */
    private Devi createController() {
        try {
            // Handle special cases that require constructor parameters
            if (controllerType == RealTimeController.class) {
                var constructorName = name != null ? name : "Prime Mover Real-Time Simulation";
                Constructor<? extends Devi> constructor = controllerType.getConstructor(String.class);
                return constructor.newInstance(constructorName);
            }

            // For SimulationController and SteppingController, use no-arg constructor
            return controllerType.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(
            "Cannot instantiate controller type: " + controllerType.getName() + ". " +
            "Ensure it is a concrete class (not abstract) and has an appropriate constructor.", e);
        }
    }
}
