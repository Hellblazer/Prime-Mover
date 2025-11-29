# Prime Mover Framework Module (Runtime)

## Overview

The framework module provides the simulation runtime implementation for Prime Mover. It contains the core controllers, event execution engine, and virtual thread-based continuation support.

**Artifact**: `com.hellblazer.primeMover:runtime`

## Purpose

This module implements the **core simulation runtime** that all Prime Mover applications depend on:
1. **Controller Implementations** - Time-driven simulation engines
2. **Event Execution** - Time-ordered event processing via priority queue
3. **Continuation Support** - Virtual thread-based blocking operation handling
4. **Channel Implementation** - Synchronous inter-entity communication
5. **Runtime API** - `Kairos`, the actual implementation of the public `Kronos` API

## Module Scope

The framework module provides the **minimal essential runtime** for discrete event simulation. Optional simulation features (DESMOJ-compatible blocking primitives, distributions, and reporting) are provided by the `desmoj-ish` module, which depends on this core runtime. This separation allows:

- **Lean core**: Applications using only basic Prime Mover features have minimal dependencies
- **Composability**: The `desmoj-ish` module extends the core with DESMOJ-compatible APIs
- **Clear separation of concerns**: Core event execution vs. optional simulation constructs
- **Cross-package integration**: Core runtime exports necessary methods for desmoj-ish blocking primitives

## Key Components

### Controllers (`com.hellblazer.primeMover.controllers`)

Controllers are the heart of the simulation engine. They manage simulation time, process events in time order, and provide statistics.

#### `SimulationController`
The primary implementation for discrete event simulation.

**Features:**
- Time-ordered event processing using a priority queue
- Event statistics tracking (count, timing, dependencies)
- Optional event source tracking (for debugging)
- Optional event logging
- Virtual thread-based event execution

**Usage:**
```java
SimulationController controller = new SimulationController();
Kronos.setController(controller);

// Schedule initial event
entity.someMethod();  // Becomes an event

// Run simulation
controller.eventLoop();  // Process all events until done
```

**Key Methods:**
```java
void eventLoop()                    // Main simulation loop
void eventLoop(long endTime)        // Run until specific time
long getEventCount()                // Query total events
double getEventRate()               // Events per millisecond
Map<String, Long> getEventStats()   // Detailed statistics
```

#### `SteppingController`
A variant that processes one event at a time, useful for debugging and visualization.

**Usage:**
```java
SteppingController controller = new SteppingController();
Kronos.setController(controller);

while (controller.hasEvents() && controller.getCurrentTime() < endTime) {
    controller.processOneEvent();
}
```

#### `RealTimeController`
Executes events in real time, pacing event execution to match wall-clock time.

**Usage:**
```java
RealTimeController controller = new RealTimeController();
Kronos.setController(controller);
controller.eventLoop();  // Executes events synchronized with real time
```

#### `StatisticalController`
Interface for querying simulation statistics. Implemented by other controllers.

**Key Methods:**
```java
long getEventCount()
double getEventRate()
Map<String, Long> getEventStats()
```

### Runtime API (`com.hellblazer.primeMover.runtime`)

#### `Kairos` Class
The actual runtime implementation of the public `Kronos` static API. When bytecode is transformed, all `Kronos.X()` calls are rewritten to `Kairos.X()`.

**Implementation Highlights:**
- Thread-local controller access for virtual thread safety
- Blocking sleep via virtual thread continuations
- Channel creation and management
- Static event invocation

**Key Methods:**
```java
public static void sleep(long duration)
public static void blockingSleep(long duration)
public static <T> SynchronousQueue<T> createChannel(Class<T> type)
public static Controller getController()
public static void setController(Controller c)
// ... (mirrors Kronos API)
```

#### `Devi` Abstract Base Class
Abstract controller that provides virtual thread-based event execution.

**Features:**
- Uses virtual thread executor for event processing
- Handles thread-local controller propagation
- Provides foundation for concrete controller implementations

**Key Methods:**
```java
void postEvent(EntityReference entity, int event, Object... args)
Object postContinuingEvent(EntityReference entity, int event, Object... args)
public void post(EventImpl event)                    // Post an event (public for cross-package use)
public EventImpl swapCaller(EventImpl newCaller)     // Swap caller context (public for cross-package use)
ExecutorService getExecutor()  // Virtual thread executor
```

**Visibility Notes:**
The `post()` and `swapCaller()` methods are public to allow the `desmoj-ish` module to manage blocking event state across package boundaries. This is intentional and supports the architecture where blocking primitives reside in the separate `desmoj-ish` module.

#### `EventImpl` Class
Concrete implementation of the `Event` interface representing a scheduled simulation event.

**Attributes:**
```java
long time              // Simulation time of event
EntityReference entity // Target entity
int event              // Event method identifier
Object[] arguments     // Method arguments
Event source           // Chain to preceding event (optional)
CompletableFuture<?>   // Continuation state for blocking
```

**Key Methods:**
```java
long getTime()
EntityReference getEntity()
int getEvent()
Event getSource()       // For source chain tracking
void execute()          // Run the event
public Continuation getContinuation()  // Get continuation state (public for cross-package use)
public void setTime(long time)         // Set event time (public for cross-package use)
```

**Visibility Notes:**
The `getContinuation()` and `setTime()` methods are public to allow the `desmoj-ish` module to properly manage blocking event continuations. This is intentional and supports the architecture where blocking primitives reside in the separate `desmoj-ish` module while maintaining tight integration with the core runtime.

#### Event Queue Implementation
Standard Java `PriorityQueue` for efficient time-ordered event queue management.

**Characteristics:**
- Uses heap-based priority queue structure
- O(log n) insertion and extraction
- Standard library implementation - well-tested
- Maintains time ordering via `EventImpl` comparisons

**Key Operations:**
```java
queue.add(event)           // Add event to queue
EventImpl next = queue.poll() // Get next event (by time)
queue.size()
queue.isEmpty()
queue.clear()
```

#### `BlockingSleep` Class
Special entity used internally for blocking sleep operations.

**Purpose:**
- Represents the blocking sleep pseudo-entity
- Used for continuation-based blocking in `@Blocking` methods
- Automatically managed by framework

#### `Continuation` Class
Represents the state of a blocked virtual thread continuation.

**Purpose:**
- Captures suspended execution state
- Enables resumption after blocking operation completes
- Works with `CompletableFuture` for synchronization

### Inter-Entity Communication

#### `SynchronousQueue<T>` Implementation
Bounded, synchronous queue for safe communication between entities.

**Characteristics:**
- Both `put()` and `take()` block until operation completes
- No buffering - producer and consumer must synchronize
- Safe for virtual thread blocking

**Usage:**
```java
@Entity
public class Producer {
    void produce() {
        SynchronousQueue<String> channel = Kronos.createChannel(String.class);
        // ... use channel
    }
}
```

### Supporting Classes

#### `Framework` Utility Class
Static utilities for the runtime system.

**Key Responsibilities:**
- Managing controller thread-local state
- Dispatching events to entities
- Handling blocking continuations

#### `UnsafeExecutors` Class
Creates virtual thread executors with proper configuration.

**Purpose:**
- Provides factory for virtual thread `ExecutorService`
- Handles thread naming and configuration
- Ensures proper thread-local propagation

#### `StaticEntityReference` Class
Implementation of `EntityReference` for static methods.

**Purpose:**
- Enables scheduling static method events
- Used by `Kronos.callStatic()`

#### `SimulationEnd` Class
Special pseudo-entity for simulation termination.

**Purpose:**
- Represents the special "end simulation" event
- Triggers controller shutdown at specified time

## Event Execution Flow

```
1. Entity method called:
   bank.openAccount("Alice")

2. Transformation intercepts call and schedules event:
   controller.postEvent(bankRef, 0, "Alice")

3. Event added to priority queue by time

4. SimulationController.eventLoop() runs:
   - Extract next event from priority queue
   - Execute in virtual thread via Devi executor
   - Event methods run with Kairos.getController()
   - If blocking: pause via CompletableFuture
   - Event completes: remove from queue

5. Continue until queue empty or end condition

6. Return from eventLoop()
```

## Blocking Operation Flow

For methods marked with `@Blocking`:

```
1. Method calls Kronos.blockingSleep(100)
   Transformed to: Kairos.blockingSleep(100)

2. Kairos schedules a BlockingSleep event

3. Current event's CompletableFuture suspends execution

4. Virtual thread parks, freeing OS resources

5. Simulation time advances

6. BlockingSleep event completes

7. Continuation resumes the original event

8. Original method continues

9. Method completes normally
```

## Statistics and Debugging

### Event Statistics
```java
controller.setDebugEvents(true);    // Enable expensive debugging
controller.setTrackEventSources(true); // Track event chains
controller.setEventLogger(logger);  // Log all events

// Access statistics
Map<String, Long> stats = controller.getEventStats();
double rate = controller.getEventRate();
```

### Event Source Tracking
When enabled, each event maintains a reference to the event that triggered it. This creates chains useful for debugging:
```
Event5 <- Event4 <- Event3 <- Initial Event
```

**Warning**: Event source tracking prevents garbage collection of completed events - use only during debugging.

## Performance Characteristics

### Time Complexity
- **Event scheduling**: O(log n) via priority queue
- **Event execution**: O(1) per event in queue
- **Total simulation**: O(m log n) where m = event count, n = queue size

### Space Complexity
- **Event queue**: O(n) where n = concurrent pending events
- **Event objects**: Reused efficiently by framework
- **Continuations**: Only active for blocking events

### Throughput
- **Typical**: 50,000+ events per second on modern hardware
- **With debugging**: 10,000-20,000 events per second
- **Scales linearly**: Proportional to CPU count with sufficient entities

## Dependencies

The framework module depends on:
- `com.hellblazer.primeMover:api` - Public contracts
- `org.slf4j:slf4j-api` - Logging
- `ch.qos.logback:logback-classic` - Default logging (optional)

## Virtual Threads Integration

The framework is built on Java 21+ virtual threads (Project Loom):

**Benefits:**
- Efficient blocking: Virtual threads are lightweight
- High concurrency: Can create millions without resource exhaustion
- Compatibility: Works with existing blocking Java APIs
- Transparency: No code changes needed from user perspective

**Requirements:**
- Java 21 or later
- No special VM flags needed
- Compatible with `CompletableFuture` for continuations

## Thread Safety

The framework is designed for single-threaded simulation from user perspective:
- Controllers maintain thread-local controller reference
- Events execute in dedicated virtual threads
- Framework ensures proper synchronization
- User code doesn't need explicit synchronization within events

## See Also

- **api module**: Public API contracts
- **transform module**: Bytecode transformation that creates events
- **demo module**: Usage examples
