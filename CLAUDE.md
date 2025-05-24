# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Test Commands

**Requirements**: Java 24+ (GraalVM recommended)

```bash
# Build entire project
./mvnw clean install

# Build specific module
./mvnw clean install -pl <module-name>

# Run tests for specific module
./mvnw test -pl <module-name>

# Run single test class
./mvnw test -pl <module-name> -Dtest=<TestClassName>
```

## Core Architecture

Prime Mover is an event-driven simulation framework that uses bytecode transformation to convert regular Java code into discrete event simulations. Key architectural concepts:

### Multi-Module Structure
- **framework/runtime**: Core simulation runtime with `Controller`, `Event`, `Kronos` API
- **transform**: ASM-based bytecode transformation engine
- **primemover-maven-plugin**: Maven plugin for build-time class transformation
- **sim-agent**: Java agent for runtime class transformation
- **demo**: Example usage and integration patterns
- **desmoj-ish**: Alternative simulation framework implementation

### Simulation Transformation Process
1. Classes annotated with `@Entity` are transformed at build time by the Maven plugin
2. Method calls become events scheduled through a `Controller`
3. The `Kronos` static API provides simulation primitives (sleep, time, channels)
4. Events execute in virtual threads with continuation support for blocking operations

### Key Annotations
- `@Entity`: Marks classes as simulation entities (methods become events)
- `@Blocking`: Methods that block simulation until completion
- `@Event`/`@NonEvent`: Explicitly include/exclude methods from event transformation
- `@Transformed`: Applied automatically to transformed classes

### Event Flow
1. **Entity Creation**: `@Entity` classes define simulation participants
2. **Event Scheduling**: Method calls create time-ordered events via `Controller`
3. **Execution**: `SimulationController` processes events through `SplayQueue`
4. **Continuation**: Blocking events pause/resume using virtual thread continuations

### Essential Components
- `Controller`: Interface for simulation time management and event posting
- `SimulationController`: Main discrete event simulation implementation
- `EntityReference`: Generated interface for transformed entity method dispatch
- `EventImpl`: Concrete event representation with timing and continuation state
- `Devi`: Base controller using virtual threads for event execution

## Working with Transformations

When adding simulation code:
1. Annotate classes with `@Entity` 
2. Use `Kronos.sleep(duration)` for time advancement
3. The Maven plugin automatically transforms classes during build
4. For runtime transformation, use the sim-agent jar as `-javaagent`

Example transformation setup (see demo/pom.xml):
```xml
<plugin>
    <groupId>com.hellblazer.primeMover</groupId>
    <artifactId>primemover-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>transform</goal>
                <goal>transform-test</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```