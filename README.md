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
        <version>0.1.0</version>
    </dependency>

    <!-- Runtime (needed for test/runtime) -->
    <dependency>
        <groupId>com.hellblazer.primeMover</groupId>
        <artifactId>runtime</artifactId>
        <version>0.1.0</version>
        <scope>test</scope>  <!-- or compile if needed -->
    </dependency>
</dependencies>
```

### 2. Build-Time Transformation

Add the Maven plugin to enable build-time transformation:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.hellblazer.primeMover</groupId>
            <artifactId>primemover-maven-plugin</artifactId>
            <version>0.1.0</version>
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
primemover-maven-plugin/      - Build-time transformation
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
- **[primemover-maven-plugin/README.md](./primemover-maven-plugin/README.md)** - Build-time integration
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

**Version**: 0.1.0

**Recent Changes**:
- Migrated from ASM to Java 25 ClassFile API
- Removed external bytecode manipulation dependencies
- Full virtual thread (Project Loom) support
- Enhanced documentation and examples
- Published to GitHub Packages

**Roadmap**:
- Performance optimization
- More example simulations
- Advanced debugging tools
- Tutorial documentation

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
public class Account {
    private EntityReference __entityRef;

    // New dispatch method - what users call
    public void deposit(double amount) {
        Controller c = Kairos.getController();
        c.postEvent(__entityRef, 0, amount);  // Schedule as event
    }

    // Original renamed - executed by framework
    public void __event_deposit(double amount) {
        Kairos.sleep(50);  // Rewritten from Kronos
        System.out.println("Deposited: " + amount);
    }
}
```

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

ComplexAgent agent = Composite.assemble(ComplexAgent.class,
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
├── pom.xml                       (root Maven POM)
├── api/                          (public API module)
├── framework/                    (runtime module)
├── transform/                    (transformation engine)
├── primemover-maven-plugin/      (Maven plugin)
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
- Java 24+ code style
- All tests pass (`./mvnw test`)
- Documentation updated
- Clear commit messages

## Getting Help

1. **Read the module READMEs** - Each module has detailed documentation
2. **Review examples** - See `demo/` module for usage patterns
3. **Check CLASSFILE_API_ANALYSIS.md** - Technical deep-dive on transformation
4. **Run demos** - Execute example programs to understand concepts

## Citation

If you use Prime Mover in research, please cite:

```bibtex
@software{hildebrand2025primemover,
  title={Prime Mover: Event-Driven Simulation Framework for Java},
  author={Hildebrand, Hal},
  year={2025},
  url={https://github.com/Hellblazer/Prime-Mover}
}
```

## Contact

For questions, issues, or suggestions:
- Open an issue on GitHub
- Visit the repository: https://github.com/hellblazer/Prime-Mover

---

**Happy simulating!**

