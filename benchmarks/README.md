# Prime Mover JMH Benchmarks

Comprehensive performance benchmarks for the Prime Mover event-driven simulation framework using JMH (Java Microbenchmark Harness).

## Overview

This module provides JMH benchmarks to measure:

1. **Event Throughput** - Events/second with varying payload sizes
2. **Continuation Throughput** - Blocking events/second and return value overhead
3. **Tracking Overhead** - Cost of event source tracking, spectrum tracking, and debug mode
4. **Spectrum Tracking** - Performance impact of event type counting
5. **Memory Overhead** - Per-event memory consumption with different configurations

## Building

```bash
cd /Users/hal.hildebrand/git/Prime-Mover
./mvnw clean install -pl benchmarks -am
```

This will:
1. Build the benchmarks module and its dependencies
2. Transform @Entity classes using the PrimeMover Maven plugin
3. Create an uber JAR with JMH runner at `benchmarks/target/benchmarks.jar`

## Running Benchmarks

### Run All Benchmarks

```bash
java -jar benchmarks/target/benchmarks.jar
```

### Run Specific Benchmark

```bash
# Event throughput only
java -jar benchmarks/target/benchmarks.jar EventThroughputBenchmark

# Continuation throughput only
java -jar benchmarks/target/benchmarks.jar ContinuationThroughputBenchmark

# Tracking overhead only
java -jar benchmarks/target/benchmarks.jar TrackingOverheadBenchmark

# Spectrum tracking only
java -jar benchmarks/target/benchmarks.jar SpectrumTrackingBenchmark

# Memory overhead only
java -jar benchmarks/target/benchmarks.jar MemoryOverheadBenchmark
```

### Run Specific Test Method

```bash
java -jar benchmarks/target/benchmarks.jar EventThroughputBenchmark.eventNoPayload
```

### Additional JMH Options

```bash
# List all available benchmarks
java -jar benchmarks/target/benchmarks.jar -l

# Show help
java -jar benchmarks/target/benchmarks.jar -h

# Custom iterations and warmup
java -jar benchmarks/target/benchmarks.jar -wi 5 -i 10 -f 3

# Enable GC profiler for memory analysis
java -jar benchmarks/target/benchmarks.jar MemoryOverheadBenchmark -prof gc

# Enable stack profiler
java -jar benchmarks/target/benchmarks.jar -prof stack

# Export results to JSON
java -jar benchmarks/target/benchmarks.jar -rf json -rff results.json
```

## Benchmark Details

### EventThroughputBenchmark

Measures event processing throughput with different payload sizes:

- **eventNoPayload**: Baseline with no parameters
- **eventWithPrimitive**: Single int parameter
- **eventWithMultiplePrimitives**: int, long, double parameters
- **eventWithTypicalPayload**: 10 objects (5 Strings, 5 Integers)
- **eventWithHeavyPayload**: 300 objects (100 each of String[], Integer[], Double[])

**Configuration**:
- Mode: Throughput (ops/sec)
- Warmup: 10 iterations × 1 second
- Measurement: 10 iterations × 1 second
- Forks: 3
- JVM: -Xms2g -Xmx2g

### ContinuationThroughputBenchmark

Measures blocking event (continuation) throughput:

- **blockingEventNoPayload**: Baseline blocking event
- **blockingEventWithReturn**: Return int value
- **blockingEventWithParamAndReturn**: Parameter + return value
- **blockingEventTypicalPayload**: Typical payload + return value

**Configuration**: Same as EventThroughputBenchmark

### TrackingOverheadBenchmark

Measures overhead of tracking features with parameterized configurations:

- **Parameters**:
  - `trackSources`: false/true (event caller chains)
  - `trackSpectrum`: false/true (event type counts)
  - `debugEvents`: false/true (stack traces)
- **Tests**: Event creation and blocking events with all combinations (2³ = 8 configurations)

**Expected Results**:
- No tracking: Baseline performance
- Spectrum only: ~0.01% overhead
- Sources only: ~2-5% overhead (GC pressure)
- Debug mode: ~50-100% overhead (StackWalker per event)

### SpectrumTrackingBenchmark

Focused comparison of spectrum tracking overhead:

- **mixedEventTypes**: Multiple event types to stress ConcurrentHashMap.merge()
- **spectrumAccess**: Cost of reading spectrum map

**Parameters**: `trackSpectrum` false/true

### MemoryOverheadBenchmark

Measures per-event memory allocation:

- **Parameters**:
  - `trackSources`: false/true
  - `trackSpectrum`: false/true
  - `eventCount`: 1, 10, 100, 1000
- **Tests**: Non-blocking events, blocking events, typical payload events

**Usage**:
```bash
# Run with GC profiler to see allocations
java -jar benchmarks/target/benchmarks.jar MemoryOverheadBenchmark -prof gc
```

**Expected Results**:
- Per event (no tracking): ~100-150 bytes
- Per event (with spectrum): ~150-200 bytes
- Per event (with sources): ~200-300 bytes (caller chain references)
- Blocking event: +50-100 bytes (continuation state)

## Interpreting Results

### Throughput Benchmarks

```
Benchmark                                    Mode  Cnt       Score      Error  Units
EventThroughputBenchmark.eventNoPayload     thrpt   30  500000.123 ± 5000.456  ops/s
```

- **Score**: Operations per second (higher is better)
- **Error**: 95% confidence interval
- **Cnt**: Total iterations (forks × measurement iterations)

### Memory Benchmarks

```
Benchmark                                           Mode  Cnt    Score    Error   Units
MemoryOverheadBenchmark.eventMemoryOverhead        avgt   30   45.123 ±  2.456   ns/op
MemoryOverheadBenchmark.eventMemoryOverhead:·gc.*  alloc  30  128.000           bytes
```

- **avgt**: Average time per operation (lower is better)
- **·gc.alloc**: Total bytes allocated per operation
- **·gc.count**: Number of GC pauses during benchmark

## Baseline Performance Targets

Based on initial benchmarking (to be updated with actual results):

| Benchmark | Target | Notes |
|-----------|--------|-------|
| Event throughput (no payload) | >500K ops/sec | Baseline event creation |
| Event throughput (typical payload) | >300K ops/sec | 10-object payload |
| Continuation throughput | >100K ops/sec | Virtual thread overhead |
| Tracking overhead (spectrum) | <1% | ConcurrentHashMap merge |
| Tracking overhead (sources) | <5% | GC pressure acceptable |
| Memory per event | <200 bytes | Without tracking |
| Memory per blocking event | <300 bytes | Including continuation |

## Regression Detection

To establish baseline for regression detection:

1. Run all benchmarks on clean build:
   ```bash
   java -jar benchmarks/target/benchmarks.jar -rf json -rff baseline.json
   ```

2. Store `baseline.json` in version control

3. Compare future runs:
   ```bash
   java -jar benchmarks/target/benchmarks.jar -rf json -rff current.json
   # Use JMH comparison tools or custom scripts to diff
   ```

4. Alert on regressions >10% from baseline

## JVM Settings

All benchmarks use:
- **Heap**: -Xms2g -Xmx2g (consistent heap size)
- **GC**: Default (G1GC in Java 25)
- **Threads**: Unbounded virtual threads (Project Loom)

For memory benchmarks, explicitly use G1GC for consistent allocation tracking:
```bash
java -XX:+UseG1GC -jar benchmarks/target/benchmarks.jar MemoryOverheadBenchmark -prof gc
```

## Development Notes

### Adding New Benchmarks

1. Create @Entity in `benchmarks/entities/`
2. Create @Benchmark class in `benchmarks/`
3. Rebuild: `./mvnw clean install -pl benchmarks -am`

### Benchmark Best Practices

- Use `Blackhole.consume()` to prevent dead code elimination
- Set proper warmup iterations (10+ for JIT)
- Use multiple forks (3+) for statistical confidence
- Avoid I/O in tight benchmark loops
- Use `@State(Scope.Thread)` for thread-local state
- Use `@State(Scope.Benchmark)` for parameterized configs

## Profiling

### GC Profiling
```bash
java -jar benchmarks/target/benchmarks.jar -prof gc
```

### Stack Profiling
```bash
java -jar benchmarks/target/benchmarks.jar -prof stack
```

### Perfasm (Linux only)
```bash
java -jar benchmarks/target/benchmarks.jar -prof perfasm
```

## License

Copyright (c) 2025, Hal Hildebrand. All rights reserved.

This file is part of the Prime Mover Event Driven Simulation Framework.

GNU Affero General Public License V3 - see LICENSE file.
