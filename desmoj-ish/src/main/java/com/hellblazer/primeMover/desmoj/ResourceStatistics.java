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
 * Statistics tracking for resource pool operations.
 * Tracks utilization, wait times, and acquisition patterns.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class ResourceStatistics {
    private final int capacity;
    
    // Acquisition tracking
    private long acquisitions = 0;
    private long releases = 0;
    
    // Wait time tracking
    private double sumWaitTime = 0;
    private long maxWaitTime = 0;
    
    // Utilization tracking (time-weighted)
    private long lastUpdateTime = -1;
    private double timeWeightedUtilizationSum = 0;
    private int currentInUse = 0;
    
    public ResourceStatistics(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = capacity;
    }
    
    /**
     * Record a resource acquisition.
     * 
     * @param count number of resources acquired
     * @param time current simulation time
     * @param waitTime time spent waiting for resources
     */
    public void recordAcquire(int count, long time, long waitTime) {
        updateUtilization(time);
        
        acquisitions++;
        currentInUse += count;
        sumWaitTime += waitTime;
        
        if (waitTime > maxWaitTime) {
            maxWaitTime = waitTime;
        }
    }
    
    /**
     * Record a resource release.
     * 
     * @param count number of resources released
     * @param time current simulation time
     */
    public void recordRelease(int count, long time) {
        updateUtilization(time);
        
        releases++;
        currentInUse -= count;
        
        if (currentInUse < 0) {
            throw new IllegalStateException("Released more resources than acquired");
        }
    }
    
    /**
     * Update the time-weighted utilization sum.
     */
    private void updateUtilization(long currentTime) {
        if (lastUpdateTime >= 0) {
            var duration = currentTime - lastUpdateTime;
            var utilization = (double) currentInUse / capacity;
            timeWeightedUtilizationSum += utilization * duration;
        }
        lastUpdateTime = currentTime;
    }
    
    /**
     * Get the capacity of the resource pool.
     */
    public int getCapacity() {
        return capacity;
    }
    
    /**
     * Get the current number of resources in use.
     */
    public int getCurrentInUse() {
        return currentInUse;
    }
    
    /**
     * Get the current number of available resources.
     */
    public int getAvailableCount() {
        return capacity - currentInUse;
    }
    
    /**
     * Get the total number of acquisitions.
     */
    public long getTotalAcquisitions() {
        return acquisitions;
    }
    
    /**
     * Get the average wait time across all acquisitions.
     */
    public double getAvgWaitTime() {
        return acquisitions == 0 ? 0 : sumWaitTime / acquisitions;
    }
    
    /**
     * Get the maximum wait time recorded.
     */
    public long getMaxWaitTime() {
        return maxWaitTime;
    }
    
    /**
     * Get the time-weighted average utilization.
     * 
     * @param currentTime current simulation time
     * @return utilization as a fraction (0.0 to 1.0)
     */
    public double getUtilization(long currentTime) {
        updateUtilization(currentTime);
        
        if (lastUpdateTime <= 0) {
            return 0;
        }
        
        return timeWeightedUtilizationSum / lastUpdateTime;
    }
    
    /**
     * Reset all statistics to initial state.
     */
    public void reset() {
        acquisitions = 0;
        releases = 0;
        sumWaitTime = 0;
        maxWaitTime = 0;
        lastUpdateTime = -1;
        timeWeightedUtilizationSum = 0;
        currentInUse = 0;
    }
}
