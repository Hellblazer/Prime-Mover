# Blocking Primitives SPI Stability Contract

## Overview

This document defines the **stability guarantees** for the Blocking Primitives SPI. It establishes versioning policies, deprecation procedures, and migration guidance to ensure blocking primitive implementations remain compatible across Prime Mover releases.

**Contract Version**: 1.0
**Effective Date**: 2026-01-19 (Prime-Mover 1.0.5)
**Module**: `com.hellblazer.primeMover:runtime`

## Stability Tiers

### Tier 1: Stable API (Guaranteed)

These methods are **guaranteed stable** and will not change signature without major version increment:

| Class | Method | Signature |
|-------|--------|-----------|
| `Devi` | `post` | `public abstract void post(EventImpl event)` |
| `Devi` | `swapCaller` | `public EventImpl swapCaller(EventImpl newCaller)` |
| `EventImpl` | `getContinuation` | `public Continuation getContinuation()` |
| `EventImpl` | `setTime` | `public void setTime(long time)` |
| `Continuation` | `setReturnValue` | `public void setReturnValue(Object returnValue)` |

**Guarantee**: These methods will maintain binary and source compatibility within a major version.

### Tier 2: Supporting API (Stable)

These methods are used by the SPI but are not primary entry points:

| Class | Method | Signature |
|-------|--------|-----------|
| `Devi` | `getCurrentTime` | `public long getCurrentTime()` |
| `Continuation` | `setReturnState` | `public void setReturnState(Object returnValue, Throwable exception)` |
| `EventImpl` | `getCaller` | `public EventImpl getCaller()` |
| `EventImpl` | `setCaller` | `public void setCaller(EventImpl caller)` |

**Guarantee**: These methods are stable within a major version but may be marked internal in future releases.

### Tier 3: Internal API (Unstable)

Methods not documented in [BLOCKING_PRIMITIVES_SPI.md](BLOCKING_PRIMITIVES_SPI.md) are **internal** and may change without notice. Do not depend on them for blocking primitive implementations.

## Versioning Strategy

### Semantic Versioning

Prime Mover follows [Semantic Versioning 2.0.0](https://semver.org/):

```
MAJOR.MINOR.PATCH
```

| Version Component | SPI Impact |
|-------------------|------------|
| **MAJOR** (e.g., 1.x.x to 2.0.0) | May introduce breaking SPI changes |
| **MINOR** (e.g., 1.0.x to 1.1.0) | Adds new SPI methods, backwards compatible |
| **PATCH** (e.g., 1.0.0 to 1.0.1) | Bug fixes only, no SPI changes |

### SPI Version Number

The SPI has its own version number documented in [BLOCKING_PRIMITIVES_SPI.md](BLOCKING_PRIMITIVES_SPI.md):

| SPI Version | Runtime Version | Description |
|-------------|-----------------|-------------|
| 1.0 | 1.0.5+ | Initial formalization |

SPI version increments independently when:
- New methods are added to the SPI (minor increment)
- Existing methods change signature (major increment)

## Deprecation Policy

### Phase 1: Announcement (MINOR release)

When an SPI method needs to change:

1. Add `@Deprecated(since = "X.Y", forRemoval = true)` annotation
2. Add Javadoc `@deprecated` tag with migration guidance
3. Document in release notes
4. Log warning at runtime if deprecated method is called

**Example**:
```java
/**
 * @deprecated since 1.1, for removal in 2.0. Use {@link #newMethod()} instead.
 */
@Deprecated(since = "1.1", forRemoval = true)
public void oldMethod() {
    logger.warn("oldMethod() is deprecated, use newMethod() instead");
    // Implementation continues to work
}
```

### Phase 2: Migration Period (MINOR releases)

During migration period (minimum 2 minor releases):

1. Both old and new methods available
2. Old method delegates to new method
3. Migration guide provided
4. Warnings logged

### Phase 3: Removal (MAJOR release)

At next major version:

1. Deprecated method removed
2. Breaking change documented in CHANGELOG
3. Migration path clearly documented

## Breaking Change Criteria

A **breaking change** requires MAJOR version increment:

1. **Signature Change**: Method parameter types or return type modified
2. **Behavioral Change**: Observable behavior differs for same inputs
3. **Removal**: Method removed entirely
4. **Threading Model Change**: Thread-safety guarantees modified

A **non-breaking change** can occur in MINOR version:

1. **New Method**: Additional SPI method added
2. **Implementation Detail**: Internal optimization without behavioral change
3. **Documentation**: Clarifications to existing documentation

## Migration Guidelines

### If SPI Changes in Major Version

Follow this migration process:

1. **Review Release Notes**: Check CHANGELOG for SPI changes
2. **Update Dependencies**: Bump runtime dependency version
3. **Address Deprecations**: Replace deprecated method calls
4. **Test Thoroughly**: Run full test suite against new version
5. **Verify Behavior**: Ensure blocking primitives work correctly

### Example Migration

If `swapCaller` signature changed in hypothetical 2.0:

**Before (1.x)**:
```java
public EventImpl swapCaller(EventImpl newCaller);
```

**After (2.0)**:
```java
public Optional<EventImpl> swapCaller(EventImpl newCaller);
```

**Migration**:
```java
// 1.x code
var waiter = controller.swapCaller(null);

// 2.0 code
var waiter = controller.swapCaller(null).orElse(null);
```

## Compatibility Matrix

| Blocking Primitive Version | Runtime 1.0.x | Runtime 1.1.x | Runtime 2.x |
|---------------------------|---------------|---------------|-------------|
| desmoj-ish 1.0.x | Compatible | Compatible | See migration |
| desmoj-ish 1.1.x | Compatible | Compatible | See migration |
| desmoj-ish 2.x | N/A | N/A | Compatible |

## Testing Requirements

### For SPI Consumers (Blocking Primitive Implementers)

1. **Unit Tests**: Test each SPI method in isolation
2. **Integration Tests**: Test complete blocking/resume cycles
3. **Version Tests**: Test against minimum supported runtime version

### For Runtime Maintainers

1. **SPI Contract Tests**: Automated tests verifying SPI stability
2. **Binary Compatibility Tests**: Ensure old bytecode works with new runtime
3. **Performance Tests**: Monitor for SPI method performance regression

## Exception Handling

SPI methods may throw:

| Exception | When | Action |
|-----------|------|--------|
| `NullPointerException` | Invalid null argument | Fix caller code |
| `IllegalStateException` | Called outside event context | Ensure proper context |
| `SimulationException` | Simulation error propagation | Handle or rethrow |

Exception types are part of the SPI contract and will not change within major version.

## Thread Safety Contract

The SPI threading model is **guaranteed stable**:

1. **Single Event Evaluation**: Serializer semaphore enforces one-at-a-time
2. **Virtual Thread Execution**: Each event in own virtual thread
3. **Safe Context**: SPI methods safe within event processing

Changes to threading model require MAJOR version increment.

## Support Policy

| Runtime Version | Support Status | SPI Support |
|-----------------|----------------|-------------|
| 1.0.x | Active | Full SPI 1.0 support |
| 0.x | Legacy | No SPI guarantees |

## Contact and Feedback

For SPI-related issues:

1. **Bug Reports**: GitHub Issues with `[SPI]` prefix
2. **Feature Requests**: GitHub Discussions
3. **Security Issues**: Report via GitHub Security Advisory

## Document History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2024 | Initial stability contract |

---

**Commitment**: The Prime Mover project commits to following this stability contract. Breaking this contract requires explicit justification and community discussion before any MAJOR release that affects the SPI.
