# Prime Mover Simulation Agent

## Overview

The sim-agent module provides a Java agent for runtime transformation of `@Entity` classes. Instead of transforming classes during the build, the agent transforms them as they are loaded by the JVM.

**Artifact**: `com.hellblazer.primeMover:sim-agent`

**Type**: Java Agent JAR (javaagent)

## Purpose

This module:
1. **Intercepts** class loading in the JVM
2. **Identifies** `@Entity` classes
3. **Transforms** them on-the-fly
4. **Enables** dynamic simulation code without build-time plugin
5. **Supports** exploration and rapid prototyping

## When to Use

### Use the Maven Plugin (Recommended)
- Building production applications
- Running test suites
- Performance-critical code
- CI/CD pipelines
- Pre-validation of transformations

### Use the Java Agent
- Rapid prototyping
- Exploration and experimentation
- Dynamic class loading scenarios
- Learning the framework
- Running third-party code with simulation
- Development and debugging
- IntelliJ IDEA run configurations (via Prime Mover plugin)

## Installation

### Build the Agent

```bash
cd <prime-mover-root>
./mvnw clean package -pl sim-agent
```

This creates: `sim-agent/target/sim-agent.jar`

### Use the Agent

To use the agent with your simulation:

```bash
java -javaagent:/path/to/sim-agent.jar MySimulationClass
```

### With Arguments

The agent accepts a comma-separated list of class patterns to transform:

```bash
# Transform all classes with "com.example" in the package
java -javaagent:/path/to/sim-agent.jar=com.example.* MySimulationClass

# Transform specific packages
java -javaagent:/path/to/sim-agent.jar=com.example.*,org.test.* MyClass

# No arguments: transform all @Entity classes
java -javaagent:/path/to/sim-agent.jar MyClass
```

## How It Works

### Class Loading Interception

```
1. JVM starts with -javaagent flag
2. Agent's premain() called before user code
3. ClassFileTransformer registered with ClassLoader
4. User code starts loading classes
5. Agent intercepts each class load
6. Checks if class has @Entity
7. If yes, transforms bytecode before class definition
8. Class defined in JVM with transformed bytecode
```

### Transformation Process

```
Original Class Bytecode
    |
    v
Agent.transform() called
    |
    v
Has @Transformed annotation?
    |
    +--Yes--> Return bytecode unchanged (already transformed)
    |
    +--No--> Continue checking
                |
                v
            Is this an @Entity class?
                |
                +--Yes--> Full entity transformation
                |         (EntityGenerator adds @Transformed)
                |
                +--No--> Does it reference Kronos?
                            |
                            +--Yes--> Remap Kronos→Kairos
                            |
                            +--No--> Return unchanged
    |
    v
Class defined in JVM with transformed code
```

**Note**: The `@Transformed` check allows sim-agent to work alongside Maven plugin or IntelliJ JPS plugin. If a class was already transformed at build time, sim-agent will skip it.

## Integration Examples

### Basic Maven Project

```xml
<project>
    <dependencies>
        <dependency>
            <groupId>com.hellblazer.primeMover</groupId>
            <artifactId>api</artifactId>
        </dependency>
        <dependency>
            <groupId>com.hellblazer.primeMover</groupId>
            <artifactId>runtime</artifactId>
        </dependency>
        <dependency>
            <groupId>com.hellblazer.primeMover</groupId>
            <artifactId>sim-agent</artifactId>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- NO Maven plugin transformation needed -->
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <configuration>
                    <arguments>
                        <argument>-javaagent:${com.hellblazer.primeMover:sim-agent:jar}</argument>
                    </arguments>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### JUnit Tests

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.0.0</version>
    <configuration>
        <argLine>-javaagent:${settings.localRepository}/com/hellblazer/primeMover/sim-agent/${project.version}/sim-agent-${project.version}.jar</argLine>
    </configuration>
</plugin>
```

### Gradle

```gradle
test {
    jvmArgs = ["-javaagent:${configurations.simAgent.singleFile}"]
}

configurations {
    simAgent
}

dependencies {
    simAgent 'com.hellblazer.primeMover:sim-agent:1.0.5-SNAPSHOT'
}
```

## Performance Characteristics

### Startup Time Impact

```
Without Agent:        ~200ms (typical)
With Agent:           ~300-500ms (typical)
Impact:               +100-300ms

Factors:
- Number of classes: More classes = longer startup
- Package filters: More restrictive = faster startup
- Complex entities: More methods to analyze = slower
```

### Runtime Performance

**No Performance Penalty**: After class loading completes, transformed code runs identically to build-time transformed code.

### Memory Overhead

- **Agent Process**: ~2-5MB
- **Per Class**: ~1-5KB depending on complexity
- **Negligible** for most applications

## Debugging Transformed Code

When using the agent, you can debug with:

```bash
# With remote debugging
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 \
     -javaagent:/path/to/sim-agent.jar \
     MyClass
```

Then connect your IDE debugger to localhost:5005.

**Debug Support**:
- Original source lines preserved
- Stack traces show actual execution
- Method names visible (with `__event_` prefix for event methods)
- Breakpoints work normally

## Troubleshooting

### Classes Not Transformed

**Problem**: `@Entity` classes throw `UnsupportedOperationException` from `Kronos.sleep()`.

**Causes**:
- Agent jar not in classpath
- `javaagent` flag malformed
- Class pattern doesn't match

**Solution**:
```bash
# Verify agent JAR exists
ls -l /path/to/sim-agent.jar

# Ensure full path
java -javaagent:$(pwd)/sim-agent/target/sim-agent.jar MyClass

# Check the agent is loaded (look for transformation log messages)
```

### Agent Load Failure

**Problem**: `java.lang.instrument.IllegalClassFormatException`

**Causes**:
- Corrupted agent jar
- Version mismatch
- Incompatible JVM version

**Solution**:
```bash
# Rebuild agent
mvn clean package -pl sim-agent

# Use Java 25+
java -version  # Should show 25 or later

# Check agent jar
jar tf sim-agent.jar | grep AgentMain
```

### Performance Degradation

**Problem**: Application starts very slowly.

**Causes**:
- Too many classes being transformed
- Package pattern too broad
- Complex entity classes

**Solution**:
```bash
# Be more specific with class patterns
java -javaagent:/path/to/sim-agent.jar=com.mycompany.simulation.* MyClass

# Skip non-entity classes
java -javaagent:/path/to/sim-agent.jar=*Entity,*Simulation MyClass
```

### Stack Overflow During Transformation

**Problem**: `StackOverflowError` during class loading.

**Causes**:
- Circular class dependencies
- Agent transformation code itself triggering transformation
- Recursive initialization

**Solution**:
- Ensure agent jar not in target classes
- Separate transformation and runtime code
- Use Maven or build-time plugin instead

## Advanced Configuration

### Custom ClassLoader Handling

The agent works with:
- System classloader
- Application classloader
- Custom classloaders
- Maven Surefire test runner

Special cases handled automatically.

### Multiple Agents

Multiple agents can be specified:

```bash
java -javaagent:agent1.jar \
     -javaagent:agent2.jar \
     MyClass
```

Prime Mover agent plays well with other agents like JProfiler, YourKit, etc.

## Limitations

1. **No selective transformation**: Transforms all `@Entity` classes found
2. **Cannot untransform**: No way to disable after JVM startup
3. **Type inspection**: Tools using reflection may see transformed bytecode
4. **Debugging tools**: Some bytecode analyzers may show unexpected structure
5. **Dynamic proxies**: May interact with dynamic proxy generation

## Development Notes

### Implementation

The agent is implemented using the Java instrumentation API:

```java
public class AgentMain {
    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new SimulationTransformer());
    }
}
```

The transformer:
1. Checks class annotations
2. Uses transform module to analyze and transform
3. Returns transformed bytecode or original

### Building Custom Agents

To create a derivative agent:

1. Extend `ClassFileTransformer`
2. Use transform module API
3. Add custom filtering logic
4. Package as agent jar with proper manifest

```java
public class MyTransformer implements ClassFileTransformer {
    public byte[] transform(ClassLoader loader, String className,
                          Class<?> classBeingRedefined,
                          ProtectionDomain protectionDomain,
                          byte[] classfileBuffer) {
        // Custom logic
    }
}
```

## IntelliJ IDEA Integration

The **primemover-intellij-plugin** can automatically add the sim-agent to your run configurations:

1. Install the Prime Mover IntelliJ plugin
2. The plugin detects Prime Mover projects automatically
3. Run configurations are patched to include `-javaagent` flag
4. No manual configuration needed

This provides the best development experience - edit code and run immediately without rebuilding.

## See Also

- **transform module**: Contains transformation logic
- **primemover-maven-plugin**: Build-time alternative
- **api module**: Annotations and contracts
- **framework module**: Runtime implementation
- **demo module**: Example usage patterns
- **primemover-intellij-plugin**: IntelliJ IDEA integration

## Comparison: Maven Plugin vs Java Agent

| Feature | Maven Plugin | Java Agent |
|---------|--------------|-----------|
| **When** | Build time | Runtime |
| **Speed** | Once per build | Every startup |
| **Rebuild Needed** | Yes, after code change | No, run immediately |
| **IDE Support** | Good | Fair |
| **Production** | Recommended | Not recommended |
| **Prototyping** | OK | Excellent |
| **Third-party Code** | No | Yes |
| **Debugging** | IDE breakpoints | Yes, requires setup |
| **Error Messages** | At build time | At runtime |

### Recommendation

**Use Maven Plugin for:**
- Production deployments
- CI/CD pipelines
- Performance-critical applications
- Team projects with build standards

**Use Java Agent for:**
- Learning the framework
- Rapid prototyping
- Research and experimentation
- Ad-hoc testing
- Running third-party code
