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

package com.hellblazer.primeMover.runtime;

import com.hellblazer.primeMover.controllers.SimulationController;

import com.hellblazer.primeMover.runtime.entities.BenchmarkEntity;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark for measuring event throughput in Prime Mover
 * Measures events/second with varying payload sizes
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms2g", "-Xmx2g"})
public class EventThroughputBenchmark {

    private SimulationController controller;
    private BenchmarkEntity entity;

    // Payloads for testing
    private final String[] typicalStrings = {"s1", "s2", "s3", "s4", "s5"};
    private final Integer[] typicalIntegers = {1, 2, 3, 4, 5};

    private final String[] heavyStrings = new String[100];
    private final Integer[] heavyIntegers = new Integer[100];
    private final Double[] heavyDoubles = new Double[100];

    @Setup(Level.Iteration)
    public void setup() {
        controller = new SimulationController();
        Framework.setController(controller);
        entity = new BenchmarkEntity();

        // Initialize heavy payloads
        for (int i = 0; i < 100; i++) {
            heavyStrings[i] = "string" + i;
            heavyIntegers[i] = i;
            heavyDoubles[i] = (double) i;
        }
    }

    @TearDown(Level.Iteration)
    public void teardown() throws Exception {
        if (controller != null) {
            controller.close();
        }
        Framework.setController(null);
    }

    /**
     * Baseline: event with no payload
     */
    @Benchmark
    public void eventNoPayload(Blackhole blackhole) throws Exception {
        entity.eventNoPayload();
        controller.eventLoop();
        blackhole.consume(entity.getCounter());
    }

    /**
     * Event with single primitive parameter
     */
    @Benchmark
    public void eventWithPrimitive(Blackhole blackhole) throws Exception {
        entity.eventWithPrimitive(42);
        controller.eventLoop();
        blackhole.consume(entity.getCounter());
    }

    /**
     * Event with multiple primitives
     */
    @Benchmark
    public void eventWithMultiplePrimitives(Blackhole blackhole) throws Exception {
        entity.eventWithMultiplePrimitives(42, 12345L, 3.14159);
        controller.eventLoop();
        blackhole.consume(entity.getCounter());
    }

    /**
     * Event with typical payload (10 objects)
     */
    @Benchmark
    public void eventWithTypicalPayload(Blackhole blackhole) throws Exception {
        entity.eventWithTypicalPayload(
            typicalStrings[0], typicalStrings[1], typicalStrings[2], typicalStrings[3], typicalStrings[4],
            typicalIntegers[0], typicalIntegers[1], typicalIntegers[2], typicalIntegers[3], typicalIntegers[4]
        );
        controller.eventLoop();
        blackhole.consume(entity.getCounter());
    }

    /**
     * Event with heavy payload (100+ objects)
     */
    @Benchmark
    public void eventWithHeavyPayload(Blackhole blackhole) throws Exception {
        entity.eventWithHeavyPayload(heavyStrings, heavyIntegers, heavyDoubles);
        controller.eventLoop();
        blackhole.consume(entity.getCounter());
    }
}
