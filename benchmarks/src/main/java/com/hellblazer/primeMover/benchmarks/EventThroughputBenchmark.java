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

package com.hellblazer.primeMover.benchmarks;

import com.hellblazer.primeMover.annotations.Entity;
import com.hellblazer.primeMover.api.Kronos;
import com.hellblazer.primeMover.controllers.SimulationController;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

import static com.hellblazer.primeMover.api.Kronos.sleep;

/**
 * JMH benchmark for Prime Mover event throughput.
 * Measures events per second with various batch sizes and parameter types.
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 2)
@Fork(1)
public class EventThroughputBenchmark {

    @Param({"100", "1000", "10000"})
    private int batchSize;

    private SimulationController controller;

    @Setup(Level.Iteration)
    public void setup() {
        controller = new SimulationController();
        Kronos.setController(controller);
    }

    @TearDown(Level.Iteration)
    public void teardown() {
        Kronos.setController(null);
        controller = null;
    }

    @Benchmark
    public void nullEvents() throws Exception {
        var entity = new EventEntity("NULL", batchSize);
        entity.start();
        controller.eventLoop();
    }

    @Benchmark
    public void intEvents() throws Exception {
        var entity = new EventEntity("INT", batchSize);
        entity.start();
        controller.eventLoop();
    }

    @Benchmark
    public void doubleEvents() throws Exception {
        var entity = new EventEntity("DOUBLE", batchSize);
        entity.start();
        controller.eventLoop();
    }

    @Benchmark
    public void stringEvents() throws Exception {
        var entity = new EventEntity("STRING", batchSize);
        entity.start();
        controller.eventLoop();
    }

    @Entity
    public static class EventEntity {
        private final int limit;
        private final String mode;
        private int nEvents;

        public EventEntity(String mode, int limit) {
            this.mode = mode;
            this.limit = limit;
        }

        public void start() {
            switch (mode) {
                case "NULL" -> nullOperation();
                case "INT" -> intOperation(1);
                case "DOUBLE" -> doubleOperation(1.0);
                case "STRING" -> stringOperation("prime mover");
                default -> throw new IllegalArgumentException("Unknown mode: " + mode);
            }
        }

        public void nullOperation() {
            nEvents++;
            if (nEvents >= limit) {
                return;
            }
            sleep(1);
            nullOperation();
        }

        public void intOperation(int i) {
            nEvents++;
            if (nEvents >= limit) {
                return;
            }
            sleep(1);
            intOperation(i);
        }

        public void doubleOperation(double d) {
            nEvents++;
            if (nEvents >= limit) {
                return;
            }
            sleep(1);
            doubleOperation(d);
        }

        public void stringOperation(String s) {
            nEvents++;
            if (nEvents >= limit) {
                return;
            }
            sleep(1);
            stringOperation(s);
        }
    }
}
