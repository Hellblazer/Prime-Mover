# Performance Characteristics

This document provides validated performance measurements for Prime Mover's event-driven simulation framework.

## Executive Summary

**"Zero-Overhead" Clarification**: Prime Mover uses build-time bytecode transformation, which means there is **zero transformation overhead at runtime**. The classes are transformed during the build process, so no transformation happens when your application runs. However, the simulation framework itself has inherent costs (event scheduling, priority queue management, virtual thread coordination) which are necessary for discrete event simulation.

**Measured Performance** (Java 25, GraalVM, Apple Silicon M1):
- **Event Throughput**: ~120,000 non-blocking events/second (average: 119,046 events/sec)
- **Blocking Event Throughput**: ~45,000 blocking events/second (average: 45,202 events/sec)
- **Build-Time Transformation**: <0.5 seconds for 7 classes
- **Memory Overhead**: Modest - transformed classes add ~1-2KB per class for dispatch infrastructure

## What "Zero-Overhead" Means

### Runtime Transformation Overhead: Zero

Prime Mover offers two transformation approaches:

1. **Build-Time Transformation** (Maven/Gradle Plugin)
   - Classes are transformed after compilation
   - Transformed bytecode is written to disk
   - **Zero runtime transformation overhead**
   - Recommended for production use

2. **Runtime Transformation** (Java Agent)
   - Classes are transformed as they are loaded by the JVM
   - Slight startup overhead from on-the-fly transformation
   - Useful for prototyping and debugging

The "zero-overhead" claim specifically refers to build-time transformation having no transformation cost at runtime.

### Simulation Framework Overhead: Necessary and Efficient

The simulation framework itself has inherent costs that are necessary for discrete event simulation:

1. **Event Scheduling**: Method calls are converted to scheduled events in a priority queue
2. **Time Management**: Simulation time is managed by the controller
3. **Virtual Thread Coordination**: Blocking events use virtual thread continuations
4. **Event Dispatch**: Events are dispatched through generated `__invoke()` methods

These costs are **not** "overhead" in the traditional sense - they are the core functionality of discrete event simulation. Prime Mover makes these operations as efficient as possible using:
- Index-based event dispatch (no reflection)
- Native Java priority queue for event ordering
- Virtual threads (Project Loom) for efficient blocking
- Minimal object allocation per event

## Benchmark Methodology

Benchmarks use the existing demo module test suite:

### Event Throughput Benchmark

**Test**: `EventThroughput` class schedules 100,000 non-blocking events with `Kronos.sleep(1)` between each event.

**What it measures**: Raw event scheduling and execution throughput without blocking operations.

**Results** (3 runs):
```
Run 1: 117,233 events/second
Run 2: 116,144 events/second
Run 3: 123,762 events/second

Average: 119,046 events/second
```

### Continuation Throughput Benchmark

**Test**: `ContinuationThroughput` class schedules 100,000 blocking events (marked with `@Blocking`) that use `Kronos.blockingSleep()`.

**What it measures**: Blocking event throughput including virtual thread continuation overhead.

**Results** (3 runs):
```
Run 1: 41,684 continuation events/second
Run 2: 46,664 continuation events/second
Run 3: 47,259 continuation events/second

Average: 45,202 continuation events/second
```

### Build-Time Transformation

**Measurement**: Maven plugin output during `process-classes` phase.

**Results**:
- 7 classes transformed in <0.5 seconds total
- Transformation is fast and happens automatically during build
- No impact on application startup or runtime

**Class Size Impact**:
- Original classes: ~3-4 KB each
- Transformed classes: ~4-6 KB each (adds 1-2 KB for dispatch infrastructure)
- Added code: `__invoke()` method, `__signatureFor()` method, `EntityReference` implementation

## Performance Comparison

### Blocking vs Non-Blocking Events

Non-blocking events (`Kronos.sleep()`) are ~2.6x faster than blocking events (`Kronos.blockingSleep()`):
- Non-blocking: 119,046 events/sec
- Blocking: 45,202 events/sec
- Overhead: 62% slower for blocking operations

This is expected because blocking events require:
1. Creating and parking a virtual thread continuation
2. Scheduling a continuation event
3. Resuming the virtual thread when the event completes

**Recommendation**: Use non-blocking `Kronos.sleep()` when possible; use `@Blocking` and `Kronos.blockingSleep()` only when entity code needs to wait for results.

### Comparison to Direct Method Calls

Comparing simulation events to direct Java method calls is not meaningful because:
1. Direct calls execute immediately with no time semantics
2. Simulation events must be scheduled in time order
3. The priority queue and event management are necessary for DES

However, we can note that **transformed code does not add extra overhead beyond the necessary simulation mechanics**. The transformation generates efficient index-based dispatch without reflection.

## Performance Characteristics by Controller

Different controller implementations have different performance trade-offs:

| Controller | Event Throughput | Thread Safety | Overhead | Use Case |
|------------|------------------|---------------|----------|----------|
| `SimulationController` | Highest (~120K/sec) | Single-threaded | Lowest | Standard DES |
| `SteppingController` | Controlled | Single-threaded | Minimal | Debugging |
| `RealTimeController` | Variable | Multi-threaded | Locking overhead | Real-time simulation |

## Memory Characteristics

### Transformed Class Size

Transformation adds minimal bytecode:
- Typical increase: 1-2 KB per @Entity class
- Added methods: `__invoke()`, `__signatureFor()`
- Added interface: `EntityReference` implementation

### Runtime Memory

Per-event memory overhead is modest:
- `EventImpl` object: ~80 bytes
- Arguments array: variable (depends on method signature)
- Priority queue entry: ~40 bytes
- Total: ~120 bytes + arguments per scheduled event

For 100,000 events: ~12 MB heap usage (excluding arguments)

Virtual threads have minimal memory footprint compared to platform threads:
- Virtual thread: ~1 KB stack (grows as needed)
- Platform thread: ~1 MB stack (pre-allocated)

## Optimization Guidelines

### For Maximum Event Throughput

1. **Prefer non-blocking operations**: Use `Kronos.sleep()` instead of `Kronos.blockingSleep()` when possible
2. **Minimize event arguments**: Pass primitive types instead of objects when feasible
3. **Batch operations**: Schedule multiple state changes in a single event rather than many tiny events
4. **Use appropriate end time**: Set realistic `controller.setEndTime()` to avoid unnecessary event processing

### For Blocking Operations

1. **Mark methods `@Blocking`**: Enables virtual thread continuations
2. **Use `blockingSleep()` for waits**: Properly suspends virtual thread without blocking OS thread
3. **Leverage channels**: Use `SynchronousQueue` for inter-entity communication
4. **Understand continuation cost**: Blocking events are ~2.6x slower than non-blocking

### For Large Simulations

1. **Monitor heap usage**: Large event queues can consume memory
2. **Consider event pruning**: Remove obsolete events if simulation logic allows
3. **Use appropriate data structures**: ConcurrentHashMap for shared state (if using RealTimeController)
4. **Profile with JFR**: Use Java Flight Recorder to identify hotspots

## Measurement Environment

All benchmarks run on:
- **JVM**: Java 25 (GraalVM)
- **OS**: macOS (Darwin 25.2.0)
- **Hardware**: Apple Silicon M1
- **Build**: Maven 3.9.4
- **Test Framework**: JUnit 5

Results may vary on different hardware and JVM implementations.

## Validation

To run benchmarks yourself:

```bash
cd demo
./mvnw clean test -Dtest=TestMe#benchmarkEventThroughput
./mvnw clean test -Dtest=TestMe#benchmarkContinuationThroughput
```

Output shows events/second for each benchmark.

## Conclusion

Prime Mover provides efficient discrete event simulation with:

1. **True zero-overhead transformation**: Build-time transformation has no runtime cost
2. **Efficient event processing**: ~120K events/sec for non-blocking operations
3. **Effective blocking support**: ~45K events/sec using virtual thread continuations
4. **Minimal memory footprint**: Virtual threads and compact event objects
5. **Scalable architecture**: Single-threaded determinism with multi-threaded option

The "zero-overhead" claim is accurate when properly understood: the bytecode transformation itself adds no runtime cost because it happens at build time. The simulation framework has necessary costs for event management, which are efficiently implemented using modern Java features (virtual threads, native ClassFile API, efficient data structures).
