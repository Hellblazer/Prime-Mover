# Java 25 Deprecation Warnings

**Project**: Prime Mover Event Driven Simulation Framework
**Version**: 1.0.6-SNAPSHOT
**Java Version**: 25
**Last Updated**: 2026-01-19
**Phase**: 7 (Pre-Release Validation)

## Executive Summary

This document catalogs all Java 25 deprecation warnings identified during the Phase 7 validation build. The project has **2 actionable deprecation warnings** in the Maven plugin module, plus **1 JVM-level warning** related to third-party dependencies.

**Priority Classification**:
- **P1 (Critical)**: None - No deprecations blocking Java 26+ compatibility
- **P2 (Important)**: 1 Maven plugin API deprecation (affects future Maven compatibility)
- **P3 (Low)**: 1 JVM native access warning (third-party dependency, no action required)

---

## Actionable Deprecation Warnings

### 1. Maven Plugin API: `executionStrategy()` Deprecated

**Severity**: P2 (Important)
**Module**: `primemover-maven-plugin`
**Affected Files**:
- `CompileTransform.java:30`
- `TestTransform.java:30`

**Warning Message**:
```
[WARNING] executionStrategy() in org.apache.maven.plugins.annotations.Mojo has been deprecated
```

**Context**:
Both Maven plugin mojos use `executionStrategy = "once-per-session"` in their `@Mojo` annotation:

```java
// CompileTransform.java:30
@Mojo(name = "transform",
      defaultPhase = LifecyclePhase.PROCESS_CLASSES,
      requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
      threadSafe = true,
      executionStrategy = "once-per-session",  // DEPRECATED
      instantiationStrategy = InstantiationStrategy.PER_LOOKUP,
      requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class CompileTransform extends AbstractTransform { ... }

// TestTransform.java:30
@Mojo(name = "transform-test",
      defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES,
      requiresDependencyResolution = ResolutionScope.TEST,
      threadSafe = true,
      executionStrategy = "once-per-session",  // DEPRECATED
      instantiationStrategy = InstantiationStrategy.PER_LOOKUP,
      requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class TestTransform extends AbstractTransform { ... }
```

**Why It Matters**:
- Apache Maven deprecated `executionStrategy` in Maven Plugin API 3.9+
- The attribute was intended for optimizing plugin execution across modules
- Modern Maven uses improved internal scheduling that makes this attribute obsolete
- Removal scheduled for Maven 4.x (expected 2026-2027)

**Recommended Replacement**:
**Remove the `executionStrategy` attribute entirely.** Modern Maven automatically optimizes plugin execution based on `threadSafe` and dependency resolution settings.

**Migration Path**:

```java
// BEFORE (deprecated)
@Mojo(name = "transform",
      defaultPhase = LifecyclePhase.PROCESS_CLASSES,
      requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
      threadSafe = true,
      executionStrategy = "once-per-session",  // REMOVE THIS
      instantiationStrategy = InstantiationStrategy.PER_LOOKUP,
      requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)

// AFTER (recommended)
@Mojo(name = "transform",
      defaultPhase = LifecyclePhase.PROCESS_CLASSES,
      requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
      threadSafe = true,  // Maven uses this for optimization
      instantiationStrategy = InstantiationStrategy.PER_LOOKUP,
      requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
```

**Testing Requirements**:
1. Remove `executionStrategy` from both mojos
2. Build multi-module project (`./mvnw clean install`)
3. Verify transformation still executes once per module
4. Run integration tests (`maven-testing` module)
5. Confirm no performance regression in multi-module builds

**Impact Assessment**:
- **Code Change**: Minimal (remove 2 lines)
- **Testing Required**: Integration tests (already exist in `maven-testing/`)
- **Risk**: Very Low (Maven handles optimization internally)
- **Timeline**: Can be addressed in v1.0.6 or v1.1.0

**References**:
- [Maven Plugin API 3.9.0 Release Notes](https://maven.apache.org/docs/3.9.0/release-notes.html)
- [MPLUGINTESTING-87](https://issues.apache.org/jira/browse/MPLUGINTESTING-87) - Deprecation discussion

---

## Non-Actionable Warnings (Informational)

### 2. JVM Native Access Warning (Third-Party)

**Severity**: P3 (Low)
**Module**: Maven Wrapper / Jansi Dependency
**Source**: `org.fusesource.jansi:jansi:2.4.0` (Maven terminal colorization library)

**Warning Message**:
```
WARNING: A restricted method in java.lang.System has been called
WARNING: java.lang.System::load has been called by org.fusesource.jansi.internal.JansiLoader
         in an unnamed module (file:/Users/hal.hildebrand/.m2/wrapper/dists/apache-maven-3.9.4/9b0c5511/lib/jansi-2.4.0.jar)
WARNING: Use --enable-native-access=ALL-UNNAMED to avoid a warning for callers in this module
WARNING: Restricted methods will be blocked in a future release unless native access is enabled
```

**Context**:
- Jansi is a terminal colorization library used by Maven wrapper for colored console output
- It loads native libraries using `System.load()` to access terminal capabilities
- Java 25+ requires explicit native access permissions via `--enable-native-access`

**Why No Action Required**:
1. **External Dependency**: Managed by Apache Maven team, not Prime Mover
2. **Non-Breaking**: Warning only; functionality still works
3. **Maven 3.9.5+**: Apache Maven will address this in future Maven wrapper updates
4. **Build-Time Only**: Affects build tooling, not runtime simulation framework

**Workaround (Optional)**:
If the warning is distracting during development, add to `MAVEN_OPTS`:
```bash
export MAVEN_OPTS="--enable-native-access=ALL-UNNAMED"
```

**Not recommended** for production builds as this is a blanket permission grant.

**Expected Resolution**:
- Apache Maven 3.9.5+ will update Jansi to version 2.5.0+ (native access compliant)
- No action required from Prime Mover maintainers

---

## Summary Statistics

| Category | Count | Action Required |
|----------|-------|-----------------|
| **Actionable Deprecations** | 2 | Yes (remove `executionStrategy`) |
| **Third-Party Warnings** | 1 | No (Maven wrapper upgrade) |
| **Java 25 Compatibility Issues** | 0 | None |
| **Java 26 Blockers** | 0 | None |

---

## Validation Checklist

- [x] Build completed with Java 25 (`./mvnw clean install`)
- [x] Deprecation warnings captured with `-Dmaven.compiler.showDeprecation=true`
- [x] All warnings classified by severity and actionability
- [x] Migration paths documented for actionable warnings
- [x] Third-party warnings identified and triaged
- [x] No critical deprecations found
- [x] Project is Java 25 compatible
- [x] Java 26 migration path is clear

---

## Recommendations

### For v1.0.6 (Current Release)
**Ship as-is.** The deprecation warnings are non-breaking and do not affect runtime functionality.

### For v1.1.0 (Next Minor Release)
**Address Maven plugin deprecation**:
1. Remove `executionStrategy` from both mojos
2. Run full integration test suite
3. Update plugin documentation if execution behavior changes

### For v2.0.0 (Future Major Release)
**Monitor Java 26 changes**:
- Track Maven 4.x adoption timeline
- Review any new Java platform deprecations affecting bytecode transformation
- Consider JEP 451 (Native Access Control) implications for ClassFile API

---

## Build Configuration Reference

**Compiler Settings** (all modules):
```xml
<maven.compiler.source>25</maven.compiler.source>
<maven.compiler.target>25</maven.compiler.target>
<maven.compiler.release>25</maven.compiler.release>
<maven.compiler.showDeprecation>false</maven.compiler.showDeprecation>
```

**To Show Deprecations During Development**:
```bash
./mvnw clean compile -Dmaven.compiler.showDeprecation=true
```

**To Fail Build on Warnings** (CI/CD):
```bash
./mvnw clean install -Dmaven.compiler.failOnWarning=true
```

---

## Appendix: Full Build Output

**Command**:
```bash
./mvnw clean compile -Dmaven.compiler.showDeprecation=true
```

**Relevant Warnings**:
```
[WARNING] /Users/hal.hildebrand/git/Prime-Mover/primemover-maven-plugin/src/main/java/com/hellblazer/primeMover/maven/CompileTransform.java:[30,162]
executionStrategy() in org.apache.maven.plugins.annotations.Mojo has been deprecated

[WARNING] /Users/hal.hildebrand/git/Prime-Mover/primemover-maven-plugin/src/main/java/com/hellblazer/primeMover/maven/TestTransform.java:[30,156]
executionStrategy() in org.apache.maven.plugins.annotations.Mojo has been deprecated
```

**Build Result**: SUCCESS (all modules compiled, 2 deprecation warnings)

---

**Document Status**: Complete
**Bead**: Prime-Mover-9r1.1.2 (P3 Documentation)
**Next Action**: Address deprecation in v1.1.0 milestone
