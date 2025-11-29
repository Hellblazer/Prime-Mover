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

/**
 * Statistics tracking for queue operations.
 * Tracks wait times, queue lengths, and throughput.
 * 
 * This is NOT an entity - just a regular statistics tracking class.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class QueueStatistics {
    
    // Entry/exit counts
    private long entries = 0;
    private long exits = 0;
    
    // Time tracking for time-weighted average length
    private long lastUpdateTime = -1;
    private double timeWeightedLengthSum = 0;
    
    // Wait time tracking
    private double sumWaitTime = 0;
    private long maxWaitTime = 0;
    
    // Length tracking
    private int currentLength = 0;
    private int maxLength = 0;
    
    /**
     * Records an entry into the queue at the given time.
     * Updates length statistics and time-weighted average.
     * 
     * @param time the simulation time when the entry occurred
     */
    public void recordEntry(long time) {
        updateTimeWeightedLength(time);
        entries++;
        currentLength++;
        if (currentLength > maxLength) {
            maxLength = currentLength;
        }
    }
    
    /**
     * Records an exit from the queue at the given time.
     * Calculates and records wait time, updates length statistics.
     * 
     * @param time the simulation time when the exit occurred
     * @param entryTime the simulation time when the item entered the queue
     */
    public void recordExit(long time, long entryTime) {
        updateTimeWeightedLength(time);
        exits++;
        currentLength--;
        
        var waitTime = time - entryTime;
        sumWaitTime += waitTime;
        if (waitTime > maxWaitTime) {
            maxWaitTime = waitTime;
        }
    }
    
    /**
     * Updates the time-weighted length sum.
     * Called before any length change to accumulate the time spent at the current length.
     * 
     * @param currentTime the current simulation time
     */
    private void updateTimeWeightedLength(long currentTime) {
        if (lastUpdateTime >= 0) {
            var timeSpan = currentTime - lastUpdateTime;
            timeWeightedLengthSum += currentLength * timeSpan;
        }
        lastUpdateTime = currentTime;
    }
    
    /**
     * Updates the time-weighted length for a length change operation (remove/clear).
     * 
     * @param currentTime the current simulation time
     * @param newLength the new length after the operation
     */
    public void updateLength(long currentTime, int newLength) {
        updateTimeWeightedLength(currentTime);
        currentLength = newLength;
    }
    
    /**
     * @return the current number of items in the queue
     */
    public int getCurrentLength() {
        return currentLength;
    }
    
    /**
     * @return the maximum queue length observed
     */
    public int getMaxLength() {
        return maxLength;
    }
    
    /**
     * @return the total number of entries into the queue
     */
    public long getTotalEntries() {
        return entries;
    }
    
    /**
     * @return the total number of exits from the queue
     */
    public long getTotalExits() {
        return exits;
    }
    
    /**
     * Calculates the average wait time for items that have exited the queue.
     * 
     * @return the average wait time, or 0 if no items have exited
     */
    public double getAvgWaitTime() {
        if (exits == 0) {
            return 0;
        }
        return sumWaitTime / exits;
    }
    
    /**
     * @return the maximum wait time observed
     */
    public long getMaxWaitTime() {
        return maxWaitTime;
    }
    
    /**
     * Calculates the time-weighted average queue length.
     * This gives a more accurate picture of queue utilization than simple averaging,
     * as it accounts for how long the queue was at each length.
     * 
     * @param currentTime the current simulation time
     * @return the time-weighted average length, or 0 if no time has passed
     */
    public double getAvgLength(long currentTime) {
        updateTimeWeightedLength(currentTime);
        
        if (lastUpdateTime <= 0) {
            return 0;
        }
        
        return timeWeightedLengthSum / lastUpdateTime;
    }
    
    /**
     * Resets all statistics to initial state.
     */
    public void reset() {
        entries = 0;
        exits = 0;
        lastUpdateTime = -1;
        timeWeightedLengthSum = 0;
        sumWaitTime = 0;
        maxWaitTime = 0;
        currentLength = 0;
        maxLength = 0;
    }
}
