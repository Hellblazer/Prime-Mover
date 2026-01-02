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

import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Multi-threaded real-time simulation controller that evaluates events using the wall clock
 * as the simulation time.
 * <p>
 * Thread safety model:
 * <ul>
 *   <li>Event posting can occur from any thread while the animator runs</li>
 *   <li>Uses internal locking via {@code queueLock} for event queue synchronization</li>
 *   <li>The {@code notEmpty} condition is used to signal when events are available</li>
 *   <li>Lock ordering: always acquire {@code queueLock} before accessing {@code eventQueue}</li>
 *   <li>Statistics use atomic types and ConcurrentHashMap for thread-safe access</li>
 * </ul>
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class RealTimeController extends Devi implements StatisticalController {
    private static final Logger log = LoggerFactory.getLogger(RealTimeController.class);

    protected final Queue<EventImpl>       eventQueue      = new PriorityQueue<>();
    protected final String                 name;
    protected final AtomicBoolean          running         = new AtomicBoolean(false);
    protected final Lock                   queueLock       = new ReentrantLock();
    protected final Condition              notEmpty        = queueLock.newCondition();
    protected final Map<String, Integer>   spectrum        = new ConcurrentHashMap<>();
    protected final AtomicInteger          totalEvents     = new AtomicInteger(0);
    protected final AtomicLong             simulationStart = new AtomicLong(0);
    protected final AtomicLong             simulationEnd   = new AtomicLong(0);
    protected volatile boolean             trackSpectrum   = false;
    protected       Thread                 animator;
    private         long                   offset;

    public RealTimeController(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getSimulationStart() {
        return simulationStart.get();
    }

    @Override
    public long getSimulationEnd() {
        return simulationEnd.get();
    }

    @Override
    public Map<String, Integer> getSpectrum() {
        return spectrum;
    }

    @Override
    public int getTotalEvents() {
        return totalEvents.get();
    }

    public boolean isTrackSpectrum() {
        return trackSpectrum;
    }

    public void setTrackSpectrum(boolean track) {
        this.trackSpectrum = track;
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
        simulationStart.set(0);
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
        simulationEnd.set(advance());
        var animatorThread = animator;
        queueLock.lock();
        try {
            eventQueue.clear();
            // Signal the condition to wake up the animator thread
            notEmpty.signalAll();
        } finally {
            queueLock.unlock();
        }
        if (animatorThread != null) {
            animatorThread.interrupt();
        }
        animator = null;
    }

    protected long advance() {
        return System.nanoTime() - offset;
    }

    /**
     * The event loop of the simulation controller.
     * <p>
     * Uses condition-based waiting for thread-safe queue access:
     * <ul>
     *   <li>Waits on {@code notEmpty} condition when queue is empty</li>
     *   <li>All queue operations are performed under {@code queueLock}</li>
     *   <li>Uses {@code awaitNanos} for timed waiting until next event</li>
     * </ul>
     */
    protected Runnable eventLoop() {
        return () -> {
            EventImpl event = null;
            boolean eventFired;
            long waitNanos;
            while (running.get()) {
                queueLock.lock();
                try {
                    // Wait for queue to become non-empty
                    while (eventQueue.isEmpty() && running.get()) {
                        try {
                            notEmpty.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    if (!running.get()) {
                        return;
                    }
                    event = eventQueue.peek();
                    var eventTime = event.getTime();
                    var currentTime = advance();
                    eventFired = eventTime <= currentTime;
                    if (eventFired) {
                        eventQueue.poll();
                    } else {
                        waitNanos = eventTime - currentTime;
                    }
                } finally {
                    queueLock.unlock();
                }

                if (eventFired) {
                    try {
                        evaluate(event);
                        totalEvents.incrementAndGet();
                        if (trackSpectrum) {
                            spectrum.merge(event.getSignature(), 1, Integer::sum);
                        }
                    } catch (SimulationException e) {
                        log.error("Error firing: {}", event, e.getCause());
                    }
                } else {
                    // Wait outside the lock until the next event time
                    queueLock.lock();
                    try {
                        // Re-check if a new earlier event was added
                        var head = eventQueue.peek();
                        if (head != null && head == event) {
                            try {
                                notEmpty.awaitNanos(event.getTime() - advance());
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                    } finally {
                        queueLock.unlock();
                    }
                }
            }
        };
    }

    @Override
    public void post(EventImpl event) {
        queueLock.lock();
        try {
            eventQueue.add(event);
            // Signal if this event is now at the head (earliest) or queue was empty
            if (eventQueue.peek() == event) {
                notEmpty.signal();
            }
        } finally {
            queueLock.unlock();
        }
    }
}
