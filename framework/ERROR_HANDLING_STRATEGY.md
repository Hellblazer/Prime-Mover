# Prime Mover Error Handling Strategy

**Effective Date**: 2026-01-19
**Applies to**: Prime-Mover 1.0.5+

## Overview

This document defines the comprehensive error handling and recovery strategy for the Prime Mover discrete event simulation framework. It covers the exception taxonomy, recovery patterns, continuation safety, and best practices for handling errors in simulation code.

**Related Documentation:**
- [ERROR_MESSAGE_STANDARD.md](ERROR_MESSAGE_STANDARD.md) - Error message formatting standards
- [EVENT_TRACKING_PERFORMANCE.md](EVENT_TRACKING_PERFORMANCE.md) - Performance impact of debugging features

## Exception Taxonomy

Prime Mover uses a layered exception model that distinguishes between simulation control flow, simulation errors, and system errors.

### Exception Hierarchy

```
Throwable
├── Error
│   └── SimulationEnd             // Simulation control flow (normal termination)
└── Exception
    └── SimulationException       // Simulation-specific errors (wraps user exceptions)
        └── (user exceptions)     // Wrapped: checked and unchecked user exceptions
```

### SimulationEnd (Control Flow)

**Type:** `com.hellblazer.primeMover.runtime.SimulationEnd extends Error`

**Purpose:** Signals normal simulation termination. This is NOT an error - it's a control flow mechanism used to cleanly stop the event loop.

**When Thrown:**
- `Kronos.endSimulation()` is called
- Scheduled end-simulation event fires at specified time
- User code explicitly throws `new SimulationEnd()`

**Handling:**
```java
// Framework handling (Devi.evaluation)
catch (SimulationEnd se) {
    throw se;  // Always propagate - signals normal termination
}
```

**User Code Pattern:**
```java
@Entity
public class MyEntity {
    public void checkTermination() {
        if (shouldStop()) {
            Kronos.endSimulation();  // Cleanly terminates simulation
        }
    }
}
```

**Why Error, Not Exception?**
SimulationEnd extends `Error` rather than `Exception` to:
1. Bypass catch blocks that catch `Exception`
2. Ensure it propagates through user code that doesn't expect it
3. Clearly distinguish simulation control flow from error handling

### SimulationException (Simulation Errors)

**Type:** `com.hellblazer.primeMover.api.SimulationException extends Exception`

**Purpose:** Wraps exceptions that occur during event processing, providing simulation context (time, event signature, entity).

**When Thrown:**
- User code throws any exception during event processing
- Framework detects invalid simulation state
- Continuation failure in blocking operations

**Constructors:**
```java
SimulationException()
SimulationException(String message)
SimulationException(String message, Throwable cause)
SimulationException(Throwable cause)
```

**Handling:**
```java
// Framework wraps user exceptions with context
throw new SimulationException(
    "[Devi] Event evaluation failed for entity " + entityName +
    " at time " + currentTime + ": " + event.getSignature(),
    userException);
```

**User Code Pattern:**
```java
try {
    controller.eventLoop();
} catch (SimulationException e) {
    // Extract root cause for diagnosis
    Throwable cause = e.getCause();
    if (cause instanceof MyBusinessException) {
        // Handle known business error
    }
    // Log simulation context from message
    log.error("Simulation failed: {}", e.getMessage(), e);
}
```

### User Exceptions (Wrapped)

**Types:** Any `Throwable` thrown by user code during event processing

**Handling:**
- Checked exceptions: Wrapped in `SimulationException`
- Unchecked exceptions: Wrapped in `SimulationException`
- Errors (except SimulationEnd): Propagated directly

**Framework Logic (Devi.evaluation):**
```java
// In ExecutionException handler
if (e.getCause() instanceof SimulationEnd se) {
    throw se;  // Propagate control flow
}
if (e.getCause() instanceof SimulationException se) {
    throw se;  // Already wrapped, propagate
}
// Wrap unknown exceptions with simulation context
throw new SimulationException(
    "[Devi] Event evaluation failed for entity " + entityName +
    " at time " + currentTime + ": " + event.getSignature(),
    e.getCause());
```

### Error (JVM-Level Problems)

**Types:** `OutOfMemoryError`, `StackOverflowError`, `NoClassDefFoundError`, etc.

**Handling:** Always propagate immediately. These indicate JVM-level problems that the simulation cannot recover from.

```java
// Errors propagate through the framework
catch (Error e) {
    throw e;  // Cannot handle JVM errors
}
```

## Recovery Patterns

Prime Mover supports four recovery patterns, chosen based on simulation requirements.

### Pattern Selection Guide

Use this decision tree to select the appropriate recovery pattern:

```
                         Error Occurs
                              |
                              v
                   Is data integrity critical?
                         /           \
                       Yes            No
                        |              |
                        v              v
                  Fail-Fast      Is failure transient?
                              /               \
                            Yes               No
                             |                 |
                             v                 v
                          Retry         Is partial data useful?
                                       /               \
                                     Yes               No
                                      |                 |
                                      v                 v
                            Graceful Degradation   Fail-Fast
```

**Decision Factors:**
- **Data integrity critical**: Simulations with strict accuracy requirements (financial, safety-critical)
- **Failure transient**: Temporary issues like network timeouts, resource availability
- **Partial data useful**: Simulations where progress up to failure has value (analytics, trend analysis)

### 1. Fail-Fast (Default)

**Description:** Let exceptions propagate immediately. The simulation stops on first error.

**When to Use:**
- Development and debugging
- Simulations where partial results are meaningless
- When data integrity is critical

**Implementation:**
```java
// Default behavior - no special handling
controller.eventLoop();  // Throws SimulationException on any error
```

**Advantages:**
- Immediate error detection
- Clean stack traces
- Preserves simulation state at error point

### 2. Graceful Degradation

**Description:** Catch, log, and continue with next event. Failed entities are effectively removed from simulation.

**When to Use:**
- Large-scale simulations where some entity failures are acceptable
- When studying system robustness
- Production simulations with partial result value

**Implementation:**
```java
public void eventLoopWithRecovery() throws SimulationException {
    while (getCurrentTime() < endTime) {
        try {
            singleStep();
        } catch (SimulationException e) {
            log.warn("[SimulationController] Event failed, continuing: {}",
                     e.getMessage());
            failedEvents.add(e);  // Track for analysis
            // Continue with next event
        }
    }
}
```

**Risks:**
- Simulation may produce inconsistent results
- Failed entities may leave system in unexpected state
- Downstream events may fail due to missing dependencies

### 3. Retry Pattern

**Description:** Re-schedule failed event after delay for transient failures.

**When to Use:**
- Transient resource contention
- Network timeouts (in real-time simulations)
- Rate-limited operations

**Implementation:**
```java
@Entity
public class RetryableEntity {
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;

    public void processWithRetry() {
        try {
            doWork();
        } catch (TransientException e) {
            if (retryCount < MAX_RETRIES) {
                retryCount++;
                Kronos.sleep(backoffTime());  // Schedule retry
                processWithRetry();
            } else {
                throw new SimulationException("Max retries exceeded", e);
            }
        }
    }

    private long backoffTime() {
        return (long) Math.pow(2, retryCount) * 100;  // Exponential backoff
    }
}
```

**Considerations:**
- Must limit retry attempts to prevent infinite loops
- Backoff strategy prevents thundering herd
- Track retry metrics for analysis

### 4. Abort Pattern

**Description:** Immediately halt simulation and return error to user.

**When to Use:**
- Critical invariant violations
- Unrecoverable resource failures
- When continuing would corrupt results

**Implementation:**
```java
@Entity
public class CriticalEntity {
    public void criticalOperation() {
        if (detectInvariantViolation()) {
            log.error("[CriticalEntity] Invariant violated at time {}",
                      Kronos.currentTime());
            Kronos.endSimulation();  // Clean shutdown
            // Or throw for immediate abort:
            // throw new SimulationException("Critical invariant violated");
        }
    }
}
```

## Continuation Safety

Blocking operations (methods marked `@Blocking`) require special error handling consideration.

### Blocking Event Lifecycle

```
1. Entity calls @Blocking method
2. Method calls Kronos.blockingSleep() or blocking primitive
3. Continuation captured, current event parked
4. Blocking event scheduled
5. Time advances
6. Blocking event completes
7. Continuation resumed, original method continues
8. Original method completes
```

### Exception During Blocking

When exceptions occur in blocking operations:

**In the Blocking Event:**
```java
// If the blocking event throws:
@Blocking
public void waitForResource() {
    var resource = acquireResource();  // Throws ResourceException
}
```

The exception propagates to the continuation, which resumes with the exception:

```java
// Continuation.setReturnState handles exceptions
void setReturnState(Object result, Throwable exception) {
    this.result = result;
    this.exception = exception;
}
```

**In the Original Event After Resume:**
```java
@Entity
public class BlockingEntity {
    @Blocking
    public void safeBlockingCall() {
        try {
            Kronos.blockingSleep(100);
        } catch (Throwable t) {
            // Handle exception from blocking operation
            handleBlockingFailure(t);
        }
    }
}
```

### Continuation Cleanup

Continuations are automatically cleaned up:
- On successful completion
- On exception (exception stored in continuation state)
- Virtual thread handles cleanup via try-finally

```java
// EventImpl.proceed() handles continuation lifecycle
void proceed() {
    final var cont = continuation;
    continuation = null;  // Clear for GC
    cont.resume();        // Resume or throw
}
```

### Best Practices for Blocking Operations

1. **Always handle exceptions in blocking methods:**
```java
@Blocking
public void robustBlockingMethod() {
    try {
        performBlockingOperation();
    } catch (Exception e) {
        // Log and handle - don't let exceptions escape unlogged
        log.error("[MyEntity] Blocking operation failed at time {}",
                  Kronos.currentTime(), e);
        throw e;  // Re-throw if needed
    }
}
```

2. **Use timeouts for blocking primitives:**
```java
@Blocking
public void waitWithTimeout() {
    var resource = queue.poll(1000);  // Timeout prevents deadlock
    if (resource == null) {
        handleTimeout();
    }
}
```

3. **Clean up resources on failure:**
```java
@Blocking
public void acquireAndProcess() {
    Resource resource = null;
    try {
        resource = acquire();
        process(resource);
    } finally {
        if (resource != null) {
            release(resource);
        }
    }
}
```

## When to Catch vs. Propagate

### Always Propagate

| Exception Type | Reason |
|---------------|--------|
| `SimulationEnd` | Control flow signal - never catch in user code |
| `Error` (non-SimulationEnd) | JVM errors cannot be handled |
| `SimulationException` (unknown cause) | Preserves simulation context |

### Safe to Catch

| Exception Type | When to Catch |
|---------------|---------------|
| Known business exceptions | When recovery is possible |
| `IOException` | In I/O-heavy entities, with fallback |
| `InterruptedException` | Must restore interrupt flag |

### Catch-and-Rethrow Pattern

When you need to log or transform an exception:

```java
@Entity
public class LoggingEntity {
    public void processEvent() {
        try {
            doWork();
        } catch (BusinessException e) {
            log.error("[LoggingEntity] Business error at time {}: {}",
                      Kronos.currentTime(), e.getMessage());
            throw e;  // Preserve original type
        }
    }
}
```

## Resource Cleanup on Errors

### Using try-with-resources

```java
@Entity
public class ResourceEntity {
    public void processWithResources() {
        try (var connection = openConnection();
             var stream = openStream(connection)) {
            processData(stream);
        }  // Resources closed even on exception
    }
}
```

### Entity-Level Cleanup

For simulation-wide resources, implement cleanup in entity:

```java
@Entity
public class StatefulEntity implements AutoCloseable {
    private final List<Resource> resources = new ArrayList<>();

    public void allocate() {
        resources.add(acquireResource());
    }

    @Override
    public void close() {
        for (var resource : resources) {
            try {
                resource.release();
            } catch (Exception e) {
                log.warn("[StatefulEntity] Resource cleanup failed", e);
            }
        }
        resources.clear();
    }
}

// In simulation setup
try (var entity = new StatefulEntity()) {
    controller.eventLoop();
}  // Entity cleaned up even on simulation failure
```

### Controller Cleanup

`SimulationController` is `AutoCloseable`:

```java
try (var controller = new SimulationController()) {
    Kronos.setController(controller);
    // Setup and run
    controller.eventLoop();
}  // Controller resources released
```

## Error Context Preservation

### Include Simulation Context

Always include simulation-relevant context in error messages:

```java
throw new SimulationException(
    "[MyEntity] Operation failed at time " + Kronos.currentTime() +
    " in state " + currentState +
    " processing " + currentItem,
    cause);
```

### Event Source Tracking

Enable event source tracking for debugging:

```java
controller.setTrackEventSources(true);

// When error occurs, print event chain
catch (SimulationException e) {
    Event event = controller.getCurrentEvent();
    if (event != null) {
        event.printTrace(System.err);
    }
}
```

**Warning:** Event source tracking prevents garbage collection of completed events. Use only during debugging.

### Debug Events

Enable debug events for source location tracking:

```java
controller.setDebugEvents(true);

// Events now include source location
// Example output: "100 : MyEntity.process(String) @ MyEntity.java:42"
```

## Thread Safety Implications

Prime Mover uses single-threaded event processing:

### Guarantees

- Only one event executes at a time (enforced by `Semaphore`)
- Entity state access is serialized
- No synchronization needed in entity code

### Non-Guarantees

- State shared outside events is NOT protected
- External resources may have concurrent access
- Statistics access during simulation requires care

### Safe Exception Handling Pattern

```java
// Safe - within event processing
@Entity
public class SafeEntity {
    private int state;  // Safe - accessed only from events

    public void processEvent() {
        try {
            state = compute();  // Safe modification
        } catch (Exception e) {
            state = DEFAULT;    // Safe recovery
            throw e;
        }
    }
}
```

## Checklist: Implementing Error Handling

When implementing error handling in simulation code:

- [ ] Use appropriate exception types (see taxonomy)
- [ ] Include simulation context in error messages
- [ ] Handle blocking operation exceptions explicitly
- [ ] Implement resource cleanup via try-with-resources
- [ ] Choose appropriate recovery pattern for simulation requirements
- [ ] Never catch `SimulationEnd` in user code
- [ ] Test error scenarios (see ErrorRecoveryTest)
- [ ] Enable debug features during development

## Example: Complete Error Handling

```java
@Entity
public class RobustEntity implements AutoCloseable {
    private final Logger log = LoggerFactory.getLogger(RobustEntity.class);
    private Resource resource;

    public void initialize() {
        try {
            resource = acquireResource();
        } catch (ResourceException e) {
            log.error("[RobustEntity] Resource acquisition failed at time {}",
                      Kronos.currentTime());
            throw new SimulationException("Initialization failed", e);
        }
    }

    @Blocking
    public void processWithBlocking() {
        try {
            Kronos.blockingSleep(100);
            performWork();
        } catch (TransientException e) {
            // Retry pattern
            log.warn("[RobustEntity] Transient failure, retrying", e);
            Kronos.sleep(50);
            processWithBlocking();
        } catch (FatalException e) {
            // Abort pattern
            log.error("[RobustEntity] Fatal error, ending simulation", e);
            Kronos.endSimulation();
        }
    }

    @Override
    public void close() {
        if (resource != null) {
            try {
                resource.release();
            } catch (Exception e) {
                log.warn("[RobustEntity] Cleanup failed", e);
            }
        }
    }
}
```

## Testing Error Scenarios

See `ErrorRecoveryTest.java` for comprehensive tests covering:
- User exception in event
- SimulationException propagation
- Blocking event with exception
- Controller shutdown on error
- Resource cleanup verification
