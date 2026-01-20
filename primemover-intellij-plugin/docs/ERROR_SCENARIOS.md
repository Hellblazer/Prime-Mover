# Prime Mover IntelliJ Plugin - Error Scenarios and Recovery

This document defines comprehensive error scenarios, detection strategies, user-facing messages, and recovery procedures for the Prime Mover IntelliJ plugin.

## Table of Contents

1. [Annotation Scanning Failures](#annotation-scanning-failures)
2. [Entity Transformation Failures](#entity-transformation-failures)
3. [ClassFile API Errors](#classfile-api-errors)
4. [Compilation Stage Errors](#compilation-stage-errors)
5. [IDE Sandbox Violations](#ide-sandbox-violations)
6. [Memory and Performance Issues](#memory-and-performance-issues)
7. [Configuration Errors](#configuration-errors)

---

## Annotation Scanning Failures

### Scenario 1: Missing @Entity Annotation

**Detection Strategy:**
- Class expected to be transformed but no @Entity annotation found
- Detected during annotation scanning phase in `PrimeMoverClassInstrumenter`

**Error Category:** `ANNOTATION_SCANNING`
**Severity:** `MEDIUM`

**User-Facing Message:**
```
Prime Mover detected a simulation class without @Entity annotation.

File: com/example/MySimulation.java

The class appears to use Prime Mover simulation features but is missing the
required @Entity annotation. Without this annotation, the class will not be
transformed for simulation.

Suggested actions:
1. Add @Entity annotation to the class
2. Import: import com.hellblazer.primeMover.annotations.Entity;
3. Rebuild the project
```

**Recovery Steps:**
1. Check class source for `@Entity` presence
2. Verify `import com.hellblazer.primeMover.annotations.Entity;`
3. Ensure api module is in project dependencies
4. Rebuild project

**Escalation Path:**
- If annotation is present but not detected: Check classpath configuration
- If import fails: Verify api module dependency
- If persists: Export diagnostic report and file GitHub issue

---

### Scenario 2: Classpath Issues - API Module Missing

**Detection Strategy:**
- `@Entity` annotation class not found during scanning
- `ClassNotFoundException` or `NoClassDefFoundError` for annotation classes

**Error Category:** `ANNOTATION_SCANNING`
**Severity:** `HIGH`

**User-Facing Message:**
```
Prime Mover annotations not found on classpath.

The Prime Mover API module is not available during compilation. This prevents
the plugin from detecting and transforming @Entity classes.

Suggested actions:
1. Add api module dependency to pom.xml or build.gradle
2. For Maven: Verify primemover-api dependency exists
3. For Gradle: Verify implementation('com.hellblazer.primeMover:api:VERSION')
4. Sync/reload project configuration
```

**Recovery Steps:**
1. Check `pom.xml` or `build.gradle` for `primemover-api` dependency
2. Add dependency if missing:
   ```xml
   <dependency>
       <groupId>com.hellblazer.primeMover</groupId>
       <artifactId>api</artifactId>
       <version>1.0.6-SNAPSHOT</version>
   </dependency>
   ```
3. Reload Maven/Gradle project
4. Invalidate caches and restart IDE

**Escalation Path:**
- If dependency is present: Check Maven/Gradle configuration errors
- If build tool sync fails: Check network connectivity, repository configuration
- If persists: Review IDE build configuration

---

### Scenario 3: Invalid Annotation Usage

**Detection Strategy:**
- `@Entity` applied to interface, enum, or abstract class
- Detected during annotation validation phase

**Error Category:** `ANNOTATION_SCANNING`
**Severity:** `MEDIUM`

**User-Facing Message:**
```
Invalid @Entity annotation usage detected.

File: com/example/AbstractSimulation.java

@Entity annotation cannot be applied to abstract classes, interfaces, or enums.
Only concrete classes can be simulation entities.

Suggested actions:
1. Remove @Entity from abstract class/interface/enum
2. Create concrete implementation class with @Entity
3. Ensure entity class has accessible constructor
```

**Recovery Steps:**
1. Identify class type (abstract, interface, enum)
2. Remove `@Entity` from invalid target
3. Create concrete implementation if needed
4. Rebuild project

**Escalation Path:**
- If architectural constraint requires abstract entity: Design refactoring needed
- Review simulation architecture patterns

---

## Entity Transformation Failures

### Scenario 4: Unsupported Method Pattern - Synchronized

**Detection Strategy:**
- Method marked with `synchronized` keyword in `@Entity` class
- Detected during bytecode analysis phase

**Error Category:** `ENTITY_TRANSFORMATION`
**Severity:** `HIGH`

**User-Facing Message:**
```
Unsupported method pattern in entity class.

File: com/example/MyEntity.java
Method: public synchronized void process()

Prime Mover does not support the 'synchronized' keyword in @Entity classes.
Simulation entities use event-based concurrency control instead.

Suggested actions:
1. Remove 'synchronized' keyword from method
2. Use simulation-safe concurrency patterns (e.g., event sequencing)
3. Review Prime Mover concurrency documentation
4. Consider using @Blocking for serialization if needed
```

**Recovery Steps:**
1. Locate synchronized methods in entity class
2. Remove `synchronized` keyword
3. Implement event-based concurrency if needed
4. Use `@Blocking` annotation for blocking operations
5. Rebuild project

**Escalation Path:**
- If concurrent access required: Redesign using simulation patterns
- Consult Prime Mover concurrency guide
- File GitHub issue if valid use case not supported

---

### Scenario 5: Native Method in Entity

**Detection Strategy:**
- Method marked as `native` in `@Entity` class
- Detected during method scanning

**Error Category:** `ENTITY_TRANSFORMATION`
**Severity:** `HIGH`

**User-Facing Message:**
```
Native method detected in entity class.

File: com/example/MyEntity.java
Method: public native int computeNative()

Native methods cannot be transformed for simulation and are not supported
in @Entity classes.

Suggested actions:
1. Remove native method from entity class
2. Move native functionality to separate non-entity utility class
3. Call utility class from entity methods
4. Mark utility methods with @NonEvent if needed
```

**Recovery Steps:**
1. Identify native methods in entity
2. Extract to separate utility class
3. Update entity to call utility methods
4. Rebuild project

**Escalation Path:**
- If native method critical: Architectural redesign needed
- Consider JNA/JNI wrapper approach in non-entity code

---

### Scenario 6: Transformation Bytecode Error

**Detection Strategy:**
- Exception during bytecode transformation
- Caught during ClassFile API operations

**Error Category:** `ENTITY_TRANSFORMATION`
**Severity:** `HIGH`

**User-Facing Message:**
```
Bytecode transformation failed for entity class.

File: com/example/MyEntity.java
Error: Invalid stack frame at instruction 42

An error occurred while transforming the class bytecode. This may indicate
a compiler bug, bytecode corruption, or an unsupported language feature.

Suggested actions:
1. Clean and rebuild project
2. Enable Prime Mover debug logging
3. Check for exotic language features (inline classes, pattern matching)
4. Export diagnostic report
5. File GitHub issue with stack trace
```

**Recovery Steps:**
1. Clean project (`Build > Clean Project`)
2. Rebuild from scratch
3. Enable debug logging: `Settings > Prime Mover > Enable Debug Logging`
4. Review recent code changes
5. Export diagnostic report: `Tools > Prime Mover > Export Diagnostics`

**Escalation Path:**
- Export full diagnostic report
- File GitHub issue with:
  - Stack trace
  - Source code of failing class
  - Java version
  - IDE version
- Temporarily disable transformation for specific class

---

### Scenario 7: Missing Constructor

**Detection Strategy:**
- Entity class has no accessible constructor
- All constructors are private

**Error Category:** `ENTITY_TRANSFORMATION`
**Severity:** `HIGH`

**User-Facing Message:**
```
Entity class has no accessible constructor.

File: com/example/MyEntity.java

Prime Mover requires entity classes to have at least one package-private or
public constructor for instantiation.

Suggested actions:
1. Add public or package-private constructor
2. If using factory pattern, ensure factory is in same package
3. Remove private-only constructor restriction
```

**Recovery Steps:**
1. Add accessible constructor to entity class
2. Ensure at least one constructor is public or package-private
3. Rebuild project

**Escalation Path:**
- If factory pattern required: Redesign entity creation
- Use EntityReference for indirect instantiation

---

## ClassFile API Errors

### Scenario 8: Incompatible Bytecode Version

**Detection Strategy:**
- Class file version mismatch
- ClassFile API throws version compatibility exception

**Error Category:** `CLASSFILE_API`
**Severity:** `HIGH`

**User-Facing Message:**
```
Incompatible class file version detected.

File: com/example/MyEntity.class
Class File Version: 52.0 (Java 8)
Required Version: 69.0+ (Java 25+)

Prime Mover requires Java 25 or later for ClassFile API support. The class
was compiled with an older Java version.

Suggested actions:
1. Verify project Java version is 25 or later
2. Check Project Structure > Project > SDK
3. Update Maven/Gradle to use Java 25+
4. Rebuild project with correct Java version
```

**Recovery Steps:**
1. Check IDE Java SDK: `File > Project Structure > Project > SDK`
2. Verify SDK is Java 25 or newer
3. Update build tool configuration:
   - Maven: `<maven.compiler.source>25</maven.compiler.source>`
   - Gradle: `sourceCompatibility = '25'`
4. Clean and rebuild

**Escalation Path:**
- If Java 25 not available: Install Java 25+ (GraalVM recommended)
- Update IDE bundled JDK if needed
- Check environment variables (JAVA_HOME)

---

### Scenario 9: Malformed Class File

**Detection Strategy:**
- ClassFile API throws parsing exception
- Corrupted bytecode detected

**Error Category:** `CLASSFILE_API`
**Severity:** `HIGH`

**User-Facing Message:**
```
Malformed class file detected during transformation.

File: com/example/MyEntity.class
Error: Invalid constant pool entry at index 23

The compiled class file appears to be corrupted or malformed. This may
indicate a compiler bug or disk corruption.

Suggested actions:
1. Clean build output directory
2. Rebuild project from scratch
3. Check disk space and file system integrity
4. Update to latest Java compiler
5. File bug report with class file if reproducible
```

**Recovery Steps:**
1. Delete build output: `Build > Clean Project`
2. Delete IDE caches: `File > Invalidate Caches and Restart`
3. Rebuild from clean state
4. Check disk space: `df -h`
5. Run file system check if needed

**Escalation Path:**
- If reproducible: File GitHub issue with class file
- Check Java compiler bug database
- Try alternative compiler (ECJ vs javac)

---

## Compilation Stage Errors

### Scenario 10: Generated Code Compilation Failure

**Detection Strategy:**
- Transformed class fails to compile
- Compilation errors in generated EntityReference

**Error Category:** `COMPILATION`
**Severity:** `HIGH`

**User-Facing Message:**
```
Generated simulation code failed to compile.

File: com/example/MyEntity.java
Generated: com/example/MyEntity$EntityReference.java
Error: Cannot find symbol: method __invoke_transformed

The bytecode transformation generated code that does not compile. This
indicates a bug in the transformation logic.

Suggested actions:
1. Enable debug logging to see generated code
2. Check for unsupported method signatures
3. Export diagnostic report
4. File GitHub issue with failing class source
```

**Recovery Steps:**
1. Enable debug output: `Settings > Prime Mover > Debug Logging`
2. Review IDE build messages for details
3. Check entity class for unusual method signatures
4. Export diagnostic report
5. File GitHub issue

**Escalation Path:**
- Critical bug - report immediately
- Include full source of failing class
- Include generated bytecode if available
- Disable transformation for specific class as workaround

---

### Scenario 11: Missing Runtime Dependency

**Detection Strategy:**
- Compilation fails with missing Prime Mover runtime classes
- `Symbol not found: Kronos`, `Symbol not found: Controller`

**Error Category:** `COMPILATION`
**Severity:** `HIGH`

**User-Facing Message:**
```
Prime Mover runtime module not found.

The transformed code references Prime Mover runtime classes (Kronos, Controller)
but the runtime module is not in the project dependencies.

Suggested actions:
1. Add runtime module dependency to pom.xml or build.gradle
2. For Maven: Add primemover-runtime dependency
3. For Gradle: Add implementation('com.hellblazer.primeMover:runtime:VERSION')
4. Reload project configuration
```

**Recovery Steps:**
1. Add runtime dependency:
   ```xml
   <dependency>
       <groupId>com.hellblazer.primeMover</groupId>
       <artifactId>runtime</artifactId>
       <version>1.0.6-SNAPSHOT</version>
   </dependency>
   ```
2. Reload Maven/Gradle project
3. Rebuild

**Escalation Path:**
- Check Maven Central availability
- Check local repository configuration
- Verify version compatibility

---

## IDE Sandbox Violations

### Scenario 12: File Access Denied

**Detection Strategy:**
- IOException during class file write
- Permission denied errors

**Error Category:** `SANDBOX_VIOLATION`
**Severity:** `MEDIUM`

**User-Facing Message:**
```
File access denied during transformation.

File: /path/to/build/classes/MyEntity.class
Error: Permission denied (write failed)

The Prime Mover plugin cannot write transformed class files to the build
output directory. This may be due to file permissions or IDE sandbox restrictions.

Suggested actions:
1. Check file system permissions on build directory
2. Ensure IDE has write access to project directories
3. Review IDE sandbox settings
4. Check for conflicting bytecode instrumentation plugins
```

**Recovery Steps:**
1. Check directory permissions: `ls -la build/classes`
2. Fix permissions if needed: `chmod -R u+w build/`
3. Review IDE sandbox: `Settings > Advanced Settings > IDE Security`
4. Restart IDE
5. Clean and rebuild

**Escalation Path:**
- If macOS: Check System Preferences > Security > Files and Folders
- If Windows: Check folder permissions
- Disable other bytecode instrumentation plugins temporarily

---

### Scenario 13: Plugin Conflict - Multiple Instrumentation

**Detection Strategy:**
- Multiple bytecode transformers detected
- Conflicts with Lombok, AspectJ, or other instrumentation

**Error Category:** `SANDBOX_VIOLATION`
**Severity:** `MEDIUM`

**User-Facing Message:**
```
Bytecode instrumentation conflict detected.

Prime Mover detected other bytecode instrumentation plugins (Lombok, AspectJ)
that may conflict with simulation transformation.

Suggested actions:
1. Ensure Prime Mover transformation runs after other instrumenters
2. Check plugin execution order in IDE settings
3. Try disabling other plugins temporarily to isolate issue
4. Use Maven plugin for transformation instead of IDE plugin
```

**Recovery Steps:**
1. List active plugins: `Settings > Plugins`
2. Identify bytecode instrumentation plugins
3. Test with plugins disabled one at a time
4. Adjust plugin load order if possible
5. Consider Maven-based transformation as alternative

**Escalation Path:**
- Document conflict scenario
- File GitHub issue with plugin compatibility report
- Use Maven plugin for controlled transformation order

---

## Memory and Performance Issues

### Scenario 14: Out of Memory During Transformation

**Detection Strategy:**
- `OutOfMemoryError` during transformation
- Heap exhaustion detected

**Error Category:** `PERFORMANCE`
**Severity:** `MEDIUM`

**User-Facing Message:**
```
Insufficient memory for bytecode transformation.

Error: OutOfMemoryError: Java heap space

The transformation process exhausted available memory. This typically occurs
with very large classes or many entities being transformed simultaneously.

Suggested actions:
1. Increase IDE memory: Help > Edit Custom VM Options
2. Add or increase -Xmx setting (e.g., -Xmx4096m)
3. Reduce number of @Entity classes
4. Split large entity classes into smaller ones
5. Restart IDE after memory changes
```

**Recovery Steps:**
1. Edit IDE VM options: `Help > Edit Custom VM Options`
2. Increase heap: `-Xmx4096m` (or higher)
3. Restart IDE
4. If persists, reduce transformation scope
5. Consider incremental transformation

**Escalation Path:**
- Profile transformation memory usage
- Identify memory-intensive classes
- File GitHub issue if reasonable class causes OOM
- Consider streaming transformation approach

---

### Scenario 15: Transformation Timeout

**Detection Strategy:**
- Transformation takes longer than threshold
- Build times out during transformation

**Error Category:** `PERFORMANCE`
**Severity:** `MEDIUM`

**User-Facing Message:**
```
Bytecode transformation timeout.

File: com/example/LargeEntity.java
Time: 45 seconds (exceeded 30s threshold)

The transformation of this class is taking unusually long. This may indicate
a performance issue or extremely complex class structure.

Suggested actions:
1. Simplify entity class structure
2. Reduce method count in entity
3. Split into multiple smaller entity classes
4. Disable real-time transformation for this class
5. Use Maven plugin for batch transformation instead
```

**Recovery Steps:**
1. Analyze class complexity (method count, LOC)
2. Refactor into smaller classes if needed
3. Temporarily exclude from transformation
4. Use Maven plugin for offline transformation
5. File performance report

**Escalation Path:**
- Export class structure metrics
- File GitHub issue with performance profile
- Identify transformation bottleneck
- Consider optimization or caching

---

## Configuration Errors

### Scenario 16: Invalid Plugin Settings

**Detection Strategy:**
- Invalid configuration values in settings
- Settings validation fails

**Error Category:** `CONFIGURATION`
**Severity:** `LOW`

**User-Facing Message:**
```
Invalid Prime Mover plugin configuration.

Setting: transformation.timeout
Value: -1 (invalid)
Expected: Positive integer (seconds)

The plugin configuration contains invalid values that prevent proper operation.

Suggested actions:
1. Open Settings > Prime Mover
2. Reset to default settings
3. Review and correct invalid values
4. Apply changes and restart IDE
```

**Recovery Steps:**
1. Open `Settings > Prime Mover`
2. Click "Reset to Defaults"
3. Review each setting
4. Apply and restart

**Escalation Path:**
- If reset fails: Manually delete settings file
- Location: `.idea/primemover-settings.xml`

---

### Scenario 17: Maven Plugin Duplication

**Detection Strategy:**
- Both Maven plugin and IDE plugin are transforming classes
- Double transformation detected

**Error Category:** `CONFIGURATION`
**Severity:** `MEDIUM`

**User-Facing Message:**
```
Duplicate transformation detected.

Both the Prime Mover Maven plugin and IntelliJ plugin are configured to
transform @Entity classes. This causes classes to be transformed twice,
which may lead to errors.

Suggested actions:
1. Choose ONE transformation method:
   - IDE Plugin: Remove Maven plugin from pom.xml
   - Maven Plugin: Disable IDE transformation in Settings
2. For production builds, Maven plugin is recommended
3. For development, IDE plugin provides faster feedback
```

**Recovery Steps:**
1. Decide on transformation approach (Maven vs IDE)
2. If Maven: Disable IDE plugin transformation
3. If IDE: Remove Maven plugin from `pom.xml`
4. Clean and rebuild

**Escalation Path:**
- No escalation needed - configuration choice
- Document recommended approach in project README

---

## Error Recovery Workflow

### General Recovery Process

1. **Identify Error Category**
   - Check notification balloon
   - Review IDE event log
   - Examine build output

2. **Apply Suggested Actions**
   - Follow numbered recovery steps
   - Test after each step
   - Document which step resolved issue

3. **Escalation Decision Tree**
   ```
   Error Resolved?
     YES → Done
     NO  → Tried all recovery steps?
             YES → Export diagnostics → File GitHub issue
             NO  → Continue recovery steps
   ```

4. **Diagnostic Export**
   - `Tools > Prime Mover > Export Diagnostics`
   - Generates comprehensive error report
   - Include in GitHub issue

5. **GitHub Issue Template**
   ```markdown
   ## Error Category
   [e.g., Entity Transformation Failure]

   ## Error Message
   [Copy full error message]

   ## Steps to Reproduce
   1. [Step 1]
   2. [Step 2]
   ...

   ## Environment
   - IDE Version: [IntelliJ IDEA version]
   - Java Version: [java -version output]
   - Plugin Version: [Prime Mover plugin version]
   - OS: [Operating system]

   ## Diagnostic Report
   [Attach exported diagnostic report]

   ## Attempted Recovery
   - [x] Tried recovery step 1
   - [x] Tried recovery step 2
   - [ ] Not applicable
   ```

---

## Prevention and Best Practices

### Reduce Annotation Scanning Errors
- Always import annotations explicitly
- Use IDE auto-import features
- Verify dependencies before large changes

### Reduce Transformation Errors
- Follow Prime Mover coding guidelines
- Avoid `synchronized` and `native` in entities
- Keep entity classes focused and small
- Use `@NonEvent` for utility methods

### Reduce Performance Issues
- Monitor entity class size
- Profile transformation times
- Increase IDE memory proactively
- Use incremental builds

### Reduce Configuration Errors
- Document transformation approach in README
- Choose Maven OR IDE plugin, not both
- Validate settings after IDE upgrades
- Keep plugin updated

---

## Monitoring and Metrics

The error handling system tracks:
- Error frequency by category
- Recovery action success rates
- Time to resolution
- Escalation rates

Use `Tools > Prime Mover > View Error Statistics` to access metrics.

---

## Future Enhancements

Planned improvements:
- Automatic error pattern detection
- Predictive error prevention
- Intelligent recovery action suggestions
- Integration with IDE quick-fixes
- Real-time transformation validation
- Performance profiling dashboard

---

## Support Resources

- **Documentation:** https://github.com/Hellblazer/Prime-Mover/wiki
- **GitHub Issues:** https://github.com/Hellblazer/Prime-Mover/issues
- **Discussions:** https://github.com/Hellblazer/Prime-Mover/discussions
- **Email:** hal.hildebrand@gmail.com

When reporting errors, always include:
1. Full error message
2. IDE and Java versions
3. Diagnostic report
4. Minimal reproducible example
