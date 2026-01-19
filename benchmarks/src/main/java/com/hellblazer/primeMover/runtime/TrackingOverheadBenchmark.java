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
 * JMH benchmark for measuring overhead of event tracking features:
 * - Event source tracking (caller chains)
 * - Event spectrum tracking (event type counts)
 * - Debug mode (stack traces)
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms2g", "-Xmx2g"})
public class TrackingOverheadBenchmark {

    @State(Scope.Benchmark)
    public static class TrackingState {
        @Param({"false", "true"})
        public boolean trackSources;

        @Param({"false", "true"})
        public boolean trackSpectrum;

        @Param({"false", "true"})
        public boolean debugEvents;
    }

    private SimulationController controller;
    private BenchmarkEntity entity;

    @Setup(Level.Iteration)
    public void setup(TrackingState state) {
        controller = new SimulationController();
        controller.setTrackEventSources(state.trackSources);
        controller.setTrackSpectrum(state.trackSpectrum);
        controller.setDebugEvents(state.debugEvents);
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
     * Measure event creation overhead with different tracking configurations
     */
    @Benchmark
    public void eventCreationOverhead(TrackingState state, Blackhole blackhole) throws Exception {
        entity.eventNoPayload();
        controller.eventLoop();
        blackhole.consume(entity.getCounter());
    }

    /**
     * Measure blocking event overhead with different tracking configurations
     */
    @Benchmark
    public void blockingEventOverhead(TrackingState state, Blackhole blackhole) throws Exception {
        entity.blockingEventNoPayload();
        controller.eventLoop();
        blackhole.consume(entity.getCounter());
    }
}
