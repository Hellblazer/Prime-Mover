# Prime Mover: Event-Driven Simulation Framework for Java

![Build Status](https://github.com/hellblazer/prime-mover/actions/workflows/maven.yml/badge.svg)
![License: AGPL v3](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)
![Java Version](https://img.shields.io/badge/Java-25%2B-blue)

Prime Mover is a modern, high-performance discrete event simulation framework for Java. It uses bytecode transformation to convert regular Java code into event-driven simulations, with native support for blocking operations via Java virtual threads (Project Loom).

## Quick Start

### 1. Add Dependencies

First, configure access to GitHub Packages in your `~/.m2/settings.xml`:

```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>YOUR_GITHUB_USERNAME</username>
            <password>YOUR_GITHUB_TOKEN</password>
        </server>
    </servers>
</settings>
```

> **Note**: Generate a GitHub Personal Access Token with `read:packages` scope at https://github.com/settings/tokens

Then add the repository and dependencies to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/Hellblazer/Prime-Mover</url>
    </repository>
</repositories>

<pluginRepositories>
    <pluginRepository>
        <id>github</id>
        <url>https://maven.pkg.github.com/Hellblazer/Prime-Mover</url>
    </pluginRepository>
</pluginRepositories>

<dependencies>
    <!-- API for @Entity, Kronos, etc. -->
    <dependency>
        <groupId>com.hellblazer.primeMover</groupId>
        <artifactId>api</artifactId>
        <version>1.0.5-SNAPSHOT</version>
    </dependency>

    <!-- Runtime (needed for test/runtime) -->
    <dependency>
        <groupId>com.hellblazer.primeMover</groupId>
        <artifactId>runtime</artifactId>
        <version>1.0.5-SNAPSHOT</version>
        <scope>test</scope>  <!-- or compile if needed -->
    </dependency>
</dependencies>
```

### 2. Configure Transformation

Choose one of three transformation methods:

**Option A: IntelliJ IDEA Plugin** (Recommended for IDE users)
- Install from **Settings** > **Plugins** > Search "Prime Mover"
- Automatically transforms classes on build
- See [primemover-intellij-plugin/README.md](./primemover-intellij-plugin/README.md)

**Option B: Maven Plugin** (Recommended for CI/CD)
```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.hellblazer.primeMover</groupId>
            <artifactId>primemover-maven-plugin</artifactId>
            <version>1.0.5-SNAPSHOT</version>
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
    </plugins>
</build>
```

**Option C: Runtime Java Agent** (For testing/prototyping)
```bash
java -javaagent:path/to/sim-agent.jar -cp ... YourSimulation
```

### 3. Write Your First Simulation

```java
import com.hellblazer.primeMover.annotations.Entity;
import com.hellblazer.primeMover.api.Kronos;
import com.hellblazer.primeMover.controllers.SimulationController;

@Entity
public class HelloWorld {
    public static void main(String[] args) {
        // Create simulation controller
        SimulationController controller = new SimulationController();
        Kronos.setController(controller);

        // Schedule initial event
        new HelloWorld().event1();

        // Run simulation
        controller.eventLoop();
    }

    public void event1() {
        Kronos.sleep(1);           // Advance time by 1 unit
        event1();                   // Schedule next occurrence
        System.out.println("Hello @ " + Kronos.currentTime());
    }
}
```

### 4. Build and Run

```bash
./mvnw clean package
java -cp target/classes:... HelloWorld
```

## Key Concepts

### Entities (`@Entity`)

Mark any class as a simulation entity - its methods become simulation events:

```java
@Entity
public class Bank {
    public void openAccount(String name) {
        System.out.println("Opening: " + name);
        Kronos.sleep(100);  // Advance simulation time
        System.out.println("Account opened");
    }
}
```

When called, methods don't execute directly - they're scheduled as time-ordered events.

### Kronos API

The static API for simulation code:

```java
Kronos.sleep(duration)              // Non-blocking time advance
Kronos.blockingSleep(duration)      // Blocking time advance
Kronos.currentTime()                // Query simulation time
Kronos.getController()              // Access the controller
Kronos.createChannel()              // Create inter-entity communication
```

All these calls are rewritten during transformation to use the actual runtime implementation.

### Blocking Operations (`@Blocking`)

Mark methods that need to suspend execution:

```java
@Entity
public class Task {
    @Blocking
    public void longOperation() {
        Kronos.blockingSleep(1000);  // Suspends using virtual thread continuation
        System.out.println("Done");
    }
}
```

Blocking uses Java virtual threads - no threads actually block, just virtual continuations.

### Controllers

The simulation engine that processes events:

```java
// Standard discrete event simulation
SimulationController controller = new SimulationController();
controller.eventLoop();

// Step through events for debugging
SteppingController controller = new SteppingController();
while (controller.hasEvents()) {
    controller.processOneEvent();
}

// Real-time paced simulation
RealTimeController controller = new RealTimeController();
controller.eventLoop();
```

## Architecture

### Module Structure

```
api/                          - Public API (@Entity, Kronos, Controller)
  |
framework/ (runtime)          - Runtime implementation (Kairos, SimulationController)
  |
transform/                    - Bytecode transformation engine
  |
primemover-maven-plugin/      - Build-time transformation (Maven)
  |
primemover-intellij-plugin/   - IDE integration (IntelliJ IDEA)
  |
sim-agent/                    - Runtime transformation (Java agent)
  |
demo/                         - Example simulations
  |
janus/                        - Composite/mixin pattern support
  |
space-ghost/                  - Example application using Janus
  |
desmoj-ish/                   - DESMOJ-compatible simulation framework
  |
maven-testing/                - Plugin testing infrastructure
```

### Transformation Pipeline

```
Original Java Code (@Entity classes)
    |
    v
Bytecode Transformation (ClassFile API)
    - Identify event methods
    - Rewrite method calls to event scheduling
    - Generate EntityReference for dispatch
    - Rewrite Kronos -> Kairos
    |
    v
Transformed Bytecode
    |
    v
Execution (Virtual Threads + Priority Queue)
    - Events execute in time order
    - Blocking suspends/resumes continuations
    - Concurrent entities at same time
    |
    v
Simulation Results
```

## Performance

**Typical Performance**:
- **Event Throughput**: 50,000+ events per second
- **Blocking Events**: 600+ blocking events per second
- **Memory Efficiency**: Minimal overhead with virtual threads
- **Scales Linearly**: With available CPU cores

## Documentation

### Conceptual Foundations

- **[CONCEPTS.md](./CONCEPTS.md)** - Deep-dive into DES theory, Kronos/Kairos design, event semantics, and virtual thread continuations

### Module Documentation

Each module has detailed documentation:

- **[api/README.md](./api/README.md)** - Public API and annotations
- **[framework/README.md](./framework/README.md)** - Runtime implementation and controllers
- **[transform/README.md](./transform/README.md)** - Bytecode transformation details
- **[primemover-maven-plugin/README.md](./primemover-maven-plugin/README.md)** - Build-time integration (Maven)
- **[primemover-intellij-plugin/README.md](./primemover-intellij-plugin/README.md)** - IDE integration (IntelliJ IDEA)
- **[sim-agent/README.md](./sim-agent/README.md)** - Runtime transformation via Java agent
- **[demo/README.md](./demo/README.md)** - Example programs and patterns
- **[janus/README.md](./janus/README.md)** - Composite/mixin pattern support
- **[desmoj-ish/README.md](./desmoj-ish/README.md)** - DESMOJ-compatible simulation features
- **[CLASSFILE_API_ANALYSIS.md](./CLASSFILE_API_ANALYSIS.md)** - Technical deep-dive on ClassFile API

## Build and Test

**Requirements**: Java 25 or later (GraalVM recommended)

```bash
# Build entire project
./mvnw clean install

# Build specific module
./mvnw clean install -pl api

# Run tests
./mvnw test

# Run specific test
./mvnw test -pl demo -Dtest=TestClassName

# Build with verbose output
./mvnw clean install -X
```

## Current Status

**Version**: 1.0.5-SNAPSHOT

**Recent Changes**:
- IntelliJ IDEA plugin released for seamless IDE integration
- Migrated from ASM to Java 25 ClassFile API (complete)
- Removed external bytecode manipulation dependencies
- Full virtual thread (Project Loom) support
- Enhanced documentation and examples
- Published to GitHub Packages

**Roadmap**:
- Performance optimization
- More example simulations
- Advanced debugging tools
- Tutorial documentation
- Additional IDE integrations (Eclipse, VS Code)

## Transformation Example

**Original Code**:
```java
@Entity
public class Account {
    public void deposit(double amount) {
        Kronos.sleep(50);
        System.out.println("Deposited: " + amount);
    }
}
```

**After Transformation** (conceptual):
```java
@Transformed
@Entity
public class Account implements EntityReference {

    // Generated dispatch method - invoked by Controller
    public Object __invoke(int eventIndex, Object[] args) throws Throwable {
        return switch (eventIndex) {
            case 0 -> { __event_deposit((Double) args[0]); yield null; }
            default -> throw new IllegalArgumentException("Unknown event: " + eventIndex);
        };
    }

    // Generated signature method - for debugging
    public String __signatureFor(int eventIndex) {
        return switch (eventIndex) {
            case 0 -> "deposit(double)";
            default -> throw new IllegalArgumentException("Unknown event: " + eventIndex);
        };
    }

    // Original public method - now schedules event
    public void deposit(double amount) {
        Kairos.getController().postContinuingEvent(this, 0, amount);
    }

    // Original implementation renamed - executed by framework
    public void __event_deposit(double amount) {
        Kairos.sleep(50);  // Rewritten from Kronos
        System.out.println("Deposited: " + amount);
    }
}
```

The entity class becomes its own `EntityReference` implementation with `__invoke()` and `__signatureFor()` methods.

## Advanced Features

### Inter-Entity Communication

Use synchronous channels for safe communication:

```java
@Entity
public class Producer {
    public void produce(SynchronousQueue<String> channel) {
        channel.put("item");
    }
}

@Entity
public class Consumer {
    public void consume(SynchronousQueue<String> channel) {
        String item = channel.take();  // Blocks until item available
    }
}
```

### Composite Entities (Janus)

Combine multiple behaviors using the Janus mixin system:

```java
public interface ComplexAgent extends Movable, Communicative, Intelligent {
}

var assembler = Composite.instance();
ComplexAgent agent = assembler.assemble(ComplexAgent.class,
    new Composite.CompositeClassLoader(getClass().getClassLoader()),
    new MovementBehavior(),
    new CommunicationBehavior(),
    new DecisionMaking());
```

### DESMOJ-Compatible API

For users familiar with DESMOJ, there's a compatibility layer with distributions, queues, and resources.

## Repository Structure

```
Prime-Mover/
├── README.md                     (this file)
├── CLAUDE.md                     (AI assistant guidance)
├── CLASSFILE_API_ANALYSIS.md    (technical analysis)
├── CONCEPTS.md                   (conceptual deep-dive)
├── pom.xml                       (root Maven POM)
├── api/                          (public API module)
├── framework/                    (runtime module)
├── transform/                    (transformation engine)
├── primemover-maven-plugin/      (Maven plugin)
├── primemover-intellij-plugin/   (IntelliJ IDEA plugin)
├── sim-agent/                    (Java agent)
├── demo/                         (examples)
├── janus/                        (composite/mixin)
├── space-ghost/                  (example app)
├── desmoj-ish/                   (DESMOJ compatibility)
└── maven-testing/                (plugin testing)
```

## License

Licensed under AGPL v3.0. See [LICENSE](./LICENSE) for details.

## Contributing

Contributions welcome! Please ensure:
- Java 25+ code style (modern patterns, `var`, virtual threads)
- All tests pass (`./mvnw test`)
- Documentation updated
- Clear commit messages

## Troubleshooting

### Common Issues

**`UnsupportedOperationException` when calling Kronos methods**
- **Cause**: Code is not transformed. Kronos methods throw by default and are rewritten to Kairos during transformation.
- **Fix**: Ensure transformation is configured (Maven plugin, IntelliJ plugin, or sim-agent).

**Classes not being transformed**
- **Cause**: Missing `@Entity` annotation or wrong import.
- **Fix**: Verify `import com.hellblazer.primeMover.annotations.Entity` (not javax.persistence.Entity).

**`NoClassDefFoundError` for Kairos**
- **Cause**: Runtime dependency missing.
- **Fix**: Add `runtime` artifact to dependencies with appropriate scope.

**Events executing in unexpected order**
- **Cause**: Misunderstanding event scheduling vs direct execution.
- **Fix**: Remember that method calls on `@Entity` classes schedule events at the current simulation time. Use `Kronos.sleep()` to advance time between events.

**Blocking method not suspending**
- **Cause**: Missing `@Blocking` annotation or not using `blockingSleep()`.
- **Fix**: Mark method with `@Blocking` and use `Kronos.blockingSleep()` instead of `sleep()`.

### Debugging Tips

1. Use `SteppingController` to step through events one at a time
2. Check `controller.getTotalEvents()` to verify events are being scheduled
3. Enable logging to see event posting and execution
4. Verify transformation with `javap -c YourClass` to inspect bytecode

## Getting Help

1. **Read the module READMEs** - Each module has detailed documentation
2. **Review examples** - See `demo/` module for usage patterns
3. **Check CLASSFILE_API_ANALYSIS.md** - Technical deep-dive on transformation
4. **Run demos** - Execute example programs to understand concepts

## Contact

For questions, issues, or suggestions:
- Open an issue on GitHub
- Visit the repository: https://github.com/hellblazer/Prime-Mover

---

**Happy simulating!**

