# Prime Mover API Module

## Overview

The API module defines the core public interfaces and annotations for the Prime Mover discrete event simulation framework. This module contains zero implementation code - it provides only the contracts that simulation code uses.

**Artifact**: `com.hellblazer.primeMover:api`
**Version**: 1.0.5-SNAPSHOT

## Purpose

This module defines:
1. **Annotations** - Markers that transform regular Java code into simulation entities
2. **Public APIs** - The static simulation kernel interface (`Kronos`) and core runtime interfaces
3. **Contract Definitions** - Core interfaces like `Controller`, `Event`, `EntityReference`, and `SynchronousQueue`

## Key Components

### Annotations (`com.hellblazer.primeMover.annotations`)

#### `@Entity`
Marks a class as a simulation entity. When a class is annotated with `@Entity`, the bytecode transformation process will convert public methods into simulation events.

```java
@Entity
public class Bank {
    public void openAccount(String name) {
        // Method becomes an event
    }
}
```

**Parameters:**
- `value`: Optional array of interfaces that explicitly define which methods should be events. If omitted, all public methods become events.

```java
@Entity({Customer.class})  // Only methods in Customer interface are events
public class Bank implements Customer {
    // ...
}

// For multiple interfaces:
@Entity({Customer.class, Auditable.class})
public class AuditedBank implements Customer, Auditable {
    // Only methods from Customer and Auditable become events
}
```

#### `@Blocking`
Marks an event method as blocking - it will suspend execution until the blocking operation completes. Blocking events enable proper handling of long-running operations using virtual thread continuations.

```java
@Entity
public class Task {
    @Blocking
    public void longRunningTask() {
        Kronos.blockingSleep(1000);  // Blocks until simulation time advances
    }
}
```

#### `@Event` and `@NonEvent`
Explicitly include or exclude methods from event transformation. These provide fine-grained control over which methods become events when `@Entity` is used without an interface parameter.

**`@Event`** - Explicitly marks a method as an event:
- Use on protected/package-private methods that should be events (they get made public)
- Use for clarity when you want to explicitly document that a method is an event
- Takes precedence over interface-based filtering

**`@NonEvent`** - Explicitly excludes a method from being an event:
- Use on public methods that should NOT be scheduled as events
- Useful for utility methods, getters/setters that should execute immediately
- Common for methods called during event execution that shouldn't themselves be events

```java
@Entity
public class Service {
    @Event
    protected void internalEvent() {
        // Protected method explicitly becomes an event (made public)
    }

    @NonEvent
    public void helperMethod() {
        // Public method explicitly NOT an event (executes immediately when called)
    }

    public void normalEvent() {
        // Public method becomes event by default
        helperMethod();  // This executes immediately, not scheduled
    }
}
```

#### `@Transformed`
Applied automatically by the transformation process to mark classes that have been transformed. This annotation serves as a guard against multiple transformations - all transformation entry points (Maven plugin, sim-agent, IntelliJ JPS plugin) check for this annotation and skip classes that already have it. This allows you to safely use multiple transformation tools together (e.g., Maven plugin for CI/CD builds and sim-agent for local development) without risk of double transformation. Not typically used by developers.

#### `@AllMethodsMarker`
Internal marker interface used as the default value for `@Entity` to indicate "all public methods".

### Public API (`com.hellblazer.primeMover.api`)

#### `Kronos` Class
The **static simulation API** that simulation code calls. All methods in this class throw `UnsupportedOperationException` because they are rewritten by the bytecode transformer during compilation.

**Core Methods:**

**Time Management:**
```java
public static void sleep(long duration)              // Non-blocking time advance
public static void blockingSleep(long duration)      // Blocking time advance
public static long currentTime()                      // Query current simulation time
```

**Note**: There is no `advance()` method in Kronos - use `sleep()` instead. The `advance()` method exists only on the Controller interface.

**Simulation Control:**
```java
public static void endSimulation()                    // End at current time
public static void endSimulationAt(long time)         // End at specific time
public static boolean simulationIsRunning()           // Query running state
```

**Controller Access:**
```java
public static Controller getController()              // Get current thread's controller
public static Controller queryController()            // Get controller or null
public static void setController(Controller c)        // Set thread's controller
```

**Event Scheduling:**
```java
public static void run(Runnable r)                    // Schedule at current time
public static void runAt(Runnable r, long instant)    // Schedule at specific time
public static void callStatic(Method m, Object... args)     // Call static event now
public static void callStatic(long t, Method m, Object...) // Call static event later
```

**Inter-Entity Communication:**
```java
public static <T> SynchronousQueue<T> createChannel(Class<T> type)
```

#### `Controller` Interface
The runtime interface for simulation control. Implementations process events, manage simulation time, and provide event statistics.

**Key Methods:**
```java
void advance(long duration)                              // Advance time
void clear()                                             // Reset controller state
void setCurrentTime(long time)                           // Set current time
Event getCurrentEvent()                                  // Get current event
long getCurrentTime()                                    // Get current time
void postEvent(EntityReference e, int event, Object...) // Schedule event now
void postEvent(long time, EntityReference e, int event, Object...) // Schedule at time
Object postContinuingEvent(EntityReference e, int event, Object...) // Blocking event
boolean isDebugEvents()                                  // Query debug status
void setDebugEvents(boolean debug)                      // Enable event debugging
boolean isTrackEventSources()                            // Query tracking status
void setTrackEventSources(boolean track)               // Enable source tracking
void setEventLogger(Logger log)                        // Set event log
```

**Note**: Statistics methods (`getEventCount()`, `getEventRate()`, `getEventStats()`) are provided by the `StatisticalController` interface in the framework module, not this base interface.

#### `Event` Interface
Represents a simulation event. Provides access to timing information, execution state, and event source chains.

**Key Concepts:**
- Events are time-ordered and execute in sequence
- Events have an associated entity and method selector
- Events can have continuation state for blocking operations
- Event sources can be tracked for debugging

#### `EntityReference` Interface
A generated interface that provides type-safe method dispatch for transformed entities. Each transformed entity class implements this interface directly (the entity becomes its own EntityReference).

**Key Methods:**
```java
Object __invoke(int event, Object[] arguments) throws Throwable  // Invoke event by index
String __signatureFor(int event)                                  // Get method signature for event index
```

#### `SynchronousQueue<T>` Interface
A bounded, synchronous queue for inter-entity communication. Both take() and put() operations block until the operation can complete.

## Dependencies

The API module only depends on:
- `org.slf4j:slf4j-api` - For logging interfaces

This minimal dependency set ensures that code using the API remains lightweight.

## Transformation Behavior

When the bytecode transformer processes `@Entity` classes:

1. **Method Rewriting**: Public methods matching the event specification are rewritten to schedule events instead of executing directly
2. **Kronos Replacement**: All calls to `Kronos.X()` are replaced with calls to `Kairos.X()` (the runtime implementation)
3. **EntityReference Implementation**: The entity class implements `EntityReference` directly with `__invoke()` and `__signatureFor()` methods
4. **Bytecode Marking**: The `@Transformed` annotation is added to the class

### Example Transformation

**Original Code:**
```java
@Entity
public class Bank {
    public void openAccount(String name) {
        System.out.println("Opening account: " + name);
        Kronos.sleep(100);
        System.out.println("Account opened");
    }
}
```

**Transformed Code (conceptually):**
```java
@Transformed
@Entity
public class Bank implements EntityReference {

    // Generated dispatch method - invoked by Controller
    public Object __invoke(int event, Object[] arguments) throws Throwable {
        return switch (event) {
            case 0 -> { __event_openAccount((String) arguments[0]); yield null; }
            default -> throw new IllegalArgumentException("Unknown event: " + event);
        };
    }

    // Generated signature method - for debugging
    public String __signatureFor(int event) {
        return switch (event) {
            case 0 -> "openAccount(String)";
            default -> throw new IllegalArgumentException("Unknown event: " + event);
        };
    }

    // Original public method - now schedules event
    public void openAccount(String name) {
        Kairos.getController().postContinuingEvent(this, 0, name);
    }

    // Original method implementation renamed
    public void __event_openAccount(String name) {
        System.out.println("Opening account: " + name);
        Kairos.sleep(100);  // Rewritten from Kronos.sleep
        System.out.println("Account opened");
    }
}
```

**Note**: The entity class becomes its own `EntityReference` implementation - there is no separate generated class.

## Usage Guidelines

### For Simulation Developers

1. **Annotate entity classes**: Mark any class that represents a simulation participant with `@Entity`
2. **Use Kronos API**: Call `Kronos.*` methods for time management - these will be rewritten to the actual runtime
3. **Mark blocking methods**: Use `@Blocking` on methods that call `blockingSleep()`
4. **Set controller**: Before running simulation, set the controller: `Kronos.setController(new SimulationController())`

### For Framework Developers

1. **Never call Kronos directly in non-transformed code**: The static methods throw exceptions
2. **Use Kairos for runtime code**: Framework code calls the actual implementation in the `runtime` module
3. **Respect contracts**: Implement Controller, Event, and EntityReference contracts correctly
4. **Version compatibility**: This is the public contract - changes require major version bumps

## API Stability

The API module defines the public contract of Prime Mover. Changes to this module affect all users:
- **Adding new annotations**: Backward compatible
- **Adding methods to interfaces**: Backward incompatible
- **Removing annotations or interfaces**: Backward incompatible
- **Changing annotation behavior**: Backward incompatible

## See Also

- **transform module**: Implements the bytecode transformation that processes `@Entity` classes
- **framework module**: Provides runtime implementation (`Kairos`, `SimulationController`, etc.)
- **demo module**: Examples of using the API
