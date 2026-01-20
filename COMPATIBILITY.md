# Prime Mover Compatibility Matrix

This document provides version compatibility information for the Prime Mover event-driven simulation framework.

## Quick Reference

| Prime-Mover | Java | IntelliJ IDEA | Maven | Gradle |
|-------------|------|---------------|-------|--------|
| 1.0.6+ | 25+ | 2025.3+ | 3.8.3+ | 9.1+ |
| 1.0.5 | 25+ | 2025.3+ | 3.8.3+ | 9.1+ |
| 1.0.4 | 24+ | N/A | 3.8.3+ | N/A |
| 1.0.3 | 24+ | N/A | 3.8.3+ | N/A |
| 1.0.0-1.0.2 | 24+ | N/A | 3.8.3+ | N/A |

## JDK Version Support

### Requirements

- **Minimum**: Java 25 (for v1.0.5+) - ClassFile API requirement
- **Recommended**: GraalVM 25 for optimal performance
- **Previous**: Java 24 (for v1.0.0-1.0.4)

### Why Java 25?

Prime Mover v1.0.5+ uses the Java ClassFile API (`jdk.classfile`) for bytecode transformation. This API:

- Was preview in Java 22-24
- Became stable/final in Java 25
- Replaces external dependencies (ASM, Javassist) with native JDK functionality
- Provides better forward compatibility with future Java versions

### Testing Status

| JDK Distribution | Version | Status |
|------------------|---------|--------|
| Oracle GraalVM | 25 | Fully tested |
| Oracle OpenJDK | 25 | Fully tested |
| Eclipse Temurin | 25 | Expected compatible |
| Amazon Corretto | 25 | Expected compatible |

### Future Compatibility

- **Java 26+**: Expected forward compatible (ClassFile API is stable)
- No breaking changes anticipated for minor Java versions

## IDE Integration

### IntelliJ IDEA

| IDE Version | Plugin Support | Notes |
|-------------|----------------|-------|
| 2025.3+ (build 253+) | Full support | JPS ModuleLevelBuilder |
| 2024.x and earlier | Not supported | Use Maven plugin fallback |

**Plugin Features**:
- Automatic post-compile transformation
- Auto-detection of Prime Mover projects
- Run configuration enhancement (-javaagent injection)
- Incremental compilation support
- Duplicate transformation warnings

**Installation**: See [primemover-intellij-plugin/README.md](./primemover-intellij-plugin/README.md)

### Other IDEs

| IDE | Support Level | Approach |
|-----|---------------|----------|
| Eclipse | Via Maven | Use Maven plugin for transformation |
| VS Code | Via Maven | Use Maven plugin for transformation |
| NetBeans | Via Maven | Use Maven plugin for transformation |

For IDEs without native plugin support, the Maven plugin provides build-time transformation.

## Build Tool Support

### Maven

| Maven Version | Status |
|---------------|--------|
| 3.9.x | Fully supported |
| 3.8.3+ | Minimum required |
| 3.8.0-3.8.2 | May work, not tested |
| < 3.8 | Not supported |

**Configuration**:
```xml
<plugin>
    <groupId>com.hellblazer.primeMover</groupId>
    <artifactId>primemover-maven-plugin</artifactId>
    <version>1.0.6</version>
    <executions>
        <execution>
            <phase>process-classes</phase>
            <goals>
                <goal>transform</goal>
                <goal>transform-test</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Gradle

| Gradle Version | Status | Notes |
|----------------|--------|-------|
| 9.1+ | Supported | IntelliJ plugin build |
| 8.x | Not tested | May work for consuming artifacts |

Gradle is used to build the IntelliJ plugin. For simulation projects, Maven is the primary supported build tool.

## Runtime Dependencies

### Core Dependencies

| Dependency | Version | Module |
|------------|---------|--------|
| SLF4J API | 2.0.16 | api, framework |
| JUnit Jupiter | 5.13.0-M2 | test |
| Logback | 1.5.18 | test |
| Commons Math3 | 3.6.1 | desmoj-ish |
| Commons Collections4 | 4.4 | desmoj-ish |
| Mockito | 5.16.1 | test |

### JDK Modules (Required)

```
jdk.classfile    # Bytecode transformation
java.base        # Core functionality
```

No external bytecode manipulation libraries required (ASM, Javassist, etc.).

## Version History

### v1.0.6 (Development)

**Target Changes**:
- JPS IDE integration improvements
- Enhanced memory management
- Ordinal stability enhancements

### v1.0.5 (Current Stable)

**Released**: January 2026

**Changes**:
- IntelliJ IDEA plugin (JPS builder)
- Simulation controller enhancements
- @Transformed annotation clarifications
- Migrated to Java 25 ClassFile API (stable)

**Breaking Changes**:
- Minimum Java version increased from 24 to 25

### v1.0.4

**Changes**:
- Fix inherited interface detection in entity transform

### v1.0.3

**Changes**:
- Classpath scanning improvements

### v1.0.0-1.0.2

**Initial Stable Releases**:
- Core DES functionality
- Virtual thread continuations
- Maven plugin
- Runtime agent (sim-agent)

## Simulation Features by Version

| Feature | v1.0.0 | v1.0.5 | v1.0.6 (planned) |
|---------|--------|--------|------------------|
| Core DES engine | Yes | Yes | Yes |
| Virtual thread continuations | Yes | Yes | Yes |
| @Entity transformation | Yes | Yes | Yes |
| @Blocking support | Yes | Yes | Yes |
| Kronos/Kairos API | Yes | Yes | Yes |
| Maven plugin | Yes | Yes | Yes |
| Runtime agent (sim-agent) | Yes | Yes | Yes |
| DESMOJ compatibility layer | Yes | Yes | Yes |
| Janus composite/mixin | Yes | Yes | Yes |
| IntelliJ IDEA plugin | No | Yes | Yes |
| Real-time simulation modes | Partial | Partial | Enhanced |

## Transformation Tool Compatibility

Multiple transformation tools can be safely combined. The `@Transformed` annotation prevents double transformation.

| Combination | Compatible | Notes |
|-------------|------------|-------|
| Maven + IntelliJ | Yes | First transforms, second skips |
| Maven + sim-agent | Yes | Maven at build, agent for dynamic classes |
| IntelliJ + sim-agent | Yes | JPS at compile, agent as fallback |
| All three | Yes | Recommended for comprehensive coverage |

## Known Issues

### General

- **Large simulations**: Memory usage scales with virtual thread count; monitor heap size
- **Debugging**: Use `SteppingController` for step-through debugging

### IntelliJ Plugin

- **Minimum version**: Requires IntelliJ IDEA 2025.3 (build 253)
- **Gradle projects**: May need explicit dependency sync

### Java Agent

- **Hot reload**: Agent handles classes compiled outside IDE
- **Performance**: Slight overhead vs build-time transformation

## Migration Guides

### From v1.0.4 to v1.0.5+

1. **Update Java**: Install Java 25 or later
2. **Update dependencies**: Change version to 1.0.5
3. **Optional**: Install IntelliJ plugin for IDE integration

```xml
<!-- Update version -->
<dependency>
    <groupId>com.hellblazer.primeMover</groupId>
    <artifactId>api</artifactId>
    <version>1.0.5</version>
</dependency>
```

### From Pre-1.0 to 1.0.x

1. **Package changes**: Verify import statements
2. **API changes**: Review Kronos method signatures
3. **Build**: Update Maven plugin configuration

## Support Matrix

| Version | Status | Support |
|---------|--------|---------|
| 1.0.6-SNAPSHOT | Development | Active |
| 1.0.5 | Current Stable | Full support |
| 1.0.4 | Previous | Critical fixes only |
| 1.0.0-1.0.3 | Legacy | No active support |
| 0.x | Deprecated | Upgrade recommended |

## Verifying Compatibility

### Check Java Version

```bash
java -version
# Should show: openjdk version "25" or later
```

### Check Maven Version

```bash
mvn -version
# Should show: Apache Maven 3.8.3 or later
```

### Verify Transformation

```bash
# After building, check for @Transformed annotation
javap -v target/classes/com/example/MyEntity.class | grep Transformed
```

### Test Runtime

```java
// Verify Kronos methods are transformed
@Entity
public class CompatibilityTest {
    public static void main(String[] args) {
        SimulationController controller = new SimulationController();
        Kronos.setController(controller);
        // If this runs without UnsupportedOperationException,
        // transformation is working
        System.out.println("Time: " + Kronos.currentTime());
    }
}
```

## Getting Help

- **GitHub Issues**: [Prime-Mover Issues](https://github.com/Hellblazer/Prime-Mover/issues)
- **Documentation**: Module-specific READMEs in each subdirectory
- **Examples**: See `demo/` module for working examples

## Related Documentation

- [README.md](./README.md) - Project overview and quick start
- [CONCEPTS.md](./CONCEPTS.md) - Conceptual foundations
- [PERFORMANCE.md](./PERFORMANCE.md) - Performance benchmarks
- [CLASSFILE_API_ANALYSIS.md](./CLASSFILE_API_ANALYSIS.md) - Bytecode transformation details
- [primemover-intellij-plugin/README.md](./primemover-intellij-plugin/README.md) - IDE plugin documentation
- [primemover-maven-plugin/README.md](./primemover-maven-plugin/README.md) - Maven plugin documentation
