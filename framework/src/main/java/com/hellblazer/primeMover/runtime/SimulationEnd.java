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

package com.hellblazer.primeMover.runtime;

import java.lang.reflect.Method;

/**
 * The event which signals the end of a simulation.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public class SimulationEnd extends Error {
    public static Method      END_SIMULATION_METHOD = getEndSimulationMethod();
    private static final long serialVersionUID      = 1L;

    public static void endSimulation() {
        throw new SimulationEnd("Simulation has ended");
    }

    static Method getEndSimulationMethod() {
        try {
            return SimulationEnd.class.getDeclaredMethod("endSimulation");
        } catch (Exception e) {
            throw new IllegalStateException(
                                            "Unable to acquire endSimuation() event",
                                            e);
        }
    }

    public SimulationEnd() {
        super();
    }

    public SimulationEnd(String message) {
        super(message);
    }

    public SimulationEnd(String message, Throwable cause) {
        super(message, cause);
    }

    public SimulationEnd(Throwable cause) {
        super(cause);
    }
}
