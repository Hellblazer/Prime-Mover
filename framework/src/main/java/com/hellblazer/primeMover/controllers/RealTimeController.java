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

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hellblazer.primeMover.SimulationException;
import com.hellblazer.primeMover.runtime.Devi;
import com.hellblazer.primeMover.runtime.EventImpl;
import com.hellblazer.primeMover.runtime.SplayQueue;

/**
 * This controller evaluates the events using the wall clock as the simulation
 * time
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
abstract public class RealTimeController extends Devi {
    private static final Logger log        = Logger.getLogger(RealTimeController.class.getCanonicalName());
    protected Thread            animator;
    protected Queue<EventImpl>  eventQueue = new SplayQueue<EventImpl>();
    protected String            name;

    protected AtomicBoolean     running    = new AtomicBoolean(false);

    /**
     * the event loop of the simulation controller
     */
    protected void eventLoop() {
        EventImpl event;
        boolean eventFired;
        long millis;
        while (true) {
            // Wait for queue to become non-empty
            while (eventQueue.isEmpty() && running.get()) {
                LockSupport.park();
            }
            if (Thread.interrupted()) {
                break;
            }
            synchronized (eventQueue) {
                if (eventQueue.isEmpty()) {
                    break;
                }
                event = eventQueue.peek();
                millis = event.getTime();
                if (eventFired = millis <= System.currentTimeMillis()) {
                    eventQueue.remove();
                }
            }
            if (eventFired) {
                try {
                    evaluate(event);
                } catch (SimulationException e) {
                    log.log(Level.SEVERE, "Error firing: " + event,
                            e.getCause());
                }
            } else {
                LockSupport.parkUntil(millis);
                if (Thread.interrupted()) {
                    break;
                }
            }
        }
    }

    @Override
    protected void post(EventImpl event) {
        synchronized (eventQueue) {
            eventQueue.add(event);
            if (eventQueue.peek() == event) {
                LockSupport.unpark(animator);
            }
        }
    }

    /**
     * Set the name of the simulation.
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Start the controller
     */
    public void start() {
        if (running.getAndSet(true)) {
            return;
        }
        animator = new Thread(new Runnable() {
            @Override
            public void run() {
                eventLoop();
            }
        }, "Event Animation Thread [" + name + "]");
        animator.start();
    }

    /**
     * Stop the simulation
     */
    public void stop() {
        if (!running.getAndSet(false)) {
            return;
        }
        synchronized (eventQueue) {
            eventQueue.clear();
            eventQueue.notify();
        }
        if (animator != null) {
            animator.interrupt();
        }
        animator = null;
    }
}
