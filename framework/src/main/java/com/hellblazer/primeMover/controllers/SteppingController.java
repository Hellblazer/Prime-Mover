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
    protected Queue<EventImpl> eventQueue;

    public SteppingController() {
        this(new PriorityQueue<>());
    }

    public SteppingController(Queue<EventImpl> eventQueue) {
        this.eventQueue = eventQueue;
        this.name = "Stepping Controller";
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
                    recordEvent(event);
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

    /**
     * Process a single event from the queue. Useful for debugging and stepping
     * through simulations one event at a time.
     *
     * @return true if an event was processed, false if the queue was empty
     * @throws SimulationException if an error occurs during event processing
     */
    public boolean stepOne() throws SimulationException {
        if (getCurrentTime() < 0) {
            setCurrentTime(0);
        }
        if (simulationStart == 0) {
            simulationStart = getCurrentTime();
        }
        Devi current = Framework.queryController();
        try {
            Kairos.setController(this);
            var event = eventQueue.poll();
            if (event == null) {
                return false;
            }
            evaluate(event);
            recordEvent(event);
            simulationEnd = getCurrentTime();
            return true;
        } catch (SimulationEnd e) {
            simulationEnd = getCurrentTime();
            return true;
        } finally {
            Kairos.setController(current);
        }
    }

    /**
     * Check if there are pending events in the queue.
     *
     * @return true if there are events waiting to be processed
     */
    public boolean hasMoreEvents() {
        return !eventQueue.isEmpty();
    }

    /**
     * Peek at the next event without removing it from the queue.
     *
     * @return the next event, or null if the queue is empty
     */
    public EventImpl peekNextEvent() {
        return eventQueue.peek();
    }

    /**
     * Clear all pending events and reset statistics.
     */
    public void reset() {
        eventQueue.clear();
        spectrum.clear();
        totalEvents = 0;
        simulationStart = 0;
        simulationEnd = 0;
        clear();
    }

    @Override
    public void post(EventImpl event) {
        eventQueue.add(event);
    }
}
