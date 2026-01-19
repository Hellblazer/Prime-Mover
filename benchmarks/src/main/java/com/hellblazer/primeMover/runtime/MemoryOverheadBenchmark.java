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
 * JMH benchmark for measuring memory overhead per event with different tracking options
 * Uses JMH's memory profiling to capture allocation rates
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
@BenchmarkMode({Mode.AverageTime, Mode.SampleTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms2g", "-Xmx2g", "-XX:+UseG1GC"})
public class MemoryOverheadBenchmark {

    @State(Scope.Benchmark)
    public static class MemoryState {
        @Param({"false", "true"})
        public boolean trackSources;

        @Param({"false", "true"})
        public boolean trackSpectrum;

        @Param({"1", "10", "100", "1000"})
        public int eventCount;
    }

    private SimulationController controller;
    private BenchmarkEntity entity;

    @Setup(Level.Iteration)
    public void setup(MemoryState state) {
        controller = new SimulationController();
        controller.setTrackEventSources(state.trackSources);
        controller.setTrackSpectrum(state.trackSpectrum);
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
     * Measure memory overhead for event creation and execution
     * Run with -prof gc to see allocation rates
     */
    @Benchmark
    public void eventMemoryOverhead(MemoryState state, Blackhole blackhole) throws Exception {
        for (int i = 0; i < state.eventCount; i++) {
            entity.eventNoPayload();
            controller.eventLoop();
        }
        blackhole.consume(entity.getCounter());
    }

    /**
     * Measure memory overhead for blocking events with continuations
     */
    @Benchmark
    public void blockingEventMemoryOverhead(MemoryState state, Blackhole blackhole) throws Exception {
        for (int i = 0; i < state.eventCount; i++) {
            entity.blockingEventNoPayload();
            controller.eventLoop();
        }
        blackhole.consume(entity.getCounter());
    }

    /**
     * Measure memory overhead for events with typical payload
     */
    @Benchmark
    public void typicalPayloadMemoryOverhead(MemoryState state, Blackhole blackhole) throws Exception {
        for (int i = 0; i < state.eventCount; i++) {
            entity.eventWithTypicalPayload(
                "s1", "s2", "s3", "s4", "s5",
                1, 2, 3, 4, 5
            );
            controller.eventLoop();
        }
        blackhole.consume(entity.getCounter());
    }
}
