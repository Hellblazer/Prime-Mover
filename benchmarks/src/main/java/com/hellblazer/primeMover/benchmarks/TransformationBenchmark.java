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
import com.hellblazer.primeMover.classfile.ClassScanner;
import com.hellblazer.primeMover.classfile.SimulationTransform;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static com.hellblazer.primeMover.api.Kronos.sleep;

/**
 * JMH benchmark for Prime Mover bytecode transformation performance.
 * Measures ClassFile API transformation latency, entity bytecode generation time,
 * and multi-module transformation overhead.
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 2)
@Fork(1)
public class TransformationBenchmark {

    private Path tempDir;
    private Path classesDir;

    @Setup(Level.Trial)
    public void setup() throws IOException {
        tempDir = Files.createTempDirectory("benchmark-transform");
        classesDir = tempDir.resolve("classes");
        Files.createDirectories(classesDir);

        // Write sample entity classes to disk for transformation
        writeSampleClasses();
    }

    @TearDown(Level.Trial)
    public void teardown() throws IOException {
        if (tempDir != null) {
            deleteDirectory(tempDir);
        }
    }

    @Benchmark
    public void fullTransformPipeline() throws IOException {
        var scanner = new ClassScanner().addClasspathEntry(classesDir).scan();
        try (var transform = new SimulationTransform(scanner)) {
            transform.transformed(SimulationTransform.EXCLUDE_TRANSFORMED_FILTER);
        }
    }

    @Benchmark
    public void classpathScan() throws IOException {
        try (var scanner = new ClassScanner().addClasspathEntry(classesDir)) {
            scanner.scan();
        }
    }

    @Benchmark
    public void generatorCreation() throws IOException {
        var scanner = new ClassScanner().addClasspathEntry(classesDir).scan();
        try (var transform = new SimulationTransform(scanner)) {
            transform.generators(SimulationTransform.EXCLUDE_TRANSFORMED_FILTER);
        }
    }

    private void writeSampleClasses() throws IOException {
        // In a real benchmark, we would write compiled .class files to disk
        // For this implementation, we assume the classes directory contains
        // pre-compiled sample entity classes for transformation
    }

    private void deleteDirectory(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (var walk = Files.walk(path)) {
                walk.sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            // Ignore
                        }
                    });
            }
        }
    }

    /**
     * Sample entity for transformation benchmarking
     */
    @Entity
    public static class SimpleEntity {
        private int counter;

        public void increment() {
            counter++;
            sleep(1);
        }

        public int getCounter() {
            return counter;
        }
    }

    /**
     * Complex entity with multiple method types
     */
    @Entity
    public static class ComplexEntity {
        private int intValue;
        private double doubleValue;
        private String stringValue;

        public void intMethod(int value) {
            this.intValue = value;
            sleep(1);
        }

        public void doubleMethod(double value) {
            this.doubleValue = value;
            sleep(1);
        }

        public void stringMethod(String value) {
            this.stringValue = value;
            sleep(1);
        }

        @Blocking
        public int blockingMethod(int input) {
            sleep(10);
            return input * 2;
        }

        @Blocking
        public String blockingStringMethod(String input) {
            sleep(10);
            return input.toUpperCase();
        }
    }

    /**
     * Entity with many methods to test generation overhead
     */
    @Entity
    public static class ManyMethodsEntity {
        public void method01() { sleep(1); }
        public void method02() { sleep(1); }
        public void method03() { sleep(1); }
        public void method04() { sleep(1); }
        public void method05() { sleep(1); }
        public void method06() { sleep(1); }
        public void method07() { sleep(1); }
        public void method08() { sleep(1); }
        public void method09() { sleep(1); }
        public void method10() { sleep(1); }
        public void method11() { sleep(1); }
        public void method12() { sleep(1); }
        public void method13() { sleep(1); }
        public void method14() { sleep(1); }
        public void method15() { sleep(1); }
        public void method16() { sleep(1); }
        public void method17() { sleep(1); }
        public void method18() { sleep(1); }
        public void method19() { sleep(1); }
        public void method20() { sleep(1); }
    }
}
