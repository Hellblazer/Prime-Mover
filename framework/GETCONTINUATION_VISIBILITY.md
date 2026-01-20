# getContinuation() Visibility Documentation

This document explains why `EventImpl.getContinuation()` is public, what it exposes, and how it should be used. This method is part of the Blocking Primitives SPI and is intended for internal framework use only.

## Why Public?

The `getContinuation()` method was changed from package-private to public to enable **cross-package blocking primitive implementations**.

### The Problem

The `desmoj-ish` module implements DESMOJ-compatible blocking primitives (`SimSignal`, `SimCondition`, `Bin`, `Resource`, etc.) that need to:

1. Capture blocked events for later resumption
2. Set return values before resuming blocked methods
3. Schedule continuation events at specific simulation times

Without public access to `getContinuation()`, these primitives would need to reside in the `com.hellblazer.primeMover.runtime` package, violating module separation.

### The Solution

Making `getContinuation()` public allows code in any package to access continuation state:

```java
// In desmoj-ish module (different package)
package com.hellblazer.primeMover.desmoj;

public class SimCondition<T> {
    public void signal(T value) {
        var waiter = waiters.removeFirst();
        waiter.setTime(controller.getCurrentTime());
        waiter.getContinuation().setReturnValue(value);  // Cross-package access
        controller.post(waiter);
    }
}
```

## What It Exposes

### Return Value

```java
public Continuation getContinuation()
```

Returns the `Continuation` object associated with this event, or `null` if the event is not a continuation event.

### Continuation Object

The `Continuation` class provides:

| Method | Purpose |
|--------|---------|
| `setReturnValue(Object)` | Sets the value returned by the blocked `@Blocking` method |
| `setReturnState(Object, Throwable)` | Sets return value and/or exception for blocked method |
| `isParked()` | Returns `true` if the continuation is currently parked (blocked) |
| `resume()` | Unparks the blocked virtual thread (internal use) |

### State Exposed

- **Return value**: The value that will be returned when the blocked method resumes
- **Exception**: Any exception to be thrown when the blocked method resumes
- **Thread reference**: The parked virtual thread (for `resume()` operation)

## Intended Use

### WHO Should Call This

**Only blocking primitive implementations** should call `getContinuation()`. This includes:

- `SimSignal` and `SimCondition` in `desmoj-ish`
- `Bin`, `Resource`, `Stock` in `desmoj-ish`
- `SynchronousQueueImpl` in `framework` (channels)
- Custom blocking primitives in third-party modules

### WHO Should NOT Call This

- **User simulation code**: Entity methods should never access continuations directly
- **Non-blocking primitives**: Code that doesn't implement blocking semantics
- **External monitoring code**: Threads outside the simulation context

## Security Model

### Not Part of Public User API

Although technically `public`, this method is **internal SPI**:

1. **No User Documentation**: Not mentioned in user-facing guides
2. **SPI Documentation**: Documented only in [BLOCKING_PRIMITIVES_SPI.md](BLOCKING_PRIMITIVES_SPI.md)
3. **Stability Contract**: Covered by [SPI_STABILITY_CONTRACT.md](SPI_STABILITY_CONTRACT.md)

### Access Requirements

Calling `getContinuation()` meaningfully requires:

1. A valid `EventImpl` instance (only obtainable during event processing)
2. An event that is actually a continuation (blocked `@Blocking` method)
3. Controller access to post the event for resumption

These requirements prevent misuse from code outside the simulation context.

## Usage Pattern

### Correct Implementation

```java
@Entity
public class MyBlockingPrimitive {
    private final Devi controller;
    private final Deque<EventImpl> waiters = new ArrayDeque<>();

    /**
     * Block until signaled.
     * @return The signaled value
     */
    @Blocking
    public Object await() {
        // Capture the current event as a waiter
        var waiter = controller.swapCaller(null);
        waiters.addLast(waiter);
        return null;  // Actual value set via getContinuation().setReturnValue()
    }

    /**
     * Signal the first waiter with a value.
     */
    public void signal(Object value) {
        if (!waiters.isEmpty()) {
            var waiter = waiters.removeFirst();

            // Set execution time to current simulation time
            waiter.setTime(controller.getCurrentTime());

            // CORRECT: Set return value via continuation
            var continuation = waiter.getContinuation();
            if (continuation != null) {
                continuation.setReturnValue(value);
            }

            // Post event to resume execution
            controller.post(waiter);
        }
    }
}
```

### Null Check

Always check for `null` before using the continuation:

```java
var continuation = event.getContinuation();
if (continuation != null) {
    continuation.setReturnValue(result);
}
```

A `null` continuation means the event is not a blocking continuation (it's a regular event invocation).

## Future Stability

### Internal API Status

This method is marked as **internal SPI** and is subject to the stability guarantees in [SPI_STABILITY_CONTRACT.md](SPI_STABILITY_CONTRACT.md):

| Tier | Guarantee |
|------|-----------|
| **Tier 1: Stable API** | Binary and source compatibility within major version |

### Potential Future Changes

While stable within major versions, future major releases may:

1. **Add parameters**: e.g., `getContinuation(ContinuationType type)`
2. **Return wrapper**: e.g., `Optional<Continuation>` instead of nullable
3. **Deprecate in favor of interface**: e.g., `BlockingContext.getContinuation()`

Any such changes will follow the deprecation policy documented in the stability contract.

## Related Documentation

- [BLOCKING_PRIMITIVES_SPI.md](BLOCKING_PRIMITIVES_SPI.md) - Complete SPI specification
- [SPI_STABILITY_CONTRACT.md](SPI_STABILITY_CONTRACT.md) - Versioning and stability guarantees
- [CLASSLOADER_HIERARCHY.md](CLASSLOADER_HIERARCHY.md) - Module boundaries and cross-package access
- [README.md](README.md) - Framework overview with visibility change notes

## Summary

| Aspect | Detail |
|--------|--------|
| **Visibility** | `public` (was package-private) |
| **Return Type** | `Continuation` or `null` |
| **Intended Users** | Blocking primitive implementations only |
| **User API** | No - internal SPI |
| **Thread Safety** | Safe to call from event processing context |
| **Stability** | Tier 1: Stable within major version |
