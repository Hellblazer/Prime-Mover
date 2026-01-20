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

import com.hellblazer.primeMover.annotations.Blocking;
import com.hellblazer.primeMover.annotations.Entity;
import com.hellblazer.primeMover.api.Kronos;
import com.hellblazer.primeMover.controllers.SimulationController;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

import static com.hellblazer.primeMover.api.Kronos.sleep;

/**
 * JMH benchmark for Prime Mover blocking event overhead and continuation performance.
 * Measures virtual thread creation, continuation park/resume cycles, and blocking event latency.
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 2)
@Fork(1)
public class ContinuationBenchmark {

    private static final byte[] BYTE_ARRAY = new byte[]{};

    @Param({"100", "500", "1000"})
    private int iterations;

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
    public void blockingNull() throws Exception {
        var entity = new ContinuationEntity(iterations);
        entity.runNull();
        controller.eventLoop();
    }

    @Benchmark
    public void blockingInt() throws Exception {
        var entity = new ContinuationEntity(iterations);
        entity.runInt();
        controller.eventLoop();
    }

    @Benchmark
    public void blockingDouble() throws Exception {
        var entity = new ContinuationEntity(iterations);
        entity.runDouble();
        controller.eventLoop();
    }

    @Benchmark
    public void blockingString() throws Exception {
        var entity = new ContinuationEntity(iterations);
        entity.runString();
        controller.eventLoop();
    }

    @Benchmark
    public void blockingArray() throws Exception {
        var entity = new ContinuationEntity(iterations);
        entity.runArray();
        controller.eventLoop();
    }

    @Benchmark
    public void virtualThreadCreation() throws Exception {
        var entity = new ContinuationEntity(iterations);
        entity.runVirtualThreads();
        controller.eventLoop();
    }

    @Benchmark
    public void platformThreadCreation() throws Exception {
        var entity = new ContinuationEntity(iterations);
        entity.runPlatformThreads();
        controller.eventLoop();
    }

    @Entity
    public static class ContinuationEntity {
        private final int limit;

        public ContinuationEntity(int limit) {
            this.limit = limit;
        }

        public void runNull() {
            for (var i = 0; i < limit; i++) {
                sleep(1);
                operationNull();
            }
        }

        public void runInt() {
            for (var i = 0; i < limit; i++) {
                sleep(1);
                operationInt(i);
            }
        }

        public void runDouble() {
            for (var i = 0; i < limit; i++) {
                sleep(1);
                operationDouble(i * 1.5);
            }
        }

        public void runString() {
            for (var i = 0; i < limit; i++) {
                sleep(1);
                operationString("iteration-" + i);
            }
        }

        public void runArray() {
            for (var i = 0; i < limit; i++) {
                sleep(1);
                var result = operationArray(BYTE_ARRAY);
                if (result == null) {
                    throw new IllegalStateException("Null result");
                }
            }
        }

        public void runVirtualThreads() {
            for (var i = 0; i < limit; i++) {
                sleep(1);
                operationVirtualThread();
            }
        }

        public void runPlatformThreads() {
            for (var i = 0; i < limit; i++) {
                sleep(1);
                operationPlatformThread();
            }
        }

        @Blocking
        public void operationNull() {
            sleep(1);
        }

        @Blocking
        public void operationInt(int value) {
            sleep(1);
        }

        @Blocking
        public void operationDouble(double value) {
            sleep(1);
        }

        @Blocking
        public void operationString(String value) {
            sleep(1);
        }

        @Blocking
        public byte[] operationArray(byte[] array) {
            sleep(1);
            return array;
        }

        @Blocking
        public void operationVirtualThread() {
            sleep(1);
        }

        @Blocking
        public void operationPlatformThread() {
            sleep(1);
        }
    }
}
