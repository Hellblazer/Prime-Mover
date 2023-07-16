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

import java.lang.reflect.Method;

/**
 * The static API of the simulation kernel as exposed to compilation. In the
 * execution of the simulation, usages of these static methods will be replaced
 * the actual simulation runtime implementation.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class Kronos {

    /**
     * Advance the simulation time, blocking the event invoking this API until such
     * time is reached in the simulation
     * 
     * @param duration - the duration to advance the simulation
     */
    @Blocking
    public static void blockingSleep(long duration) {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }

    /**
     * Schedule the call of the static event in the simulation at the indicated
     * simulation time
     * 
     * @param time
     * @param event
     * @param arguments
     */
    public static void callStatic(long time, Method method, Object... arguments) {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }

    /**
     * Schedule the call of the static event in the simulation at the current time
     * 
     * @param event
     * @param arguments
     */
    public static void callStatic(Method method, Object... arguments) {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }

    /**
     * Create a channel
     * 
     * @return
     */
    public static <T> SynchronousQueue<T> createChannel(Class<T> elementType) {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }

    /**
     * Answer the current time of the simulation
     * 
     * @return
     */
    public static long currentTime() {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }

    /**
     * End the simulation at the current time
     */
    public static void endSimulation() {
        endSimulationAt(currentTime() + 1);
    }

    /**
     * End the simulation at the indicated time
     * 
     * @param time
     */
    public static void endSimulationAt(long time) {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }

    /**
     * @return the current controller of the thread
     * @throws IllegalStateException - if there is no controller set for the current
     *                               thread
     */
    public static Controller getController() {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }

    /**
     * @return the current controller of the thread, or null if none.
     */
    public static Controller queryController() {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }

    /**
     * Execute a runnable at the currently scheduled simulatioin time.
     * 
     * @param r - the Runnable to schedule
     */
    public static void run(Runnable r) {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }

    /**
     * Execute a runnable at the indicated instant in the simulation
     * 
     * @param r       - the Runnable to schedule
     * @param instant - the instant in time the runnable is scheduled
     */
    public static void runAt(Runnable r, long instant) {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }

    /**
     * Set the current thread's simulation controller
     * 
     * @param controller
     */
    public static void setController(Controller controller) {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }

    /**
     * Answer true if the simulation is running
     * 
     * @return
     */
    public static boolean simulationIsRunning() {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }

    /**
     * Advance the simulation time. Do not block the event invoking this API
     * 
     * @param duration - the measure of time to advance the simulation
     */
    public static void sleep(long duration) {
        throw new UnsupportedOperationException("This event should have been rewritten to call the simulation API");
    }
}
