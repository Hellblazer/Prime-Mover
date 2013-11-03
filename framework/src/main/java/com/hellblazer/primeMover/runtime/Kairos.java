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

package com.hellblazer.primeMover.runtime;

import static com.hellblazer.primeMover.runtime.BlockingSleep.BLOCKING_SLEEP_INSTANCE;
import static com.hellblazer.primeMover.runtime.BlockingSleep.SLEEP_EVENT;
import static com.hellblazer.primeMover.runtime.Framework.postContinuingEvent;
import static com.hellblazer.primeMover.runtime.SimulationEnd.END_SIMULATION_METHOD;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.hellblazer.primeMover.Blocking;
import com.hellblazer.primeMover.Controller;
import com.hellblazer.primeMover.SynchronousQueue;

/**
 * The implementation of the static API to the simulation kernel.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
final public class Kairos {
    private static Method RUN_METHOD = getRunMethod();

    /**
     * Advance the simulation time, blocking the event invoking this API until
     * such time is reached in the simulation
     * 
     * @param duration
     *            - the duration to advance the simulation
     */
    @Blocking
    public static void blockingSleep(long duration) {
        try {
            postContinuingEvent(BLOCKING_SLEEP_INSTANCE,
                                new Object[] { duration }, SLEEP_EVENT);
        } catch (Throwable e) {
            throw new IllegalStateException(
                                            "No exception should have been thrown",
                                            e);
        }
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
        int modifiers = method.getModifiers();
        if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
            getController().postEvent(time, new StaticEntityReference(method),
                                      0, arguments);
        } else {
            throw new IllegalArgumentException(
                                               "Must be a public static event: "
                                                       + method.toGenericString());
        }
    }

    /**
     * Schedule the call of the static event in the simulation at the current
     * time
     * 
     * @param event
     * @param arguments
     */
    public static void callStatic(Method method, Object... arguments) {
        callStatic(currentTime(), method, arguments);
    }

    /**
     * Create a channel
     * 
     * @return
     */
    public static <T> SynchronousQueue<T> createChannel(Class<T> elementType) {
        return new SynchronousQueueImpl.entity<T>(Framework.getController());
    }

    /**
     * Answer the current time of the simulation
     * 
     * @return
     */
    public static long currentTime() {
        return getController().getCurrentTime();
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
        callStatic(time, END_SIMULATION_METHOD);
    }

    /**
     * @return the current controller of the thread
     * @throws IllegalStateException
     *             - if there is no controller set for the current thread
     */
    public static Controller getController() {
        return Framework.getController();
    }

    private static Method getRunMethod() {
        try {
            return Framework.class.getDeclaredMethod("run", Runnable.class);
        } catch (Exception e) {
            throw new IllegalStateException(
                                            "Unable to acquire run(Runnable) event",
                                            e);
        }
    }

    /**
     * @return the current controller of the thread, or null if none.
     */
    public static Controller queryController() {
        return Framework.queryController();
    }

    /**
     * Execute a runnable at the currently scheduled simulatioin time.
     * 
     * @param r
     *            - the Runnable to schedule
     */
    public static void run(Runnable r) {
        callStatic(RUN_METHOD, r);
    }

    /**
     * Execute a runnable at the indicated instant in the simulation
     * 
     * @param r
     *            - the Runnable to schedule
     * @param instant
     *            - the instant in time the runnable is scheduled
     */
    public static void runAt(Runnable r, long instant) {
        callStatic(instant, RUN_METHOD, r);
    }

    /**
     * Set the current thread's simulation controller
     * 
     * @param controller
     */
    public static void setController(Controller controller) {
        if (!(controller instanceof Devi)) {
            throw new IllegalArgumentException(
                                               "Controller must be instanceof Devi");
        }
        Framework.setController((Devi) controller);
    }

    /**
     * Answer true if the simulation is running
     * 
     * @return
     */
    public static boolean simulationIsRunning() {
        return Framework.simulationIsRunning();
    }

    /**
     * Advance the simulation time. Do not block the event invoking this API
     * 
     * @param duration
     *            - the measure of time to advance the simulation
     */
    public static void sleep(long duration) {
        getController().advance(duration);
    }

    private Kairos() {

    }
}
