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

package com.hellblazer.primeMover;

import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Logger;

import com.hellblazer.primeMover.runtime.Devi;
import com.hellblazer.primeMover.runtime.EventImpl;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

public class ControllerImpl extends Devi {
    public final Queue<EventImpl> eventQueue = new PriorityBlockingQueue<EventImpl>();

    public boolean send() throws SimulationException {
        if (eventQueue.isEmpty()) {
            return false;
        }
        try {
            evaluate(eventQueue.remove());
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;
    }

    @Override
    protected void post(EventImpl event) {
        eventQueue.add(event);
        Logger.getLogger(ControllerImpl.class.getCanonicalName()).info("Posting: %s".formatted(event));
    }
}
