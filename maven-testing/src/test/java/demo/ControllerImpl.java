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

package demo;

import java.util.NoSuchElementException;
import java.util.Queue;

import com.hellblazer.primeMover.api.SimulationException;
import com.hellblazer.primeMover.runtime.Devi;
import com.hellblazer.primeMover.runtime.EventImpl;
import com.hellblazer.primeMover.runtime.SplayQueue;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

public class ControllerImpl extends Devi {
    public Queue<EventImpl> eventQueue = new SplayQueue<EventImpl>();

    public void run(String entityClassName, String eventName,
                    Class<?>[] argumentTypes, Object[] arguments)
                                                                 throws ClassNotFoundException,
                                                                 SecurityException,
                                                                 NoSuchMethodException,
                                                                 IllegalAccessException,
                                                                 InstantiationException {
    }

    public boolean send() throws SimulationException {
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
    }
}
