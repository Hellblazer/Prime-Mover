# Prime Mover DESMOJ-ish Module

## Overview

The desmoj-ish module provides a DESMOJ-compatible simulation framework built on top of Prime Mover. It offers familiar APIs for users migrating from DESMOJ, including distributions, queues, resources, and reporting facilities.

**Artifact**: `com.hellblazer.primeMover:desmoj-ish`

**Purpose**: Compatibility layer and advanced simulation features

## Motivation

DESMOJ (Discrete Event Simulation for Java) is a popular discrete event simulation framework. Prime Mover provides a cleaner, more modern architecture, but users familiar with DESMOJ APIs may want compatible abstractions.

This module bridges the gap by providing:
- Familiar DESMOJ-style APIs
- Random distributions (uniform, normal, exponential, etc.)
- Queues and resource pools
- Statistical reporting
- Named entities

## Architecture

The module is built as a layer on top of Prime Mover:

```
DESMOJ-ish API
    |
    v
Prime Mover Runtime (Kronos, Controllers)
    |
    v
Bytecode Transformation
    |
    v
Virtual Threads + Simulation Events
```

## Key Components

### Distribution Support

Random number generation with common distributions:

```java
// Uniform distribution [0, 100)
UniformDistribution uniform = new UniformDistribution(0, 100);
double value = uniform.nextValue();

// Normal distribution (mean=50, stdDev=10)
NormalDistribution normal = new NormalDistribution(50, 10);
double value = normal.nextValue();

// Exponential distribution (mean=5)
ExponentialDistribution exp = new ExponentialDistribution(5);
double value = exp.nextValue();
```

### Queue Management

First-in-first-out and priority queues:

```java
@Entity
public class QueueExample {
    private Queue<Customer> customers = new LinkedList<>();

    public void addCustomer(Customer c) {
        customers.add(c);
        if (customers.size() == 1) {
            serveNext();
        }
    }

    public void serveNext() {
        if (!customers.isEmpty()) {
            Customer c = customers.remove();
            serveCustomer(c);
        }
    }

    public void serveCustomer(Customer c) {
        Kronos.sleep(50);  // Service time
        serveNext();
    }
}
```

### Resource Pools

Limited resources that entities must acquire:

```java
@Entity
public class ResourcePoolExample {
    private ResourcePool tellers = new ResourcePool(5);  // 5 tellers

    public void serveCustomer(Customer c) {
        Resource teller = tellers.acquire();  // Blocks until available
        try {
            // Serve customer
            Kronos.sleep(100);
        } finally {
            teller.release();
        }
    }
}
```

### Statistical Reporting

Collect and report simulation statistics:

```java
// During simulation
statisticalReporter.record("service_time", 45.2);
statisticalReporter.record("wait_time", 12.3);

// After simulation
statisticalReporter.report();
// Output:
// service_time: min=10.0, max=120.0, mean=45.2, stdDev=25.1, count=1000
// wait_time:    min=0.0,  max=85.0,  mean=12.3, stdDev=8.9,  count=1000
```

## Usage Patterns

### Simulation Setup

```java
SimulationModel model = new SimulationModel("Bank Simulation");
Kronos.setController(model.getController());

// Create entities
Bank bank = new Bank(model);
CustomerGenerator generator = new CustomerGenerator(model);

// Schedule initial events
generator.generateNextCustomer();

// Run simulation
model.simulate();

// Get results
model.report();
```

### Named Entities

```java
@Entity
public class Bank extends SimulationEntity {
    public Bank(SimulationModel model) {
        super(model, "Bank");
    }

    public void openAccount(String name) {
        // ...
    }
}
```

### Event Scheduling

```java
// Schedule event at specific time
Kronos.scheduleAt(time, entity, "eventName", args);

// Schedule event after delay
Kronos.scheduleIn(delay, entity, "eventName", args);

// Schedule repeating event
Kronos.scheduleEvery(interval, entity, "eventName", args);
```

## Distributions

Supported distributions:

**Continuous**:
- Uniform(min, max)
- Normal(mean, stdDev)
- Exponential(mean)
- Triangular(min, mode, max)
- Lognormal(mean, stdDev)
- Gamma(shape, scale)
- Beta(alpha, beta)
- Weibull(shape, scale)

**Discrete**:
- Poisson(lambda)
- Binomial(n, p)
- GeometricDistribution(p)
- NegativeBinomial(r, p)

### Usage

```java
Distribution<Double> dist = new NormalDistribution(100, 15);

// Get next random value
double sample = dist.nextValue();

// Get statistics
double mean = dist.getMean();
double variance = dist.getVariance();
```

## Advanced Features

### Conditional Events

```java
@Entity
public class ConditionalExample {
    private Queue queue;

    public void checkQueue() {
        if (!queue.isEmpty()) {
            processNext();
        } else {
            // Schedule check later
            Kronos.scheduleIn(100, this, "checkQueue");
        }
    }
}
```

### Event Cancellation

```java
// Schedule event
Event event = Kronos.scheduleAt(time, entity, "event");

// Cancel if needed
event.cancel();
```

### Simulation Pause/Resume

```java
SimulationModel model = new SimulationModel();
model.run();        // Run to completion
model.pause();      // Pause execution
// ... inspect state ...
model.resume();     // Resume from pause
```

## Compatibility Notes

**DESMOJ Features NOT Included**:
- Process-based entities (Prime Mover uses events instead)
- Coroutine simulation (uses virtual threads instead)
- Some advanced reporting features

**DESMOJ Features INCLUDED**:
- Distributions and random sampling
- Queues and resource management
- Statistical reporting
- Named entities
- Time-stepped simulation

## Performance Considerations

The desmoj-ish layer adds minimal overhead:
- Distributions: <1% overhead
- Queue operations: O(1) amortized
- Statistics collection: <2% overhead
- Resource pools: O(log n) for manage waiting queues

## Migration from DESMOJ

### Before (DESMOJ)
```java
public class CustomerEntity extends SimulationEntity {
    public void scheduleNewCustomer() {
        Customer customer = new Customer(getModel(), "Customer", true);
        customer.schedule(new java.util.concurrent.TimeUnit().convert(
            getDistribution().nextValue(), TimeUnit.SECONDS));
    }
}
```

### After (Prime Mover + desmoj-ish)
```java
@Entity
public class CustomerEntity {
    private Distribution dist;

    public void scheduleNewCustomer() {
        Customer customer = new Customer();
        Kronos.scheduleIn((long) dist.nextValue(), customer, "arrive");
    }
}
```

## See Also

- **framework module**: Core runtime that this layer uses
- **api module**: Kronos API and @Entity annotation
- **demo module**: Example simulations

## Status

This module is under development. Early version with core features implemented.

**Planned Features**:
- More statistical distributions
- Advanced queue discipline (priority, sorted)
- Transporter entities
- Advanced reporting and visualization
- Batch processing
- Variance reduction techniques
