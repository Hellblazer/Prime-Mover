# Prime Mover IntelliJ Plugin

Seamless bytecode transformation for the [Prime Mover](https://github.com/Hellblazer/Prime-Mover) discrete event simulation framework.

## Features

- **Automatic Post-Compile Transformation**: Classes annotated with `@Entity` are automatically transformed after Java compilation
- **Auto-Detection**: Automatically detects Prime Mover projects by scanning `pom.xml` or `build.gradle`
- **Run Configuration Enhancement**: Automatically adds `-javaagent:sim-agent.jar` to Java run configurations
- **Incremental Compilation Support**: Only transforms modified classes for faster builds
- **Duplicate Transformation Warning**: Warns when both IDE plugin and Maven plugin are configured

## Requirements

- IntelliJ IDEA 2025.3 or later
- Java 25 or later (uses ClassFile API for bytecode transformation)
- Prime Mover framework in project dependencies

## Installation

### From JetBrains Marketplace

1. Open IntelliJ IDEA
2. Go to **Settings** > **Plugins** > **Marketplace**
3. Search for "Prime Mover"
4. Click **Install**

### Manual Installation

1. Download the plugin `.zip` from [GitHub Releases](https://github.com/Hellblazer/Prime-Mover/releases) or build from source (see below)
2. Go to **Settings** > **Plugins** > **⚙️** > **Install Plugin from Disk...**
3. Select the `.zip` file from `plugin/build/distributions/` (if built from source) or the downloaded file

## Usage

### Basic Setup

1. Add Prime Mover dependencies to your project:

**Maven:**
```xml
<dependency>
    <groupId>com.hellblazer.primeMover</groupId>
    <artifactId>api</artifactId>
    <version>1.0.5-SNAPSHOT</version>  <!-- Use latest released version -->
</dependency>
<dependency>
    <groupId>com.hellblazer.primeMover</groupId>
    <artifactId>runtime</artifactId>
    <version>1.0.5-SNAPSHOT</version>  <!-- Use latest released version -->
</dependency>
```

**Gradle:**
```groovy
implementation 'com.hellblazer.primeMover:api:1.0.5-SNAPSHOT'  // Use latest released version
implementation 'com.hellblazer.primeMover:runtime:1.0.5-SNAPSHOT'  // Use latest released version
```

2. The plugin automatically detects Prime Mover and transforms `@Entity` classes on build

### Configuration

Access settings via **Settings** > **Build, Execution, Deployment** > **Prime Mover**:

| Setting | Description | Default |
|---------|-------------|---------|
| Enable transformation | Enable/disable bytecode transformation | ✓ |
| Show notifications | Show notifications on project open | ✓ |
| Warn on Maven plugin | Warn when both IDE and Maven plugins are configured | ✓ |
| Auto-add agent | Automatically add `-javaagent` to run configurations | ✓ |

### How It Works

1. **Detection**: On project open, the plugin scans `pom.xml` or `build.gradle` for Prime Mover dependencies
2. **Transformation**: After Java compilation, `@Entity` classes are transformed using the ClassFile API
3. **Run Enhancement**: When running Java applications, `-javaagent:sim-agent.jar` is automatically added

### Transformation Details

The plugin transforms `@Entity` classes to:
- Rewrite `Kronos` static method calls to `Kairos` thread-local calls
- Convert method invocations into scheduled events
- Add continuation support for `@Blocking` methods
- Mark classes with `@Transformed` to prevent double transformation

### sim-agent Runtime

The `sim-agent.jar` provides runtime transformation for scenarios where build-time transformation hasn't been applied:

- **Hot-reload**: Classes compiled outside IDE
- **Fallback**: JPS transformation failures
- **Debugging**: Runtime transformation with source mapping
- **Hybrid builds**: Mix of Maven and IDE compilation

To use sim-agent, add it to your dependencies:

```xml
<dependency>
    <groupId>com.hellblazer.primeMover</groupId>
    <artifactId>sim-agent</artifactId>
    <version>1.0.5-SNAPSHOT</version>  <!-- Use latest released version -->
    <scope>runtime</scope>
</dependency>
```

## Avoiding Duplicate Transformation

If you're using both this IDE plugin and the Maven plugin (`primemover-maven-plugin`), classes may be transformed twice. The plugin will warn you about this.

**Recommended configurations:**

1. **IDE-only**: Remove Maven plugin from `pom.xml`, rely on IDE for transformation
2. **Maven-only**: Disable IDE plugin in settings, use Maven for CI/CD builds
3. **Hybrid**: Disable IDE plugin transformation but keep run configuration enhancement

## Troubleshooting

### Classes not being transformed

1. Check that `@Entity` annotation is from `com.hellblazer.primeMover.api`
2. Verify Prime Mover dependencies are in the classpath
3. Check **Settings** > **Prime Mover** > "Enable transformation" is checked
4. Rebuild the project (**Build** > **Rebuild Project**)

### Duplicate transformation errors

1. Check if Maven plugin is also configured
2. Disable one of the transformation methods (see above)

### sim-agent not found

1. Ensure `sim-agent` is in your project dependencies
2. Check Maven/Gradle sync completed successfully

## Building from Source

```bash
# Clone the repository
git clone https://github.com/Hellblazer/Prime-Mover.git
cd Prime-Mover/primemover-intellij-plugin

# Build the plugin
./gradlew buildPlugin

# Run all tests (25 test methods across plugin and jps-plugin modules)
./gradlew test

# Run tests for specific modules
./gradlew :jps-plugin:test
./gradlew :plugin:test

# Plugin ZIP will be in plugin/build/distributions/
```

## Architecture

The plugin consists of two modules:

- **plugin**: Main IDE plugin with settings, detection, and run configuration
- **jps-plugin**: JPS builder for post-compile transformation

### Key Components

| Component | Purpose |
|-----------|---------|
| `PrimeMoverBuilderService` | Registers JPS builder |
| `PrimeMoverClassInstrumenter` | ModuleLevelBuilder for transformation |
| `PrimeMoverProjectDetector` | Detects Prime Mover in build files |
| `PrimeMoverSettings` | Project-level settings persistence |
| `PrimeMoverJavaProgramPatcher` | Auto-adds -javaagent to run configs |
| `SimAgentDetector` | Finds sim-agent.jar in dependencies |

## License

This project is licensed under the GNU Affero General Public License v3.0 (AGPL-3.0).

See [LICENSE](LICENSE) for the full license text.

### AGPL Compliance

- Source code must be made available to users who interact with the software
- Modifications must be released under AGPL-3.0
- The Prime Mover framework itself is also AGPL-3.0 licensed

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests: `./gradlew :jps-plugin:unitTest :plugin:unitTest`
5. Submit a pull request

## Support

- **Issues**: [GitHub Issues](https://github.com/Hellblazer/Prime-Mover/issues)
- **Documentation**: [Prime Mover Wiki](https://github.com/Hellblazer/Prime-Mover/wiki)
