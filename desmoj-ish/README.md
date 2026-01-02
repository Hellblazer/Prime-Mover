# Prime Mover DESMOJ-ish Module

## Overview

The desmoj-ish module provides simulation blocking primitives, probability distributions, and statistical reporting for Prime Mover simulations. These components enable building sophisticated discrete event simulations with familiar patterns from simulation frameworks like DESMOJ.

**Artifact**: `com.hellblazer.primeMover:desmoj-ish`

**Purpose**: Blocking primitives, distributions, and reporting for simulations

## Module Structure

The module is organized into three packages:

### Core Blocking Primitives (`com.hellblazer.primeMover.desmoj`)

- **SimSignal**: Basic signal/await primitive for event synchronization
- **SimCondition**: Condition variable with await/signal/signalAll semantics
- **Resource**: Pool of resources with blocking acquire/release
- **ResourceToken**: Token representing acquired resources
- **Loan**: Auto-closeable resource wrapper for try-with-resources
- **ResourceStatistics**: Statistics tracking for Resource usage
- **ProcessQueue**: FIFO queue for process coordination
- **QueueStatistics**: Statistics tracking for queues
- **Bin**: Store for homogeneous items with blocking take
- **Stock**: Continuous quantity storage with blocking withdraw

### Probability Distributions (`com.hellblazer.primeMover.desmoj.dist`)

- **Distribution<T>**: Base interface for all distributions with `sample()`, `reset()`, `reset(seed)` methods
- **ContinuousDistribution**: Abstract base for continuous distributions with factory methods
  - Provides `sampleDuration()` and `sampleDuration(scale)` for simulation time
  - Uses `L64X128MixRandom` generator for high-quality randomness
- **Uniform**: Uniform distribution over `[min, max]` range
- **Normal**: Normal (Gaussian) distribution with mean and standard deviation
- **Exponential**: Exponential distribution with mean (commonly used for inter-arrival times)
- **Triangular**: Triangular distribution with min, mode (peak), and max
- **Constant**: Constant "distribution" (always returns same value, useful for testing)

### Reporting (`com.hellblazer.primeMover.desmoj.report`)

- **Reportable**: Interface for reportable simulation components
- **Reporter**: Base class for generating reports
- **ReportOutput**: Interface for report output destinations
- **JsonReportOutput**: JSON-formatted report output
- **SimulationReport**: Aggregates all reporters for full simulation report
- **QueueReporter**: Report generator for queue statistics
- **ResourceReporter**: Report generator for resource statistics

## Key Patterns

### Blocking Resource Acquisition

Resources use `@Blocking` methods with internal `SimSignal` for suspension:

```java
@Entity
@Transformed
public static class Customer {
    @Blocking
    public void visit() {
        var arrivalTime = controller.getCurrentTime();

        // Acquire the server - blocks if busy
        var token = server.acquire();

        // Simulate service time
        Kronos.blockingSleep(serviceTime);

        // Release the server
        server.release(token);
    }
}
```

### Using Loans (Auto-Release)

```java
@Blocking
public void processWithLoan() {
    try (var loan = resource.loan()) {
        // Use the resource
        Kronos.blockingSleep(processingTime);
    }  // Auto-released when exiting try block
}
```

### Signal/Await Pattern

```java
// Waiter blocks until signaled
@Blocking
public void waitForEvent() {
    signal.await();
    // Continues after signal() is called
}

// Signaler wakes one waiter
public void triggerEvent() {
    signal.signal();
}

// Wake all waiters
public void broadcastEvent() {
    signal.signalAll();
}
```

### Using Distributions

```java
// Create distributions (with optional seed for reproducibility)
var interArrival = new Exponential(10.0);           // Mean of 10
var serviceTime = new Uniform(5.0, 15.0);           // Between 5-15

// With explicit seed for reproducible simulations
var interArrivalSeeded = new Exponential(10.0, 12345L);
var serviceTimeSeeded = new Uniform(5.0, 15.0, 12345L);

// Factory methods (from ContinuousDistribution)
var exp = ContinuousDistribution.exponential(10.0);
var norm = ContinuousDistribution.normal(100.0, 15.0);
var tri = ContinuousDistribution.triangular(5.0, 10.0, 20.0);

// Sample values as doubles
double nextArrivalTime = interArrival.sample();
double serviceTime = serviceTime.sample();

// Sample as simulation time duration (rounds to long)
long duration = interArrival.sampleDuration();
long scaledDuration = serviceTime.sampleDuration(1000.0);  // Scale by 1000
```

## Example: M/M/1 Queue

A complete M/M/1 queue simulation demonstrating Resource usage:

```java
@Test
public void testMM1Queue() throws Exception {
    try (var controller = new SimulationController()) {
        var servedCount = new AtomicInteger(0);

        // Create a single server (M/M/1 has exactly 1 server)
        var server = new Resource.entity(controller, 1);

        // Schedule customer arrivals
        for (int i = 0; i < numCustomers; i++) {
            var customer = new Customer.entity(controller, i, server, servedCount);
            controller.postEvent(i * interArrivalTime, customer, Customer.entity.VISIT);
        }

        // Run the simulation
        controller.eventLoop();

        // Check statistics
        var stats = server.statistics();
        System.out.println("Average wait time: " + stats.getAvgWaitTime());
        System.out.println("Total acquisitions: " + stats.getTotalAcquisitions());
    }
}
```

## Architecture

The module builds on Prime Mover's core transformation system:

```
Blocking Primitives (SimSignal, Resource, etc.)
    |
    v
Prime Mover Runtime (Devi, EventImpl, continuations)
    |
    v
Virtual Threads (Project Loom)
```

Blocking operations work by:
1. Entity calls `@Blocking` method (e.g., `resource.acquire()`)
2. If blocked, internal `SimSignal.await()` suspends the event
3. Another event calls `signal()` when condition is satisfied
4. Original event resumes from where it left off

## Statistics and Reporting

Components track statistics automatically:

```java
var resource = new Resource.entity(controller, 5);
// ... run simulation ...

var stats = resource.statistics();
System.out.println("Total acquisitions: " + stats.getTotalAcquisitions());
System.out.println("Average wait time: " + stats.getAvgWaitTime());
System.out.println("Max wait time: " + stats.getMaxWaitTime());
System.out.println("Average utilization: " + stats.getAverageUtilization());
```

For full simulation reports:

```java
var report = new SimulationReport();
report.addReportable(resource);
report.addReportable(queue);

var output = new JsonReportOutput();
report.generateReport(output);
System.out.println(output.toJson());
```

## Testing

The module includes comprehensive tests:

- `SimSignalTest`: Basic signal/await semantics
- `SimConditionTest`: Condition variable behavior
- `ResourceTest`: Resource pool acquire/release
- `BinTest`: Bin store/retrieve operations
- `StockTest`: Stock deposit/withdraw
- `ProcessQueueTest`: Queue operations
- `DistributionTest`: Distribution sampling
- `ReportingTest`: Report generation
- `MM1QueueTest`: Integration example

Run tests with:

```bash
./mvnw test -pl desmoj-ish
```

## See Also

- **api module**: `@Entity`, `@Blocking` annotations and `Kronos` API
- **framework module**: `Devi`, `SimulationController`, event queue management
- **demo module**: Additional simulation examples
