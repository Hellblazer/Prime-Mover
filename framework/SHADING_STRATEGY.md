# Shading Strategy for Prime Mover

This document explains the shading approach used in Prime Mover modules, particularly for the `sim-agent` and related transformation tooling.

## Why We Shade

### The ClassFile API Challenge

Prime Mover uses the Java ClassFile API (JEP 484, finalized in Java 25) for bytecode transformation. This modern API provides:
- Native Java bytecode manipulation without external dependencies
- Type-safe code generation
- Better integration with the Java module system

However, the ClassFile API presents a distribution challenge:

1. **Preview Feature History**: Prior to Java 25, ClassFile API was a preview feature requiring `--enable-preview`
2. **Module Encapsulation**: The API lives in `java.lang.classfile` which is part of `java.base` but has specific accessibility rules
3. **Version Compatibility**: Different JDK versions may have different API surfaces during the preview period

### Shading Rationale

For self-contained distribution of the `sim-agent` (and potentially the Maven plugin), we use the Maven Shade Plugin to create fat JARs that:

1. **Bundle all dependencies**: The agent JAR contains everything needed to run
2. **Avoid classpath conflicts**: Shaded classes have relocated package names
3. **Simplify deployment**: Single JAR deployment without dependency management

## What We Shade

### sim-agent Module

The `sim-agent` shades its dependencies to create a standalone Java agent:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.6.0</version>
    <configuration>
        <generateUniqueDependencyReducedPom>true</generateUniqueDependencyReducedPom>
        <useDependencyReducedPomInJar>true</useDependencyReducedPomInJar>
        <transformers>
            <transformer implementation="...ManifestResourceTransformer">
                <manifestEntries>
                    <Agent-Class>com.hellblazer.primeMover.agent.SimAgent</Agent-Class>
                    <Premain-Class>com.hellblazer.primeMover.agent.SimAgent</Premain-Class>
                    <Can-Redefine-Classes>true</Can-Redefine-Classes>
                    <Can-Retransform-Classes>true</Can-Retransform-Classes>
                </manifestEntries>
            </transformer>
        </transformers>
    </configuration>
</plugin>
```

**Shaded dependencies include:**
- `transform` module (EntityGenerator, ClassRemapper, ClassMetadata, etc.)
- `runtime` module (Kairos, Devi, SimulationController, etc.)
- `api` module (Kronos, annotations)
- SLF4J API (logging interface)

### Package Relocation Pattern

When package relocation is needed (e.g., to avoid conflicts with user code), the pattern is:

```
Original:         com.hellblazer.primeMover.classfile
Relocated:        com.hellblazer.primeMover.shaded.classfile
```

**Note**: The current configuration does not relocate packages because Prime Mover's own packages are unlikely to conflict with user code. Relocation would be added if:
- Users report classpath conflicts
- We shade third-party libraries that might conflict (e.g., SLF4J implementations)

## How It Works

### Build Process

1. **Compile Phase**: All modules compile normally against the ClassFile API
2. **Package Phase**: Shade plugin runs, creating uber-JAR
3. **Manifest Injection**: Agent manifest entries are added
4. **Dependency Reduction**: Original dependency JARs are excluded from final artifact

### Runtime Behavior

When the shaded agent JAR is used:

```bash
java -javaagent:sim-agent-1.0.6-SNAPSHOT.jar -jar myapp.jar
```

1. JVM loads the agent JAR
2. `premain()` method in `SimAgent` is invoked
3. `SimulationTransformerClassFileAPI` is registered as a class file transformer
4. As classes load, the transformer inspects and modifies bytecode
5. All transformation dependencies are resolved from within the shaded JAR

## Fat JAR Impact

### Size

The shaded sim-agent JAR is larger than a thin JAR:

| Component | Approximate Size |
|-----------|------------------|
| sim-agent classes | ~30 KB |
| transform module | ~50 KB |
| runtime module | ~40 KB |
| api module | ~10 KB |
| Total shaded JAR | ~130 KB |

This is acceptable for a deployment artifact but would be wasteful if used as a library dependency.

### Startup Time

Shading has minimal impact on startup:
- JAR is loaded once at agent attachment
- No additional classpath scanning
- Classes loaded on-demand from single JAR

### Debugging Implications

Shaded JARs can complicate debugging:
- Stack traces show shaded package names (if relocated)
- Source attachment may require configuration
- IDE debugging works but may show internal class names

**Recommendation**: For development, use the non-shaded module dependencies. Use the shaded JAR only for deployment.

## Alternatives Considered

### 1. Module Path Distribution

```bash
java --module-path mods:libs -m com.hellblazer.primeMover.agent
```

**Pros**: Clean module boundaries, explicit dependencies
**Cons**: Requires user to manage module path, more complex deployment

### 2. ClassPath with Thin JARs

```bash
java -cp "sim-agent.jar:transform.jar:runtime.jar:api.jar" ...
```

**Pros**: Smaller individual JARs, easier source attachment
**Cons**: Deployment complexity, potential for missing dependencies

### 3. Direct ClassFile API (No Shading)

Require users to have JDK 25+ with ClassFile API available.

**Pros**: Simplest build, smallest artifact
**Cons**: Version coupling, preview feature complications

### Decision

We chose shading for `sim-agent` because:
1. **Simplicity**: Single JAR deployment
2. **Reliability**: No dependency resolution at runtime
3. **Compatibility**: Works regardless of user's classpath setup

## Future Path

### When ClassFile API Becomes Stable

As of Java 25, ClassFile API is finalized (no longer preview). Future considerations:

1. **Minimum Java Version**: Prime Mover already requires Java 25+
2. **Potential Thin JAR Mode**: Could offer both shaded and thin JAR variants
3. **Maven Plugin**: May not need shading since Maven manages plugin dependencies

### Versioning Strategy

- Shaded JARs are versioned with the project (`1.0.6-SNAPSHOT`)
- No separate versioning for shaded vs non-shaded variants
- Dependency-reduced POM is generated for consumers who want thin JARs

## Related Documentation

- [sim-agent/README.md](../sim-agent/README.md) - Agent usage documentation
- [Maven Shade Plugin](https://maven.apache.org/plugins/maven-shade-plugin/) - Official documentation
- [JEP 484: Class-File API](https://openjdk.org/jeps/484) - ClassFile API specification

## License

GNU Affero General Public License v3.0 - See [LICENSE](../LICENSE) for details.
