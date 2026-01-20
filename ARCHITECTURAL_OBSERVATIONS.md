# Architectural Observations Backlog

**Document Version**: 1.0
**Last Updated**: 2026-01-19
**Purpose**: Capture non-critical architectural observations from code reviews for future consideration

This document catalogs architectural improvements identified during comprehensive code reviews that do not block current development but could enhance maintainability, performance, or design consistency. These observations are organized by module and prioritized for future development planning.

---

## Priority Definitions

- **Low**: Nice-to-have improvements with minimal impact
- **Medium**: Improvements that would enhance code quality or maintainability
- **High**: Significant improvements that should be considered for next major version

---

## api Module

### OBS-API-1: Stable Ordinal Assignment Strategy
**Priority**: Medium
**Current State**: Method ordinals determined by alphabetical sort order (name + descriptor)
**Location**: `EntityGenerator.java:61` - `METHOD_ORDER = Comparator.comparing(MethodMetadata::getName).thenComparing(MethodMetadata::getDescriptor)`

**Concern**:
Adding or removing methods changes ordinals for all subsequent methods. This would break serialized events if event queues were ever persisted to disk.

**Technical Context**:
Current implementation:
```java
// Methods assigned ordinals based on sort order:
// 0: action()
// 1: process()
// 2: update()

// Adding newMethod() changes ordinals:
// 0: action()
// 1: newMethod()  // inserted
// 2: process()     // changed from 1
// 3: update()      // changed from 2
```

**Impact If Addressed**:
- Enable event queue serialization/persistence
- Support binary compatibility across versions
- Allow method additions without breaking existing event references

**Suggested Approach**:
- Option A: Hash-based ordinals (hash of method name + descriptor)
- Option B: Explicit `@EventOrdinal(42)` annotation for manual assignment
- Option C: Metadata file tracking ordinals across builds

**Effort Estimate**: 6-8 hours
**Related Issues**: None currently

---

### OBS-API-2: EntityReference Dispatch Could Use Switch Expressions
**Priority**: Low
**Current State**: `__invoke()` uses traditional switch statements
**Location**: Generated `EntityReference` implementations

**Concern**:
Generated code uses Java 17-style switch statements instead of modern switch expressions (Java 21+).

**Current Pattern**:
```java
public Object __invoke(int event, Object[] arguments) {
    switch (event) {
        case 0: return method0(arguments);
        case 1: return method1(arguments);
        default: throw new IllegalArgumentException("Unknown event: " + event);
    }
}
```

**Modern Pattern**:
```java
public Object __invoke(int event, Object[] arguments) {
    return switch (event) {
        case 0 -> method0(arguments);
        case 1 -> method1(arguments);
        default -> throw new IllegalArgumentException("Unknown event: " + event);
    };
}
```

**Impact If Addressed**:
- Cleaner generated code
- Matches project's modern Java 25 usage
- Marginally better performance (compiler optimization)

**Suggested Approach**:
Update `EntityGenerator` to emit switch expressions when generating `__invoke()` bytecode.

**Effort Estimate**: 2-3 hours
**Related Files**: `transform/src/main/java/com/hellblazer/primeMover/classfile/EntityGenerator.java`

---

### OBS-API-3: Kronos API Could Support Duration Objects
**Priority**: Low
**Current State**: All timing methods use `long` milliseconds
**Location**: `api/src/main/java/com/hellblazer/primeMover/api/Kronos.java`

**Concern**:
Modern Java code uses `java.time.Duration` for time spans. Current API requires manual conversion.

**Current Usage**:
```java
Kronos.sleep(5000); // 5 seconds - not obvious
Kronos.blockingSleep(TimeUnit.MINUTES.toMillis(2)); // verbose
```

**Potential Enhancement**:
```java
Kronos.sleep(Duration.ofSeconds(5));
Kronos.blockingSleep(Duration.ofMinutes(2));
```

**Impact If Addressed**:
- More expressive simulation code
- Better alignment with java.time API
- Reduced manual time unit conversions

**Suggested Approach**:
Add overloads accepting `Duration`, keep `long` versions for performance-sensitive code.

**Effort Estimate**: 3-4 hours (API + transformation)
**Related Issues**: None

---

## framework (runtime) Module

### OBS-FRAMEWORK-1: Thread Safety Documentation Inconsistency
**Priority**: Medium
**Current State**: Thread safety documented inconsistently across controllers
**Location**: `SimulationController.java:40-46`, `RealTimeController.java`, `SteppingController.java`

**Concern**:
- `SimulationController` documented as single-threaded but uses `ConcurrentHashMap` for spectrum
- `RealTimeController` mentioned as thread-safe but implementation details not fully documented
- No comprehensive thread safety guide for framework users

**Example Inconsistency**:
```java
// SimulationController.java:44
// "Methods of this class are not thread-safe"
// But line 58:
private final Map<String, Integer> spectrum = new ConcurrentHashMap<>();
```

**Impact If Addressed**:
- Clearer understanding of when synchronization is needed
- Better documentation for users implementing custom controllers
- Reduced risk of concurrency bugs

**Suggested Approach**:
- Add `@ThreadSafe`, `@NotThreadSafe` annotations from `javax.annotation`
- Create comprehensive threading documentation in `framework/README.md`
- Document why spectrum uses `ConcurrentHashMap` (for safe reads during simulation)

**Effort Estimate**: 4-6 hours
**Related Files**: All controller implementations, `Devi.java`

---

### OBS-FRAMEWORK-2: Event Tracking Memory Leak Documentation
**Priority**: Medium
**Current State**: `setTrackEventSources(true)` prevents completed event GC
**Location**: `Controller.java:135-142`, `EventImpl.java:49`

**Concern**:
Current documentation warns about GC implications but doesn't quantify impact or suggest mitigations.

**Current Warning** (Controller.java:135):
```java
/**
 * Note: Enabling this tracking can have significant memory implications for long-running
 * simulations...
 */
```

**Memory Impact Example**:
```
1M events with tracking = 1M EventImpl objects retained
= ~120 bytes/event * 1M = ~120 MB minimum
+ arguments + stack traces = 200-500 MB typical
```

**Impact If Addressed**:
- Users can make informed decisions about tracking
- Clear guidance on when tracking is appropriate
- Potential for bounded tracking or sampling

**Suggested Approach**:
- Document actual memory cost per event
- Add `setTrackEventSourcesBounded(int maxDepth)` option
- Consider weak references for older events
- Add metrics for tracking memory overhead

**Effort Estimate**: 6-8 hours (documentation + bounded tracking implementation)
**Related Issues**: None

---

### OBS-FRAMEWORK-3: Debug Mode Performance Cost Not Quantified
**Priority**: Low
**Current State**: `setDebugEvents(true)` documented as "expensive" but no measurements
**Location**: `Devi.java:122`, `Devi.java:221`

**Concern**:
Documentation states debug mode is expensive but provides no quantitative guidance.

**Current Documentation** (Devi.java:122):
```java
// "This is expensive and significantly impacts performance"
```

**Unknown Metrics**:
- Slowdown factor (2x? 10x? 100x?)
- Memory overhead per event
- When is debug mode acceptable?

**Impact If Addressed**:
- Users can decide when debug mode is acceptable
- Benchmarks provide regression detection
- Could inform optimization priorities

**Suggested Approach**:
- Add benchmark comparing debug on/off
- Document results in `PERFORMANCE.md`
- Consider sampling debug mode (e.g., every 100th event)

**Effort Estimate**: 3-4 hours
**Related Files**: `demo/EventThroughput.java`, `PERFORMANCE.md`

---

### OBS-FRAMEWORK-4: PriorityQueue Could Be Abstracted
**Priority**: Low
**Current State**: All controllers use `java.util.PriorityQueue` directly
**Location**: `SimulationController.java:54`

**Concern**:
Direct dependency on `PriorityQueue` prevents experimenting with alternative implementations (e.g., Fibonacci heap, pairing heap).

**Current Implementation**:
```java
// SimulationController.java:54
private final Queue<EventImpl> eventQueue;
// Constructed as: new PriorityQueue<>()
```

**Potential Enhancement**:
```java
public interface EventQueue extends Queue<EventImpl> {
    // Custom methods if needed
}

class PriorityQueueEventQueue implements EventQueue { ... }
class FibonacciHeapEventQueue implements EventQueue { ... }
```

**Impact If Addressed**:
- Enable performance experiments with different heap implementations
- Better testability (mock event queues)
- Potential performance improvements for large event counts

**Suggested Approach**:
- Create `EventQueue` interface
- Provide factory method: `EventQueue.createDefault()`
- Allow custom implementations via constructor

**Effort Estimate**: 4-5 hours
**Related Issues**: None

---

### OBS-FRAMEWORK-5: Blocking Primitive SPI Not Formalized
**Priority**: Medium
**Current State**: Cross-package access documented but not formalized as extension point
**Location**: `CLAUDE.md:147-159`, `Devi.java`, `EventImpl.java`

**Concern**:
Methods made public for desmoj-ish (post, swapCaller, getContinuation, setTime) are ad-hoc API, not formal SPI.

**Current Documentation** (CLAUDE.md:147):
```
These visibility changes are intentional and allow the desmoj-ish module
to properly manage blocking event continuations...
```

**Missing Formalization**:
- No `BlockingPrimitiveSPI` interface
- No contract documentation
- No examples for third-party implementors

**Impact If Addressed**:
- Clear API for external blocking primitive implementations
- Better documentation for extension points
- Protection of internal APIs via Java modules

**Suggested Approach**:
- Create `BlockingPrimitiveSPI` interface in runtime module
- Document contract with examples
- Use Java module system `exports` to control access
- Create example external blocking primitive

**Effort Estimate**: 8-10 hours
**Related Files**: `Devi.java`, `EventImpl.java`, new `BlockingPrimitiveSPI` interface

---

### OBS-FRAMEWORK-6: Controller Factory Pattern
**Priority**: Low
**Current State**: Controllers created via direct constructors
**Location**: Multiple test files, demo code

**Concern**:
No centralized factory for controller creation. Common setup (time, end time, tracking) repeated across tests.

**Current Pattern**:
```java
var controller = new SimulationController();
controller.setCurrentTime(0);
controller.setEndTime(10000);
controller.setTrackSpectrum(true);
Kronos.setController(controller);
```

**Potential Enhancement**:
```java
var controller = Controllers.simulation()
    .withStartTime(0)
    .withEndTime(10000)
    .trackingSpectrum(true)
    .build();
```

**Impact If Addressed**:
- Reduced boilerplate in test code
- Consistent controller configuration
- Better discoverability of configuration options

**Suggested Approach**:
- Create `Controllers` factory class
- Provide fluent builder API
- Keep direct constructors for advanced use

**Effort Estimate**: 4-5 hours
**Related Files**: New `Controllers.java`, update demo code

---

## transform Module

### OBS-TRANSFORM-1: Bytecode Generation Error Messages
**Priority**: Medium
**Current State**: ClassFile API errors are low-level and hard to interpret
**Location**: `EntityGenerator.java`, transformation exceptions

**Concern**:
When bytecode generation fails, error messages reference JVM internals rather than source-level concepts.

**Example Error**:
```
Error transforming class: java.lang.IllegalArgumentException:
  Invalid stack map frame at offset 42
```

**Better Error**:
```
Error transforming @Entity class com.example.MyEntity:
  Method 'processEvent' at line 45 cannot be transformed.
  Cause: Method contains finally block with non-deterministic control flow.
  Suggestion: Refactor using try-with-resources or extract to helper method.
```

**Impact If Addressed**:
- Faster debugging of transformation issues
- Better user experience
- Reduced support burden

**Suggested Approach**:
- Wrap ClassFile API exceptions with context
- Map bytecode offsets back to source lines (if debug info available)
- Provide actionable suggestions for common errors

**Effort Estimate**: 6-8 hours
**Related Files**: `EntityGenerator.java`, `SimulationTransform.java`

---

### OBS-TRANSFORM-2: Transformation Verification
**Priority**: Medium
**Current State**: No automatic verification that transformed classes maintain semantics
**Location**: Maven plugin, sim-agent

**Concern**:
Transformation bugs could silently change behavior. No verification step ensures transformed code equivalent to original.

**Suggested Verification**:
- Check all original methods have corresponding `$event` methods
- Verify wrapper methods correctly box/unbox arguments
- Validate `EntityReference.__invoke()` covers all ordinals
- Ensure `@Blocking` methods use `postContinuingEvent()`

**Impact If Addressed**:
- Earlier detection of transformation bugs
- Increased confidence in correctness
- Better test failure diagnostics

**Suggested Approach**:
- Create `TransformationVerifier` class
- Run verification after each class transformation
- Optional strict mode for development (fail on warnings)

**Effort Estimate**: 8-10 hours
**Related Files**: New `TransformationVerifier.java`, integrate in `SimulationTransform.java`

---

### OBS-TRANSFORM-3: Transformation Consistency Across Entry Points
**Priority**: Medium
**Current State**: Maven plugin, sim-agent, and IDE plugin each implement transformation independently
**Location**: `primemover-maven-plugin`, `sim-agent`, `primemover-intellij-plugin`

**Concern**:
Three entry points could diverge if not carefully maintained. No checksum verification ensures identical output.

**Current State**:
- Maven plugin: Build-time transformation
- sim-agent: Runtime transformation via -javaagent
- IDE plugin: IDE-integrated transformation

**Risk**:
Bug fix in Maven plugin but not sim-agent → inconsistent behavior depending on how class is loaded.

**Impact If Addressed**:
- Guaranteed consistency across transformation modes
- Easier maintenance (single source of truth)
- Better testing (verify all modes produce identical output)

**Suggested Approach**:
- Extract core transformation logic to shared `transform` module
- Create integration tests comparing Maven plugin vs sim-agent output
- Add checksum to `@Transformed` annotation

**Effort Estimate**: 12-16 hours (refactoring + testing)
**Related Issues**: None

---

## desmoj-ish Module

### OBS-DESMOJ-1: Distribution API Modernization
**Priority**: Low
**Current State**: DESMOJ-style distribution API predates modern Java patterns
**Location**: `desmoj-ish/src/main/java/desmoj/core/dist/`

**Concern**:
Distribution classes use `sample()` method instead of implementing `java.util.random.RandomGenerator`.

**Current API**:
```java
ContDistExponential dist = new ContDistExponential(model, "arrivals", 5.0);
double value = dist.sample();
```

**Modern API**:
```java
RandomGenerator dist = Distributions.exponential(5.0);
double value = dist.nextDouble();
```

**Impact If Addressed**:
- Better integration with Java's random API
- Compatibility with `Random.Sampler` interface
- Stream-based sampling: `dist.doubles().limit(100)`

**Suggested Approach**:
- Create wrapper implementing `RandomGenerator`
- Keep existing API for compatibility
- Add factory methods: `Distributions.exponential()`, etc.

**Effort Estimate**: 6-8 hours
**Related Files**: All distribution classes in `desmoj/core/dist/`

---

### OBS-DESMOJ-2: Resource Statistics JSON Schema
**Priority**: Low
**Current State**: JSON output format not formally specified
**Location**: `desmoj-ish` reporting code

**Concern**:
JSON report format documented by example but no schema. External tools must reverse-engineer structure.

**Current State**:
- JSON generated by toString() methods
- No schema validation
- Breaking changes possible without notice

**Impact If Addressed**:
- Formal contract for report consumers
- Schema validation
- Versioned report format

**Suggested Approach**:
- Create JSON Schema file: `reports/simulation-report-v1.schema.json`
- Add schema validation option
- Version reports with `"schema_version": "1.0"`

**Effort Estimate**: 4-5 hours
**Related Files**: Reporting classes, new schema file

---

### OBS-DESMOJ-3: Queue Statistics Integration with Framework
**Priority**: Medium
**Current State**: desmoj-ish statistics separate from framework statistics
**Location**: `SimulationController` spectrum vs desmoj-ish resource statistics

**Concern**:
Two unintegrated statistics systems:
- Framework: Event spectrum (signature → count)
- desmoj-ish: Resource/queue statistics (utilization, wait times)

**User Impact**:
Users must query both systems separately for complete picture.

**Example**:
```java
// Framework statistics
Map<String, Integer> spectrum = controller.getSpectrum();

// desmoj-ish statistics (separate)
ResourceStatistics stats = resource.getStatistics();
```

**Impact If Addressed**:
- Unified statistics API
- Single report generation
- Better correlation between event counts and resource usage

**Suggested Approach**:
- Create `StatisticsCollector` interface
- Framework controller aggregates all collectors
- Single `getStatistics()` call returns unified view

**Effort Estimate**: 10-12 hours
**Related Files**: `StatisticalController`, desmoj-ish statistics classes

---

## demo Module

### OBS-DEMO-1: Benchmark Organization
**Priority**: Low
**Current State**: Benchmarks in demo module, not separate module
**Location**: `demo/src/test/java/com/hellblazer/primeMover/demo/`

**Concern**:
Benchmarks mixed with demo code. No clear separation of performance tests vs examples.

**Current Structure**:
```
demo/
  src/main/java/  - Demo examples
  src/test/java/  - Benchmarks + tests
```

**Better Structure**:
```
demo/ - Just examples
benchmarks/ - Separate module for JMH benchmarks
```

**Impact If Addressed**:
- Clearer purpose of each module
- Benchmarks can have different dependencies (JMH)
- Better organization for performance tracking

**Suggested Approach**:
- Create separate `benchmarks` module
- Move JMH benchmarks to new module
- Keep simple examples in demo

**Effort Estimate**: 3-4 hours
**Related Files**: New `benchmarks/` module, move files

---

### OBS-DEMO-2: Example Code Documentation
**Priority**: Low
**Current State**: Demo examples lack inline documentation
**Location**: `demo/src/main/java/com/hellblazer/primeMover/demo/`

**Concern**:
Examples are working code but lack explanatory comments for learning.

**Example** (Demo.java):
```java
@Entity
public class Demo {
    public void action() {
        Kronos.sleep(100);
    }
}
```

**Better Example**:
```java
/**
 * Simple demonstration of entity with time advancement.
 * Methods on @Entity classes become simulation events.
 */
@Entity
public class Demo {
    /**
     * Advances simulation time by 100 milliseconds.
     * Non-blocking: control returns immediately.
     */
    public void action() {
        Kronos.sleep(100);
    }
}
```

**Impact If Addressed**:
- Better learning resource
- Clearer usage patterns
- Easier onboarding for new users

**Suggested Approach**:
- Add JavaDoc to all demo classes
- Include usage comments explaining patterns
- Create `demo/README.md` with examples guide

**Effort Estimate**: 4-6 hours
**Related Files**: All demo classes

---

## General / Cross-Cutting

### OBS-GENERAL-1: Module System Adoption
**Priority**: Medium
**Current State**: Modules use classpath, not Java module system fully
**Location**: All modules

**Concern**:
Project uses `module-info.java` but doesn't leverage full module system benefits (exports, requires transitive).

**Current State**:
- module-info.java exists
- Limited use of exports clauses
- No `requires transitive` for API propagation

**Potential Benefits**:
- Stronger encapsulation
- Clearer API boundaries
- Better protection of internal packages

**Impact If Addressed**:
- Protected internal APIs (transform internals)
- Formalized public API surface
- Better compile-time dependency checks

**Suggested Approach**:
- Audit each module-info.java
- Add explicit exports for public API only
- Use `requires transitive` where appropriate
- Mark internal packages with `exports ... to` for testing

**Effort Estimate**: 8-12 hours
**Related Files**: All `module-info.java` files

---

### OBS-GENERAL-2: Logging Standards
**Priority**: Low
**Current State**: Inconsistent logging across modules
**Location**: Various classes

**Concern**:
- Some classes use SLF4J, others don't log
- No guidance on what to log
- No structured logging conventions

**Current Patterns**:
```java
// Some classes:
log.debug("Event posted: {}", event);

// Others: no logging at all
```

**Suggested Standards**:
- TRACE: Method entry/exit
- DEBUG: Event processing details
- INFO: Simulation milestones (start/end)
- WARN: Performance degradation (queue size)
- ERROR: Transformation failures

**Impact If Addressed**:
- Better production observability
- Consistent debugging experience
- Easier troubleshooting

**Suggested Approach**:
- Create `LOGGING_GUIDELINES.md`
- Add logging to key framework classes
- Use structured logging (key=value pairs)

**Effort Estimate**: 6-8 hours
**Related Files**: Framework classes, new guideline document

---

### OBS-GENERAL-3: Test Utility Library
**Priority**: Low
**Current State**: Test utilities duplicated across modules
**Location**: Test code in multiple modules

**Concern**:
Common test patterns (controller setup, entity creation) repeated in each module's tests.

**Example Duplication**:
```java
// Repeated in many test classes:
var controller = new SimulationController();
controller.setCurrentTime(0);
Kronos.setController(controller);
```

**Suggested Approach**:
- Create `test-utilities` module
- Provide: `TestControllers`, `TestEntities`, `TestAssertions`
- Share across all modules

**Impact If Addressed**:
- Reduced test code duplication
- Consistent test setup
- Reusable test utilities for external users

**Effort Estimate**: 6-8 hours
**Related Files**: New `test-utilities` module

---

## Observations by Priority

### High Priority (3)
- OBS-FRAMEWORK-5: Blocking Primitive SPI Not Formalized
- OBS-GENERAL-1: Module System Adoption
- (Consider for next major version)

### Medium Priority (8)
- OBS-API-1: Stable Ordinal Assignment Strategy
- OBS-FRAMEWORK-1: Thread Safety Documentation Inconsistency
- OBS-FRAMEWORK-2: Event Tracking Memory Leak Documentation
- OBS-FRAMEWORK-5: Blocking Primitive SPI Not Formalized
- OBS-TRANSFORM-1: Bytecode Generation Error Messages
- OBS-TRANSFORM-2: Transformation Verification
- OBS-TRANSFORM-3: Transformation Consistency Across Entry Points
- OBS-DESMOJ-3: Queue Statistics Integration with Framework

### Low Priority (10)
- OBS-API-2: EntityReference Dispatch Could Use Switch Expressions
- OBS-API-3: Kronos API Could Support Duration Objects
- OBS-FRAMEWORK-3: Debug Mode Performance Cost Not Quantified
- OBS-FRAMEWORK-4: PriorityQueue Could Be Abstracted
- OBS-FRAMEWORK-6: Controller Factory Pattern
- OBS-DESMOJ-1: Distribution API Modernization
- OBS-DESMOJ-2: Resource Statistics JSON Schema
- OBS-DEMO-1: Benchmark Organization
- OBS-DEMO-2: Example Code Documentation
- OBS-GENERAL-2: Logging Standards
- OBS-GENERAL-3: Test Utility Library

---

## Epic Groupings

### Epic: API Modernization (Java 25 Patterns)
- OBS-API-2: Switch Expressions
- OBS-API-3: Duration Objects
- OBS-DESMOJ-1: Distribution API Modernization

**Total Effort**: 11-15 hours
**Value**: Align with modern Java patterns

### Epic: Testing & Verification
- OBS-TRANSFORM-2: Transformation Verification
- OBS-TRANSFORM-3: Transformation Consistency
- OBS-GENERAL-3: Test Utility Library

**Total Effort**: 26-34 hours
**Value**: Increase confidence in correctness

### Epic: Documentation & Observability
- OBS-FRAMEWORK-1: Thread Safety Documentation
- OBS-FRAMEWORK-2: Event Tracking Memory Leak
- OBS-FRAMEWORK-3: Debug Mode Performance Cost
- OBS-GENERAL-2: Logging Standards

**Total Effort**: 19-26 hours
**Value**: Better production support

### Epic: Extension Points
- OBS-FRAMEWORK-5: Blocking Primitive SPI
- OBS-GENERAL-1: Module System Adoption

**Total Effort**: 16-22 hours
**Value**: Better extensibility

---

## When to Revisit

These observations should be re-evaluated:
1. **Before next major version** (2.0): High and medium priority items
2. **During maintenance cycles**: Low priority items as time permits
3. **When issues arise**: Specific observations may become urgent based on user feedback
4. **Quarterly review**: Re-prioritize based on user needs and project direction

---

## Contributing

To add new observations:
1. Use format: `OBS-<MODULE>-<NUMBER>: <Brief Title>`
2. Include: Priority, Current State, Concern, Impact, Suggested Approach, Effort
3. Link to relevant code locations
4. Update priority summary and epic groupings

**Last Review**: 2026-01-19
**Next Scheduled Review**: Q2 2026
