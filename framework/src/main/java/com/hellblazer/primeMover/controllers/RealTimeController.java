/**
 * Copyright (C) 2008 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.primeMover.controllers;

import com.hellblazer.primeMover.SimulationException;
import com.hellblazer.primeMover.runtime.Devi;
import com.hellblazer.primeMover.runtime.EventImpl;
import com.hellblazer.primeMover.runtime.SplayQueue;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This controller evaluates the events using the wall clock as the simulation time
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
abstract public class RealTimeController extends Devi {
    private static final Logger log = Logger.getLogger(RealTimeController.class.getCanonicalName());

    protected Thread           animator;
    protected Queue<EventImpl> eventQueue = new SplayQueue<EventImpl>();
    protected String           name;
    protected AtomicBoolean    running    = new AtomicBoolean(false);
    protected Lock             queueLock  = new ReentrantLock();

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
        animator = Thread.ofVirtual().name("Event Animation Thread [" + name + "]").factory().newThread(eventLoop());
        animator.start();
    }

    /**
     * Stop the simulation
     */
    public void stop() {
        if (!running.getAndSet(false)) {
            return;
        }
        queueLock.lock();
        try {
            eventQueue.clear();
            eventQueue.notify();
        } finally {
            queueLock.unlock();
        }
        if (animator != null) {
            animator.interrupt();
        }
        animator = null;
    }

    /**
     * the event loop of the simulation controller
     */
    protected Runnable eventLoop() {
        return () -> {
            EventImpl event;
            boolean eventFired;
            long millis;
            while (running.get()) {
                // Wait for queue to become non-empty
                while (eventQueue.isEmpty() && running.get()) {
                    LockSupport.park();
                }
                if (Thread.interrupted()) {
                    break;
                }

                queueLock.lock();
                try {
                    if (eventQueue.isEmpty()) {
                        break;
                    }
                    event = eventQueue.peek();
                    millis = event.getTime();
                    eventFired = millis <= System.currentTimeMillis();
                    if (eventFired) {
                        eventQueue.remove();
                    }
                } finally {
                    queueLock.unlock();
                }
                if (eventFired) {
                    try {
                        evaluate(event);
                    } catch (SimulationException e) {
                        log.log(Level.SEVERE, "Error firing: " + event, e.getCause());
                    }
                } else {
                    LockSupport.parkUntil(millis);
                    if (Thread.interrupted()) {
                        break;
                    }
                }
            }
        };
    }

    @Override
    protected void post(EventImpl event) {
        queueLock.lock();
        try {
            eventQueue.add(event);
            if (eventQueue.peek() == event) {
                LockSupport.unpark(animator);
            }
        } finally {
            queueLock.unlock();
        }
    }
}
