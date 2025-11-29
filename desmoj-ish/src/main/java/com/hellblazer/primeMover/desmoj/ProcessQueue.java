/**
 * Copyright (C) 2024 Hal Hildebrand. All rights reserved.
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

package com.hellblazer.primeMover.desmoj;

import com.hellblazer.primeMover.runtime.Devi;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * A FIFO queue with statistics tracking for simulation use.
 * Entry times are tracked for wait time calculation.
 * 
 * This is NOT an entity - it's a simple data structure that tracks entry times
 * for statistics. Blocking operations would be built on top of this using
 * SimSignal/SimCondition.
 * 
 * @param <E> the type of elements in this queue
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class ProcessQueue<E> implements Iterable<E> {
    
    /**
     * Inner record to track entry time with element
     */
    private record QueueEntry<E>(E element, long entryTime) {}
    
    private final Deque<QueueEntry<E>> queue = new ArrayDeque<>();
    private final QueueStatistics stats = new QueueStatistics();
    private final Devi controller;
    
    /**
     * Creates a new ProcessQueue.
     * 
     * @param controller the simulation controller (needed to get current time)
     */
    public ProcessQueue(Devi controller) {
        this.controller = controller;
    }
    
    /**
     * Adds an element to the back of the queue.
     * Records the entry time from the controller.
     * 
     * @param element the element to add
     */
    public void enqueue(E element) {
        var currentTime = controller.getCurrentTime();
        queue.addLast(new QueueEntry<>(element, currentTime));
        stats.recordEntry(currentTime);
    }
    
    /**
     * Removes and returns the element from the front of the queue.
     * Calculates and records wait time statistics.
     * 
     * @return the element at the front of the queue, or null if empty
     */
    public E dequeue() {
        if (queue.isEmpty()) {
            return null;
        }
        
        var currentTime = controller.getCurrentTime();
        var entry = queue.removeFirst();
        stats.recordExit(currentTime, entry.entryTime());
        return entry.element();
    }
    
    /**
     * Returns the element at the front of the queue without removing it.
     * 
     * @return the element at the front, or null if empty
     */
    public E peek() {
        var entry = queue.peekFirst();
        return entry != null ? entry.element() : null;
    }
    
    /**
     * @return true if the queue is empty
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }
    
    /**
     * @return the number of elements in the queue
     */
    public int size() {
        return queue.size();
    }
    
    /**
     * Checks if the queue contains the specified element.
     * 
     * @param element the element to check for
     * @return true if the queue contains the element
     */
    public boolean contains(E element) {
        for (var entry : queue) {
            if (entry.element().equals(element)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Removes the first occurrence of the specified element from the queue.
     * Updates statistics to reflect the length change.
     * 
     * @param element the element to remove
     * @return true if the element was removed
     */
    public boolean remove(E element) {
        var removed = queue.removeIf(entry -> entry.element().equals(element));
        if (removed) {
            var currentTime = controller.getCurrentTime();
            stats.updateLength(currentTime, queue.size());
        }
        return removed;
    }
    
    /**
     * Removes all elements from the queue.
     * Updates statistics to reflect the length change.
     */
    public void clear() {
        queue.clear();
        var currentTime = controller.getCurrentTime();
        stats.updateLength(currentTime, 0);
    }
    
    /**
     * @return the statistics object for this queue
     */
    public QueueStatistics statistics() {
        return stats;
    }
    
    /**
     * @return the current number of items in the queue
     */
    public int getCurrentLength() {
        return stats.getCurrentLength();
    }
    
    /**
     * @return the maximum queue length observed
     */
    public int getMaxLength() {
        return stats.getMaxLength();
    }
    
    /**
     * @return the total number of entries into the queue
     */
    public long getTotalEntries() {
        return stats.getTotalEntries();
    }
    
    /**
     * @return the total number of exits from the queue
     */
    public long getTotalExits() {
        return stats.getTotalExits();
    }
    
    /**
     * @return the average wait time for items that have exited
     */
    public double getAvgWaitTime() {
        return stats.getAvgWaitTime();
    }
    
    /**
     * @return the maximum wait time observed
     */
    public long getMaxWaitTime() {
        return stats.getMaxWaitTime();
    }
    
    /**
     * Calculates the time-weighted average queue length.
     * 
     * @param currentTime the current simulation time
     * @return the time-weighted average length
     */
    public double getAvgLength(long currentTime) {
        return stats.getAvgLength(currentTime);
    }
    
    /**
     * Resets all statistics.
     */
    public void resetStatistics() {
        stats.reset();
    }
    
    /**
     * Returns an iterator over the elements in the queue.
     * The iterator returns elements in FIFO order (front to back).
     * The iterator does not support removal.
     * 
     * @return an iterator over the elements
     */
    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private final Iterator<QueueEntry<E>> entryIterator = queue.iterator();
            
            @Override
            public boolean hasNext() {
                return entryIterator.hasNext();
            }
            
            @Override
            public E next() {
                return entryIterator.next().element();
            }
        };
    }
}
