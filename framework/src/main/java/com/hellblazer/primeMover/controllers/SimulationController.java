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

package com.hellblazer.primeMover.controllers;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hellblazer.primeMover.api.SimulationException;
import com.hellblazer.primeMover.runtime.Devi;
import com.hellblazer.primeMover.runtime.EventImpl;
import com.hellblazer.primeMover.runtime.Kairos;

/**
 * Single-threaded discrete event simulation controller that gathers statistics
 * of the events processed.
 * <p>
 * Thread safety model:
 * <ul>
 *   <li>The event loop ({@link #eventLoop()}) runs on a single thread</li>
 *   <li>Event posting via {@link #post(EventImpl)} is not thread-safe</li>
 *   <li>For concurrent event posting, use {@link RealTimeController}</li>
 *   <li>The spectrum map uses ConcurrentHashMap for safe reads during simulation</li>
 * </ul>
 *
 * <h2>Event Spectrum Tracking</h2>
 * <p>
 * SimulationController can track the distribution of event types processed during simulation.
 * The <em>event spectrum</em> is a histogram showing how many times each event signature (method)
 * was executed, providing insight into simulation behavior and performance characteristics.
 *
 * <h3>Purpose and Use Cases</h3>
 * <ul>
 *   <li><b>Performance Analysis:</b> Identify event hotspots and bottlenecks by finding
 *       which events dominate execution time</li>
 *   <li><b>Simulation Validation:</b> Verify that the simulation produces the expected
 *       distribution of events (e.g., ensuring rare events actually occur)</li>
 *   <li><b>Debugging:</b> Understand simulation dynamics by examining which events fire
 *       and how frequently they occur</li>
 *   <li><b>Profiling:</b> Compare event distributions across different simulation
 *       configurations or parameter values</li>
 * </ul>
 *
 * <h3>Usage Pattern</h3>
 * <pre>{@code
 * // Enable spectrum tracking (enabled by default)
 * var controller = new SimulationController();
 * controller.setTrackSpectrum(true);  // Optional - tracking is on by default
 *
 * // Run simulation
 * Kronos.setController(controller);
 * // ... create entities and schedule events ...
 * controller.eventLoop();
 *
 * // Analyze event distribution
 * System.out.println("Event spectrum:");
 * for (var entry : controller.getSpectrum().entrySet()) {
 *     System.out.println("  " + entry.getValue() + " events: " + entry.getKey());
 * }
 * }</pre>
 *
 * <h3>Event Signatures</h3>
 * <p>
 * The spectrum uses event signatures as keys. An event signature is the fully-qualified
 * method name that was transformed into an event, such as:
 * <ul>
 *   <li>{@code com.example.MyEntity.processOrder(Order)}</li>
 *   <li>{@code demo.EventThroughput.stringOperation(String)}</li>
 *   <li>{@code hello.HelloWorld.event1()}</li>
 * </ul>
 * <p>
 * Each signature represents a unique event type in your simulation. The spectrum map
 * counts how many times each signature was processed during the simulation run.
 *
 * <h3>Performance Impact</h3>
 * <ul>
 *   <li><b>CPU Overhead:</b> One HashMap lookup and increment per event processed (typically
 *       nanoseconds per event)</li>
 *   <li><b>Memory Usage:</b> O(n) where n = number of unique event signatures in the simulation.
 *       Each signature requires a String key and Integer value in the spectrum map</li>
 *   <li><b>Recommendation:</b> Keep tracking enabled for development and analysis. Disable
 *       with {@link #setTrackSpectrum(boolean)} only if profiling shows it's a bottleneck
 *       (unlikely except for very high-throughput simulations with millions of events)</li>
 * </ul>
 *
 * <h3>Thread Safety</h3>
 * <p>
 * The spectrum map uses {@link ConcurrentHashMap} for thread-safe updates and reads.
 * While SimulationController's event loop is single-threaded, the spectrum can be safely
 * read from other threads during or after simulation (useful for monitoring tools).
 *
 * <h3>Example Output</h3>
 * <pre>
 * Event spectrum:
 *   100000 events: demo.EventThroughput.stringOperation(String)
 *   1 events: demo.EventThroughput.finish()
 *   1 events: demo.EventThroughput.start()
 *   1 events: demo.Driver.runEventBenchmark(String, Integer, Integer)
 * </pre>
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * @see #setTrackSpectrum(boolean)
 * @see #getSpectrum()
 * @see StatisticalController
 */
public class SimulationController extends Devi implements StatisticalController {
    private static final Logger log = LoggerFactory.getLogger(SimulationController.class);

    protected long               endTime           = Long.MAX_VALUE;
    protected Queue<EventImpl>   eventQueue;
    protected boolean            simulationRunning = false;

    public SimulationController() {
        this(new PriorityQueue<>());
    }

    public SimulationController(Queue<EventImpl> eventQueue) {
        this.eventQueue = eventQueue;
        this.name = "Prime Mover Simulation Event Evaluation";
        this.trackSpectrum = true;
        this.spectrum = new ConcurrentHashMap<>();
    }

    /**
     * Continuously process events until the end of the simulation is reached, or
     * the simulation clock has advanced to the simulation end time.
     *
     * @throws SimulationException
     */
    public void eventLoop() throws SimulationException {
        simulationRunning = true;
        try {
            totalEvents = 0;
            if (simulationStart < 0) {
                simulationStart = 0;
            }
            super.setCurrentTime(simulationStart);  // Use super to bypass validation for internal initialization
            Kairos.setController(this);
            log.info("[SimulationController] Simulation '{}' started at time {}", name, simulationStart);
            try {
                while (getCurrentTime() < endTime) {
                    try {
                        singleStep();
                    } catch (NoSuchElementException e) {
                        break;
                    }
                }
                simulationEnd = getCurrentTime();
                log.info("[SimulationController] Simulation '{}' ended at time {} ({} events processed)",
                        name, simulationEnd, totalEvents);
            } finally {
                Kairos.setController(null);
            }
        } finally {
            simulationRunning = false;
        }
    }

    /**
     * Answer the scheduled end time of the simulation.
     * <p>
     * The end time determines when the event loop terminates. Events scheduled
     * at or after this time will not be processed.
     *
     * @return the scheduled end time (in simulation time units)
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Answer the name of the simulation.
     * 
     * @return
     */
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.hellblazer.primeMover.runtime.StatisticalController#getSimulationEnd()
     */
    @Override
    public long getSimulationEnd() {
        return simulationEnd;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.hellblazer.primeMover.runtime.StatisticalController#getSimulationStart()
     */
    @Override
    public long getSimulationStart() {
        return simulationStart;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.hellblazer.primeMover.runtime.StatisticalController#getSpectrum()
     */
    @Override
    public Map<String, Integer> getSpectrum() {
        return spectrum;
    }

    /**
     * Answer the start time of the simulation.
     * <p>
     * This is the time value at which the simulation began execution,
     * as set by {@link #setStartTime(long)} or defaulting to 0.
     *
     * @return the simulation start time (in simulation time units)
     */
    public long getStartTime() {
        return simulationStart;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.hellblazer.primeMover.runtime.StatisticalController#getTotalEvents()
     */
    @Override
    public int getTotalEvents() {
        return totalEvents;
    }

    /**
     * Set the scheduled end time of the simulation.
     * <p>
     * The end time determines when {@link #eventLoop()} will terminate. Events scheduled
     * at times greater than or equal to this value will not be processed.
     * <p>
     * <b>Valid Call Timing:</b>
     * <ul>
     *   <li>BEFORE calling {@link #eventLoop()} - configures when simulation should stop</li>
     *   <li>DURING event processing - dynamically adjusts simulation termination time</li>
     * </ul>
     * <p>
     * <b>Typical Usage Pattern:</b>
     * <pre>{@code
     * var controller = new SimulationController();
     * controller.setCurrentTime(0);    // Set initial clock to 0
     * controller.setStartTime(0);      // Simulation begins at time 0
     * controller.setEndTime(1000);     // Simulation terminates at time 1000
     * // ... schedule initial events ...
     * controller.eventLoop();          // Run until time >= 1000
     * }</pre>
     * <p>
     * <b>Relationship to Other Time Methods:</b>
     * <ul>
     *   <li>{@link #setStartTime(long)} - defines where simulation begins</li>
     *   <li>{@link #setCurrentTime(long)} - sets the initial simulation clock value</li>
     *   <li>{@link Devi#getCurrentTime()} - reads the current simulation time</li>
     * </ul>
     *
     * @param endTime the simulation end time (in simulation time units)
     * @throws IllegalArgumentException if endTime is negative
     * @throws IllegalArgumentException if endTime is less than the current start time
     */
    public void setEndTime(long endTime) {
        if (endTime < 0) {
            throw new IllegalArgumentException("End time cannot be negative: " + endTime);
        }
        if (endTime < simulationStart) {
            throw new IllegalArgumentException(
            "End time (" + endTime + ") cannot be less than start time (" + simulationStart + ")");
        }
        this.endTime = endTime;
    }

    /**
     * Set the name of the simulation.
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set the start time of the simulation.
     * <p>
     * This defines the initial time value when the simulation begins. When {@link #eventLoop()}
     * starts, it will set the simulation clock to this value (or 0 if negative).
     * <p>
     * <b>Valid Call Timing:</b>
     * <ul>
     *   <li>BEFORE calling {@link #eventLoop()} - configures initial simulation time</li>
     *   <li>NOT VALID during or after {@link #eventLoop()} - will not affect running simulation</li>
     * </ul>
     * <p>
     * <b>Typical Usage Pattern:</b>
     * <pre>{@code
     * var controller = new SimulationController();
     * controller.setStartTime(0);      // Simulation will begin at time 0
     * controller.setCurrentTime(0);    // Set initial clock to match start time
     * controller.setEndTime(1000);     // Simulation will end at time 1000
     * // ... schedule initial events ...
     * controller.eventLoop();          // Clock starts at startTime
     * }</pre>
     * <p>
     * <b>Relationship to Other Time Methods:</b>
     * <ul>
     *   <li>{@link #setCurrentTime(long)} - sets the current simulation clock before the loop starts</li>
     *   <li>{@link #setEndTime(long)} - defines when simulation terminates</li>
     *   <li>{@link #getSimulationStart()} - retrieves the actual start time used</li>
     * </ul>
     * <p>
     * <b>Important Notes:</b>
     * <ul>
     *   <li>Negative values are normalized to 0 when {@link #eventLoop()} starts</li>
     *   <li>This does NOT immediately change the simulation clock - use {@link #setCurrentTime(long)} for that</li>
     *   <li>Typically set to the same value as {@link #setCurrentTime(long)} to avoid confusion</li>
     * </ul>
     *
     * @param startTime the simulation start time (in simulation time units)
     * @throws IllegalArgumentException if startTime is negative
     * @throws IllegalStateException if called during simulation execution
     */
    public void setStartTime(long startTime) {
        if (startTime < 0) {
            throw new IllegalArgumentException("Start time cannot be negative: " + startTime);
        }
        if (simulationRunning) {
            throw new IllegalStateException("Cannot change start time while simulation is running");
        }
        simulationStart = startTime;
    }

    /**
     * Set the current simulation time.
     * <p>
     * This method directly sets the simulation clock to the specified value. It is typically
     * used BEFORE starting the simulation to initialize the clock.
     * <p>
     * <b>Valid Call Timing:</b>
     * <ul>
     *   <li>BEFORE calling {@link #eventLoop()} - initializes the simulation clock</li>
     *   <li>NOT RECOMMENDED during event processing - the event loop manages time advancement</li>
     * </ul>
     * <p>
     * <b>Typical Usage Pattern:</b>
     * <pre>{@code
     * var controller = new SimulationController();
     * controller.setStartTime(0);      // Simulation begins at time 0
     * controller.setCurrentTime(0);    // Initialize clock to 0
     * controller.setEndTime(1000);     // Simulation ends at time 1000
     * // ... schedule initial events ...
     * controller.eventLoop();          // Run simulation
     * }</pre>
     * <p>
     * <b>Relationship to Other Time Methods:</b>
     * <ul>
     *   <li>{@link #setStartTime(long)} - defines the logical start time for statistics</li>
     *   <li>{@link #setEndTime(long)} - defines when simulation terminates</li>
     *   <li>{@link Devi#getCurrentTime()} - reads the current simulation time</li>
     *   <li>{@link Devi#advance(long)} - advances time by a relative duration</li>
     * </ul>
     * <p>
     * <b>Important Notes:</b>
     * <ul>
     *   <li>This method sets the absolute time value, not a relative offset</li>
     *   <li>During event processing, time is advanced automatically by the event loop</li>
     *   <li>Already-scheduled events with times before the new current time will still execute</li>
     *   <li>For consistency, typically set this to the same value as {@link #setStartTime(long)}</li>
     * </ul>
     *
     * @param time the simulation time to set (in simulation time units)
     * @throws IllegalArgumentException if time is negative
     * @throws IllegalStateException if called during simulation execution
     */
    @Override
    public void setCurrentTime(long time) {
        if (time < 0) {
            throw new IllegalArgumentException("Current time cannot be negative: " + time);
        }
        if (simulationRunning) {
            throw new IllegalStateException(
            "Cannot directly set current time during simulation - time is managed by the event loop");
        }
        super.setCurrentTime(time);
    }

    /**
     * Configure whether the controller tracks event spectrum statistics.
     * <p>
     * Event spectrum tracking is enabled by default. It captures the distribution of event types
     * (method signatures) processed during simulation, useful for performance analysis, debugging,
     * and validation. See class-level documentation for details on event spectrum features.
     * <p>
     * Disabling spectrum tracking provides a small performance improvement (one HashMap operation
     * per event is eliminated) but is rarely necessary except for extremely high-throughput
     * simulations with millions of events.
     *
     * @param track true to track event spectrum, false to disable tracking
     * @see #getSpectrum()
     * @see #isTrackSpectrum()
     */
    public void setTrackSpectrum(boolean track) {
        this.trackSpectrum = track;
    }

    /**
     * Check whether event spectrum tracking is currently enabled.
     * <p>
     * Spectrum tracking is enabled by default. When enabled, the controller maintains a histogram
     * of event types processed during simulation.
     *
     * @return true if spectrum tracking is enabled, false otherwise
     * @see #setTrackSpectrum(boolean)
     * @see #getSpectrum()
     */
    public boolean isTrackSpectrum() {
        return trackSpectrum;
    }

    /**
     * Process the head of the queued events.
     *
     * @throws SimulationException
     */
    public void singleStep() throws SimulationException {
        var current = eventQueue.remove();
        evaluate(current);
        recordEvent(current);
        // Clear references to allow garbage collection of entities and caller chains
        current.clearReferences();
    }

    @Override
    public void post(EventImpl event) {
        eventQueue.add(event);
    }
}
