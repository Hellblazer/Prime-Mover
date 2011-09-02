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

import com.hellblazer.primeMover.Blocking;

/**
 * Static core utilities representing the simulation framework. The framework
 * stores the active controller for the framework in a thread local and most of
 * the methods are nothing more than trampolines to the active controller
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
final public class Framework {
    private final static InheritableThreadLocal<Devi> CONTROLLER = new InheritableThreadLocal<Devi>();

    public static Devi getController() {
        Devi controller = CONTROLLER.get();
        if (controller == null) {
            throw new IllegalStateException(
                                            "No controller has been set for the current thread");
        }
        return controller;
    }

    public static ContinuationFrame popFrame() {
        return getController().popFrame();
    }

    @Blocking
    public static Object postContinuingEvent(EntityReference entity,
                                             Object[] arguments, int event)
                                                                           throws Throwable {
        return getController().postContinuingEvent(entity, event, arguments);
    }

    public static void postEvent(EntityReference entity, Object[] arguments,
                                 int event) {
        if (entity == null) {
            throw new NullPointerException("entity is null");
        }
        getController().postEvent(entity, event, arguments);
    }

    public static void pushFrame(ContinuationFrame frame) {
        getController().pushFrame(frame);
    }

    public static Devi queryController() {
        return CONTROLLER.get();
    }

    public static boolean restoreFrame() {
        Devi current = CONTROLLER.get();
        if (current == null) {
            return false;
        }
        return current.restoreFrame();
    }

    public static void run(Runnable r) {
        r.run();
    }

    public static boolean saveFrame() {
        Devi current = CONTROLLER.get();
        if (current == null) {
            return false;
        }
        return current.saveFrame();
    }

    public static void setController(Devi controller) {
        CONTROLLER.set(controller);
    }

    public static boolean simulationIsRunning() {
        return CONTROLLER.get() != null;
    }

    private Framework() {
    }
}
