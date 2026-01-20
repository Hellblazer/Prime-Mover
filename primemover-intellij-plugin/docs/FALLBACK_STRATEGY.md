# Prime Mover IntelliJ Plugin: Fallback Strategy

This document provides comprehensive guidance for handling failures in the JPS plugin transformation pipeline and the strategies for recovery.

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Failure Scenarios](#failure-scenarios)
3. [Fallback Implementation Strategies](#fallback-implementation-strategies)
4. [Decision Matrix](#decision-matrix)
5. [Recovery Procedures](#recovery-procedures)
6. [Monitoring and Diagnostics](#monitoring-and-diagnostics)

---

## Executive Summary

The Prime Mover IntelliJ plugin performs bytecode transformation of `@Entity` annotated classes during JPS (JetBrains Project System) build. When this transformation fails, three fallback strategies exist:

| Priority | Strategy | Latency | Complexity | Coverage |
|----------|----------|---------|------------|----------|
| Primary | Runtime Agent (sim-agent) | +50-100ms startup | Low | Full |
| Secondary | Maven Plugin | Build-time | Medium | Full |
| Tertiary | Manual Transformation | Build-time | High | Full |

**Recommended Approach**: Enable automatic sim-agent injection in plugin settings as a safety net for any JPS transformation failures.

---

## Failure Scenarios

### 1. Annotation Scanning Failures

**Description**: The `SimulationTransform` class fails to detect `@Entity` annotations on compiled classes.

**Root Causes**:
- ClassFile API parsing errors on malformed bytecode
- Class file version incompatibility (future JDK versions)
- Missing annotation retention (must be `RUNTIME`)
- Corrupted `.class` files in output directory

**Detection Strategy**:
```
Build Messages:
- "Prime Mover: 0 classes transformed, 0 skipped"
- Classes with @Entity annotation exist but no transformation occurs

Runtime Symptoms:
- UnsupportedOperationException from Kronos static methods
- Entity method calls execute synchronously instead of as events
```

**Impact Assessment**:
- **Severity**: Critical - simulation code will fail at runtime
- **Scope**: All entity classes in affected module
- **User Experience**: Application crashes or incorrect simulation behavior

**Remediation Steps**:
1. Verify annotation retention: `@Retention(RetentionPolicy.RUNTIME)`
2. Check class file version compatibility with current JDK
3. Clean build: `./gradlew clean build` or `mvn clean install`
4. Inspect build output for corrupted class files
5. Check `ClassScanner` output for annotation discovery

**Fallback Approach**:
- Enable sim-agent auto-injection in plugin settings
- The runtime agent performs identical annotation scanning

---

### 2. Entity Transformation Failures

**Description**: The `EntityGenerator` fails to transform an `@Entity` class after successful detection.

**Root Causes**:
- Invalid method signatures (unsupported parameter/return types)
- Circular dependencies in entity class hierarchy
- ClassFile API bytecode generation errors
- Method descriptor parsing failures
- Large class files exceeding 10MB limit

**Detection Strategy**:
```
Build Messages:
- "Failed to create EntityGenerator for [ClassName]"
- "Transformation failed: [ExceptionType]: [message]"
- "Entity class [X] has no entity interfaces and no AllMethodsMarker annotation"

IDE Indicators:
- Red error markers in Build Tool Window
- CompilerMessage with Kind.ERROR
```

**Impact Assessment**:
- **Severity**: Critical - affected entity unusable
- **Scope**: Single entity class (other entities may work)
- **User Experience**: Partial application failure

**Remediation Steps**:
1. Review entity class for unsupported constructs:
   - Synthetic methods
   - Bridge methods
   - Generic type erasure edge cases
2. Check for `@NonEvent` annotation on problematic methods
3. Simplify entity interface hierarchy
4. Split large entity classes
5. Verify all entity interfaces are accessible at compile time

**Fallback Approach**:
- Use `@NonEvent` to exclude problematic methods
- Split entity into smaller classes
- Fall back to sim-agent for runtime transformation

---

### 3. ClassFile API Incompatibilities

**Description**: Java ClassFile API (JEP 484) behaves unexpectedly or is unavailable.

**Root Causes**:
- JDK version mismatch (requires Java 25+)
- Internal ClassFile API changes in future JDK releases
- Preview feature flag changes
- Platform-specific bytecode variations

**Detection Strategy**:
```
Build Messages:
- NoClassDefFoundError for java.lang.classfile.* classes
- "UnsupportedClassVersionError"
- "IllegalAccessError" accessing ClassFile API

JVM Arguments:
- Missing --enable-preview flag (if still preview)
```

**Impact Assessment**:
- **Severity**: Critical - no transformation possible
- **Scope**: All entities in all modules
- **User Experience**: Complete transformation failure

**Remediation Steps**:
1. Verify JDK version: `java -version` (must be 25+)
2. Check Gradle/Maven JDK configuration
3. Verify IntelliJ is using correct JDK for JPS builds
4. Add `--enable-preview` if ClassFile API is still preview

**Fallback Approach**:
- Use sim-agent (same ClassFile API, but runtime)
- If JDK issue, use Maven plugin with alternative bytecode library
- Consider ASM-based transformation as emergency fallback

---

### 4. Incremental Compilation Issues

**Description**: JPS incremental compilation causes transformation inconsistencies.

**Root Causes**:
- Stale `.class` files not recompiled
- `@Transformed` annotation incorrectly present/absent
- Class dependency tracking failures
- Partial builds leaving inconsistent state

**Detection Strategy**:
```
Runtime Symptoms:
- "Already transformed" classes fail verification
- Method ordinal mismatches between entity versions
- ClassCastException on EntityReference
- Switch case index out of bounds

Build Indicators:
- "Prime Mover: 0 classes transformed, X skipped (already transformed)"
  when classes should be retransformed
```

**Impact Assessment**:
- **Severity**: High - intermittent failures
- **Scope**: Modified entities and their dependents
- **User Experience**: Unpredictable behavior, hard to diagnose

**Remediation Steps**:
1. **Clean rebuild**: Delete output directories and rebuild
   ```bash
   ./gradlew clean build
   # or
   mvn clean install
   # or in IntelliJ
   Build > Rebuild Project
   ```

2. **Invalidate caches**: File > Invalidate Caches / Restart

3. **Check timestamp consistency**:
   - Verify `@Transformed(timestamp=...)` matches build time
   - Look for future-dated timestamps

4. **Force retransformation**:
   - Delete specific `.class` files and rebuild
   - Modify source file to trigger recompilation

**Fallback Approach**:
- sim-agent handles incremental issues at runtime
- Always performs fresh transformation on class load
- No dependency on build-time state

---

### 5. IDE Sandbox Environment Conflicts

**Description**: JPS plugin execution fails due to IntelliJ sandbox restrictions.

**Root Causes**:
- Class loader isolation preventing transform module access
- Missing dependencies in JPS plugin classpath
- Security manager restrictions
- Conflicting plugin versions

**Detection Strategy**:
```
Build Messages:
- ClassNotFoundException for transform classes
- "NoClassDefFoundError: com/hellblazer/primeMover/classfile/..."
- SecurityException on file operations

IDE Logs (~/.IntelliJIdea/system/log/idea.log):
- Plugin loading errors
- Class loader chain failures
```

**Impact Assessment**:
- **Severity**: Critical - plugin non-functional
- **Scope**: All projects in IDE
- **User Experience**: Plugin appears to do nothing

**Remediation Steps**:
1. **Verify plugin installation**:
   - Settings > Plugins > Installed > Prime Mover
   - Check version compatibility

2. **Check JPS dependencies**:
   - jps-plugin must bundle transform module
   - Verify `build.gradle` dependency configuration

3. **Review plugin descriptor**:
   - `jps-plugin/src/main/resources/META-INF/services/`
   - Must contain `org.jetbrains.jps.incremental.BuilderService`

4. **Test in development sandbox**:
   ```bash
   ./gradlew runIde
   ```

5. **Check for plugin conflicts**:
   - Disable other bytecode manipulation plugins
   - Test with minimal plugin set

**Fallback Approach**:
- Use Maven plugin for build-time transformation
- Enable sim-agent for IDE run configurations
- Manual class transformation for debugging

---

## Fallback Implementation Strategies

### Strategy 1: Runtime Agent (sim-agent) - PRIMARY FALLBACK

**Overview**: The sim-agent Java agent performs identical transformations at class load time.

**Activation**:
1. **Automatic** (recommended):
   - Plugin Settings > Prime Mover > Enable "Auto-add agent to run configurations"
   - Plugin automatically adds `-javaagent:sim-agent.jar` to Java run configurations

2. **Manual**:
   - Edit Run Configuration > VM Options
   - Add: `-javaagent:/path/to/sim-agent-1.0.x.jar`

**How It Works**:
```
1. JVM loads application class
2. SimAgent.premain() registers ClassFileTransformer
3. For each class load:
   a. AnnotationScanner checks for @Entity
   b. If entity and not @Transformed:
      - EntityGenerator transforms bytecode
      - ClassRemapper rewrites Kronos -> Kairos
   c. If non-entity but uses Kronos:
      - Only applies Kronos -> Kairos remapping
4. Transformed bytecode returned to JVM
```

**Trade-offs**:

| Aspect | Build-time (JPS) | Runtime (sim-agent) |
|--------|------------------|---------------------|
| Startup latency | None | +50-100ms |
| Debug experience | Full line mapping | Full line mapping |
| Hot reload | Requires rebuild | Automatic |
| CI/CD builds | Transformed in artifact | Requires agent in runtime |
| Memory overhead | None at runtime | Transform cache |

**Configuration**:
```properties
# In plugin settings (primemover.xml)
<option name="autoAddAgent" value="true" />
```

**Verification**:
- Look for log message: `[SimAgent] Premain - installing transformer`
- Entity methods should dispatch through Controller

---

### Strategy 2: Maven Plugin - SECONDARY FALLBACK

**Overview**: Use the Maven build to perform transformation instead of JPS.

**Activation**:
1. Ensure `primemover-maven-plugin` is in pom.xml
2. Disable JPS transformation in plugin settings
3. Use Maven build instead of IDE build

**Configuration** (pom.xml):
```xml
<plugin>
    <groupId>com.hellblazer.primeMover</groupId>
    <artifactId>primemover-maven-plugin</artifactId>
    <version>${primemover.version}</version>
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

**Trade-offs**:

| Aspect | JPS Plugin | Maven Plugin |
|--------|------------|--------------|
| IDE integration | Seamless | Requires Maven sync |
| Build speed | Incremental | Full module rebuild |
| Configuration | Plugin settings | pom.xml |
| CI/CD | N/A | Standard |

**Workflow**:
1. Make code changes in IDE
2. Run `mvn compile` or use IDE's Maven panel
3. IDE automatically picks up transformed classes

**Verification**:
- Check Maven build output for transformation messages
- Verify `@Transformed` annotation on compiled classes

---

### Strategy 3: Manual Transformation - LAST RESORT

**Overview**: Programmatically transform classes outside the build system.

**When to Use**:
- Debugging transformation issues
- One-off transformation testing
- Emergency recovery

**Implementation**:
```java
import com.hellblazer.primeMover.classfile.SimulationTransform;
import java.nio.file.Files;
import java.nio.file.Path;

public class ManualTransformer {
    public static void transform(Path classesDir) throws Exception {
        try (var transform = new SimulationTransform(classesDir)) {
            var transformed = transform.transformed();

            for (var entry : transformed.entrySet()) {
                var classMetadata = entry.getKey();
                var bytecode = entry.getValue();

                // Compute output path
                var className = classMetadata.getName();
                var relativePath = className.replace('.', '/') + ".class";
                var outputPath = classesDir.resolve(relativePath);

                // Write transformed bytecode
                Files.write(outputPath, bytecode);
                System.out.println("Transformed: " + className);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: ManualTransformer <classes-directory>");
            System.exit(1);
        }
        transform(Path.of(args[0]));
    }
}
```

**Execution**:
```bash
# Compile the transformer
javac -cp transform.jar ManualTransformer.java

# Run transformation
java -cp .:transform.jar:api.jar:runtime.jar ManualTransformer target/classes
```

**Trade-offs**:
- Maximum control and visibility
- Requires manual execution
- No IDE integration
- Useful for debugging only

---

## Decision Matrix

Use this matrix to select the appropriate fallback strategy:

| Symptom | Likely Cause | Recommended Fallback |
|---------|--------------|---------------------|
| No classes transformed, no errors | Annotation scanning | sim-agent |
| Transform error for specific class | EntityGenerator issue | Fix class, or sim-agent |
| NoClassDefFoundError on ClassFile API | JDK version | Upgrade JDK, then Maven |
| Intermittent failures | Incremental compilation | Clean build, then sim-agent |
| Plugin not loading | Sandbox/classpath | Reinstall plugin, Maven fallback |
| All strategies fail | Unknown | Manual transformation for diagnosis |

### Automatic Fallback Chain

The plugin can be configured to automatically fall back:

```
JPS Transformation
    |
    v (on failure)
sim-agent Auto-injection (if enabled)
    |
    v (if agent not found)
Warning: "Transformation failed, no fallback available"
```

**Enable automatic fallback**:
```
Settings > Prime Mover >
  [x] Enable transformation (JPS)
  [x] Auto-add agent to run configurations (fallback)
  [x] Show notifications on project open
```

---

## Recovery Procedures

### Procedure 1: Complete Recovery

When transformation is completely broken:

1. **Stop all builds**
2. **Clean all outputs**:
   ```bash
   ./gradlew clean
   rm -rf out/ build/ target/
   ```
3. **Invalidate IDE caches**:
   - File > Invalidate Caches / Restart > Invalidate and Restart
4. **Verify JDK**:
   - File > Project Structure > Project > SDK
   - Must be JDK 25+
5. **Rebuild**:
   - Build > Rebuild Project
6. **If still failing**, enable sim-agent fallback

### Procedure 2: Single Class Recovery

When one entity fails transformation:

1. **Identify the class** from error messages
2. **Check for unsupported constructs**:
   - Varargs methods with complex types
   - Methods with >255 parameters
   - Recursive generic types
3. **Simplify or annotate**:
   ```java
   @NonEvent  // Exclude from transformation
   public void problematicMethod() { ... }
   ```
4. **Rebuild the module**
5. **If still failing**, use sim-agent for that module

### Procedure 3: Incremental State Recovery

When incremental builds cause issues:

1. **Identify affected classes**:
   ```bash
   find target/classes -name "*.class" -exec javap -v {} \; | grep -l Transformed
   ```
2. **Compare timestamps**:
   - Check `@Transformed(timestamp=...)` values
   - Should match last build time
3. **Force recompilation**:
   - Touch source files: `touch src/**/*.java`
   - Or delete specific `.class` files
4. **Rebuild**

---

## Monitoring and Diagnostics

### Build Output Analysis

**Healthy transformation**:
```
Prime Mover: 5 classes transformed, 0 skipped (already transformed)
Transformed: com.example.MyEntity
Transformed: com.example.AnotherEntity
...
```

**Warning signs**:
```
Prime Mover: 0 classes transformed, 0 skipped
# No entities detected - check annotation retention

Prime Mover: 0 classes transformed, 5 skipped (already transformed)
# All already transformed - incremental build working OR stale classes

Failed to transform entity class: com.example.MyEntity - [error]
# Transformation error - check class structure
```

### Runtime Verification

**Check if transformation was applied**:
```java
// In test or debug code
Class<?> entityClass = MyEntity.class;

// Check for @Transformed annotation
boolean transformed = entityClass.isAnnotationPresent(
    com.hellblazer.primeMover.annotations.Transformed.class);

// Check for EntityReference interface
boolean implementsRef =
    com.hellblazer.primeMover.api.EntityReference.class.isAssignableFrom(entityClass);

System.out.println("Transformed: " + transformed);
System.out.println("Implements EntityReference: " + implementsRef);
```

**Check sim-agent activation**:
```java
// Look for agent log at startup
// Should see: "[SimAgent] Premain - installing transformer"

// Or check programmatically
String agents = System.getProperty("sun.java.command");
boolean hasAgent = agents != null && agents.contains("sim-agent");
```

### IDE Diagnostics

**Check Build Tool Window**:
- View > Tool Windows > Build
- Look for Prime Mover Instrumenter messages

**Check Event Log**:
- View > Tool Windows > Event Log
- Filter for "Prime Mover"

**Check IDE Logs**:
- Help > Show Log in Finder/Explorer
- Search for `PrimeMover` or `primemover`

---

## Appendix: Error Message Reference

| Error Message | Cause | Solution |
|--------------|-------|----------|
| "Entity class X has no entity interfaces and no AllMethodsMarker annotation" | @Entity without value and no public methods | Add public methods or specify interfaces |
| "Failed to write transformed class X" | File I/O error | Check disk space, permissions |
| "No bytes found for class X" | ClassMetadata not populated | Clean rebuild |
| "Unknown event index for class X" | Ordinal mismatch | Clean rebuild |
| "Class file exceeds maximum size" | Entity class > 10MB | Split class |
| "NoClassDefFoundError: java/lang/classfile/ClassFile" | JDK < 25 | Upgrade JDK |

---

## Document Metadata

- **Version**: 1.0
- **Last Updated**: 2026-01-19
- **Applies To**: Prime Mover IntelliJ Plugin 1.0.x
- **Related Beads**: BEAD-l5w (Fallback Strategy)
