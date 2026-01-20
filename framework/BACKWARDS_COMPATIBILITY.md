# Backwards Compatibility Policy

This document defines Prime-Mover's commitment to API stability and backwards compatibility.

## Semantic Versioning

Prime-Mover follows [Semantic Versioning 2.0.0](https://semver.org/) (MAJOR.MINOR.PATCH):

- **MAJOR version** increments indicate breaking API changes
  - Requires minimum 6-month deprecation notice
  - Migration guide provided
  - Rare occurrences, planned well in advance

- **MINOR version** increments add functionality in a backwards-compatible manner
  - New features and capabilities
  - Deprecations may be introduced
  - No breaking changes to existing code

- **PATCH version** increments are backwards-compatible bug fixes
  - Internal improvements
  - Performance enhancements
  - Security fixes

## API Stability Tiers

Prime-Mover classifies its APIs into stability tiers with different guarantees:

### Tier 1: Public API (Stable)

**Scope**: Classes and methods intended for simulation application code.

**Components**:
- `Kronos` static API (api module)
- `Controller` interface (api module)
- `Event` interface (api module)
- `@Entity`, `@Blocking`, `@Event`, `@NonEvent` annotations (api module)

**Guarantees**:
- Backwards compatible across all MINOR versions within a MAJOR version
- Minimum 2-release deprecation period before removal
- Deprecation warnings in javadoc and compilation output
- Migration path documented in release notes

**Example**:
```java
// This code written against v1.0.5 will work unchanged in v1.0.6, v1.1.0, etc.
@Entity
public class SimulationEntity {
    public void process() {
        Kronos.sleep(Duration.ofSeconds(1));
    }
}
```

### Tier 2: Simulation SPI (Stable)

**Scope**: Interfaces and classes used by bytecode transformation and entity lifecycle.

**Components**:
- `EntityReference` interface (api module)
- `@Transformed` annotation (api module)
- Transformed class contract (method ordinals, `__invoke`, `__signatureFor`)

**Guarantees**:
- Method ordinal numbers are deterministic and stable (see STABILITY.md)
- `EntityReference` interface contract maintained across MINOR versions
- Transformed bytecode format remains compatible
- Generated code structure backwards compatible

**Stability Details**:
- Method ordinals computed using stable SHA-256 hashing
- Ordinal collision detection prevents silent breakage
- `__invoke(int ordinal, Object[] args)` signature stable
- `__signatureFor(int ordinal)` signature stable

### Tier 3: Extension SPI (Experimental)

**Scope**: Interfaces for extending simulation runtime with blocking primitives.

**Components**:
- `Devi.post(EventImpl)` (runtime module)
- `Devi.swapCaller(EventImpl)` (runtime module)
- `EventImpl.getContinuation()` (runtime module)
- `EventImpl.setTime(long)` (runtime module)

**Guarantees**:
- **Experimental** - may change in MINOR versions
- Currently used only by desmoj-ish module
- Changes will be documented in release notes
- Future stabilization planned for v1.1.0

**Rationale**: These methods enable advanced blocking primitives (queues, resources, distributions) but are not yet finalized for general extension use.

### Tier 4: Internal API (Unstable)

**Scope**: Implementation details not intended for external use.

**Components**:
- `Devi` protected methods (runtime module)
- `EventImpl` package-private methods (runtime module)
- `SimulationController` internal state (runtime module)
- Transformation internals (transform module)

**Guarantees**:
- **No stability guarantees**
- May change in any release (MAJOR, MINOR, or PATCH)
- Changes documented in release notes for debugging purposes
- Use at your own risk

## Bytecode Compatibility

### Transformation Contract

The bytecode transformation process maintains these guarantees:

1. **Transformed Class Format**: Stable across MINOR versions
   - `@Transformed` annotation applied consistently
   - `EntityReference` interface implemented
   - `__invoke` and `__signatureFor` methods present

2. **Method Ordinal Stability**: Deterministic computation
   - Based on method signature (name, parameter types, return type)
   - Uses SHA-256 hash mod Integer.MAX_VALUE
   - Collision detection prevents ordinal reuse
   - Adding methods does not change existing ordinals

3. **Event Method Rewriting**: Stable pattern
   - `Kronos.*` calls rewritten to `Kairos.*` calls
   - Controller thread-local access pattern unchanged
   - Blocking event continuation mechanism stable

### Build Tool Compatibility

- **Maven Plugin**: Follows project versioning, compatible within MAJOR version
- **IntelliJ IDEA Plugin**: Requires IntelliJ 2025.3+ for JPS integration
- **Java Agent**: Runtime transformation compatible with build-time transformation

## Migration Paths

### v1.0.5 → v1.0.6 (Current Release)

**Status**: Fully backwards compatible, no code changes required.

**Changes**:
- Internal refactoring of blocking primitives to desmoj-ish module
- Visibility changes to Extension SPI (Tier 3) - experimental API
- No changes to Public API (Tier 1) or Simulation SPI (Tier 2)

**Action Required**: None. Recompile and redeploy.

### v1.0.6 → v1.1.0 (Future)

**Status**: Planned for Q2 2026.

**Expected Changes**:
- Stabilization of Extension SPI (Tier 3) for third-party blocking primitives
- Potential deprecation of legacy continuation patterns
- New controller variants for specialized simulation types

**Migration Plan**: Will include detailed migration guide, deprecation warnings, and example code updates.

## Platform Requirements

### Java Version Requirements

- **Minimum Runtime**: Java 21 (for Virtual Threads / Project Loom)
- **Recommended Runtime**: Java 25 (for ClassFile API native support)
- **Build Requirement**: Java 25+ (for ClassFile API during transformation)

**Compatibility Notes**:
- Virtual Threads are core to event continuation mechanism (Java 21+ required)
- ClassFile API eliminates ASM dependency (Java 25+ required for transformation)
- No backport to Java 17 or earlier planned (fundamental Loom dependency)

### IntelliJ IDEA Requirements

- **Minimum Version**: IntelliJ IDEA 2025.3
- **Reason**: JPS (IntelliJ build system) integration for bytecode transformation
- **Alternative**: Use Maven plugin for transformation outside IntelliJ

## Testing and Validation

### Compatibility Testing

Prime-Mover CI pipeline validates:
1. **Minimum Version**: Java 21 runtime, Java 25 build
2. **Maximum Version**: Latest stable JDK release
3. **Early Access**: Nightly tests against OpenJDK EA builds

### Release Validation Checklist

Before each release:
- [ ] All Tier 1 (Public API) tests pass on minimum and maximum Java versions
- [ ] Method ordinal stability verified across releases
- [ ] Transformed bytecode decompiles cleanly
- [ ] Maven plugin transformation matches agent transformation
- [ ] IntelliJ plugin integration tests pass
- [ ] Demo and desmoj-ish modules run without modification

## Support Window

### Active Support

- **Current Version** (v1.0.6): Full support including bug fixes, security patches, and feature development
- **Previous Version** (v1.0.5): Critical bug fixes and security patches only
- **Older Versions**: Community support only (GitHub issues, Stack Overflow)

### Long-Term Support (LTS)

Currently under evaluation. Potential LTS versions will be announced with:
- Extended support window (24+ months)
- Backported security fixes
- Enterprise support options

## Deprecation Process

When deprecating APIs:

1. **Announcement**: Minimum 2 MINOR releases before removal
2. **Deprecation Markers**: `@Deprecated` annotation with javadoc explaining alternative
3. **Migration Guide**: Published in release notes and project wiki
4. **Compatibility Period**: Deprecated API remains functional during deprecation window
5. **Removal**: Only in next MAJOR version

**Example Deprecation Notice**:
```java
/**
 * @deprecated Use {@link Kronos#blockingSleep(Duration)} instead.
 * Scheduled for removal in v2.0.0.
 */
@Deprecated(since = "1.1.0", forRemoval = true)
public static void sleep(long millis) {
    // Legacy implementation
}
```

## Known Limitations

### ClassFile API Dependency

- **Constraint**: Java 25+ required for bytecode transformation
- **Impact**: Build environment must use Java 25+
- **Workaround**: None planned (fundamental design choice to eliminate ASM dependency)

### Virtual Threads Requirement

- **Constraint**: Java 21+ required for runtime execution
- **Impact**: Cannot run simulations on Java 17 or earlier
- **Workaround**: None planned (Virtual Threads core to continuation mechanism)

### IntelliJ Plugin JPS Integration

- **Constraint**: IntelliJ IDEA 2025.3+ required for IDE-integrated transformation
- **Impact**: Older IntelliJ versions must use Maven plugin
- **Workaround**: Use Maven CLI for transformation, then refresh IntelliJ project

## Contact and Feedback

For backwards compatibility questions or concerns:
- GitHub Issues: [Prime-Mover Issues](https://github.com/Hellblazer/Prime-Mover/issues)
- Discussions: [Prime-Mover Discussions](https://github.com/Hellblazer/Prime-Mover/discussions)
- Security: See SECURITY.md for responsible disclosure process

## References

- [Method Ordinal Stability](STABILITY.md) - Detailed analysis of ordinal computation
- [Semantic Versioning 2.0.0](https://semver.org/) - Versioning specification
- [Java Platform Evolution](https://openjdk.org/jeps/0) - JDK enhancement proposals
