# Blocking Return Values: Best Practices Guide

## Overview

This document describes the best practices for handling return values in blocking primitives within the Prime Mover simulation framework. Blocking primitives use the `Continuation` class to pass return values from the primitive back to the blocked event when it resumes.

## Return Value Flow

When a blocking primitive needs to return a value to a blocked event, the following flow occurs:

1. **Event Blocks**: Entity calls a `@Blocking` method (e.g., `SimCondition.await()`)
2. **Event Parks**: `EventImpl.park()` creates a `Continuation` and suspends the virtual thread
3. **Primitive Stores Event**: Blocking primitive captures the blocked event via `controller.swapCaller(null)`
4. **Value Available**: When the condition is met (e.g., `signal(value)` is called)
5. **Set Return Value**: Primitive calls `getContinuation().setReturnValue(value)` on the blocked event
6. **Resume Event**: Primitive posts the event back to the controller at the current time
7. **Event Resumes**: `Continuation.resume()` unparks the virtual thread
8. **Value Returned**: The original blocking method call returns the value to the caller

## Continuation Lifecycle

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. CREATE: EventImpl.park() creates new Continuation            │
│    - Sets continuation field                                     │
│    - Thread field is null initially                              │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 2. PARK: Continuation.park() suspends virtual thread            │
│    - Records current thread                                      │
│    - Calls LockSupport.park()                                    │
│    - Thread blocks waiting for resume                            │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 3. STORE: Blocking primitive captures event                     │
│    - Calls swapCaller(null) to get blocked event                │
│    - Stores in wait queue (Deque<EventImpl>)                    │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 4. SET RETURN: Primitive sets return value                      │
│    - Gets continuation: event.getContinuation()                 │
│    - Calls setReturnValue(value) or setReturnState(value, ex)   │
│    - Thread still blocked in LockSupport.park()                  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 5. POST: Primitive reschedules event                            │
│    - Sets event time: event.setTime(currentTime)                │
│    - Posts to controller: controller.post(event)                │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 6. RESUME: EventImpl.proceed() resumes continuation             │
│    - Clears continuation field                                   │
│    - Calls continuation.resume()                                 │
│    - LockSupport.unpark(thread) wakes virtual thread            │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 7. RETURN: park() returns with value                            │
│    - Checks for exception, throws if present                     │
│    - Returns returnValue field                                   │
│    - Clears both fields (returnValue and exception)              │
│    - Clears thread field                                         │
└─────────────────────────────────────────────────────────────────┘
```

## Core Pattern: Direct Return Value Setting

All blocking primitives in the `desmoj-ish` module follow this pattern:

```java
@Blocking
public T blockingMethod() {
    // 1. Capture blocked event
    var waiter = controller.swapCaller(null);

    // 2. Store in wait queue
    waiters.addLast(waiter);

    // 3. Return null (actual value set by Continuation later)
    return null;
}

public void signalMethod(T value) {
    if (!waiters.isEmpty()) {
        // 4. Retrieve waiting event
        var waiter = waiters.removeFirst();

        // 5. Set return value via Continuation
        waiter.getContinuation().setReturnValue(value);

        // 6. Reschedule at current time
        waiter.setTime(controller.getCurrentTime());
        controller.post(waiter);
    }
}
```

### Key Points

1. **Null Check**: `getContinuation()` never returns null for properly blocked events - the continuation is created during `park()`
2. **Value Setting**: Use `setReturnValue(value)` for normal returns, `setReturnState(value, exception)` for exception cases
3. **Timing**: Always set the time before posting: `event.setTime(controller.getCurrentTime())`
4. **Type Safety**: The return value is untyped (`Object`) - type safety relies on generic type parameters

## Exception Handling

When a blocking primitive needs to propagate an exception:

```java
public void signalError(Throwable exception) {
    if (!waiters.isEmpty()) {
        var waiter = waiters.removeFirst();

        // Set exception instead of value
        waiter.getContinuation().setReturnState(null, exception);

        waiter.setTime(controller.getCurrentTime());
        controller.post(waiter);
    }
}
```

The exception is preserved and re-thrown when the continuation resumes:

```java
// In Continuation.park()
final var ex = exception;
if (ex != null) {
    exception = null;  // Clear for reuse
    throw ex;          // Re-throw original exception
}
```

### Exception Preservation Properties

- **Type Preserved**: Original exception type is maintained
- **Cause Preserved**: Stack trace and cause chain are intact
- **Thread-Safe**: Exception field is volatile for visibility
- **One-Time Use**: Exception is cleared after being thrown

## Thread Safety Guarantees

The return value mechanism is inherently thread-safe due to the simulation's single-threaded event processing model:

1. **Serialized Event Processing**: Only one event evaluates at a time (enforced by `Devi.serializer` semaphore)
2. **Volatile Fields**: `Continuation.returnValue` and `Continuation.exception` are volatile for memory visibility
3. **Blocking Operations**: `LockSupport.park()/unpark()` provide memory barriers
4. **Virtual Thread Safety**: Virtual threads use cooperative scheduling but run on platform threads with proper synchronization

### Why No Locking Required

- Blocking primitives only access `getContinuation()` during event processing
- The `Devi.serializer` semaphore ensures only one event modifies state at a time
- The continuation's volatile fields ensure visibility across virtual thread context switches
- `setReturnValue()` is always called before `post()`, establishing happens-before relationship

## Return Value Type Safety

The continuation mechanism uses `Object` for return values, relying on Java generics for type safety:

```java
public class SimCondition<T> {
    @Blocking
    public T await() {
        // Blocking primitive stores event
        var waiter = controller.swapCaller(null);
        waiters.addLast(waiter);
        return null;  // Actual value set by Continuation
    }

    public void signal(T value) {
        var waiter = waiters.removeFirst();
        // Value is typed T here, stored as Object in Continuation
        waiter.getContinuation().setReturnValue(value);
        controller.post(waiter);
    }
}
```

### Type Safety Mechanism

1. **Generic Parameter**: `SimCondition<T>` constrains the type at compile time
2. **Untyped Storage**: `Continuation.returnValue` is `Object`
3. **Safe Cast**: The blocking primitive's return type matches the generic type
4. **Runtime Safety**: Cast happens in the event's virtual thread, where type is known

**Example**: `SimCondition<String>` ensures `await()` returns `String` and `signal(String)` accepts only `String`

## State Validation

Blocking primitives should validate continuation state only when necessary:

### When NOT to Check

```java
// CORRECT: Don't check - getContinuation() is never null for blocked events
var waiter = waiters.removeFirst();
waiter.getContinuation().setReturnValue(value);  // Safe - continuation exists
```

### When TO Check (Advanced Cases)

```java
// ONLY if mixing blocking and non-blocking events in same queue
var event = eventQueue.removeFirst();
var cont = event.getContinuation();
if (cont != null && cont.isParked()) {
    // This event is truly blocked
    cont.setReturnValue(value);
} else {
    // This event is not blocked (shouldn't happen in normal primitives)
}
```

**Best Practice**: Design primitives so only blocked events enter wait queues. This eliminates the need for defensive checks.

## Common Patterns

### Pattern 1: Simple Signal (No Return Value)

```java
// SimSignal.java
@Blocking
public void await() {
    if (pendingSignals > 0) {
        pendingSignals--;
        return;  // Don't block
    }
    var waiter = controller.swapCaller(null);
    waiters.addLast(waiter);
    // Returns void - no return value to set
}

public void signal() {
    if (!waiters.isEmpty()) {
        var waiter = waiters.removeFirst();
        waiter.setTime(controller.getCurrentTime());
        waiter.getContinuation().setReturnValue(null);  // void method - null is correct
        controller.post(waiter);
    }
}
```

### Pattern 2: Value Passing

```java
// SimCondition.java
@Blocking
public T await() {
    if (!pendingValues.isEmpty()) {
        return pendingValues.removeFirst();  // Don't block
    }
    var waiter = controller.swapCaller(null);
    waiters.addLast(waiter);
    return null;  // Will be set by setReturnValue()
}

public void signal(T value) {
    if (!waiters.isEmpty()) {
        var waiter = waiters.removeFirst();
        waiter.setTime(controller.getCurrentTime());
        waiter.getContinuation().setReturnValue(value);  // Pass typed value
        controller.post(waiter);
    }
}
```

### Pattern 3: Resource Acquisition

```java
// Resource.java (uses SimSignal internally)
@Blocking
public ResourceToken acquire(int count) {
    while (available < count) {
        waitQueue.addLast(new Request(count, entryTime));
        waitSignal.await();  // Delegates to SimSignal
    }
    available -= count;
    return new ResourceToken(this, count);  // Return value is NOT set via continuation
}

// Note: Resource uses SimSignal for blocking, so SimSignal handles the continuation
// Resource's own return value (ResourceToken) is computed after resumption
```

### Pattern 4: FIFO Queue with Values

```java
// SynchronousQueueImpl.java
@Blocking
public E take() {
    Node node;
    if (waitList.isEmpty()) {
        addConsumer();  // Stores continuation internally
        return null;
    }
    // ... matching logic ...
    node.consumer.getContinuation().setReturnValue(node.data);
    controller.post(node.consumer);
    return node.data;
}
```

## Anti-Patterns to Avoid

### Anti-Pattern 1: Forgetting to Set Time

```java
// WRONG: Event will execute at old time
var waiter = waiters.removeFirst();
waiter.getContinuation().setReturnValue(value);
controller.post(waiter);  // BUG: time not updated

// CORRECT: Always update time before posting
waiter.setTime(controller.getCurrentTime());
waiter.getContinuation().setReturnValue(value);
controller.post(waiter);
```

### Anti-Pattern 2: Unnecessary Null Checks

```java
// WRONG: Defensive programming that's not needed
var waiter = waiters.removeFirst();
var cont = waiter.getContinuation();
if (cont != null) {  // Always true for blocked events
    cont.setReturnValue(value);
}

// CORRECT: Trust the continuation exists
waiter.getContinuation().setReturnValue(value);
```

### Anti-Pattern 3: Setting Return Value Before Swapping Caller

```java
// WRONG: Continuation doesn't exist yet
var currentEvent = controller.getCurrentEvent();
currentEvent.getContinuation().setReturnValue(value);  // NullPointerException!
controller.swapCaller(null);

// CORRECT: Swap first to get the event that has continuation
var waiter = controller.swapCaller(null);
waiter.getContinuation().setReturnValue(value);
```

### Anti-Pattern 4: Mixing Blocking and Non-Blocking Queue Entries

```java
// PROBLEMATIC: Queue contains both blocked and non-blocked events
eventQueue.add(someEvent);  // May or may not be blocked

// Later...
var event = eventQueue.removeFirst();
event.getContinuation().setReturnValue(value);  // May be null!

// BETTER: Separate queues or ensure only blocked events
waiters.add(blockedEvent);  // All entries are guaranteed blocked
```

## Testing Best Practices

### Test 1: Verify Return Value Propagation

```java
@Test
public void testValuePropagation() throws Exception {
    var controller = new SimulationController();
    var condition = new SimCondition.entity<String>(controller);

    // Waiter blocks and should receive "test-value"
    var waiter = new TestWaiter(condition);
    waiter.scheduleAwait();

    // Signaler sends the value
    controller.advance(100);
    condition.signal("test-value");

    controller.eventLoop();

    // Verify waiter received correct value
    assertEquals("test-value", waiter.getReceivedValue());
}
```

### Test 2: Verify Exception Propagation

```java
@Test
public void testExceptionPropagation() throws Exception {
    var controller = new SimulationController();
    var condition = new SimCondition.entity<String>(controller);

    var waiter = new TestWaiter(condition);
    waiter.scheduleAwait();

    var testException = new IllegalStateException("test error");

    controller.advance(100);
    condition.signalError(testException);

    // Verify exception is propagated
    assertThrows(IllegalStateException.class, () -> {
        controller.eventLoop();
    });
}
```

### Test 3: Verify Timing

```java
@Test
public void testResumeTime() throws Exception {
    var controller = new SimulationController();
    var condition = new SimCondition.entity<Integer>(controller);

    var results = new ArrayList<Long>();

    // Block at time 0
    var waiter = new TestWaiter(condition, results);
    controller.postEvent(0, waiter, AWAIT_EVENT);

    // Signal at time 100
    controller.postEvent(100, signaler, SIGNAL_EVENT, 42);

    controller.eventLoop();

    // Verify waiter resumed at time 100, not 0
    assertEquals(100L, results.get(results.size() - 1));
}
```

### Test 4: Verify State Transitions

```java
@Test
public void testStateTransitions() throws Exception {
    var controller = new SimulationController();
    var signal = new SimSignal.entity(controller);

    // Initially no waiters
    assertEquals(0, signal.waiterCount());

    // Schedule wait at time 0
    controller.postEvent(0, entity, WAIT_EVENT);

    // After time 0, should have 1 waiter
    controller.setEndTime(50);
    controller.eventLoop();
    assertEquals(1, signal.waiterCount());

    // Signal at time 100
    controller.setEndTime(Long.MAX_VALUE);
    controller.postEvent(100, entity, SIGNAL_EVENT);
    controller.eventLoop();

    // After signal, no waiters
    assertEquals(0, signal.waiterCount());
}
```

## Visibility Changes for Cross-Package Access

To support blocking primitives in the `desmoj-ish` module, the following visibility changes were made:

### Devi.java
- `post(EventImpl)`: Changed from `protected` to `public`
- `swapCaller(EventImpl)`: Changed from `protected` to `public`

### EventImpl.java
- `getContinuation()`: Changed from package-private to `public`
- `setTime(long)`: Changed from package-private to `public`

**Rationale**: These methods are part of the Blocking Primitives SPI and must be accessible to primitives in other modules while maintaining encapsulation of internal runtime details.

## Summary

Blocking primitives handle return values through a simple, reliable pattern:

1. **Capture** the blocked event via `swapCaller(null)`
2. **Store** the event in a wait queue
3. **Set** the return value via `getContinuation().setReturnValue(value)`
4. **Update** the event time via `setTime(currentTime)`
5. **Post** the event back to the controller via `post(event)`

The continuation mechanism is thread-safe by design, type-safe through generics, and efficient using virtual thread continuations. No defensive null checks or synchronization are needed in normal blocking primitive implementations.

## Related Documentation

- **BEAD-06**: Visibility changes for blocking primitives SPI
- **BEAD-10**: Threading model and thread-safety guarantees
- **Devi.java**: Controller threading model and serializer semaphore
- **Continuation.java**: Return value storage and virtual thread parking
- **EventImpl.java**: Event lifecycle and continuation management
