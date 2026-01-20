# Prime Mover Error Message Standard

## Overview

This document defines the standard format for error messages and exceptions throughout the Prime Mover framework to ensure consistency, clarity, and debuggability.

## Message Format

All error messages MUST follow this format:

```
[Component] Action failed: Reason - Context details
```

### Components

1. **[Component]**: The framework component raising the error
   - Examples: `[Devi]`, `[EventImpl]`, `[Framework]`, `[SimulationController]`, `[EntityGenerator]`

2. **Action failed**: What operation failed (past tense)
   - Examples: "Event evaluation failed", "Clone operation failed", "Event posting failed"

3. **Reason**: Why it failed (concise)
   - Examples: "No controller set", "Invalid event index", "Class file too large"

4. **Context details**: Relevant debugging information
   - Entity name (if available)
   - Current simulation time (if available)
   - Event signature (if available)
   - Method/class name
   - Parameter values (sanitized)

## Examples

### Good Error Messages

```java
// Framework - No controller
throw new IllegalStateException(
    "[Framework] No simulation controller set for current thread");

// EventImpl - Clone failed
throw new IllegalStateException(
    "[EventImpl] Clone operation failed for event: " + this, e);

// EntityGenerator - Invalid method index
throw new IllegalStateException(
    "[EntityGenerator] No index found for method: " + methodName +
    " in class " + className);

// Devi - Event evaluation failed with context
throw new SimulationException(
    "[Devi] Event evaluation failed at time " + currentTime +
    " for event: " + event, cause);
```

### Poor Error Messages (Don't Use)

```java
// Too vague
throw new IllegalStateException("Unknown event type");

// Internal implementation details leaked
throw new IllegalStateException("sailorMoon is already done");

// Not enough context
throw new IllegalStateException("error evaluating event");
```

## Exception Types

### IllegalStateException
Use when the framework is in an invalid state for the requested operation.

Examples:
- No controller set for current thread
- Event already evaluated
- Controller not running

### IllegalArgumentException
Use when method arguments are invalid.

Examples:
- Invalid event index
- Null entity reference
- Unknown primitive type
- Invalid method descriptor

### SimulationException
Use when a simulation-specific error occurs during event processing.

Examples:
- Event evaluation throws exception
- Entity method execution fails
- Continuation failure

### UnsupportedOperationException
Use for Kronos API methods that should have been transformed.

Example:
```java
throw new UnsupportedOperationException(
    "[Kronos] Method should have been transformed by bytecode processor - " +
    "ensure Maven plugin or Java agent is properly configured");
```

## Context-Rich Error Messages

### Include Entity Information
```java
throw new SimulationException(
    "[Devi] Event evaluation failed for entity " +
    event.getReference().getClass().getSimpleName() +
    " at time " + currentTime + ": " + event.getSignature(),
    cause);
```

### Include Timing Information
```java
log.error("[SimulationController] Event processing failed at time {}: {}",
          currentTime, event, exception);
```

### Include Method Context
```java
throw new IllegalStateException(
    "[EntityGenerator] Failed to generate bytecode for method " +
    methodName + " in class " + className + ": " + reason);
```

## Debug vs Production Messages

### Debug Mode
When debug flags are enabled (`debugEvents`, `trackEventSources`), include additional context:
- Stack traces for event source tracking
- Full event chains
- Internal state details

### Production Mode
In production, avoid:
- Internal implementation class names
- Sensitive data
- Overly verbose stack traces
- Excessive logging

Use info/warn for normal operation, error for failures.

## Logging Guidelines

### Use Appropriate Log Levels

```java
// Info - Normal simulation lifecycle
log.info("[SimulationController] Simulation started at time {}", startTime);
log.info("[SimulationController] Simulation ended at time {}", endTime);

// Warn - Recoverable issues
log.warn("[Devi] Event evaluation timeout at time {}: {}", currentTime, event);

// Error - Failures requiring attention
log.error("[Devi] Cannot evaluate event at time {}: {}", currentTime, event, exception);

// Debug - Detailed tracing (disabled by default)
log.debug("[EventImpl] Continuing event at time {}: {}", time, this);

// Trace - Very detailed internal operations (disabled by default)
log.trace("[Devi] Evaluating event: {}", event);
```

### Use SLF4J Parameterization

Always use `{}` placeholders instead of string concatenation:

```java
// CORRECT
log.error("[Devi] Event failed at time {}: {}", currentTime, event, exception);

// INCORRECT
log.error("[Devi] Event failed at time " + currentTime + ": " + event, exception);
```

## Testing Error Messages

Error messages should:
1. Be testable - catch exceptions and verify message format
2. Not change frequently (avoid breaking tests unnecessarily)
3. Include enough context to diagnose the issue from logs alone

Example test:
```java
@Test
void testNoControllerErrorMessage() {
    var exception = assertThrows(IllegalStateException.class,
                                () -> Framework.getController());
    assertTrue(exception.getMessage().startsWith("[Framework]"));
    assertTrue(exception.getMessage().contains("No simulation controller"));
}
```

## Migration Checklist

When updating error messages:
- [ ] Add `[Component]` prefix
- [ ] Use consistent action terminology (failed, missing, invalid)
- [ ] Include relevant context (time, entity, event)
- [ ] Remove internal implementation details
- [ ] Use appropriate exception type
- [ ] Update tests if message format changed
- [ ] Verify log level is appropriate

## Component Names

Standard component names for error messages:
- `[Framework]` - Framework.java (thread-local controller management)
- `[Devi]` - Devi.java (base controller)
- `[EventImpl]` - EventImpl.java (event representation)
- `[SimulationController]` - SimulationController.java
- `[RealTimeController]` - RealTimeController.java
- `[SteppingController]` - SteppingController.java
- `[EntityGenerator]` - EntityGenerator.java (bytecode transformation)
- `[Continuation]` - Continuation.java (blocking event continuations)
- `[Kairos]` - Kairos.java (internal runtime API)
- `[Kronos]` - Kronos.java (public simulation API)
