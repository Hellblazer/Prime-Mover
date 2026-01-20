# Classloader Hierarchy and Module Boundaries

This document describes the classloader hierarchy, module boundaries, and cross-module interactions in the Prime Mover framework. Understanding these boundaries is essential for debugging class visibility issues and avoiding common pitfalls in long-running simulations.

## Module Structure

Prime Mover is organized as a multi-module Maven project with clear dependency boundaries:

```
                     ┌─────────────────────────────────────────────────────┐
                     │                   api (annotations)                 │
                     │  @Entity, @Blocking, @Event, @NonEvent, @Transformed│
                     │  Kronos, Controller, EntityReference interfaces     │
                     └─────────────────────────────────────────────────────┘
                                            ▲
                                            │
                     ┌─────────────────────────────────────────────────────┐
                     │               framework (runtime)                   │
                     │    Devi, SimulationController, EventImpl, Kairos   │
                     │    Continuation, SynchronousQueueImpl              │
                     └─────────────────────────────────────────────────────┘
                          ▲                 ▲                    ▲
                          │                 │                    │
         ┌────────────────┴───┐    ┌───────┴────────┐   ┌──────┴───────┐
         │     transform      │    │   desmoj-ish   │   │    janus     │
         │  SimulationTransform│    │ SimCondition   │   │  Composite   │
         │  EntityGenerator   │    │ Resource, Bin  │   │  @Facet      │
         └────────────────────┘    └────────────────┘   └──────────────┘
                  ▲                                             ▲
                  │                                             │
    ┌─────────────┴──────────────┐                    ┌────────┴────────┐
    │  primemover-maven-plugin   │                    │   space-ghost   │
    │  primemover-intellij-plugin│                    │  (application)  │
    │  sim-agent                 │                    └─────────────────┘
    └────────────────────────────┘
```

### Module Responsibilities

| Module | Artifact ID | Responsibility |
|--------|-------------|----------------|
| **api** | `api` | Annotations and public interfaces only. No dependencies except SLF4J. |
| **framework** | `runtime` | Core simulation runtime: controllers, event queue, continuations. |
| **transform** | `transform` | Bytecode transformation using Java ClassFile API. |
| **primemover-maven-plugin** | `primemover-maven-plugin` | Maven build-time transformation. |
| **primemover-intellij-plugin** | N/A (Gradle) | IntelliJ IDEA integration for build-time transformation. |
| **sim-agent** | `sim-agent` | Java agent for runtime transformation (alternative to build-time). |
| **desmoj-ish** | `desmoj-ish` | DESMOJ-compatible blocking primitives using the Blocking Primitives SPI. |
| **janus** | `janus` | Dynamic composite/mixin pattern implementation. |

## Classloader Boundaries

### Application Classloader Context

In a typical simulation application:

```
Bootstrap Classloader (JDK classes)
       │
       ▼
System Classloader (JDK module path)
       │
       ▼
Application Classloader
  ├── api.jar
  ├── runtime.jar
  ├── desmoj-ish.jar (optional)
  └── your-simulation.jar (transformed classes)
```

**Key Points:**
- All Prime Mover modules load in the same application classloader
- Transformed entity classes must see both `api` and `runtime` JARs
- The `runtime` module's `Kairos` class provides thread-local controller access

### Plugin Classloader Context

**Maven Plugin:**
```
Maven Core Classloader
       │
       ▼
Plugin Classloader
  ├── primemover-maven-plugin.jar
  ├── transform.jar
  ├── runtime.jar
  └── api.jar
       │
       ▼ (reads, does not execute)
Project Classloader
  └── target/classes/ (classes to transform)
```

**IntelliJ JPS Builder:**
```
JPS Core Classloader
       │
       ▼
Builder Classloader
  ├── Prime Mover JPS plugin classes
  ├── transform.jar
  ├── runtime.jar
  └── api.jar
       │
       ▼ (reads, does not execute)
Module Output
  └── out/production/<module>/ (classes to transform)
```

**Important:** The transformation plugins load Prime Mover classes into their own classloader but never instantiate simulation entities. They only read and modify bytecode.

## Cross-Module Interaction

### Entity References and Bytecode Generation

When `@Entity` classes are transformed:

1. **Original class** is augmented with `@Transformed` annotation
2. **`__EntityReference`** interface is generated containing method ordinals
3. **Kronos calls** are rewritten to **Kairos calls**

Example transformation:
```java
// Before (user code)
Kronos.sleep(100);

// After (transformed bytecode)
Kairos.get().__controller().advance(100);
```

The `EntityReference` interface (from `api`) is implemented by transformed classes, allowing the runtime to invoke methods by ordinal.

### Blocking Primitives SPI

The `desmoj-ish` module uses public methods from `runtime` to implement blocking constructs:

```java
// In desmoj-ish (different package than runtime)
waiter.getContinuation().setReturnValue(value);  // Cross-package access
controller.post(waiter);                          // Cross-package access
```

These methods were made public specifically to support this cross-module pattern. See [BLOCKING_PRIMITIVES_SPI.md](BLOCKING_PRIMITIVES_SPI.md) for details.

## Debugging Classloader Issues

### Symptom: `NoClassDefFoundError` at Runtime

**Cause:** Missing dependency in application classpath.

**Diagnosis:**
```bash
# Check if class is available
java -cp your-app.jar -verbose:class | grep primeMover
```

**Solution:** Ensure both `api` and `runtime` JARs are on classpath.

### Symptom: `ClassCastException` with Same Class Name

**Cause:** Class loaded by different classloaders (common in OSGi, plugin environments).

**Diagnosis:**
```java
// Check classloader identity
System.out.println(entityInstance.getClass().getClassLoader());
System.out.println(EntityReference.class.getClassLoader());
```

**Solution:** Ensure all Prime Mover modules share the same classloader.

### Symptom: `LinkageError` After Transformation

**Cause:** Bytecode version mismatch between transformed class and runtime.

**Diagnosis:**
```bash
# Check bytecode version
javap -v YourEntity.class | grep "major version"
```

**Solution:** Ensure transform plugin version matches runtime version exactly.

### Symptom: Transformed Class Not Recognized

**Cause:** `@Transformed` annotation missing (transformation failed silently).

**Diagnosis:**
```bash
# Check for @Transformed annotation
javap -v YourEntity.class | grep Transformed
```

**Solution:** Check Maven/IntelliJ build logs for transformation errors.

## Best Practices

### Avoiding Classloader Leaks in Long-Running Simulations

1. **Clear entity references after use:**
   ```java
   // After simulation completes
   controller.clear();
   ```

2. **Avoid static references to entities:**
   ```java
   // BAD: Static reference prevents GC
   private static List<MyEntity> entities = new ArrayList<>();

   // GOOD: Instance reference allows GC
   private List<MyEntity> entities = new ArrayList<>();
   ```

3. **Use weak references for caches:**
   ```java
   private WeakHashMap<EntityReference, CacheEntry> cache = new WeakHashMap<>();
   ```

4. **Enable event source tracking only for debugging:**
   ```java
   // Only enable when debugging event chains
   controller.setTrackEventSources(false);  // Default for production
   ```

### Version Consistency

Always use the same version for all Prime Mover modules:
```xml
<properties>
    <primemover.version>1.0.5</primemover.version>
</properties>

<dependencies>
    <dependency>
        <groupId>com.hellblazer.primeMover</groupId>
        <artifactId>runtime</artifactId>
        <version>${primemover.version}</version>
    </dependency>
    <dependency>
        <groupId>com.hellblazer.primeMover</groupId>
        <artifactId>desmoj-ish</artifactId>
        <version>${primemover.version}</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>com.hellblazer.primeMover</groupId>
            <artifactId>primemover-maven-plugin</artifactId>
            <version>${primemover.version}</version>
        </plugin>
    </plugins>
</build>
```

### Testing Transformed Classes

Run tests with transformation enabled:
```xml
<plugin>
    <groupId>com.hellblazer.primeMover</groupId>
    <artifactId>primemover-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>transform-test-classes</id>
            <phase>process-test-classes</phase>
            <goals>
                <goal>transform-test</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## Related Documentation

- [BLOCKING_PRIMITIVES_SPI.md](BLOCKING_PRIMITIVES_SPI.md) - Cross-package SPI for blocking primitives
- [SPI_STABILITY_CONTRACT.md](SPI_STABILITY_CONTRACT.md) - Versioning and stability guarantees
- [README.md](README.md) - Framework overview
