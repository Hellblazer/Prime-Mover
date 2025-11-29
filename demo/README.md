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

# Event throughput benchmark
./mvnw exec:java -pl demo -Dexec.mainClass=demo.EventThroughput

# Continuation throughput benchmark
./mvnw exec:java -pl demo -Dexec.mainClass=demo.ContinuationThroughput

# Use channel example
./mvnw exec:java -pl demo -Dexec.mainClass=demo.UseChannel

# Threaded simulation
./mvnw exec:java -pl demo -Dexec.mainClass=demo.Threaded
```

## Example Programs

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
- Large entity populations

**Concept**:
Creates many entities scheduling many events, then measures how fast the controller can process them.

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
- Synchronous channels between entities
- `Kronos.createChannel()` usage
- Producer/consumer pattern
- Channel blocking behavior

**Concept**:
Two entities communicate through a bounded synchronous channel. Producer puts values, consumer takes them.

**Key Code Pattern**:
```java
@Entity
public class Producer {
    public void produce(SynchronousQueue<String> channel) {
        for (int i = 0; i < 10; i++) {
            channel.put("Item " + i);
            Kronos.sleep(10);
        }
    }
}

@Entity
public class Consumer {
    public void consume(SynchronousQueue<String> channel) {
        for (int i = 0; i < 10; i++) {
            String item = channel.take();  // Blocks until item available
            System.out.println("Got: " + item);
            Kronos.sleep(15);  // Slower consumer
        }
    }
}
```

**Key Points**:
- Channels are created via `Kronos.createChannel(String.class)`
- Both `put()` and `take()` block until match
- Channel synchronizes producers and consumers
- No buffering - must coordinate timing

### Threaded - Multiple Concurrent Entities

**File**: `src/main/java/demo/Threaded.java`

**Demonstrates**:
- Multiple independent entities
- Concurrent event execution (within single simulation time)
- Entity interaction
- Shared data structures

**Concept**:
Multiple bank teller entities serve customers, showing how concurrent simulation works.

**Pattern**:
```java
@Entity
public class Teller {
    private Queue<Customer> waitingCustomers = new LinkedList<>();

    public void serveNext() {
        Customer c = waitingCustomers.poll();
        if (c != null) {
            // Serve customer
            Kronos.sleep(100);  // Service time
            // Get next customer
            postContinuingEvent(this, "serveNext");
        }
    }
}
```

**Key Insights**:
- Multiple entities can have concurrent events
- But events are processed in time order globally
- At same simulation time: execution order may vary
- Proper synchronization needed for shared data

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
