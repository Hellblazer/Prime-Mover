# Blocking Primitives Service Provider Interface (SPI)

## Overview

This document defines the **Blocking Primitives SPI** - a set of public methods in the Prime Mover runtime that enable cross-package blocking primitive implementations. These methods allow modules like `desmoj-ish` to implement blocking simulation constructs (signals, conditions, queues, resources) while maintaining proper integration with the core event processing system.

**Module**: `com.hellblazer.primeMover:runtime`
**Package**: `com.hellblazer.primeMover.runtime`
**Since**: 1.0.5
**SPI Version**: 1.0

## Purpose

The Blocking Primitives SPI exists to enable **cross-package integration** between:

- **Core runtime** (`framework` module): Provides event scheduling, continuation management, and time control
- **Blocking primitives** (`desmoj-ish` module): Implements DESMOJ-compatible blocking constructs

Without this SPI, blocking primitives would need to reside in the core runtime module, violating separation of concerns. The SPI allows the `desmoj-ish` module to:

1. Capture blocked events for deferred resumption
2. Schedule continuation events at specific simulation times
3. Set return values for blocked methods
4. Post events to the event queue

## SPI Methods

### 1. `Devi.post(EventImpl event)`

Posts an event to the simulation event queue.

```java
public abstract void post(EventImpl event);
```

**Purpose**: Schedules a captured event for future execution.

**Threading Model**: Must be called from within event processing context (inside an `@Entity` method or blocking primitive implementation). Thread-safety depends on controller subclass:
- `SimulationController`: Single-threaded, no synchronization
- `RealTimeController`: Thread-safe with `ReentrantLock`
- `SteppingController`: Single-threaded, no synchronization

**Usage Pattern**:
```java
// In signal() - post captured waiter to resume
var waiter = waiters.removeFirst();
waiter.setTime(controller.getCurrentTime());
waiter.getContinuation().setReturnValue(result);
controller.post(waiter);  // Waiter resumes when this event processes
```

---

### 2. `Devi.swapCaller(EventImpl newCaller)`

Swaps the current event caller for continuation management.

```java
public EventImpl swapCaller(EventImpl newCaller);
```

**Purpose**: Captures the current caller event so it can be stored and resumed later.

**Parameters**:
- `newCaller`: The new caller to set, or `null` to clear for capture

**Returns**: The previous caller event (for capture/restore pattern)

**Threading Model**: Not inherently thread-safe. Relies on Devi's single-threaded event processing guarantee via the `serializer` semaphore. Only call from event processing context.

**Usage Pattern**:
```java
// In await() - capture caller for later resumption
public void await() {
    var waiter = controller.swapCaller(null);  // Capture caller, returns it
    waiters.addLast(waiter);                   // Store for later signal()
}
```

---

### 3. `EventImpl.getContinuation()`

Returns the continuation associated with a blocked event.

```java
public Continuation getContinuation();
```

**Purpose**: Accesses the continuation state to set return values before resumption.

**Returns**: The `Continuation` object, or `null` if not a continuation event

**Threading Model**: Safe to call from any thread, but value modification should occur within event processing context.

**Usage Pattern**:
```java
// Set return value before posting continuation event
waiter.getContinuation().setReturnValue(result);
controller.post(waiter);
```

---

### 4. `EventImpl.setTime(long time)`

Sets the simulation time for an event.

```java
public void setTime(long time);
```

**Purpose**: Adjusts when a captured event will execute when posted.

**Parameters**:
- `time`: The simulation time at which the event should execute

**Threading Model**: Not thread-safe. Call only from event processing context.

**Usage Pattern**:
```java
// Resume waiter at current simulation time
var waiter = waiters.removeFirst();
waiter.setTime(controller.getCurrentTime());  // Execute now, not at original time
controller.post(waiter);
```

---

### 5. `Continuation.setReturnValue(Object value)`

Sets the return value for a blocked method.

```java
public void setReturnValue(Object returnValue);
```

**Purpose**: Provides the return value that the blocked `@Blocking` method will return when it resumes.

**Parameters**:
- `returnValue`: The value to return from the blocked method

**Threading Model**: Write happens in signaling event; read happens in resuming event. Volatile field ensures visibility.

**Usage Pattern**:
```java
// SimCondition<T>.signal(T value) - pass value to waiter
waiter.getContinuation().setReturnValue(value);
```

## Complete Usage Example

Here is the complete pattern used by `SimSignal` and `SimCondition`:

```java
@Entity
public class SimCondition<T> {
    protected Devi controller;
    private final Deque<EventImpl> waiters = new ArrayDeque<>();

    /**
     * Block until signaled, returning the signaled value.
     * Uses SPI methods: swapCaller
     */
    @Blocking
    public T await() {
        // SPI: Capture current caller event
        var waiter = controller.swapCaller(null);
        waiters.addLast(waiter);
        return null;  // Actual value set by Continuation.setReturnValue()
    }

    /**
     * Signal the first waiter with a value.
     * Uses SPI methods: setTime, getContinuation, post
     */
    public void signal(T value) {
        if (!waiters.isEmpty()) {
            var waiter = waiters.removeFirst();

            // SPI: Set execution time to current simulation time
            waiter.setTime(controller.getCurrentTime());

            // SPI: Set the return value for the blocked await() method
            waiter.getContinuation().setReturnValue(value);

            // SPI: Post event to resume execution
            controller.post(waiter);
        }
    }
}
```

## Threading Model

The SPI respects Devi's **single-threaded event processing model**:

1. **Serializer Semaphore**: Only one event evaluates at a time, enforced by the `serializer` semaphore in `Devi`
2. **Virtual Thread Execution**: Each event executes in its own virtual thread, but serialization ensures no concurrent evaluation
3. **Safe Access Pattern**: SPI methods are safe when called from within event processing context

**Important**: Do not call SPI methods from threads outside the simulation (e.g., external monitoring threads) without proper synchronization.

## Security Considerations

The SPI does not introduce security vulnerabilities:

1. **No Unauthorized Access**: Methods are public but require valid `Devi` and `EventImpl` instances only obtainable within simulation context
2. **No Privilege Escalation**: SPI methods operate within the existing event processing model
3. **No Resource Exhaustion**: Event posting is bounded by simulation logic, not external factors

## Design Rationale

### Why Public Methods?

The visibility changes from `protected`/`package-private` to `public` were necessary because:

1. **Module Separation**: Blocking primitives in `desmoj-ish` cannot access protected methods in `framework`
2. **Single Responsibility**: Core runtime should not contain domain-specific simulation constructs
3. **Extensibility**: Third-party modules can implement custom blocking primitives

### Why Not Use Interfaces?

An interface-based SPI was considered but rejected because:

1. **Tight Coupling**: Blocking primitives need direct access to `EventImpl` and `Continuation` internals
2. **Performance**: Interface indirection adds overhead in high-frequency event processing
3. **Simplicity**: Public methods are straightforward and well-documented

### Why These Specific Methods?

The four core SPI methods represent the **minimal complete API** for blocking primitive implementation:

| Method | Role |
|--------|------|
| `swapCaller` | Capture blocked event |
| `setTime` | Control resume timing |
| `getContinuation` | Access return state |
| `post` | Schedule resumption |

No blocking primitive requires additional runtime access beyond these methods.

## Related Documentation

- **[SPI_STABILITY_CONTRACT.md](SPI_STABILITY_CONTRACT.md)**: Versioning and stability guarantees
- **[README.md](README.md)**: Module overview with visibility notes
- **[ERROR_MESSAGE_STANDARD.md](ERROR_MESSAGE_STANDARD.md)**: Error message formatting
- **[EVENT_TRACKING_PERFORMANCE.md](EVENT_TRACKING_PERFORMANCE.md)**: Performance considerations

## Implementations

The following blocking primitives use this SPI (in `desmoj-ish` module):

| Primitive | Description | SPI Methods Used |
|-----------|-------------|------------------|
| `SimSignal` | Basic condition variable | `swapCaller`, `setTime`, `getContinuation`, `post` |
| `SimCondition<T>` | Condition variable with value passing | All four methods |
| `Bin` | Binary semaphore | All four methods |
| `Resource` | Limited resource pool | All four methods |
| `Stock` | Inventory management | All four methods |

## Version History

| SPI Version | Runtime Version | Changes |
|-------------|-----------------|---------|
| 1.0 | 1.0.5 | Initial SPI formalization |
