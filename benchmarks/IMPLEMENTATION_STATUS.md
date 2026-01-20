# Prime Mover JMH Benchmarks - Implementation Status

**Created**: 2026-01-19
**Bead**: Prime-Mover-9nv
**Status**: Module Created - Pending Compilation Fixes

## Overview

Created comprehensive JMH benchmark suite for Prime Mover event-driven simulation framework. Module structure is complete with all benchmark classes, entities, documentation, and runner scripts in place.

## Completed Work

### Module Structure
- ✅ Created `/benchmarks/pom.xml` with JMH dependencies (v1.37)
- ✅ Configured maven-shade-plugin for uber JAR generation
- ✅ Configured primemover-maven-plugin for @Entity transformation
- ✅ Added benchmarks module to parent pom.xml

### Benchmark Entities
- ✅ Created `BenchmarkEntity.java` with event methods:
  - Non-blocking events (no payload, primitive, multiple primitives, typical payload, heavy payload)
  - Blocking events (no payload, with return, with param and return, typical payload)

### Benchmark Classes
- ✅ `EventThroughputBenchmark.java` - Measures events/second with varying payloads
- ✅ `ContinuationThroughputBenchmark.java` - Measures blocking event throughput
- ✅ `TrackingOverheadBenchmark.java` - Measures overhead of tracking features
- ✅ `SpectrumTrackingBenchmark.java` - Focused spectrum tracking comparison
- ✅ `MemoryOverheadBenchmark.java` - Per-event memory allocation

### Documentation
- ✅ Created comprehensive `README.md` with:
  - Build and run instructions
  - Benchmark details and configurations
  - JMH options guide
  - Profiling instructions
  - Baseline performance targets
  - Regression detection strategy

### Scripts
- ✅ Created `run-benchmarks.sh` convenience script with:
  - Selective benchmark execution
  - Profiler integration
  - JSON result export
  - Help documentation

## Pending Issues

### Package Structure
**Issue**: Benchmarks placed in `com.hellblazer.primeMover.runtime` package to access package-private `Framework.setController()` method.

**Impact**: This is the correct approach as `Framework.setController()` is intentionally package-private for internal use.

### Compilation Status
**Blocking**: Build currently fails due to test compilation errors in framework/transform modules (unrelated to benchmarks).

**Root Cause**: Uncommitted test files with compilation errors block the reactor build before reaching benchmarks module.

**Resolution Path**:
1. Remove problematic test files:
   - `/framework/src/test/java/com/hellblazer/primeMover/runtime/DeviExceptionHandlingTest.java`
   - `/framework/src/test/java/com/hellblazer/primeMover/runtime/ContinuationEdgeCasesTest.java`
   - `/transform/src/test/java/com/hellblazer/primeMover/classfile/OrdinalStabilityTest.java`
2. Run: `./mvnw clean install -pl '!maven-testing,!primemover-intellij-plugin' -DskipTests`
3. Verify benchmarks jar created: `benchmarks/target/benchmarks.jar`

## Benchmark Configuration

### JMH Settings
- **Mode**: Throughput (ops/sec) for most benchmarks, AverageTime/SampleTime for memory
- **Warmup**: 10 iterations × 1 second
- **Measurement**: 10 iterations × 1 second
- **Forks**: 3
- **JVM Args**: `-Xms2g -Xmx2g`

### Parameterized Tests
- **TrackingOverheadBenchmark**: 2³ = 8 configurations (trackSources × trackSpectrum × debugEvents)
- **MemoryOverheadBenchmark**: Event counts (1, 10, 100, 1000)

## Expected Baseline Performance

| Benchmark | Target | Notes |
|-----------|--------|-------|
| Event throughput (no payload) | >500K ops/sec | Baseline event creation |
| Event throughput (typical payload) | >300K ops/sec | 10-object payload |
| Continuation throughput | >100K ops/sec | Virtual thread overhead |
| Tracking overhead (spectrum) | <1% | ConcurrentHashMap merge |
| Tracking overhead (sources) | <5% | GC pressure acceptable |
| Memory per event | <200 bytes | Without tracking |
| Memory per blocking event | <300 bytes | Including continuation |

## Next Steps

1. **Fix Build**: Remove problematic test files or fix compilation errors
2. **Verify Build**: Confirm benchmarks jar is generated successfully
3. **Run Baseline**: Execute `./benchmarks/run-benchmarks.sh` to establish baseline
4. **Export Results**: `java -jar benchmarks/target/benchmarks.jar -rf json -rff baseline.json`
5. **Document Results**: Add actual measurements to README.md
6. **Regression Detection**: Set up CI integration for performance monitoring
7. **Code Review**: Spawn code-review-expert before final commit

## Files Created

```
benchmarks/
├── pom.xml
├── README.md
├── IMPLEMENTATION_STATUS.md
├── run-benchmarks.sh
└── src/main/java/com/hellblazer/primeMover/runtime/
    ├── entities/
    │   └── BenchmarkEntity.java
    ├── EventThroughputBenchmark.java
    ├── ContinuationThroughputBenchmark.java
    ├── TrackingOverheadBenchmark.java
    ├── SpectrumTrackingBenchmark.java
    └── MemoryOverheadBenchmark.java
```

## Technical Notes

### Why Package-Private Access?
The `Framework.setController()` method is package-private by design to prevent external code from manipulating the simulation controller. Benchmarks need this access to set up test scenarios, so they correctly live in the `com.hellblazer.primeMover.runtime` package alongside Framework.

### Exception Handling
All JMH @Benchmark methods declare `throws Exception` to handle:
- `SimulationException` from controller.eventLoop()
- `Exception` from controller.close()
- Any checked exceptions from blocking event invocations

This is standard JMH practice and doesn't affect benchmark accuracy.

### Transformation
BenchmarkEntity is transformed at build time by primemover-maven-plugin (phase: process-classes). This converts @Entity methods into event-based invocations.

---

**Status**: Ready for compilation fix and baseline run.
**Effort**: ~90% complete (structure + code done, pending build fixes)
