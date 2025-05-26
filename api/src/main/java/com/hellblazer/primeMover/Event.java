/**
 * Copyright (C) 2010 Hal Hildebrand. All rights reserved.
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

import java.io.PrintStream;

/**
 * Represents the event in the simulation.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public interface Event {

    /**
     * @return the string representation of the event method signature.
     */
    String getSignature();

    /**
     * @return the event that raised this event. Only available when the
     *         controller is tracking event sources
     */
    Event getSource();

    /**
     * @return the scheduled time of the event
     */
    long getTime();

    /**
     * Print the chain of events which led to this event to the System.out
     * printStream
     */
    void printTrace();

    /**
     * 
     * Print the chain of events which led to this event to the printStream
     * 
     * @param stream
     */
    void printTrace(PrintStream stream);

}