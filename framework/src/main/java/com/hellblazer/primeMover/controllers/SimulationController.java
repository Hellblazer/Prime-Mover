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
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class SimulationController extends Devi implements StatisticalController {
    private static final Logger log = LoggerFactory.getLogger(SimulationController.class);

    protected long                 endTime         = Long.MAX_VALUE;
    protected Queue<EventImpl>     eventQueue;
    protected String               name            = "Prime Mover Simulation Event Evaluation";
    protected long                 simulationEnd;
    protected long                 simulationStart;
    protected Map<String, Integer> spectrum        = new ConcurrentHashMap<>();
    protected int                  totalEvents;
    protected boolean              trackSpectrum   = true;

    public SimulationController() {
        this(new PriorityQueue<>());
    }

    public SimulationController(Queue<EventImpl> eventQueue) {
        this.eventQueue = eventQueue;
    }

    /**
     * Continuously process events until the end of the simulation is reached, or
     * the simulation clock has advanced to the simulation end time.
     * 
     * @throws SimulationException
     */
    public void eventLoop() throws SimulationException {
        totalEvents = 0;
        if (simulationStart < 0) {
            simulationStart = 0;
        }
        setCurrentTime(simulationStart);
        Kairos.setController(this);
        log.info("Simulation started at: {}", simulationStart);
        try {
            while (getCurrentTime() < endTime) {
                try {
                    singleStep();
                } catch (NoSuchElementException e) {
                    break;
                }
            }
            simulationEnd = getCurrentTime();
            log.info("Simulation ended at: {}", simulationEnd);
        } finally {
            Kairos.setController(null);
        }
    }

    /**
     * Answer the scheduled end time of the simulation
     * 
     * @return
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
     * 
     * @return
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
     * 
     * @param endTime
     */
    public void setEndTime(long endTime) {
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
     * Set the start time of the simulation
     * 
     * @param startTime
     */
    public void setStartTime(long startTime) {
        simulationStart = startTime;
    }

    /**
     * Configure whether the controller tracks event spectrum statistics.
     * Disabling spectrum tracking improves performance.
     *
     * @param track - true to track event spectrum
     */
    public void setTrackSpectrum(boolean track) {
        this.trackSpectrum = track;
    }

    /**
     * @return true if spectrum tracking is enabled
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
        totalEvents++;
        if (trackSpectrum) {
            spectrum.merge(current.getSignature(), 1, Integer::sum);
        }
    }

    @Override
    public void post(EventImpl event) {
        eventQueue.add(event);
    }
}
