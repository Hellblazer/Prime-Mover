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

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark for comparing event spectrum tracking overhead
 * Measures the cost of tracking event type counts in ConcurrentHashMap
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms2g", "-Xmx2g"})
public class SpectrumTrackingBenchmark {

    @State(Scope.Benchmark)
    public static class SpectrumState {
        @Param({"false", "true"})
        public boolean trackSpectrum;
    }

    private SimulationController controller;
    private BenchmarkEntity entity;

    @Setup(Level.Iteration)
    public void setup(SpectrumState state) {
        controller = new SimulationController();
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
     * Measure spectrum tracking overhead with mixed event types
     */
    @Benchmark
    public void mixedEventTypes(SpectrumState state, Blackhole blackhole) throws Exception {
        // Create diverse event types to test spectrum merge operations
        entity.eventNoPayload();
        controller.eventLoop();

        entity.eventWithPrimitive(42);
        controller.eventLoop();

        entity.eventWithMultiplePrimitives(1, 2L, 3.0);
        controller.eventLoop();

        entity.blockingEventNoPayload();
        controller.eventLoop();

        var result = entity.blockingEventWithReturn();
        controller.eventLoop();

        blackhole.consume(result);
        if (state.trackSpectrum) {
            Map<String, Integer> spectrum = controller.getSpectrum();
            blackhole.consume(spectrum.size());
        }
    }

    /**
     * Measure spectrum access cost
     */
    @Benchmark
    public void spectrumAccess(SpectrumState state, Blackhole blackhole) throws Exception {
        entity.eventNoPayload();
        controller.eventLoop();

        if (state.trackSpectrum) {
            Map<String, Integer> spectrum = controller.getSpectrum();
            blackhole.consume(spectrum);
        }
    }
}
