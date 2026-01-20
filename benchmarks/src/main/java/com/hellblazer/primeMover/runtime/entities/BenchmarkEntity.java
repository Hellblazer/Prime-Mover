/**
 * Copyright (C) 2025 Hal Hildebrand. All rights reserved.
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

package com.hellblazer.primeMover.runtime.entities;

import com.hellblazer.primeMover.annotations.Blocking;
import com.hellblazer.primeMover.annotations.Entity;

import static com.hellblazer.primeMover.api.Kronos.sleep;

/**
 * Entity for measuring event throughput with various payload types
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
@Entity
public class BenchmarkEntity {
    private volatile int counter = 0;

    /**
     * Event with no parameters - minimal overhead
     */
    public void eventNoPayload() {
        counter++;
        sleep(1);
    }

    /**
     * Event with primitive parameter
     */
    public void eventWithPrimitive(int value) {
        counter += value;
        sleep(1);
    }

    /**
     * Event with multiple primitive parameters
     */
    public void eventWithMultiplePrimitives(int a, long b, double c) {
        counter += a;
        sleep(1);
    }

    /**
     * Event with typical payload (10 objects)
     */
    public void eventWithTypicalPayload(String s1, String s2, String s3, String s4, String s5,
                                        Integer i1, Integer i2, Integer i3, Integer i4, Integer i5) {
        counter++;
        sleep(1);
    }

    /**
     * Event with heavy payload (100+ objects via arrays)
     */
    public void eventWithHeavyPayload(String[] strings, Integer[] ints, Double[] doubles) {
        counter++;
        sleep(1);
    }

    /**
     * Blocking event with no payload
     */
    @Blocking
    public void blockingEventNoPayload() {
        counter++;
        sleep(1);
    }

    /**
     * Blocking event with return value
     */
    @Blocking
    public int blockingEventWithReturn() {
        sleep(1);
        return counter++;
    }

    /**
     * Blocking event with parameter and return value
     */
    @Blocking
    public int blockingEventWithParamAndReturn(int value) {
        sleep(1);
        counter += value;
        return counter;
    }

    /**
     * Blocking event with typical payload and return value
     */
    @Blocking
    public String blockingEventTypicalPayload(String s1, Integer i1, Double d1) {
        sleep(1);
        counter++;
        return s1 + i1 + d1;
    }

    /**
     * Get current counter value
     */
    public int getCounter() {
        return counter;
    }

    /**
     * Reset counter
     */
    public void reset() {
        counter = 0;
    }
}
