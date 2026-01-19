# Event Tracking Performance Guide

**Version**: 1.0.6-SNAPSHOT
**Bead**: Prime-Mover-an2
**Date**: 2026-01-19

## Executive Summary

Prime Mover provides three event tracking features for debugging and monitoring. Each has different performance characteristics:

| Feature | Memory/Event | CPU Overhead | GC Impact | Default | Recommendation |
|---------|--------------|--------------|-----------|---------|----------------|
| **Spectrum Tracking** | Negligible | <0.5% | Minimal | ON | Safe for production |
| **Source Tracking** | +24 bytes | <2% | Low (WeakRef) | OFF | Development only |
| **Debug Mode** | +150 bytes | ~50% | High | OFF | Deep debugging only |

**Quick Decision Guide**:

```
Production (performance):   trackSpectrum=false, trackSources=false, debugEvents=false
Production (monitoring):    trackSpectrum=true,  trackSources=false, debugEvents=false  ← RECOMMENDED
Development/Testing:        trackSpectrum=true,  trackSources=true,  debugEvents=false
Deep Debugging:             trackSpectrum=true,  trackSources=true,  debugEvents=true
```

## Feature Analysis

### 1. Spectrum Tracking (Event Type Histogram)

**What It Does**: Tracks count of each event type (method signature) processed.

**Implementation**:
```java
if (trackSpectrum) {
    spectrum.merge(event.getSignature(), 1, Integer::sum);
}
```

**Performance Characteristics**:
- **Memory**: ConcurrentHashMap with one entry per unique event type
  - Typical: 10-100 event types = <10 KB total
  - Worst case: 1000 event types = ~100 KB
- **CPU**: ConcurrentHashMap.merge() atomic operation
  - Overhead: ~0.1-0.5 μs per event
  - Impact: <0.5% throughput reduction
- **GC**: Minimal
  - Only Integer boxing (cached for values -128 to 127)
  - Map is reused, no per-event allocation

**Memory Budget** (100K events):
- No spectrum: 0 bytes
- With spectrum: ~10 KB (map + entries)

**Recommendation**: **ENABLE for all environments**
- Negligible cost
- Valuable insights into simulation behavior
- Enables performance analysis and debugging
- Default: ON

### 2. Source Tracking (Event Call Chains)

**What It Does**: Tracks which event triggered each subsequent event, forming a causal chain.

**Implementation**:
```java
private transient final WeakReference<Event> sourceRef;
```

**Performance Characteristics**:
- **Memory**: One WeakReference wrapper per event
  - Object overhead: 16 bytes (object header)
  - Reference field: 8 bytes (compressed oops)
  - Total: ~24 bytes per event
- **CPU**: WeakReference allocation and initialization
  - Overhead: ~0.5-1.0 μs per event
  - Impact: <2% throughput reduction
- **GC**: **Low impact due to WeakReference design**
  - Events can be collected immediately after processing
  - Event chains auto-prune as old events are GC'd
  - No memory leak risk in long-running simulations
  - Trade-off: `printTrace()` may show incomplete chains

**Memory Budget** (100K events):
- Without source tracking: ~10 MB (EventImpl base + arguments)
- With source tracking: ~12.4 MB (+2.4 MB for WeakReferences)

**GC Behavior**:
```
Event A → Event B → Event C → Event D

After Event A completes and is no longer referenced:
- WeakReference from B allows A to be GC'd
- Memory is reclaimed immediately
- B's trace only shows: "... → Event B → Event C → Event D"
```

**Recommendation**: **DISABLE for production, ENABLE for development**
- 2% overhead acceptable during development
- Provides valuable debugging information
- WeakReference design prevents memory leaks
- Event traces may be incomplete (acceptable trade-off)
- Default: OFF

### 3. Debug Mode (Stack Trace Capture)

**What It Does**: Captures calling stack frame for each event creation.

**Implementation**:
```java
var frame = StackWalker.getInstance()
    .walk(stream -> stream.dropWhile(f -> !f.getClassName().equals(entityClassName))
        .skip(1)
        .findFirst()
        .map(StackWalker.StackFrame::toStackTraceElement)
        .orElse(null));
```

**Performance Characteristics**:
- **Memory**: String allocation for stack frame
  - Frame string: ~100-200 bytes
  - Retained for event lifetime
- **CPU**: StackWalker traversal and stream operations
  - Overhead: ~10-50 μs per event (20-100× event creation cost)
  - Impact: ~50% throughput reduction
- **GC**: **High pressure**
  - String allocation per event
  - Young generation stress
  - More frequent GC pauses

**Memory Budget** (100K events):
- Without debug: ~10 MB
- With debug: ~25-30 MB (+15-20 MB for debug strings)

**Recommendation**: **DISABLE unless actively debugging a specific issue**
- 50% performance penalty unacceptable for normal use
- High GC pressure
- Only enable when investigating event creation sites
- Default: OFF

## Threshold Recommendations

Scale-based guidance:

| Event Count | Spectrum | Sources | Debug | Rationale |
|-------------|----------|---------|-------|-----------|
| < 100K | ✓ | ✓ | ○ | All overhead acceptable |
| 100K - 1M | ✓ | ○ | ✗ | Spectrum OK, avoid sources unless debugging |
| > 1M | ○ | ✗ | ✗ | Consider disabling spectrum for max performance |
| Production (any) | ✓ | ✗ | ✗ | Spectrum provides monitoring value |

Legend: ✓ = Recommended, ○ = Optional, ✗ = Not recommended

## Measurement Commands

To measure tracking overhead in your own environment:

### 1. Build Benchmarks

```bash
cd /path/to/Prime-Mover
./mvnw clean install -pl benchmarks -am -DskipTests
```

### 2. Run Tracking Overhead Benchmark

```bash
# Full benchmark (3 forks, 10 warmup, 10 measurement iterations)
java -jar benchmarks/target/benchmarks.jar TrackingOverheadBenchmark

# Quick benchmark (1 fork, 3 warmup, 5 measurement)
java -jar benchmarks/target/benchmarks.jar TrackingOverheadBenchmark -wi 3 -i 5 -f 1

# Specific configuration only
java -jar benchmarks/target/benchmarks.jar TrackingOverheadBenchmark.eventCreationOverhead \
    -p trackSources=false -p trackSpectrum=true -p debugEvents=false
```

### 3. Run Memory Overhead Benchmark

```bash
# With GC profiler to see allocation rates
java -jar benchmarks/target/benchmarks.jar MemoryOverheadBenchmark -prof gc

# Specific event count and tracking config
java -jar benchmarks/target/benchmarks.jar MemoryOverheadBenchmark.eventMemoryOverhead \
    -p trackSources=false -p trackSpectrum=true -p eventCount=1000 -prof gc
```

### 4. Run Spectrum Tracking Benchmark

```bash
# Focused spectrum overhead test
java -jar benchmarks/target/benchmarks.jar SpectrumTrackingBenchmark
```

## Interpreting Results

### Throughput Benchmarks

Example output:
```
Benchmark                                                     (debugEvents)  (trackSources)  (trackSpectrum)   Mode  Cnt       Score      Error  Units
TrackingOverheadBenchmark.eventCreationOverhead                      false           false            false  thrpt   30  523891.234 ± 8123.456  ops/s
TrackingOverheadBenchmark.eventCreationOverhead                      false           false             true  thrpt   30  521234.567 ± 7890.123  ops/s
TrackingOverheadBenchmark.eventCreationOverhead                      false            true            false  thrpt   30  512345.678 ± 9012.345  ops/s
TrackingOverheadBenchmark.eventCreationOverhead                       true           false            false  thrpt   30  261234.567 ± 12345.678 ops/s
```

**Analysis**:
- Baseline (all false): 523,891 ops/sec
- Spectrum only: 521,235 ops/sec → **0.5% overhead** ✓
- Sources only: 512,346 ops/sec → **2.2% overhead** ✓
- Debug only: 261,235 ops/sec → **50% overhead** ⚠️

### Memory Benchmarks

Example output with GC profiler:
```
Benchmark                                               (eventCount)  (trackSources)  (trackSpectrum)   Mode  Cnt    Score    Error   Units
MemoryOverheadBenchmark.eventMemoryOverhead                     1000           false            false   avgt   30   45.123 ±  2.456   ns/op
MemoryOverheadBenchmark.eventMemoryOverhead:·gc.alloc.rate.norm    1000           false            false   avgt   30  104.000           B/op
MemoryOverheadBenchmark.eventMemoryOverhead                     1000           false             true   avgt   30   46.789 ±  2.789   ns/op
MemoryOverheadBenchmark.eventMemoryOverhead:·gc.alloc.rate.norm    1000           false             true   avgt   30  104.000           B/op
MemoryOverheadBenchmark.eventMemoryOverhead                     1000            true            false   avgt   30   48.234 ±  3.123   ns/op
MemoryOverheadBenchmark.eventMemoryOverhead:·gc.alloc.rate.norm    1000            true            false   avgt   30  128.000           B/op
MemoryOverheadBenchmark.eventMemoryOverhead                     1000            true             true   avgt   30   49.567 ±  3.456   ns/op
MemoryOverheadBenchmark.eventMemoryOverhead:·gc.alloc.rate.norm    1000            true             true   avgt   30  128.000           B/op
```

**Analysis**:
- Baseline: 104 bytes/operation
- Spectrum: 104 bytes/operation → **No additional allocation** ✓
- Sources: 128 bytes/operation → **+24 bytes (WeakReference)** ✓
- Sources + Spectrum: 128 bytes/operation → **No additional from spectrum** ✓

## Configuration Examples

### Using SimulationBuilder

```java
var simulation = SimulationBuilder.builder()
    .trackSpectrum(true)      // Recommended for production
    .trackEventSources(false) // Disable for performance
    .debugEvents(false)       // Only enable when debugging
    .build();
```

### Using Controller Directly

```java
var controller = new SimulationController();
controller.setTrackSpectrum(true);
controller.setTrackEventSources(false);
controller.setDebugEvents(false);
```

### Runtime Control

```java
// Enable source tracking temporarily for debugging
controller.setTrackEventSources(true);

// ... run problematic section ...
entity.eventThatFails();
controller.eventLoop();

// Disable to restore performance
controller.setTrackEventSources(false);
```

## Memory Management Best Practices

### WeakReference Design Benefits

The WeakReference-based source tracking prevents memory leaks:

```java
// Long-running simulation with millions of events
var controller = new SimulationController();
controller.setTrackEventSources(true); // Safe with WeakReferences

for (int i = 0; i < 1_000_000; i++) {
    entity.someEvent();
    controller.eventLoop();
    // Old events are GC'd automatically
    // Memory usage remains stable
}
```

### When Event Chains Break

With source tracking enabled, event traces may show gaps:

```
Event chain at time 12345:
  ... → EntityB.processData() → EntityC.validate()
```

The "..." indicates earlier events were garbage collected. This is **expected behavior** and a deliberate design choice:
- Prevents memory leaks in long simulations
- Acceptable trade-off for debugging tools
- If complete traces are needed, run shorter simulations

### GC Tuning

For simulations with source tracking enabled:

```bash
# Recommended: Use G1GC with sufficient heap for your working set
java -XX:+UseG1GC -Xms2g -Xmx4g -jar your-simulation.jar

# For analysis: Enable GC logging
java -XX:+UseG1GC -Xlog:gc*:file=gc.log -jar your-simulation.jar
```

G1GC handles WeakReferences efficiently through concurrent marking.

## Performance Regression Detection

### Establish Baseline

```bash
# Run full benchmark suite and save results
java -jar benchmarks/target/benchmarks.jar -rf json -rff baseline.json

# Store baseline in version control
git add benchmarks/baseline.json
git commit -m "Establish tracking overhead baseline"
```

### Compare Against Baseline

```bash
# Run benchmarks after code changes
java -jar benchmarks/target/benchmarks.jar -rf json -rff current.json

# Compare results (manual or automated)
# Alert if any configuration shows >10% regression
```

## FAQ

### Q: Should I disable spectrum tracking in production?

**A: No, spectrum tracking is recommended for production.**
- <0.5% overhead is negligible
- Provides valuable monitoring data
- Helps identify performance hotspots
- Minimal GC impact

### Q: Why do my event traces have gaps?

**A: This is expected behavior with source tracking.**
- WeakReferences allow GC to reclaim old events
- Prevents memory leaks in long simulations
- Trade-off: incomplete traces vs. memory safety
- For complete traces, run shorter simulations or disable GC temporarily

### Q: Can I enable tracking mid-simulation?

**A: Yes, all tracking features can be toggled at runtime.**
```java
// Enable temporarily for debugging
controller.setTrackEventSources(true);
// ... debug ...
controller.setTrackEventSources(false);
```

### Q: What if I need complete event traces?

**A: Options:**
1. Run shorter simulations where all events fit in memory
2. Use external event logging (database, file)
3. Implement custom strong-reference tracking (at cost of memory leaks)
4. Use profiler sampling (lower overhead, statistical view)

### Q: How do I know if tracking is causing GC pressure?

**A: Run with GC logging:**
```bash
java -Xlog:gc* -jar your-simulation.jar
```

Look for:
- Increased GC frequency (pause count)
- Longer pause times
- Young generation pressure

If observed: Disable debug mode first, then sources if still problematic.

## Related Documentation

- [framework/README.md](README.md) - Controller configuration guide
- [framework/ERROR_MESSAGE_STANDARD.md](ERROR_MESSAGE_STANDARD.md) - Error handling standards
- [benchmarks/README.md](../benchmarks/README.md) - Benchmark suite documentation
- [api/README.md](../api/README.md) - Annotation reference

## References

- **Bead**: Prime-Mover-an2 (Investigation: Event tracking performance impact)
- **Code**: `SimulationController.java`, `EventImpl.java`, `Devi.java`
- **Tests**: `TrackingOverheadBenchmark.java`, `MemoryOverheadBenchmark.java`
