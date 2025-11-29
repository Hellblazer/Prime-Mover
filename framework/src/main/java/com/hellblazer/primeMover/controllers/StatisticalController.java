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

import java.util.Map;

/**
 * An interface for gathering statistics on the simulation run
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public interface StatisticalController {

    /**
     * Answer the simulation clock at the end of the simulation
     *
     * @return
     */
    public abstract long getSimulationEnd();

    /**
     * Answer the simulation clock at the beginning of the simulation
     *
     * @return
     */
    public abstract long getSimulationStart();

    /**
     * Answer the spectrum of events.
     * 
     * @return a Map where the key is the signature of the event, and the value
     *         is the number of times the event was invoked
     */
    public abstract Map<String, Integer> getSpectrum();

    /**
     * Answer the total number of events processed during the simulation
     * 
     * @return
     */
    public abstract int getTotalEvents();

}
