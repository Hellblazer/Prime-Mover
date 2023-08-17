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

import java.util.NoSuchElementException;
import java.util.Queue;

import com.hellblazer.primeMover.SimulationException;
import com.hellblazer.primeMover.runtime.Devi;
import com.hellblazer.primeMover.runtime.EventImpl;
import com.hellblazer.primeMover.runtime.Framework;
import com.hellblazer.primeMover.runtime.SimulationEnd;
import com.hellblazer.primeMover.runtime.SplayQueue;

/**
 * A simulation controller which allows stepping through events
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class SteppingController extends Devi {
    protected Queue<EventImpl> eventQueue;

    public SteppingController() {
        this(new SplayQueue<EventImpl>());
    }

    public SteppingController(Queue<EventImpl> eventQueue) {
        this.eventQueue = eventQueue;
    }

    @Override
    protected void post(EventImpl event) {
        eventQueue.add(event);
    }

    public boolean step() throws SimulationException {
        if (getCurrentTime() < 0) {
            setCurrentTime(0);
        }
        Devi current = Framework.queryController();
        try {
            Framework.setController(this);
            while (true) {
                try {
                    evaluate(eventQueue.remove());
                } catch (SimulationEnd e) {
                    // simulation end
                    return false;
                } catch (NoSuchElementException e) {
                    // no more events to process
                    break;
                }
            }
        } finally {
            Framework.setController(current);
        }
        return true;
    }
}
