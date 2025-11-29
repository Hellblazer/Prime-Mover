# Prime Mover Transform Module

## Overview

The transform module implements the bytecode transformation engine for Prime Mover. It uses Java 25's ClassFile API to transform `@Entity` classes into discrete event simulation entities at compile or runtime.

**Artifact**: `com.hellblazer.primeMover:transform`

## Purpose

This module:
1. **Scans** compiled classes for `@Entity` annotations
2. **Analyzes** entity structure and identifies event methods
3. **Transforms** bytecode to convert method calls into event scheduling
4. **Generates** entity dispatch interfaces for type-safe event invocation
5. **Rewrites** `Kronos` calls to `Kairos` runtime implementation

## Architecture

### Transformation Pipeline

```
Raw Bytecode
    |
    v
ClassScanner - Finds @Entity classes and metadata
    |
    v
ClassMetadata - Analyzes class structure and annotations
    |
    v
EntityGenerator - Generates EntityReference implementation
    |
    v
SimulationTransform - Rewrites method calls and Kronos references
    |
    v
Transformed Bytecode
```

### Key Components

#### `ClassScanner`
Scans compiled class directories for `@Entity` classes and builds metadata about them.

**Responsibilities:**
- Directory traversal
- Class file identification
- Metadata extraction
- Batch processing coordination

**Key Methods:**
```java
Map<ClassDesc, ClassMetadata> scan(Path classDir)
```

#### `ClassMetadata`
Represents the structure of an entity class - what will be transformed.

**Captured Information:**
```java
ClassDesc classDescriptor        // Class name and type info
List<MethodMetadata> methods     // All methods in class
List<AnnotationMetadata> annotations // Class-level annotations
MethodMetadata[] eventMethods    // Subset: which become events
```

#### `MethodMetadata`
Represents a single method in an entity class.

**Captured Information:**
```java
MethodTypeDesc signature         // Return type and parameters
String name                      // Method name
int modifiers                    // public, static, etc.
List<AnnotationMetadata> annotations // Method-level annotations
boolean isBlocking               // Has @Blocking annotation
```

#### `AnnotationMetadata`
Represents an annotation on a class or method.

**Used For:**
- `@Entity` - Identifies simulation entities
- `@Blocking` - Marks blocking event methods
- `@Event` - Explicitly includes methods as events
- `@NonEvent` - Explicitly excludes methods from events

#### `TypeMetadata`
Represents type information (classes, interfaces, primitives).

**Usage:**
- Type hierarchy analysis
- Parameter type information
- Return type validation

#### `ParameterMetadata`
Represents a method parameter.

**Contains:**
```java
ClassDesc type                  // Parameter type
String name                     // Parameter name
int index                       // Position in signature
```

#### `EntityGenerator`
Generates bytecode for the entity dispatch interface.

**Generation Process:**
1. Creates new interface extending `EntityReference`
2. Implements single `invoke(int eventIndex, Object[] args)` method
3. Uses switch statement to dispatch by event index
4. Generates efficient event invocation for each method

**Generated Code Pattern:**
```java
public interface BankEntityReference extends EntityReference {
    @Override
    Object invoke(int eventIndex, Object[] args) {
        return switch (eventIndex) {
            case 0 -> entity.__event_openAccount((String) args[0]);
            case 1 -> entity.__event_closeAccount((String) args[0]);
            default -> throw new IllegalArgumentException("Unknown event: " + eventIndex);
        };
    }
}
```

**Key Methods:**
```java
byte[] generateEntityReference(ClassMetadata metadata)
```

#### `SimulationTransform`
The main transformation orchestrator - rewrites bytecode to create simulation events.

**Transformation Steps:**

1. **Identify Event Methods**
   - Analyze `@Entity` annotation parameters
   - Check for `@Event` / `@NonEvent` annotations
   - Build event index map

2. **Rename Original Methods**
   - Original methods renamed to `__event_<original>`
   - Allows original code to run when events execute

3. **Create Event Dispatch Methods**
   - New public methods with original signatures
   - Instead of executing code directly, they schedule events
   - Call `controller.postEvent(entityRef, eventIndex, args...)`

4. **Rewrite Kronos Calls**
   - All `Kronos.X()` -> `Kairos.X()`
   - Enables runtime implementation replacement

5. **Rewrite Method Calls**
   - Within entities, method calls become event posts
   - Preserves control flow semantics while making it event-driven

6. **Generate EntityReference**
   - Creates dispatch interface for event invocation
   - Stores reference in entity instance

7. **Add Metadata**
   - Add `@Transformed` annotation
   - Store event index mapping

**Key Methods:**
```java
byte[] transformClass(byte[] classBytes, ClassMetadata metadata)
```

#### `ClassRemapper`
Handles bytecode-level class name remapping during transformation.

**Purpose:**
- Implements dynamic class name remapping
- Used for API class substitution
- Enables version compatibility

#### `IdentitySet` and `OpenAddressingSet`
Custom hash set implementations for performance.

**Purpose:**
- Avoid dependencies on Collections APIs in transform code
- High-performance metadata tracking
- Memory-efficient set operations

## Usage Patterns

### Build-Time Transformation (Maven)

The `primemover-maven-plugin` uses this module:

```xml
<plugin>
    <groupId>com.hellblazer.primeMover</groupId>
    <artifactId>primemover-maven-plugin</artifactId>
    <version>${project.version}</version>
    <executions>
        <execution>
            <phase>process-classes</phase>
            <goals>
                <goal>transform</goal>
                <goal>transform-test</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

The plugin:
1. Runs after javac compilation
2. Scans target/classes directory
3. Transforms each `@Entity` class
4. Overwrites class files with transformed versions
5. Generates EntityReference classes

### Runtime Transformation (Java Agent)

The `sim-agent` module uses this for runtime transformation:

```bash
java -javaagent:sim-agent.jar MySimulation
```

The agent:
1. Intercepts class loading
2. Identifies `@Entity` classes
3. Transforms before class definition
4. Zero bytecode caching overhead

## Transformation Example

### Original Code
```java
@Entity
public class Bank {
    private Queue<String> queue = new LinkedList<>();

    public void openAccount(String name) {
        System.out.println("Opening: " + name);
        Kronos.sleep(50);
        System.out.println("Opened: " + name);
    }

    public void printQueue() {
        System.out.println("Queue: " + queue);
    }
}
```

### Conceptual Transformed Code
```java
@Transformed
@Entity
public class Bank {
    private Queue<String> queue = new LinkedList<>();
    private EntityReference __entityRef;  // Generated

    // Original method renamed - this is what event executes
    public void __event_openAccount(String name) {
        System.out.println("Opening: " + name);
        Kairos.sleep(50);  // Kronos rewritten to Kairos
        System.out.println("Opened: " + name);
    }

    // New dispatch method - what users call
    public void openAccount(String name) {
        // Schedule event instead of executing directly
        Controller ctrl = Kairos.getController();
        ctrl.postEvent(__entityRef, 0, name);  // Event 0 = openAccount
    }

    // printQueue wasn't transformed (not public? or excluded?)
    public void printQueue() {
        System.out.println("Queue: " + queue);
    }
}
```

### Generated EntityReference
```java
@FunctionalInterface
public interface Bank__EntityRef extends EntityReference {
    @Override
    Object invoke(int eventIndex, Object[] args) {
        return switch (eventIndex) {
            case 0 -> bank.__event_openAccount((String) args[0]);
            default -> throw new IllegalArgumentException("Unknown event: " + eventIndex);
        };
    }
}
```

## Decision Points in Transformation

### What Becomes an Event?

By default (no interface specified):
- All public methods (except constructors, synthetic, etc.)
- Can exclude with `@NonEvent`
- Can explicitly include non-public with `@Event` (gets made public)

With interface specified:
- Only methods declared in interface
- Interface must be accessible to entity class

### Blocking Behavior

Methods marked `@Blocking`:
1. Must call `Kronos.blockingSleep()` or similar
2. Execution suspends via virtual thread continuation
3. Framework resumes after blocking operation

Non-blocking methods:
1. Execute synchronously
2. Cannot call blocking APIs
3. Must return immediately for event ordering

## ClassFile API Integration

The transform module uses Java 25's ClassFile API (JEP 484):

**Benefits:**
- Type-safe bytecode manipulation
- No external ASM dependency
- Modern, lambda-based transformations
- Better error handling and debugging

**Key Classes Used:**
- `ClassFile` - Main API entry point
- `ClassModel` - Parsed bytecode representation
- `ClassTransform` - For composing transformations
- `CodeTransform` - For bytecode instruction transformation
- `ClassDesc`, `MethodTypeDesc` - Type metadata

## Event Ordering Guarantee

Transformed simulation maintains important ordering properties:

1. **Method calls on same entity** - Events execute in call order
2. **Global time ordering** - Events ordered by scheduled time
3. **Deterministic execution** - Same input produces same event sequence
4. **No implicit parallelism** - Despite virtual threads, events serialize at time level

## Thread Safety

Transform output is thread-safe for:
- Reading entity fields from event code
- Writing entity fields from event code (if careful)
- Calling other events

NOT thread-safe for:
- Concurrent modification from non-event code
- External synchronization typically still needed

## Dependencies

The transform module depends on:
- `com.hellblazer.primeMover:api` - Annotations and contracts
- JDK 25+ ClassFile API (java.lang.classfile.*) - Built-in

No external dependencies on ASM, ClassGraph, or similar.

## Performance Implications

**Transformation Cost:**
- One-time cost at build or class-load time
- Typical: <1ms per class on modern hardware
- Minimal impact on startup time

**Runtime Cost:**
- Minimal: Event dispatch is efficient
- EntityReference switch statement is jitted
- Virtual thread overhead is negligible for most simulations

## Debugging Transformed Code

When debugging transformed code:
1. Original methods are renamed (`__event_*`)
2. Stack traces show actual execution flow
3. Source line mapping preserved for debugging
4. Use IDE with source/bytecode mapping support

## Limitations and Future Work

**Current Limitations:**
- Inherited methods need careful annotation
- Generic method parameters need explicit types
- Complex initialization in constructors may not work as expected

**Planned Improvements:**
- Better handling of method inheritance
- Generic method support improvements
- Reflection API enhancements

## See Also

- **api module**: Annotations and contracts
- **primemover-maven-plugin**: Build-time transformation integration
- **sim-agent**: Runtime transformation integration
- **framework module**: Runtime that transformed code executes on
- **CLASSFILE_API_ANALYSIS.md**: Detailed ClassFile API analysis
