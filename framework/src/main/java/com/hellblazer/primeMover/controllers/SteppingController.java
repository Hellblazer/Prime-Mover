/**
 * Copyright (C) 2008 Hal Hildebrand. All rights reserved.
 * 
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.primeMover.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;

import com.hellblazer.primeMover.api.SimulationException;
import com.hellblazer.primeMover.runtime.Devi;
import com.hellblazer.primeMover.runtime.EventImpl;
import com.hellblazer.primeMover.runtime.Framework;
import com.hellblazer.primeMover.runtime.Kairos;
import com.hellblazer.primeMover.runtime.SimulationEnd;

/**
 * Single-threaded simulation controller that allows stepping through events
 * one at a time for debugging and testing.
 * <p>
 * Thread safety model:
 * <ul>
 *   <li>Not thread-safe - designed for single-threaded use</li>
 *   <li>The {@link #step()} method processes events sequentially</li>
 *   <li>Event posting via {@link #post(EventImpl)} is not thread-safe</li>
 * </ul>
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class SteppingController extends Devi implements StatisticalController {
    protected Queue<EventImpl>     eventQueue;
    protected String               name           = "Stepping Controller";
    protected Map<String, Integer> spectrum       = new HashMap<>();
    protected int                  totalEvents    = 0;
    protected long                 simulationStart = 0;
    protected long                 simulationEnd   = 0;
    protected boolean              trackSpectrum  = false;

    public SteppingController() {
        this(new PriorityQueue<>());
    }

    public SteppingController(Queue<EventImpl> eventQueue) {
        this.eventQueue = eventQueue;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public long getSimulationStart() {
        return simulationStart;
    }

    @Override
    public long getSimulationEnd() {
        return simulationEnd;
    }

    @Override
    public Map<String, Integer> getSpectrum() {
        return spectrum;
    }

    @Override
    public int getTotalEvents() {
        return totalEvents;
    }

    public boolean isTrackSpectrum() {
        return trackSpectrum;
    }

    public void setTrackSpectrum(boolean track) {
        this.trackSpectrum = track;
    }

    public boolean step() throws SimulationException {
        if (getCurrentTime() < 0) {
            setCurrentTime(0);
        }
        if (simulationStart == 0) {
            simulationStart = getCurrentTime();
        }
        Devi current = Framework.queryController();
        try {
            Kairos.setController(this);
            while (true) {
                try {
                    var event = eventQueue.remove();
                    evaluate(event);
                    totalEvents++;
                    if (trackSpectrum) {
                        spectrum.merge(event.getSignature(), 1, Integer::sum);
                    }
                } catch (SimulationEnd e) {
                    simulationEnd = getCurrentTime();
                    return true;
                } catch (NoSuchElementException e) {
                    // no more events to process
                    break;
                }
            }
            simulationEnd = getCurrentTime();
        } finally {
            Kairos.setController(current);
        }
        return true;
    }

    @Override
    public void post(EventImpl event) {
        eventQueue.add(event);
    }
}
