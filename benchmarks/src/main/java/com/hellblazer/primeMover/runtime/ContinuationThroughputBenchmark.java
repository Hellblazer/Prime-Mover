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
 * JMH benchmark for measuring continuation (blocking event) throughput in Prime Mover
 * Measures virtual thread continuation overhead and return value throughput
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms2g", "-Xmx2g"})
public class ContinuationThroughputBenchmark {

    private SimulationController controller;
    private BenchmarkEntity entity;

    @Setup(Level.Iteration)
    public void setup() {
        controller = new SimulationController();
        Framework.setController(controller);
        entity = new BenchmarkEntity();
    }

    @TearDown(Level.Iteration)
    public void teardown() throws Exception {
        if (controller != null) {
            controller.close();
        }
        Framework.setController(null);
    }

    /**
     * Baseline: blocking event with no payload
     */
    @Benchmark
    public void blockingEventNoPayload(Blackhole blackhole) throws Exception {
        entity.blockingEventNoPayload();
        controller.eventLoop();
        blackhole.consume(entity.getCounter());
    }

    /**
     * Blocking event with return value
     */
    @Benchmark
    public void blockingEventWithReturn(Blackhole blackhole) throws Exception {
        var result = entity.blockingEventWithReturn();
        controller.eventLoop();
        blackhole.consume(result);
    }

    /**
     * Blocking event with parameter and return value
     */
    @Benchmark
    public void blockingEventWithParamAndReturn(Blackhole blackhole) throws Exception {
        var result = entity.blockingEventWithParamAndReturn(42);
        controller.eventLoop();
        blackhole.consume(result);
    }

    /**
     * Blocking event with typical payload and return value
     */
    @Benchmark
    public void blockingEventTypicalPayload(Blackhole blackhole) throws Exception {
        var result = entity.blockingEventTypicalPayload("test", 123, 3.14);
        controller.eventLoop();
        blackhole.consume(result);
    }
}
