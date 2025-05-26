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

import com.hellblazer.primeMover.api.SimulationException;
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
public class RealTimeController extends Devi {
    private static final Logger log = Logger.getLogger(RealTimeController.class.getCanonicalName());

    protected final Queue<EventImpl> eventQueue = new SplayQueue<EventImpl>();
    protected final String           name;
    protected final AtomicBoolean    running    = new AtomicBoolean(false);
    protected final Lock             queueLock  = new ReentrantLock();
    protected       Thread           animator;
    private         long             offset;

    public RealTimeController(String name) {
        this.name = name;
    }

    /**
     * Start the controller
     */
    public void start() {
        start(System.nanoTime());
    }

    /**
     * Start the controller
     */
    public void start(long offset) {
        if (running.getAndSet(true)) {
            return;
        }
        this.offset = offset;
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

    protected long advance() {
        return System.nanoTime() - offset;
    }

    /**
     * the event loop of the simulation controller
     */
    protected Runnable eventLoop() {
        return () -> {
            EventImpl event;
            boolean eventFired;
            long nanos;
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
                    nanos = event.getTime();
                    eventFired = nanos <= advance();
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
                    LockSupport.parkNanos(nanos);
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
