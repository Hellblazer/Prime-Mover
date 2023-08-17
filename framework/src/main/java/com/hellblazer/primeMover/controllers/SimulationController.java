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

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.logging.Logger;

import com.hellblazer.primeMover.SimulationException;
import com.hellblazer.primeMover.runtime.Devi;
import com.hellblazer.primeMover.runtime.EventImpl;
import com.hellblazer.primeMover.runtime.Framework;
import com.hellblazer.primeMover.runtime.SimulationEnd;
import com.hellblazer.primeMover.runtime.SplayQueue;

/**
 * The cannonical simulation controller which gathers statistics of the events
 * sent
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */

public class SimulationController extends Devi implements StatisticalController {
    static final Logger            log      = Logger.getLogger(SimulationController.class.getCanonicalName());
    protected long                 endTime  = Long.MAX_VALUE;
    protected Map<String, Integer> spectrum = new HashMap<String, Integer>();
    protected Queue<EventImpl>     eventQueue;
    protected String               name     = "Prime Mover Simulation Event Evaluation";
    protected long                 simulationEnd;
    protected long                 simulationStart;

    protected int                  totalEvents;

    public SimulationController() {
        this(new SplayQueue<EventImpl>());
    }

    public SimulationController(Queue<EventImpl> eventQueue) {
        this.eventQueue = eventQueue;
    }

    /**
     * Continuously process events until the end of the simulation is reached,
     * or the simulation clock has advanced to the simulation end time.
     * 
     * @throws SimulationException
     */
    public void eventLoop() throws SimulationException {
        totalEvents = 0;
        if (simulationStart < 0) {
            simulationStart = 0;
        }
        setCurrentTime(simulationStart);
        Framework.setController(this);
        log.info("Simulation started at: " + simulationStart);
        try {
            while (getCurrentTime() < endTime) {
                try {
                    singleStep();
                } catch (SimulationEnd e) {
                    break;
                } catch (NoSuchElementException e) {
                    break;
                }
            }
            simulationEnd = getCurrentTime();
            log.info("Simulation ended at: " + simulationEnd);
        } finally {
            Framework.setController(null);
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
     * @see com.hellblazer.primeMover.runtime.StatisticalController#getSimulationEnd()
     */
    @Override
    public long getSimulationEnd() {
        return simulationEnd;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.hellblazer.primeMover.runtime.StatisticalController#getSimulationStart()
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

    @Override
    protected void post(EventImpl event) {
        eventQueue.add(event);
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
     * Process the head of the queued events.
     * 
     * @throws SimulationException
     */
    public void singleStep() throws SimulationException {
        EventImpl current = eventQueue.remove();
        String signature = current.getSignature();
        evaluate(current);
        totalEvents++;
        Integer inc = spectrum.get(signature);
        if (inc == null) {
            inc = Integer.valueOf(0);
        }
        spectrum.put(signature, inc + 1);
    }
}
