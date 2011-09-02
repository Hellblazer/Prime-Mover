/**
 * Copyright (C) 2010 Hal Hildebrand. All rights reserved.
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
package com.hellblazer.primeMover;

import java.util.logging.Logger;

import com.hellblazer.primeMover.runtime.EntityReference;

/**
 * The processor of events, the continuation of time. This is the central
 * control interface of PrimeMover.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public interface Controller {

    /**
     * Advance the current time of the controller
     * 
     * @param duration
     */
    void advance(long duration);

    /**
     * Reinitialize the state of the controller
     */
    void clear();

    /**
     * 
     * @return the current event of the controller.
     */
    Event getCurrentEvent();

    /**
     * Answer the current instant in time of the controller
     * 
     * @return the current value of the simulation clock
     */
    long getCurrentTime();

    /**
     * @return true if the controller is collecting debug information as to the
     *         source of where the event was raised
     */
    boolean isDebugEvents();

    /**
     * @return true if the controller is tracking event sources
     */
    boolean isTrackEventSources();

    /**
     * Post the event to be evaluated. The event is blocking, meaning that it
     * will cause the caller to continue execution until the event is processed.
     * 
     * @param entity
     *            - the target of the event
     * @param event
     *            - the event event
     * @param arguments
     *            - the arguments to the event
     * @return
     * @throws Throwable
     */
    Object postContinuingEvent(EntityReference entity, int event,
                               Object... arguments) throws Throwable;

    /**
     * Post the event to be evaluated
     * 
     * @param entity
     *            - the target of the event
     * @param event
     *            - the event event
     * @param arguments
     *            - the arguments to the event
     */
    void postEvent(EntityReference entity, int event, Object... arguments);

    /**
     * Post the event to be evaluated at the specified instant in time
     * 
     * @param time
     *            - the instant in time the event is to be processed
     * @param entity
     *            - the target of the event
     * @param event
     *            - the event event
     * @param arguments
     *            - the arguments to the event
     */
    void postEvent(long time, EntityReference entity, int event,
                   Object... arguments);

    /**
     * Set the current time of the controller
     * 
     * @param time
     */
    void setCurrentTime(long time);

    /**
     * Configure the collecting of debug information for raised events. When
     * debug is enabled, the controller will record the source location where an
     * event was raised. The collection of debug information for events is
     * expensive and significantly impacts the performance of the simulation
     * event processing.
     * 
     * @param debug
     *            - true to trigger the collecting of event debug information
     */
    void setDebugEvents(boolean debug);

    /**
     * Configure the logger for tracing all event processing
     * 
     * @param eventLog
     */
    void setEventLogger(Logger eventLog);

    /**
     * Configure whether the controller will track the source event of a raised
     * event. Tracking event sources has garbage collection implications, as
     * event chains prevent the elimantion of previous events which have already
     * been processed.
     * 
     * @param track
     *            - true to track event sources
     */
    void setTrackEventSources(boolean track);

}
