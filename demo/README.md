# Prime Mover Demo Module

## Overview

The demo module contains example simulations demonstrating Prime Mover concepts, patterns, and capabilities.

**Module**: `demo`

**Purpose**: Learning and reference implementations

## Quick Start

### Build

```bash
cd <prime-mover-root>
./mvnw clean package -pl demo
```

### Run Examples

```bash
# HelloWorld - Minimal example
./mvnw exec:java -pl demo -Dexec.mainClass=hello.HelloWorld

# Run all demos via Demo driver
./mvnw exec:java -pl demo -Dexec.mainClass=demo.Demo

# Individual demos are run via the Driver class (see Demo.java for examples)
```

## Example Programs

### Demo.java - Main Driver

**File**: `src/main/java/demo/Demo.java`

**Purpose**: Provides a convenient entry point to run multiple demos. Contains static methods that set up a `SimulationController` and run various benchmarks.

**Available Demos**:
- `eventThroughput()` - Runs the EventThroughput benchmark with STRING mode
- `eventContinuationThroughput()` - Runs the ContinuationThroughput benchmark with STRING mode
- `channel()` - Runs the UseChannel demonstration
- `threaded()` - Runs the Threaded demonstration

**Running**:
```bash
./mvnw exec:java -pl demo -Dexec.mainClass=demo.Demo
```

The `main()` method runs `eventContinuationThroughput()` and `eventThroughput()` by default.

### HelloWorld - Minimal Simulation

**File**: `src/main/java/hello/HelloWorld.java`

**Demonstrates**:
- Basic `@Entity` annotation
- `Kronos.sleep()` for time advancement
- `SimulationController` usage
- Recursive event calls

**Code**:
```java
@Entity
public class HelloWorld {
    public static void main(String[] argv) throws SimulationException {
        SimulationController controller = new SimulationController();
        Kronos.setController(controller);
        new HelloWorld().event1();
        controller.eventLoop();
    }

    public void event1() {
        Kronos.sleep(1);
        event1();  // Recursive call - becomes event scheduling
        System.out.println("Hello World @ time=" + Kronos.currentTime());
    }
}
```

**Key Concepts**:
- `@Entity` marks the class for transformation
- Each call to `event1()` becomes an event
- Recursive calls schedule future events
- `Kronos.sleep(1)` advances simulation time by 1 unit
- `SimulationController.eventLoop()` processes all events

**Output**:
```
Hello World @ time=9
Hello World @ time=8
Hello World @ time=7
Hello World @ time=6
Hello World @ time=5
Hello World @ time=4
Hello World @ time=3
Hello World @ time=2
Hello World @ time=1
```

Note: Events execute in reverse order due to how recursion becomes event scheduling.

### EventThroughput - Performance Benchmark

**File**: `src/main/java/demo/EventThroughput.java`

**Demonstrates**:
- High-volume event processing
- Performance measurement
- Event rate calculation
- Recursive event scheduling

**Concept**:
Benchmarks different event types (NULL, INT, DOUBLE, STRING) by recursively scheduling events and measuring throughput. Each event schedules the next event after a `sleep(1)` call.

**Output Example**:
```
Events: 1000000
Time: 45.23 ms
Rate: 22,104 events/ms (22M events/sec)
```

**Interpretation**:
- Typical modern hardware: 50,000+ events per second
- This is sufficient for most discrete event simulations
- Linear scaling with number of cores

**Optimization Tips**:
- Reduce event complexity
- Increase batch size
- Profile with Java Flight Recorder
- Use `SimulationController` (not `SteppingController`)

### ContinuationThroughput - Blocking Operation Benchmark

**File**: `src/main/java/demo/ContinuationThroughput.java`

**Demonstrates**:
- Blocking event execution
- Virtual thread continuation performance
- `@Blocking` annotation usage
- `Kronos.blockingSleep()` behavior

**Concept**:
Measures performance of blocking operations using virtual thread continuations.

**Key Difference from EventThroughput**:
- Uses `@Blocking` methods
- Calls `Kronos.blockingSleep()`
- Virtual thread suspension/resumption
- Slightly higher overhead than non-blocking

**Output Example**:
```
Blocking Events: 100000
Time: 156.45 ms
Rate: 639 blocking events/ms (639k events/sec)
```

**Performance Note**:
Blocking operations are slower than non-blocking due to virtual thread context switching, but still very fast.

### UseChannel - Inter-Entity Communication

**File**: `src/main/java/demo/UseChannel.java`

**Demonstrates**:
- Synchronous channels for coordination
- `Kronos.createChannel()` usage
- Blocking `put()` and `take()` operations
- Event coordination through channels

**Concept**:
A single entity demonstrates channel operations by calling `take()` and `put()` methods in sequence. The channel blocks operations until matching operations occur.

**Key Code**:
```java
@Entity
public class UseChannel {
    protected final SynchronousQueue<String> channel = Kronos.createChannel(String.class);

    public void test() {
        take();              // Blocks until put() happens
        Kronos.sleep(60000);
        put();               // Matches the waiting take()
        Kronos.sleep(60000);
        put();               // Blocks until another take()
        Kronos.sleep(60000);
        take();              // Matches the waiting put()
    }

    public void take() {
        System.out.println(Kronos.currentTime() + ": take called");
        Object o = channel.take();
        System.out.println(Kronos.currentTime() + ": take continues with object: " + o);
    }

    public void put() {
        System.out.println(Kronos.currentTime() + ": put called");
        channel.put("foo");
        System.out.println(Kronos.currentTime() + ": put continues");
    }
}
```

**Key Points**:
- Channels are created via `Kronos.createChannel(String.class)`
- `put()` blocks until matching `take()` (and vice versa)
- Channel provides synchronization between event methods
- No buffering - operations must be paired

### Threaded - Concurrent Blocking Operations

**File**: `src/main/java/demo/Threaded.java`

**Demonstrates**:
- Multiple concurrent processes using blocking sleep
- Interleaved execution via virtual threads
- `Kronos.blockingSleep()` behavior
- Parallel event processing

**Concept**:
Three independent "threads" (entity method calls) execute concurrently using blocking sleep operations. This demonstrates how virtual thread continuations enable concurrent blocking behavior in the simulation.

**Code**:
```java
@Entity
public class Threaded {
    public void process(int id) {
        for (int i = 1; i <= 5; i++) {
            System.out.println(Kronos.currentTime() + ": thread=" + id + ", i=" + i);
            Kronos.blockingSleep(1);
        }
    }
}

// Usage (from Driver)
Threaded threaded = new Threaded();
threaded.process(1);  // Start first "thread"
threaded.process(2);  // Start second "thread"
threaded.process(3);  // Start third "thread"
```

**Key Insights**:
- Each `process()` call runs in its own virtual thread
- `blockingSleep()` suspends the virtual thread and resumes after delay
- All three processes interleave their execution
- Output shows concurrent progress of all three processes

## Code Patterns

### Pattern 1: Simple Loop

```java
@Entity
public class SimpleEntity {
    public void repeatingEvent() {
        // Do work
        Kronos.sleep(10);
        repeatingEvent();  // Schedule next occurrence
    }
}

// Usage
SimpleEntity entity = new SimpleEntity();
entity.repeatingEvent();  // Schedule first event
```

### Pattern 2: Event Handler with State

```java
@Entity
public class StatefulEntity {
    private int state = 0;

    public void handleEvent() {
        state++;
        Kronos.sleep(5);
        if (state < 100) {
            handleEvent();
        }
    }
}
```

### Pattern 3: Blocking Operation

```java
@Entity
public class BlockingEntity {
    @Blocking
    public void longRunningOperation() {
        System.out.println("Starting at " + Kronos.currentTime());
        Kronos.blockingSleep(500);  // Blocks, then resumes
        System.out.println("Completed at " + Kronos.currentTime());
    }
}
```

### Pattern 4: Scheduled Events

```java
@Entity
public class ScheduledEntity {
    public void scheduleWork() {
        // Do work
        Kronos.sleep(100);

        // Check if more work needed
        if (moreWork()) {
            scheduleWork();  // Recurse for next work
        }
    }
}
```

### Pattern 5: Multiple Event Methods

```java
@Entity
public class MultiEventEntity {
    public void eventA() {
        // Process A
        Kronos.sleep(20);
        eventB();  // Schedule different event
    }

    public void eventB() {
        // Process B
        Kronos.sleep(30);
        eventA();  // Back to A
    }
}
```

## Configuration and Customization

### Maven Plugin Configuration

The demo module uses the Prime Mover Maven plugin:

```xml
<plugin>
    <groupId>com.hellblazer.primeMover</groupId>
    <artifactId>primemover-maven-plugin</artifactId>
    <version>${project.version}</version>
    <executions>
        <execution>
            <id>transform-classes</id>
            <phase>process-classes</phase>
            <goals>
                <goal>transform</goal>
            </goals>
        </execution>
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

### Running with Different Controllers

**SteppingController** - Debug one event at a time:
```java
SteppingController controller = new SteppingController();
Kronos.setController(controller);

while (controller.hasEvents()) {
    System.out.println("Time: " + controller.getCurrentTime());
    controller.processOneEvent();
}
```

**RealTimeController** - Execute in real time:
```java
RealTimeController controller = new RealTimeController();
Kronos.setController(controller);
controller.eventLoop();
```

## Testing Examples

The test directory contains test cases:

**File**: `src/test/java/test/TestMe.java`

Shows how to write tests for transformed entities:
```java
@Test
void testSimulation() {
    SimulationController controller = new SimulationController();
    Kronos.setController(controller);

    TestEntity entity = new TestEntity();
    entity.someEvent();

    controller.eventLoop();

    assertEquals(expectedState, entity.getState());
}
```

## Performance Tips

### 1. Measurement

Always measure, don't guess:
```java
long start = System.nanoTime();
controller.eventLoop();
long elapsed = System.nanoTime() - start;
System.out.println("Time: " + (elapsed / 1_000_000) + "ms");
```

### 2. Profiling

Use Java Flight Recorder:
```bash
java -XX:StartFlightRecording=duration=30s,filename=recording.jfr ...
jmc  # Open recording in Mission Control
```

### 3. Entity Design

- Keep event methods simple
- Minimize per-event allocations
- Reuse data structures
- Avoid unnecessary object creation

### 4. Simulation Parameters

- Start with small event counts
- Gradually increase
- Watch for GC pauses
- Monitor memory usage

## Common Mistakes

### Mistake 1: Forgetting @Entity

```java
// WRONG - won't be transformed
public class MyEntity {
    public void event() { ... }
}

// CORRECT
@Entity
public class MyEntity {
    public void event() { ... }
}
```

### Mistake 2: Direct Method Calls

```java
@Entity
public class MyEntity {
    public void eventA() {
        eventB();  // WRONG - direct call, not scheduled
    }

    public void eventB() { ... }
}

// EventB is called immediately, not scheduled as event
```

### Mistake 3: Synchronization in Events

```java
@Entity
public class MyEntity {
    public void event() {
        synchronized(this) {  // Usually unnecessary
            // ...
        }
    }
}

// Synchronization generally not needed since events serialize
```

### Mistake 4: Blocking without @Blocking

```java
@Entity
public class MyEntity {
    public void event() {
        Kronos.blockingSleep(100);  // WRONG without @Blocking
    }
}

// Throws UnsupportedOperationException at runtime
```

### Mistake 5: External Synchronization

```java
@Entity
public class MyEntity {
    static Queue<String> sharedQueue = new LinkedList<>();  // WRONG

    public void event() {
        sharedQueue.add("item");  // Not thread-safe!
    }
}
```

## Extending the Examples

### Add Your Own Simulation

1. Create class with `@Entity` annotation
2. Add event methods
3. Use `Kronos` API for time management
4. Run through `SimulationController`

### Create Benchmarks

Pattern for custom benchmarks:
```java
public class MyBenchmark {
    public static void main(String[] args) {
        SimulationController controller = new SimulationController();
        Kronos.setController(controller);

        long startTime = System.nanoTime();

        // Create entities and schedule events
        MyEntity entity = new MyEntity();
        entity.start();

        // Run simulation
        controller.eventLoop();

        long elapsed = System.nanoTime() - startTime;
        long count = controller.getEventCount();

        System.out.println("Events: " + count);
        System.out.println("Time: " + (elapsed / 1_000_000.0) + " ms");
        System.out.println("Rate: " + (count / (elapsed / 1_000_000.0)) + " events/ms");
    }
}
```

## See Also

- **api module**: Annotations and API documentation
- **framework module**: Controller implementations and runtime
- **transform module**: How transformation works
- **README.md** (root): Project overview and getting started
