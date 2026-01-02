# Prime Mover Maven Plugin

## Overview

The primemover-maven-plugin is a Maven plugin that integrates bytecode transformation into the Maven build lifecycle. It automatically transforms `@Entity` classes after compilation during the build process.

**Artifact**: `com.hellblazer.primeMover:primemover-maven-plugin`

**Type**: Maven Plugin (Goal-based)

## Purpose

This plugin:
1. **Scans** compiled class directories for `@Entity` classes
2. **Transforms** them in-place using the transform module
3. **Generates** entity reference classes
4. **Runs** at build time (before packaging)
5. **Supports** incremental compilation and IDE integration

## Installation

### Repository Configuration

First, configure access to GitHub Packages in your `~/.m2/settings.xml`:

```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>YOUR_GITHUB_USERNAME</username>
            <password>YOUR_GITHUB_TOKEN</password>
        </server>
    </servers>
</settings>
```

> **Note**: Generate a GitHub Personal Access Token with `read:packages` scope at https://github.com/settings/tokens

Then add the repository to your pom.xml:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/Hellblazer/Prime-Mover</url>
    </repository>
</repositories>

<pluginRepositories>
    <pluginRepository>
        <id>github</id>
        <url>https://maven.pkg.github.com/Hellblazer/Prime-Mover</url>
    </pluginRepository>
</pluginRepositories>
```

## Configuration

### Basic Setup

Add the plugin to your pom.xml build section:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.hellblazer.primeMover</groupId>
            <artifactId>primemover-maven-plugin</artifactId>
            <version>1.0.5-SNAPSHOT</version>
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
    </plugins>
</build>
```

### Minimal Configuration

```xml
<plugin>
    <groupId>com.hellblazer.primeMover</groupId>
    <artifactId>primemover-maven-plugin</artifactId>
    <version>${project.version}</version>
</plugin>
```

This assumes default configuration:
- Transforms main classes during `process-classes` phase
- Transforms test classes during `process-test-classes` phase

### Advanced Configuration

```xml
<plugin>
    <groupId>com.hellblazer.primeMover</groupId>
    <artifactId>primemover-maven-plugin</artifactId>
    <version>${project.version}</version>
    <configuration>
        <!-- Optional: customize transformation behavior -->
        <!-- Note: Use -Dprimemover.skip=true to skip transformation from command line -->
    </configuration>
    <executions>
        <execution>
            <id>transform-classes</id>
            <phase>process-classes</phase>
            <goals>
                <goal>transform</goal>
            </goals>
            <!-- Configuration uses defaults -->
        </execution>
        <execution>
            <id>transform-test-classes</id>
            <phase>process-test-classes</phase>
            <goals>
                <goal>transform-test</goal>
            </goals>
            <!-- Configuration uses defaults -->
        </execution>
    </executions>
</plugin>
```

## Goals

### `transform`
Transforms main classes in `target/classes`.

**Bound to Phase**: `process-classes` (by default)

**Purpose**: Transform production code

**Parameters**:
- `buildOutputDirectory` (default: `${project.build.outputDirectory}`)
- `skip` (property: `primemover.skip`, default: false)

**Example**:
```bash
mvn com.hellblazer.primeMover:primemover-maven-plugin:transform
```

### `transform-test`
Transforms test classes in `target/test-classes`.

**Bound to Phase**: `process-test-classes` (by default)

**Purpose**: Transform test code

**Parameters**:
- `testOutputDirectory` (default: `${project.build.testOutputDirectory}`)
- `skip` (property: `primemover.skip`, default: false)

**Example**:
```bash
mvn com.hellblazer.primeMover:primemover-maven-plugin:transform-test
```

## Build Lifecycle Integration

### Standard Lifecycle

```
1. compile          -> javac compiles .java -> .class
2. process-classes  -> plugin transforms @Entity classes
3. test-compile     -> javac compiles test .java -> .class
4. process-test-classes -> plugin transforms test @Entity classes
5. test            -> JUnit runs with transformed classes
6. package         -> Creates jar with transformed classes
7. install         -> Installs jar to local repository
```

### Plugin Execution

```
Maven Phase             | Plugin Action
-----------------------+---------------------------------------
process-classes        | Scans target/classes
                      | Finds @Entity classes
                      | Transforms and overwrites
                      | (production code ready)
-----------------------+---------------------------------------
process-test-classes   | Scans target/test-classes
                      | Finds @Entity classes
                      | Transforms and overwrites
                      | (test code ready)
-----------------------+---------------------------------------
test                  | JUnit runs with transformed classes
-----------------------+---------------------------------------
```

## IDE Integration

### IntelliJ IDEA

**Enable Build Phase Optimization**:
1. File -> Settings -> Build, Execution, Deployment -> Maven
2. Check "Build Project only using Maven"

**Why**: Ensures plugin runs even during incremental compilation

### Eclipse

**Enable Plugin Execution**:
1. Right-click project -> Maven -> Update Project
2. Select "Force Update of Snapshots/Releases"

### VS Code with Maven Extension

Should work automatically with Maven extension if properly configured.

## Troubleshooting

### Classes Not Transformed

**Problem**: `@Entity` classes aren't being transformed.

**Causes**:
- Plugin not in pom.xml
- Plugin execution not configured for correct phase
- Classes not compiled before plugin runs

**Solution**:
```bash
mvn clean compile process-classes -X
```

### Transformation Errors

**Problem**: `BUILD FAILURE - Transformation failed`

**Causes**:
- Bytecode corrupted
- Unsupported Java features
- Annotation not recognized

**Solution**:
```bash
# Run with verbose output
mvn clean process-classes -X

# Check class file format
javap -v target/classes/mypackage/MyClass.class
```

### Classes Not in JAR

**Problem**: Transformed classes missing from final jar.

**Causes**:
- Plugin runs too late
- JAR packed before transformation

**Solution**: Ensure `process-classes` execution, not `verify`:

```xml
<!-- WRONG -->
<phase>verify</phase>

<!-- CORRECT -->
<phase>process-classes</phase>
```

### IDE Doesn't See Transformations

**Problem**: IDE editor shows errors but builds fine.

**Causes**:
- IDE using different compiler
- Maven plugin output not updated in IDE

**Solution**:
1. Project -> Clean...
2. Project -> Build All
3. IDE -> Maven -> Update Project

## Performance Considerations

### Transformation Time

- **Typical**: 100-500ms for 100 classes
- **Overhead**: <5% of total build time
- **Scales**: Linear with number of classes

### Build Time Impact

**Small Project** (< 50 classes):
- Negligible impact
- Adds <100ms per build

**Large Project** (> 1000 classes):
- Adds 500ms-1s per build
- Can parallelize with Maven's -T option

### Optimization Tips

1. **Use `-T1C` for parallel builds**: Maven auto-parallelizes modules
   ```bash
   mvn -T1C clean install
   ```

2. **Enable IDE caching**: IntelliJ and Eclipse cache transformations

3. **Incremental builds**: Only retransforms changed classes

4. **Skip transformation when needed**:
   ```bash
   # Skip all transformations
   mvn clean install -Dprimemover.skip=true
   ```

## Common Patterns

### Recommended Complete Setup

```xml
<build>
    <plugins>
        <!-- Compiler with Java 25+ -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>25</source>
                <target>25</target>
                <release>25</release>
            </configuration>
        </plugin>

        <!-- Prime Mover Transformation -->
        <plugin>
            <groupId>com.hellblazer.primeMover</groupId>
            <artifactId>primemover-maven-plugin</artifactId>
            <version>${project.version}</version>
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

        <!-- Test Execution -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.0.0</version>
        </plugin>
    </plugins>
</build>

<!-- Dependencies -->
<dependencies>
    <!-- API for @Entity, Kronos, etc. -->
    <dependency>
        <groupId>com.hellblazer.primeMover</groupId>
        <artifactId>api</artifactId>
    </dependency>

    <!-- Runtime for SimulationController, etc. -->
    <dependency>
        <groupId>com.hellblazer.primeMover</groupId>
        <artifactId>runtime</artifactId>
        <scope>test</scope>  <!-- Or <scope>compile</scope> if needed at runtime -->
    </dependency>
</dependencies>
```

### Multiple Module Project

```xml
<!-- Root pom.xml: Plugin declaration -->
<pluginManagement>
    <plugins>
        <plugin>
            <groupId>com.hellblazer.primeMover</groupId>
            <artifactId>primemover-maven-plugin</artifactId>
            <version>${project.version}</version>
        </plugin>
    </plugins>
</pluginManagement>

<!-- Child modules: Plugin usage -->
<plugins>
    <plugin>
        <groupId>com.hellblazer.primeMover</groupId>
        <artifactId>primemover-maven-plugin</artifactId>
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
</plugins>
```

## Mojo Implementation Details

The plugin implements two Maven Mojos:
1. **TransformMojo** - Transforms main classes
2. **TransformTestMojo** - Transforms test classes

Both use the `transform` module's public API to perform actual transformation.

**Key Responsibilities**:
- Parse configuration
- Validate input directories
- Delegate to transformation engine
- Report results

## Limitations

1. **Must run after compilation**: Requires .class files exist
2. **Cannot transform .jar files**: Only .class files
3. **No selective transformation**: All @Entity classes transformed
4. **IDE limitations**: Some IDEs need manual refresh

## Migration from Previous Versions

### From ASM-based Plugin
No changes needed - API is the same. The plugin now uses ClassFile API internally.

### From Soot-based Plugin
Complete rewrite. Ensure:
1. `@Entity` annotations in place
2. `@Blocking` on blocking methods
3. Updated to Java 25+

## Future Enhancements

Planned improvements:
- Selective class transformation (include/exclude patterns)
- Configuration caching for faster builds
- Integration with IDE background compilation
- Per-class transformation progress reporting

## See Also

- **transform module**: Contains actual transformation logic
- **sim-agent**: Alternative runtime transformation via Java agent
- **api module**: Annotations and contracts
- **demo module**: Example project configuration
