# Prime Mover Transform Module

## Overview

The transform module implements the bytecode transformation engine for Prime Mover. It uses Java 25's ClassFile API (JEP 484) to transform `@Entity` classes into discrete event simulation entities at compile or runtime.

**Artifact**: `com.hellblazer.primeMover:transform`
**Version**: 1.0.5-SNAPSHOT

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
SimulationTransform - Main orchestrator
    |
    v
ClassScanner - Scans directories/JARs for @Entity classes
    |
    v
ClassMetadata - Analyzes class structure and annotations
    |
    v
EntityGenerator - Transforms entity class bytecode
                - Generates __invoke() and __signatureFor() methods
                - Rewrites Kronos → Kairos calls
                - Renames event methods to __event_* pattern
    |
    v
Transformed Bytecode
```

### Key Components

#### `ClassScanner`
Scans compiled class directories or JAR files for `@Entity` classes and builds metadata about them.

**Responsibilities:**
- Directory and JAR traversal
- Class file identification using ClassFile API
- Annotation scanning (`@Entity`, `@Blocking`, `@Event`, `@NonEvent`)
- Metadata extraction and building
- Batch processing coordination

**Key Methods:**
```java
ClassScanner addClasspathEntry(Path entry)      // Add directory or JAR to scan
ClassScanner scan()                              // Perform scanning
Map<ClassDesc, ClassMetadata> getEntityClasses() // Get discovered entities
ClassMetadata getMetadata(ClassDesc desc)        // Get metadata for specific class
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
Transforms entity class bytecode to implement simulation event semantics using Java 25 ClassFile API.

**Transformation Process:**
1. Checks for `@Transformed` annotation - skips if already transformed (prevents double transformation)
2. Analyzes event methods from `ClassMetadata`
3. Generates `__invoke(int event, Object[] arguments) throws Throwable` method with switch-based dispatch
4. Generates `__signatureFor(int event)` method for debugging
5. Renames original event methods to `__event_<originalName>` pattern
6. Rewrites all `Kronos.*` calls to `Kairos.*` using ClassRemapper
7. Adds `@Transformed` annotation (allows multiple tools to coexist safely)
8. Handles primitive type boxing/unboxing for event parameters

**Note**: The `@Transformed` annotation guard allows Maven plugin, sim-agent, and IntelliJ JPS plugin to be used together. Whichever tool transforms first marks the class; subsequent tools skip it.

**Generated Code Pattern (conceptual):**
```java
// In transformed entity class (implements EntityReference):
public Object __invoke(int event, Object[] arguments) throws Throwable {
    return switch (event) {
        case 0 -> __event_openAccount((String) arguments[0]);
        case 1 -> __event_closeAccount((String) arguments[0]);
        default -> throw new IllegalArgumentException("Unknown event: " + event);
    };
}

public String __signatureFor(int event) {
    return switch (event) {
        case 0 -> "openAccount(String)";
        case 1 -> "closeAccount(String)";
        default -> throw new IllegalArgumentException("Unknown event: " + event);
    };
}

// Original method renamed:
public void __event_openAccount(String name) {
    // Original method body with Kronos → Kairos rewritten
}
```

**Key Methods:**
```java
byte[] transformEntity(ClassMetadata metadata, byte[] originalBytes)
```

#### `SimulationTransform`
The main transformation orchestrator that coordinates the entire transformation process.

**Responsibilities:**
- Manages ClassScanner for discovering `@Entity` classes
- Coordinates transformation of multiple entity classes
- Provides filtering (e.g., exclude already transformed classes)
- Delegates actual bytecode transformation to EntityGenerator
- Manages API remapping (Kronos → Kairos)

**Transformation Workflow:**
1. **Scan Phase**: ClassScanner finds all `@Entity` classes in classpath
2. **Filter Phase**: `EXCLUDE_TRANSFORMED_FILTER` skips classes with `@Transformed` annotation
3. **Transform Phase**: For each entity class:
   - EntityGenerator analyzes metadata
   - Generates `__invoke()` and `__signatureFor()` methods
   - Renames event methods to `__event_*` pattern
   - Rewrites Kronos → Kairos calls
   - Adds `@Transformed` annotation (marks class to prevent re-transformation)
4. **Dependent Classes**: Non-entity classes that reference `Kronos` get Kronos→Kairos remapping only
5. **Output Phase**: Write transformed bytecode back to filesystem or classloader

**Key Methods:**
```java
SimulationTransform(Path classpathEntry)                        // Create from path
SimulationTransform(ClassScanner scanner)                       // Create from scanner
Map<ClassDesc, byte[]> transformEntities(Predicate<ClassMetadata> filter) // Transform all
byte[] transformEntity(ClassDesc entityClass)                   // Transform single entity
void writeTransformedClasses(Path outputDir)                    // Write to disk
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
public class Bank implements EntityReference {
    private Queue<String> queue = new LinkedList<>();

    // Generated dispatch method - invoked by Controller
    public Object __invoke(int event, Object[] arguments) throws Throwable {
        return switch (event) {
            case 0 -> { __event_openAccount((String) arguments[0]); yield null; }
            case 1 -> { __event_printQueue(); yield null; }
            default -> throw new IllegalArgumentException("Unknown event: " + event);
        };
    }

    // Generated signature method - for debugging
    public String __signatureFor(int event) {
        return switch (event) {
            case 0 -> "openAccount(String)";
            case 1 -> "printQueue()";
            default -> throw new IllegalArgumentException("Unknown event: " + event);
        };
    }

    // Original public methods now schedule events
    public void openAccount(String name) {
        Kairos.getController().postContinuingEvent(this, 0, name);
    }

    public void printQueue() {
        Kairos.getController().postContinuingEvent(this, 1);
    }

    // Original implementations renamed - executed by framework
    public void __event_openAccount(String name) {
        System.out.println("Opening: " + name);
        Kairos.sleep(50);  // Kronos rewritten to Kairos
        System.out.println("Opened: " + name);
    }

    public void __event_printQueue() {
        System.out.println("Queue: " + queue);
    }
}
```

**Note**: Transformed entities implement `EntityReference` directly - there is no separate generated class. The entity class becomes its own EntityReference implementation with `__invoke()` and `__signatureFor()` methods.

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
- JDK 25+ ClassFile API (`java.lang.classfile.*`) - Built-in to JDK

**No external dependencies**: This module uses only JDK built-in APIs. No ASM, ByteBuddy, ClassGraph, or other third-party bytecode libraries required.

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
