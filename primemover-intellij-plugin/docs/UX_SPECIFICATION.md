# Prime Mover IntelliJ Plugin - UX Specification

**Version**: 1.0
**Last Updated**: 2026-01-19
**Status**: Design Specification
**Related Beads**: BEAD-8m9 (UX Specification and Mockups)

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Design Principles](#design-principles)
3. [Settings and Preferences UI](#settings-and-preferences-ui)
4. [Error Notification System](#error-notification-system)
5. [Project Detection and Setup Assistant](#project-detection-and-setup-assistant)
6. [Version Compatibility Warnings](#version-compatibility-warnings)
7. [Build Transformation Progress Indicator](#build-transformation-progress-indicator)
8. [Event Simulation Debugging UI](#event-simulation-debugging-ui)
9. [Accessibility Considerations](#accessibility-considerations)
10. [Keyboard Navigation](#keyboard-navigation)
11. [Icon and Visual Design](#icon-and-visual-design)
12. [Mockups and Wireframes](#mockups-and-wireframes)

---

## Executive Summary

This document defines the user experience (UX) specification for the Prime Mover IntelliJ plugin. The plugin provides seamless bytecode transformation for discrete event simulation code, and this specification ensures that users can configure, monitor, and troubleshoot the transformation process effectively.

### Target Users

1. **Simulation Developers**: Primary users who write `@Entity` classes and need transformation to work transparently
2. **Build Engineers**: Users who configure build pipelines and need to understand transformation options
3. **Debugging Users**: Developers troubleshooting simulation behavior who need visibility into transformation state

### UX Goals

| Goal | Description | Metric |
|------|-------------|--------|
| **Transparency** | Users understand what the plugin does without studying documentation | < 3 clicks to understand plugin purpose |
| **Non-intrusion** | Plugin stays out of the way during normal development | < 1 notification per project session |
| **Recoverability** | Users can recover from errors with clear guidance | 90% of errors recoverable with suggested actions |
| **Efficiency** | Quick access to common actions | < 2 seconds for any configuration change |
| **Accessibility** | Usable by all developers including those with disabilities | WCAG 2.1 AA compliance |

---

## Design Principles

### 1. Non-Intrusive Default Behavior

The plugin should be "invisible" during successful operation. Users should only notice the plugin when:
- First opening a Prime Mover project (detection notification)
- An error occurs that requires attention
- They explicitly access settings

**Implementation Guidelines**:
- Single, dismissible notification on project open
- No floating windows or modal dialogs during build
- Progress information in standard IntelliJ locations (status bar, Build tool window)
- Settings follow IntelliJ conventions (no custom floating panels)

### 2. Clear Error Communication

Error messages follow a consistent pattern that provides:
- **What happened** (brief summary)
- **Why it matters** (impact on simulation)
- **What to do** (actionable recovery steps)

**Error Message Template**:
```
[Error Title]

[1-2 sentence explanation of what happened]

Impact: [How this affects your simulation code]

Actions:
1. [First recovery step]
2. [Second recovery step]
[Link to documentation]
```

### 3. Automatic Recovery Where Possible

The plugin attempts automatic recovery before showing errors:
- Automatic retry with exponential backoff for transient failures
- Automatic fallback to sim-agent when JPS transformation fails
- Automatic detection and warning of duplicate transformation tools

**Recovery Hierarchy**:
```
Error Occurs
    |
    v
Automatic Retry (3 attempts)
    |
    v (if failed)
Automatic Fallback (sim-agent)
    |
    v (if no fallback)
User Notification with Recovery Actions
```

### 4. Consistent Visual Language

All UI elements follow IntelliJ platform guidelines:
- Use standard IntelliJ icons from `AllIcons` where possible
- Custom icons follow IntelliJ icon design guidelines
- Color usage follows IntelliJ theme (adapts to Light/Dark/High Contrast)
- Spacing and typography match IntelliJ standards

### 5. Progressive Disclosure

Information is revealed progressively:
- **Level 1**: Basic status (success/failure indicator)
- **Level 2**: Summary information (classes transformed count)
- **Level 3**: Detailed diagnostics (individual class messages)
- **Level 4**: Full debug information (bytecode analysis)

---

## Settings and Preferences UI

### Location

**Path**: Settings > Build, Execution, Deployment > Prime Mover

This location follows IntelliJ conventions for build-related plugins and ensures discoverability alongside other build tools.

### Settings Panel Structure

The settings panel uses a tabbed interface with three main sections:

#### Tab 1: Configuration

```
+------------------------------------------------------------------+
|  Prime Mover Settings                                             |
+------------------------------------------------------------------+
| [Configuration] [Diagnostics] [About]                             |
+------------------------------------------------------------------+
|                                                                   |
|  Transformation                                                   |
|  ------------------------------------------------------------    |
|  [x] Enable bytecode transformation                               |
|      Transform @Entity classes after Java compilation             |
|                                                                   |
|  [x] Automatically add -javaagent to run configurations           |
|      Adds sim-agent.jar for runtime transformation fallback       |
|                                                                   |
|                                                                   |
|  Notifications                                                    |
|  ------------------------------------------------------------    |
|  [x] Show project detection notification                          |
|      Display notification when Prime Mover project detected       |
|                                                                   |
|  [x] Warn when Maven plugin is also configured                    |
|      Alert for potential duplicate transformation                 |
|                                                                   |
|                                                                   |
|  Advanced                                                         |
|  ------------------------------------------------------------    |
|  Transformation timeout: [30   ] seconds                          |
|      Maximum time for transforming a single class                 |
|                                                                   |
|  [x] Enable debug logging                                         |
|      Write detailed transformation logs to IDE log                |
|                                                                   |
|  [ ] Export transformed bytecode                                  |
|      Save .class files before and after transformation            |
|      Export directory: [____________________] [Browse...]         |
|                                                                   |
+------------------------------------------------------------------+
|                                           [Reset to Defaults]     |
+------------------------------------------------------------------+
```

#### Tab 2: Diagnostics

```
+------------------------------------------------------------------+
|  Prime Mover Settings                                             |
+------------------------------------------------------------------+
| [Configuration] [Diagnostics] [About]                             |
+------------------------------------------------------------------+
|                                                                   |
|  Project Status                                                   |
|  ------------------------------------------------------------    |
|  Project type:        [Maven project detected        ]            |
|  Prime Mover version: [1.0.6-SNAPSHOT                ]            |
|  Transformation mode: [JPS Plugin (active)           ]            |
|  Fallback available:  [sim-agent 1.0.6-SNAPSHOT found]            |
|                                                                   |
|                                                                   |
|  Last Transformation                                              |
|  ------------------------------------------------------------    |
|  Time:               2026-01-19 14:23:45                          |
|  Duration:           1.2 seconds                                  |
|  Classes scanned:    42                                           |
|  Classes transformed: 5                                           |
|  Classes skipped:     0 (already transformed)                     |
|                                                                   |
|  [View Transformation Log]  [Export Diagnostic Report]            |
|                                                                   |
|                                                                   |
|  Environment                                                      |
|  ------------------------------------------------------------    |
|  Java version:       25.0.1 (GraalVM)                             |
|  IDE version:        IntelliJ IDEA 2025.3.1                       |
|  Plugin version:     1.0.6-SNAPSHOT                               |
|  ClassFile API:      Available (java.lang.classfile)              |
|                                                                   |
|  [Check for Updates]  [Verify Installation]                       |
|                                                                   |
+------------------------------------------------------------------+
```

#### Tab 3: About

```
+------------------------------------------------------------------+
|  Prime Mover Settings                                             |
+------------------------------------------------------------------+
| [Configuration] [Diagnostics] [About]                             |
+------------------------------------------------------------------+
|                                                                   |
|        [Prime Mover Logo]                                         |
|                                                                   |
|        Prime Mover IntelliJ Plugin                                |
|        Version 1.0.6-SNAPSHOT                                     |
|                                                                   |
|        Seamless bytecode transformation for                       |
|        discrete event simulation                                  |
|                                                                   |
|  ------------------------------------------------------------    |
|                                                                   |
|  Documentation                                                    |
|    [GitHub Repository]  [Wiki]  [API Documentation]               |
|                                                                   |
|  Support                                                          |
|    [Report Issue]  [Request Feature]  [Discussions]               |
|                                                                   |
|  License                                                          |
|    GNU Affero General Public License v3.0 (AGPL-3.0)              |
|    [View License]                                                 |
|                                                                   |
|  ------------------------------------------------------------    |
|                                                                   |
|  Copyright (c) 2026 Hal Hildebrand                                |
|  All rights reserved                                              |
|                                                                   |
+------------------------------------------------------------------+
```

### Settings Behavior

#### State Persistence

Settings are persisted per-project in `.idea/primemover.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="PrimeMoverSettings">
    <option name="enabled" value="true" />
    <option name="showNotifications" value="true" />
    <option name="warnOnMavenPluginPresent" value="true" />
    <option name="autoAddAgent" value="true" />
    <option name="transformationTimeout" value="30" />
    <option name="debugLogging" value="false" />
    <option name="exportTransformedBytecode" value="false" />
    <option name="exportDirectory" value="" />
  </component>
</project>
```

#### Apply/Reset Behavior

- **Apply**: Immediately saves settings; takes effect on next build
- **Reset to Defaults**: Confirms action, then restores factory settings
- **Cancel**: Reverts all unsaved changes

#### Validation Rules

| Setting | Validation | Error Message |
|---------|------------|---------------|
| Transformation timeout | 1-300 seconds | "Timeout must be between 1 and 300 seconds" |
| Export directory | Must exist and be writable | "Directory does not exist or is not writable" |

---

## Error Notification System

### Notification Types

The plugin uses IntelliJ's notification system with four severity levels:

#### 1. Information Notifications (Blue)

**Purpose**: Status updates and confirmations

**Example - Project Detection**:
```
+------------------------------------------------------------------+
| (i) Prime Mover detected                                    [X]  |
+------------------------------------------------------------------+
| Post-compile bytecode transformation is enabled for @Entity       |
| classes in this project.                                          |
|                                                                   |
| [Don't show again]  [Configure...]                                |
+------------------------------------------------------------------+
```

**Behavior**:
- Auto-dismiss after 10 seconds
- Appears in bottom-right notification balloon area
- Logged to Event Log

#### 2. Warning Notifications (Yellow/Orange)

**Purpose**: Situations requiring user attention but not blocking

**Example - Duplicate Transformation**:
```
+------------------------------------------------------------------+
| (!) Duplicate transformation detected                       [X]  |
+------------------------------------------------------------------+
| Both the IDE plugin and Maven plugin are configured to            |
| transform @Entity classes.                                        |
|                                                                   |
| This may cause classes to be transformed twice. Consider          |
| disabling one of them for cleaner builds.                         |
|                                                                   |
| [Disable IDE Plugin]  [Disable Maven Warning]  [Learn More]       |
+------------------------------------------------------------------+
```

**Behavior**:
- Remains visible until dismissed
- Actions available inline
- Logged to Event Log with WARN level

#### 3. Error Notifications (Red)

**Purpose**: Transformation failures requiring immediate attention

**Example - Transformation Failure**:
```
+------------------------------------------------------------------+
| (X) Bytecode transformation failed                          [X]  |
+------------------------------------------------------------------+
| Failed to transform entity class:                                 |
| com.example.simulation.MyEntity                                   |
|                                                                   |
| Error: Unsupported synchronized method in @Entity class           |
|                                                                   |
| Impact: This entity will not work in simulation. Method calls     |
| will throw UnsupportedOperationException.                         |
|                                                                   |
| [View Details]  [Try Fallback]  [Open Source]                     |
+------------------------------------------------------------------+
```

**Behavior**:
- Remains visible until dismissed or action taken
- Critical errors also appear in Build tool window
- Logged to Event Log with ERROR level

#### 4. Sticky Notifications (Persistent Banner)

**Purpose**: Critical configuration issues requiring resolution

**Example - Java Version Incompatible**:
```
+------------------------------------------------------------------+
| (X) Java 25+ required for Prime Mover                             |
|     Current: Java 17  |  [Configure SDK]  [Dismiss]               |
+------------------------------------------------------------------+
```

**Behavior**:
- Appears as banner at top of editor
- Persists until resolved or dismissed
- Reappears on next project open if unresolved

### Notification Actions

Each notification includes contextual actions:

| Action | Description | Implementation |
|--------|-------------|----------------|
| **Configure...** | Opens settings panel | `ShowSettingsUtil.getInstance().showSettingsDialog(project, "Prime Mover")` |
| **View Details** | Opens detailed error view | Modal dialog with full stack trace and suggestions |
| **Try Fallback** | Enables sim-agent fallback | Sets `autoAddAgent = true` and shows confirmation |
| **Open Source** | Navigates to source file | Opens editor at entity class definition |
| **Don't show again** | Suppresses this notification type | Updates settings |
| **Learn More** | Opens documentation | Opens browser to relevant wiki page |

### Error Detail Dialog

When "View Details" is clicked, a detailed dialog appears:

```
+------------------------------------------------------------------+
|  Transformation Error Details                              [X]   |
+------------------------------------------------------------------+
|                                                                   |
|  Class: com.example.simulation.MyEntity                           |
|  File: src/main/java/com/example/simulation/MyEntity.java         |
|  Line: 45                                                         |
|                                                                   |
|  Error Type: ENTITY_TRANSFORMATION                                |
|  Severity: HIGH                                                   |
|                                                                   |
|  Message:                                                         |
|  +--------------------------------------------------------------+ |
|  | Unsupported method pattern in entity class.                  | |
|  |                                                              | |
|  | Method: public synchronized void process()                   | |
|  |                                                              | |
|  | Prime Mover does not support the 'synchronized' keyword in   | |
|  | @Entity classes. Simulation entities use event-based         | |
|  | concurrency control instead.                                 | |
|  +--------------------------------------------------------------+ |
|                                                                   |
|  Suggested Actions:                                               |
|  [x] 1. Remove 'synchronized' keyword from method                 |
|  [ ] 2. Use @NonEvent to exclude method from transformation       |
|  [ ] 3. Enable sim-agent fallback for runtime transformation      |
|                                                                   |
|  Stack Trace:                                                     |
|  [Show Stack Trace v]                                             |
|                                                                   |
+------------------------------------------------------------------+
|  [Copy to Clipboard]  [Export Report]     [Apply]  [Close]        |
+------------------------------------------------------------------+
```

---

## Project Detection and Setup Assistant

### Detection Flow

When a project is opened, the plugin performs automatic detection:

```
Project Opened
    |
    v
Scan pom.xml / build.gradle
    |
    +---> Prime Mover dependency found?
    |         |
    |         YES ---> Show detection notification
    |         |            |
    |         |            +---> Maven plugin also present?
    |         |                      |
    |         |                      YES ---> Show duplicate warning
    |         |                      NO  ---> Normal operation
    |         |
    |         NO ---> Silent (no notification)
    |
    v
Continue IDE initialization
```

### Setup Assistant Wizard

For new Prime Mover projects, a setup assistant guides users through configuration:

#### Step 1: Welcome

```
+------------------------------------------------------------------+
|  Prime Mover Setup Assistant                               [X]   |
+------------------------------------------------------------------+
|                                                                   |
|  [Prime Mover Logo]                                               |
|                                                                   |
|  Welcome to Prime Mover!                                          |
|                                                                   |
|  Prime Mover transforms your @Entity classes into discrete        |
|  event simulation participants. This wizard will help you         |
|  configure the plugin for your project.                           |
|                                                                   |
|  Detected Configuration:                                          |
|    - Build system: Maven                                          |
|    - Prime Mover API: 1.0.6-SNAPSHOT                              |
|    - Prime Mover Runtime: 1.0.6-SNAPSHOT                          |
|                                                                   |
+------------------------------------------------------------------+
|  [ ] Don't show this wizard for new projects                      |
|                                                                   |
|                            [Skip Setup]  [Next >]                 |
+------------------------------------------------------------------+
```

#### Step 2: Transformation Mode

```
+------------------------------------------------------------------+
|  Prime Mover Setup Assistant - Step 2 of 4                 [X]   |
+------------------------------------------------------------------+
|                                                                   |
|  Choose Transformation Mode                                       |
|                                                                   |
|  How should Prime Mover transform your @Entity classes?           |
|                                                                   |
|  (*) IDE Plugin (Recommended)                                     |
|      Transform during IDE builds for instant feedback             |
|      Best for: Active development, quick iteration                |
|                                                                   |
|  ( ) Maven Plugin Only                                            |
|      Transform only during Maven builds                           |
|      Best for: CI/CD pipelines, reproducible builds               |
|                                                                   |
|  ( ) Runtime Agent (sim-agent)                                    |
|      Transform at application startup                             |
|      Best for: Hot-reload, dynamic class loading                  |
|                                                                   |
|  [?] Learn more about transformation modes                        |
|                                                                   |
+------------------------------------------------------------------+
|                                                                   |
|                            [< Back]  [Next >]                     |
+------------------------------------------------------------------+
```

#### Step 3: Fallback Configuration

```
+------------------------------------------------------------------+
|  Prime Mover Setup Assistant - Step 3 of 4                 [X]   |
+------------------------------------------------------------------+
|                                                                   |
|  Configure Fallback Behavior                                      |
|                                                                   |
|  What should happen if IDE transformation fails?                  |
|                                                                   |
|  [x] Automatically add sim-agent to run configurations            |
|      If JPS transformation fails, use runtime transformation      |
|      as a fallback to ensure simulation code works                |
|                                                                   |
|  [x] Show notification on transformation errors                   |
|      Display actionable notifications when problems occur         |
|                                                                   |
|  [x] Warn about duplicate transformation tools                    |
|      Alert when multiple transformation tools are active          |
|                                                                   |
+------------------------------------------------------------------+
|                                                                   |
|                            [< Back]  [Next >]                     |
+------------------------------------------------------------------+
```

#### Step 4: Summary

```
+------------------------------------------------------------------+
|  Prime Mover Setup Assistant - Complete!                   [X]   |
+------------------------------------------------------------------+
|                                                                   |
|  Configuration Summary                                            |
|                                                                   |
|  +--------------------------------------------------------------+ |
|  | Transformation Mode: IDE Plugin                              | |
|  | Automatic Fallback:  Enabled (sim-agent)                     | |
|  | Notifications:       Enabled                                 | |
|  | Duplicate Warning:   Enabled                                 | |
|  +--------------------------------------------------------------+ |
|                                                                   |
|  Your Prime Mover configuration is complete!                      |
|                                                                   |
|  Next steps:                                                      |
|  1. Annotate your simulation classes with @Entity                 |
|  2. Build the project (Ctrl+F9 / Cmd+F9)                          |
|  3. Run your simulation                                           |
|                                                                   |
|  [View Getting Started Guide]                                     |
|                                                                   |
+------------------------------------------------------------------+
|                                                                   |
|                            [< Back]  [Finish]                     |
+------------------------------------------------------------------+
```

### Detection Notification Variations

#### New Project Detected
```
+------------------------------------------------------------------+
| (i) Prime Mover project detected                            [X]  |
+------------------------------------------------------------------+
| This project uses Prime Mover for discrete event simulation.      |
| Bytecode transformation will be applied to @Entity classes        |
| during builds.                                                    |
|                                                                   |
| [Run Setup Assistant]  [Use Defaults]  [Don't show again]         |
+------------------------------------------------------------------+
```

#### Missing Dependencies
```
+------------------------------------------------------------------+
| (!) Prime Mover dependencies incomplete                     [X]  |
+------------------------------------------------------------------+
| The project references Prime Mover but is missing dependencies:   |
|                                                                   |
|   - [x] api module (com.hellblazer.primeMover:api)                |
|   - [ ] runtime module (com.hellblazer.primeMover:runtime)        |
|                                                                   |
| Without the runtime module, simulation code will fail at runtime. |
|                                                                   |
| [Add Missing Dependencies]  [Configure Manually]                  |
+------------------------------------------------------------------+
```

---

## Version Compatibility Warnings

### Java Version Check

The plugin performs Java version validation at startup:

```
+------------------------------------------------------------------+
|  Java Version Compatibility                                       |
+------------------------------------------------------------------+
|                                                                   |
|  Current Configuration                                            |
|  ------------------------------------------------------------    |
|  Project SDK:      Java 17 (Temurin-17.0.5)                       |
|  Required:         Java 25 or later                               |
|  ClassFile API:    NOT AVAILABLE                                  |
|                                                                   |
|  Status: INCOMPATIBLE                                             |
|                                                                   |
|  Prime Mover requires Java 25+ for the ClassFile API used         |
|  in bytecode transformation. Without it, @Entity classes          |
|  cannot be transformed.                                           |
|                                                                   |
|  Options:                                                         |
|  1. Update project SDK to Java 25+                                |
|  2. Use Maven plugin for transformation (runs in separate JVM)    |
|  3. Use sim-agent at runtime (requires Java 25+ in deployment)    |
|                                                                   |
+------------------------------------------------------------------+
|  [Download Java 25]  [Configure SDK]  [Use Maven Plugin]          |
+------------------------------------------------------------------+
```

### Plugin Version Check

When the project's Prime Mover version differs from the plugin:

```
+------------------------------------------------------------------+
| (!) Version mismatch detected                               [X]  |
+------------------------------------------------------------------+
| Project Prime Mover version (1.0.5) differs from                  |
| plugin version (1.0.6-SNAPSHOT).                                  |
|                                                                   |
| This may cause transformation inconsistencies if the API          |
| has changed between versions.                                     |
|                                                                   |
| [Update Project Version]  [Update Plugin]  [Dismiss]              |
+------------------------------------------------------------------+
```

### IDE Version Check

```
+------------------------------------------------------------------+
| (!) IDE version warning                                     [X]  |
+------------------------------------------------------------------+
| Prime Mover plugin may have limited functionality on              |
| IntelliJ IDEA 2024.3.                                             |
|                                                                   |
| Recommended: IntelliJ IDEA 2025.3 or later                        |
| Current: IntelliJ IDEA 2024.3.2                                   |
|                                                                   |
| Known issues:                                                     |
| - Build messages may not display correctly                        |
| - Settings panel layout may be misaligned                         |
|                                                                   |
| [Check for IDE Updates]  [Continue Anyway]                        |
+------------------------------------------------------------------+
```

### Compatibility Matrix Display

In Diagnostics tab, show compatibility summary:

```
+--------------------------------------------------------------+
|  Compatibility Check                                          |
+--------------------------------------------------------------+
|                                                               |
|  Component            Version          Status                 |
|  -----------------------------------------------------------  |
|  Java SDK             25.0.1           [OK]  Compatible       |
|  IntelliJ IDEA        2025.3.1         [OK]  Compatible       |
|  Prime Mover API      1.0.6-SNAPSHOT   [OK]  Compatible       |
|  Prime Mover Runtime  1.0.6-SNAPSHOT   [OK]  Compatible       |
|  Prime Mover Plugin   1.0.6-SNAPSHOT   [OK]  Compatible       |
|  ClassFile API        Available        [OK]  Compatible       |
|                                                               |
|  Overall Status: All components compatible                    |
|                                                               |
+--------------------------------------------------------------+
```

---

## Build Transformation Progress Indicator

### Status Bar Integration

During build, transformation status appears in the IDE status bar:

#### Idle State
```
[Prime Mover: Ready]
```

#### Transformation In Progress
```
[Prime Mover: Transforming 5 classes... (3/5)]  [=====     ] 60%
```

#### Transformation Complete
```
[Prime Mover: 5 classes transformed]  (click for details)
```

#### Transformation Error
```
[Prime Mover: 2 errors]  (click to view)
```

### Build Tool Window Integration

Transformation messages appear in the Build tool window:

```
+------------------------------------------------------------------+
|  Build                                                      [-]  |
+------------------------------------------------------------------+
| [Build Output] [Sync] [Prime Mover]                               |
+------------------------------------------------------------------+
|                                                                   |
| Build started: 2026-01-19 14:23:45                                |
|                                                                   |
| > Task :compileJava                                               |
|   Compiling 15 Java source files                                  |
|                                                                   |
| > Prime Mover Instrumenter                                        |
|   Scanning for @Entity classes...                                 |
|   Found 5 entity classes                                          |
|   Transforming: com.example.MyEntity                              |
|   Transforming: com.example.AnotherEntity                         |
|   Transforming: com.example.ThirdEntity                           |
|   Transforming: com.example.FourthEntity                          |
|   Transforming: com.example.FifthEntity                           |
|                                                                   |
|   Prime Mover: 5 classes transformed, 0 skipped                   |
|   Transformation time: 1.2 seconds                                |
|                                                                   |
| BUILD SUCCESSFUL in 3s                                            |
|                                                                   |
+------------------------------------------------------------------+
```

### Progress Dialog (Long Transformations)

For transformations exceeding 5 seconds, show a non-modal progress dialog:

```
+------------------------------------------------------------------+
|  Prime Mover Transformation                                 [X]  |
+------------------------------------------------------------------+
|                                                                   |
|  Transforming @Entity classes...                                  |
|                                                                   |
|  [====================                    ] 50%                   |
|                                                                   |
|  Current: com.example.simulation.ComplexEntity                    |
|  Elapsed: 7.3 seconds                                             |
|  Remaining: ~7 seconds (estimated)                                |
|                                                                   |
|  Progress:                                                        |
|    Scanned:     42 classes                                        |
|    Transformed: 5 / 10 entities                                   |
|    Skipped:     0                                                 |
|    Errors:      0                                                 |
|                                                                   |
+------------------------------------------------------------------+
|  [ ] Run in background                       [Cancel]             |
+------------------------------------------------------------------+
```

### Event Log Integration

All transformation events are logged to the IDE Event Log:

```
+------------------------------------------------------------------+
|  Event Log                                                   [-] |
+------------------------------------------------------------------+
| [All] [Info] [Warning] [Error]                      [Clear All]   |
+------------------------------------------------------------------+
|                                                                   |
| 14:23:45 [INFO] Prime Mover: Build started                        |
| 14:23:46 [INFO] Prime Mover: Scanning for @Entity classes         |
| 14:23:46 [INFO] Prime Mover: Found 5 entity classes               |
| 14:23:46 [INFO] Prime Mover: Transformed com.example.MyEntity     |
| 14:23:46 [INFO] Prime Mover: Transformed com.example.AnotherEntity|
| 14:23:47 [WARN] Prime Mover: Skipped com.example.Legacy (already) |
| 14:23:47 [INFO] Prime Mover: Transformation complete (5/5)        |
|                                                                   |
+------------------------------------------------------------------+
```

---

## Event Simulation Debugging UI

### Overview

This section describes the future debugging UI for simulation events. This is a mockup for future enhancement - the current plugin does not implement these features.

### Simulation Event Inspector Tool Window

**Location**: View > Tool Windows > Prime Mover Events

```
+------------------------------------------------------------------+
|  Prime Mover Events                                         [-]  |
+------------------------------------------------------------------+
| [Events] [Entities] [Timeline] [Statistics]                       |
+------------------------------------------------------------------+
|                                                                   |
|  Active Simulation: MySimulation (running)         [Pause] [Stop] |
|  Simulation Time: 1,234.56 units                                  |
|  Real Time: 00:02:34                                              |
|                                                                   |
|  Event Queue (next 10 events):                                    |
|  +--------------------------------------------------------------+ |
|  | Time     | Entity          | Method           | State        | |
|  |----------|-----------------|------------------|------------- | |
|  | 1234.60  | Customer#42     | arrive()         | Pending      | |
|  | 1235.00  | Server#1        | startService()   | Pending      | |
|  | 1237.50  | Customer#42     | depart()         | Pending      | |
|  | 1240.00  | Customer#43     | arrive()         | Pending      | |
|  | 1242.50  | Server#2        | startService()   | Pending      | |
|  +--------------------------------------------------------------+ |
|                                                                   |
|  Event History (last 10 events):                                  |
|  +--------------------------------------------------------------+ |
|  | Time     | Entity          | Method           | Duration     | |
|  |----------|-----------------|------------------|------------- | |
|  | 1230.00  | Customer#41     | depart()         | 5.00 units   | |
|  | 1225.00  | Server#1        | endService()     | 0.01 units   | |
|  | 1220.00  | Customer#41     | startService()   | 0.02 units   | |
|  +--------------------------------------------------------------+ |
|                                                                   |
+------------------------------------------------------------------+
```

### Entity State Inspector

```
+------------------------------------------------------------------+
|  Entity Inspector: Customer#42                              [-]  |
+------------------------------------------------------------------+
|                                                                   |
|  Class: com.example.simulation.Customer                           |
|  Created: Simulation time 1200.00                                 |
|  State: Waiting in queue                                          |
|                                                                   |
|  Fields:                                                          |
|  +--------------------------------------------------------------+ |
|  | Name             | Type    | Current Value                   | |
|  |------------------|---------|-------------------------------- | |
|  | id               | int     | 42                              | |
|  | arrivalTime      | double  | 1200.00                         | |
|  | serviceTime      | double  | 5.50                            | |
|  | priority         | int     | 2                               | |
|  +--------------------------------------------------------------+ |
|                                                                   |
|  Event History:                                                   |
|  +--------------------------------------------------------------+ |
|  | Time     | Method           | Caller          | Result        | |
|  |----------|------------------|-----------------|-------------- | |
|  | 1200.00  | arrive()         | Generator       | queued        | |
|  | 1234.60  | startService()   | (scheduled)     | (pending)     | |
|  +--------------------------------------------------------------+ |
|                                                                   |
|  [Set Breakpoint]  [Step to Next Event]  [View Source]            |
|                                                                   |
+------------------------------------------------------------------+
```

### Simulation Timeline View

```
+------------------------------------------------------------------+
|  Simulation Timeline                                        [-]  |
+------------------------------------------------------------------+
|                                                                   |
|  Time Scale: [1 unit = 10 px]  [Zoom: [+][-]]                     |
|                                                                   |
|  |--1200--|--1210--|--1220--|--1230--|--1240--|--1250--|          |
|                                                                   |
|  Customer#40:                                                     |
|  |=====[arrive]==[queue]==[service]=======[depart]====|           |
|                                                                   |
|  Customer#41:                                                     |
|       |=====[arrive]==[queue]===[service]=========[depart]==|     |
|                                                                   |
|  Customer#42:                                                     |
|            |=====[arrive]==[queue (current)]............|         |
|                                                                   |
|  Server#1:                                                        |
|  |==[C40]========[idle]========[C41]========[idle]==[C42]...|     |
|                                                                   |
|  Server#2:                                                        |
|  |========[C39]========[idle]==================[C43]...........|  |
|                                                                   |
|  Legend: [====] Activity  [....] Scheduled  |  Current time       |
|                                                                   |
+------------------------------------------------------------------+
```

### Simulation Statistics Panel

```
+------------------------------------------------------------------+
|  Simulation Statistics                                      [-]  |
+------------------------------------------------------------------+
|                                                                   |
|  Performance Metrics                                              |
|  ------------------------------------------------------------    |
|  Events processed:     1,234                                      |
|  Events/second:        456.7                                      |
|  Average event time:   0.0021 seconds                             |
|  Simulation speedup:   1,000x real-time                           |
|                                                                   |
|  Entity Statistics                                                |
|  ------------------------------------------------------------    |
|  Active entities:      42                                         |
|  Total created:        156                                        |
|  Total destroyed:      114                                        |
|  Peak active:          58 (at time 890.0)                         |
|                                                                   |
|  Queue Statistics                                                 |
|  ------------------------------------------------------------    |
|  Average wait time:    12.5 units                                 |
|  Max wait time:        45.2 units                                 |
|  Queue length (avg):   3.2                                        |
|  Queue length (max):   8                                          |
|                                                                   |
|  [Export Statistics]  [Clear]  [Start Recording]                  |
|                                                                   |
+------------------------------------------------------------------+
```

### Debug Actions

| Action | Shortcut | Description |
|--------|----------|-------------|
| Pause Simulation | F7 | Pause at current simulation time |
| Step Event | F8 | Execute one event and pause |
| Step Time | Shift+F8 | Advance to next time unit |
| Resume | F9 | Continue simulation |
| Stop | Shift+F2 | Terminate simulation |
| Set Event Breakpoint | Ctrl+F8 | Break when specific event fires |
| Watch Entity | Ctrl+Shift+W | Monitor entity state changes |

---

## Accessibility Considerations

### WCAG 2.1 AA Compliance

The plugin UI adheres to WCAG 2.1 Level AA guidelines:

#### 1. Perceivable

**1.1 Text Alternatives**
- All icons have tooltip text descriptions
- Images in documentation have alt text
- Status indicators have text equivalents

**1.3 Adaptable**
- UI adapts to IntelliJ's font size settings
- Layout works with increased text spacing
- No reliance on color alone for information

**1.4 Distinguishable**
- Minimum contrast ratio 4.5:1 for normal text
- Minimum contrast ratio 3:1 for large text and UI components
- Focus indicators visible on all interactive elements

#### 2. Operable

**2.1 Keyboard Accessible**
- All functionality available via keyboard
- No keyboard traps
- Tab order follows logical flow

**2.4 Navigable**
- Settings have clear section headings
- Focus visible on all interactive elements
- Skip links for long content areas

**2.5 Input Modalities**
- Touch targets minimum 44x44 pixels
- No gesture-only actions

#### 3. Understandable

**3.1 Readable**
- Language declared (English)
- Abbreviations explained on first use
- Technical terms defined in context

**3.2 Predictable**
- Consistent navigation across settings tabs
- No unexpected context changes
- Form submission only on explicit action

**3.3 Input Assistance**
- Error messages are descriptive
- Required fields clearly marked
- Input validation provides specific guidance

#### 4. Robust

**4.1 Compatible**
- Standard HTML elements where applicable
- ARIA labels for custom components
- Works with screen readers (JAWS, NVDA, VoiceOver)

### Screen Reader Support

All custom UI components include ARIA attributes:

```java
// Example: Accessible checkbox
JBCheckBox enabledCheckbox = new JBCheckBox("Enable bytecode transformation");
enabledCheckbox.getAccessibleContext().setAccessibleDescription(
    "When enabled, classes annotated with @Entity will be transformed " +
    "after Java compilation to support discrete event simulation."
);
```

### High Contrast Theme Support

The plugin respects IntelliJ's high contrast theme:

| Element | Light Theme | Dark Theme | High Contrast |
|---------|-------------|------------|---------------|
| Error text | #CC0000 | #FF6B6B | #FF0000 |
| Warning text | #CC7700 | #FFB347 | #FFFF00 |
| Success text | #007700 | #7FBF7F | #00FF00 |
| Link text | #0066CC | #6CA0DC | #00FFFF |
| Background | #FFFFFF | #2B2B2B | #000000 |
| Foreground | #000000 | #BBBBBB | #FFFFFF |

### Reduced Motion Support

For users who prefer reduced motion:
- Progress bars use static fill instead of animation
- Notifications slide in without animation
- Status changes are instant rather than animated

Detection:
```java
if (UISettings.getInstance().getDisableMnemonicsInControls()) {
    // User prefers reduced motion - use static UI
}
```

---

## Keyboard Navigation

### Global Shortcuts

| Shortcut | Action | Context |
|----------|--------|---------|
| Ctrl+Alt+Shift+P | Open Prime Mover Settings | Anywhere |
| Ctrl+Alt+Shift+T | Toggle Transformation | Build context |
| Ctrl+Alt+Shift+D | Show Diagnostics | Anywhere |

### Settings Dialog Navigation

| Key | Action |
|-----|--------|
| Tab | Move to next control |
| Shift+Tab | Move to previous control |
| Enter | Activate button/apply setting |
| Space | Toggle checkbox |
| Escape | Cancel and close dialog |
| Ctrl+1 | Go to Configuration tab |
| Ctrl+2 | Go to Diagnostics tab |
| Ctrl+3 | Go to About tab |

### Notification Navigation

| Key | Action |
|-----|--------|
| Tab | Cycle through notification actions |
| Enter | Activate selected action |
| Escape | Dismiss notification |

### Build Tool Window Navigation

| Key | Action |
|-----|--------|
| F2 | Next error/warning |
| Shift+F2 | Previous error/warning |
| Enter | Navigate to source |
| Ctrl+F | Find in output |

### Focus Management

Focus order in Settings dialog:
1. Tab bar (Configuration, Diagnostics, About)
2. First control group (Transformation)
3. Controls within group (top to bottom)
4. Next control group
5. Action buttons (Reset, Apply, Cancel)

Focus restoration:
- When reopening settings, focus returns to last active control
- After action completion, focus returns to triggering element

---

## Icon and Visual Design

### Plugin Icon

**Main Icon** (16x16, 32x32, 48x48, 128x128):
- Concept: Stylized "PM" letters with event flow arrows
- Colors: Blue primary (#4A90D9), accent (#7CB342)
- Style: Flat design matching IntelliJ 2025 icon style

```
+----------------+
|    PM          |
|   /  \         |
|  /    \        |
| o---->o---->o  |
+----------------+
```

### Status Icons

| Status | Icon | Color | Description |
|--------|------|-------|-------------|
| Ready | Circle outline | Gray | Transformation ready |
| Transforming | Spinning circle | Blue | Transformation in progress |
| Success | Checkmark | Green | Transformation successful |
| Warning | Triangle | Yellow | Non-blocking issue |
| Error | X circle | Red | Transformation failed |
| Disabled | Circle with slash | Gray | Transformation disabled |

### Notification Icons

Use IntelliJ's built-in notification icons:
- `AllIcons.General.Information` - Info notifications
- `AllIcons.General.Warning` - Warning notifications
- `AllIcons.General.Error` - Error notifications
- `AllIcons.General.BalloonInformation` - Status updates

### Color Palette

**Primary Colors** (adapted for theme):

| Use | Light | Dark | High Contrast |
|-----|-------|------|---------------|
| Primary action | #4A90D9 | #6CA0DC | #00BFFF |
| Secondary action | #7CB342 | #9CCC65 | #00FF00 |
| Destructive action | #E53935 | #EF5350 | #FF0000 |
| Neutral | #757575 | #9E9E9E | #808080 |

**Semantic Colors**:

| Meaning | Light | Dark | High Contrast |
|---------|-------|------|---------------|
| Success | #4CAF50 | #81C784 | #00FF00 |
| Warning | #FF9800 | #FFB74D | #FFFF00 |
| Error | #F44336 | #E57373 | #FF0000 |
| Info | #2196F3 | #64B5F6 | #00BFFF |

---

## Mockups and Wireframes

### Settings Page - Configuration Tab

```
+====================================================================+
||  Prime Mover                                                    X ||
+====================================================================+
|| [Configuration] [Diagnostics] [About]                             ||
+--------------------------------------------------------------------+
||                                                                   ||
||  TRANSFORMATION                                                   ||
||  ________________________________________________________________ ||
||                                                                   ||
||  [X] Enable bytecode transformation                               ||
||      |  Transform @Entity classes after Java compilation.         ||
||      |  Converts simulation code for discrete event execution.    ||
||                                                                   ||
||  [X] Automatically add -javaagent to run configurations           ||
||      |  Enables sim-agent.jar for runtime transformation          ||
||      |  fallback when IDE transformation is unavailable.          ||
||                                                                   ||
||                                                                   ||
||  NOTIFICATIONS                                                    ||
||  ________________________________________________________________ ||
||                                                                   ||
||  [X] Show project detection notification                          ||
||      |  Displays a notification when opening a project            ||
||      |  that uses Prime Mover for simulation.                     ||
||                                                                   ||
||  [X] Warn when Maven plugin is also configured                    ||
||      |  Alerts you if both IDE and Maven plugins are              ||
||      |  transforming classes (potential duplication).             ||
||                                                                   ||
||                                                                   ||
||  ADVANCED                                 [v] Show Advanced       ||
||  ________________________________________________________________ ||
||                                                                   ||
||  Transformation timeout:  [30      ] seconds                      ||
||                          Max time for single class transformation ||
||                                                                   ||
||  [ ] Enable debug logging                                         ||
||      |  Writes detailed transformation logs to IDE log.           ||
||      |  Useful for troubleshooting.                               ||
||                                                                   ||
||  [ ] Export transformed bytecode                                  ||
||      |  Save .class files before and after transformation.        ||
||      |  Export directory: [_______________________] [...]         ||
||                                                                   ||
+--------------------------------------------------------------------+
||                                          [Reset to Defaults]      ||
+====================================================================+
```

### Settings Page - Diagnostics Tab

```
+====================================================================+
||  Prime Mover                                                    X ||
+====================================================================+
|| [Configuration] [Diagnostics] [About]                             ||
+--------------------------------------------------------------------+
||                                                                   ||
||  PROJECT STATUS                                                   ||
||  ________________________________________________________________ ||
||                                                                   ||
||  Project type:        Maven project                               ||
||  Prime Mover version: 1.0.6-SNAPSHOT                              ||
||  Transformation mode: JPS Plugin (active)                         ||
||  Fallback available:  sim-agent 1.0.6-SNAPSHOT found              ||
||                                                                   ||
||  +--------------------------------------------------------------+ ||
||  | Status: All systems operational                        [OK]  | ||
||  +--------------------------------------------------------------+ ||
||                                                                   ||
||                                                                   ||
||  LAST TRANSFORMATION                                              ||
||  ________________________________________________________________ ||
||                                                                   ||
||  Time:                2026-01-19 14:23:45                         ||
||  Duration:            1.2 seconds                                 ||
||  Classes scanned:     42                                          ||
||  Classes transformed: 5                                           ||
||  Classes skipped:     0 (already transformed)                     ||
||  Errors:              0                                           ||
||                                                                   ||
||  [View Transformation Log]     [Export Diagnostic Report]         ||
||                                                                   ||
||                                                                   ||
||  ENVIRONMENT                                                      ||
||  ________________________________________________________________ ||
||                                                                   ||
||  Java version:        25.0.1 (GraalVM)                    [OK]    ||
||  IDE version:         IntelliJ IDEA 2025.3.1              [OK]    ||
||  Plugin version:      1.0.6-SNAPSHOT                      [OK]    ||
||  ClassFile API:       Available                           [OK]    ||
||                                                                   ||
||  [Check for Updates]          [Verify Installation]               ||
||                                                                   ||
+--------------------------------------------------------------------+
||                                                                   ||
+====================================================================+
```

### Error Notification Balloon

```
     +---------------------------------------------------------------+
     |                                                               |
     | (X) Bytecode transformation failed                       [X] |
     |                                                               |
     | Failed to transform entity class:                             |
     | com.example.simulation.MyEntity                               |
     |                                                               |
     | Error: Unsupported synchronized method in @Entity class       |
     |                                                               |
     | Impact: This entity will not work in simulation.              |
     |                                                               |
     | [View Details]  [Try Fallback]  [Open Source]                 |
     |                                                               |
     +---------------------------------------------------------------+
```

### Project Setup Wizard - Step 1

```
+====================================================================+
||  Prime Mover Setup Assistant                                    X ||
+====================================================================+
||                                                                   ||
||                     +------------------+                          ||
||                     |                  |                          ||
||                     |   [PM Logo]      |                          ||
||                     |                  |                          ||
||                     +------------------+                          ||
||                                                                   ||
||                  Welcome to Prime Mover!                          ||
||                                                                   ||
||         Prime Mover transforms your @Entity classes into          ||
||         discrete event simulation participants. This wizard       ||
||         will help you configure the plugin for your project.      ||
||                                                                   ||
||                                                                   ||
||         +-----------------------------------------------------+   ||
||         |  Detected Configuration:                            |   ||
||         |                                                     |   ||
||         |    Build system:       Maven                        |   ||
||         |    Prime Mover API:    1.0.6-SNAPSHOT               |   ||
||         |    Prime Mover Runtime: 1.0.6-SNAPSHOT              |   ||
||         |    sim-agent:          1.0.6-SNAPSHOT               |   ||
||         +-----------------------------------------------------+   ||
||                                                                   ||
||                                                                   ||
||  [ ] Don't show this wizard for new Prime Mover projects          ||
||                                                                   ||
+--------------------------------------------------------------------+
||                               [Skip Setup]     [Next >]           ||
+====================================================================+
```

### Version Compatibility Warning Dialog

```
+====================================================================+
||  Java Version Incompatible                                      X ||
+====================================================================+
||                                                                   ||
||     +-------------------+                                         ||
||     |    [!]            |     Java 25+ Required                   ||
||     |   Warning         |                                         ||
||     +-------------------+                                         ||
||                                                                   ||
||  Current Configuration:                                           ||
||  ________________________________________________________________ ||
||                                                                   ||
||  Project SDK:      Java 17 (Temurin-17.0.5)                       ||
||  Required:         Java 25 or later                               ||
||  ClassFile API:    NOT AVAILABLE                                  ||
||                                                                   ||
||  ________________________________________________________________ ||
||                                                                   ||
||  Prime Mover requires Java 25 or later for the ClassFile API      ||
||  used in bytecode transformation.                                 ||
||                                                                   ||
||  Without Java 25+, @Entity classes cannot be transformed          ||
||  and simulation code will fail at runtime.                        ||
||                                                                   ||
||                                                                   ||
||  Options:                                                         ||
||  ________________________________________________________________ ||
||                                                                   ||
||  (*) Update project SDK to Java 25+ (recommended)                 ||
||  ( ) Use Maven plugin (requires Maven with Java 25+)              ||
||  ( ) Use sim-agent at runtime only                                ||
||  ( ) Continue without transformation (code will fail)             ||
||                                                                   ||
+--------------------------------------------------------------------+
||  [Download Java 25]   [Configure SDK]              [Apply]        ||
+====================================================================+
```

### Build Progress in Status Bar

**Idle State:**
```
+============================================================+
|  [Prime Mover: Ready]                      | Ln 45 Col 12  |
+============================================================+
```

**Transforming:**
```
+============================================================+
|  [PM: Transforming... 3/5] [========    ] 60%   | Ln 45    |
+============================================================+
```

**Complete:**
```
+============================================================+
|  [PM: 5 transformed] (click)               | Ln 45 Col 12  |
+============================================================+
```

**Error:**
```
+============================================================+
|  [PM: 2 errors] (!)                        | Ln 45 Col 12  |
+============================================================+
```

### Build Tool Window - Prime Mover Tab

```
+====================================================================+
||  Build                                                       - X ||
+====================================================================+
|| [Build Output] [Sync] [Prime Mover]                               ||
+--------------------------------------------------------------------+
||                                                                   ||
||  Build: MyProject                                                 ||
||  Started: 2026-01-19 14:23:45                                     ||
||                                                                   ||
||  > Compile Java                                          [DONE]   ||
||    Compiled 15 source files                                       ||
||                                                                   ||
||  > Prime Mover Transformation                            [DONE]   ||
||    |                                                              ||
||    +-- Scanning for @Entity classes...                            ||
||    |   Found 5 entity classes in 2 modules                        ||
||    |                                                              ||
||    +-- Transforming entities...                                   ||
||        |                                                          ||
||        +-- [OK] com.example.MyEntity                              ||
||        +-- [OK] com.example.AnotherEntity                         ||
||        +-- [OK] com.example.ThirdEntity                           ||
||        +-- [OK] com.example.FourthEntity                          ||
||        +-- [OK] com.example.FifthEntity                           ||
||                                                                   ||
||    Summary: 5 classes transformed, 0 skipped, 0 errors            ||
||    Time: 1.2 seconds                                              ||
||                                                                   ||
||  BUILD SUCCESSFUL in 3.4s                                         ||
||                                                                   ||
+--------------------------------------------------------------------+
```

### Future: Event Debugging Tool Window

```
+====================================================================+
||  Prime Mover Events                                          - X ||
+====================================================================+
|| [Events] [Entities] [Timeline] [Statistics]                       ||
+--------------------------------------------------------------------+
||                                                                   ||
||  Simulation: MySimulation                     [Pause]  [Stop]     ||
||  Time: 1,234.56 units | Real: 00:02:34 | Speed: 1000x             ||
||                                                                   ||
||  +--------------------------------------------------------------+ ||
||  | EVENT QUEUE (next 5)                                         | ||
||  |--------------------------------------------------------------| ||
||  | Time     | Entity          | Event            | State        | ||
||  |----------|-----------------|------------------|------------- | ||
||  | 1234.60  | Customer#42     | arrive()         | Pending      | ||
||  | 1235.00  | Server#1        | startService()   | Pending      | ||
||  | 1237.50  | Customer#42     | depart()         | Pending      | ||
||  | 1240.00  | Customer#43     | arrive()         | Pending      | ||
||  | 1242.50  | Server#2        | startService()   | Pending      | ||
||  +--------------------------------------------------------------+ ||
||                                                                   ||
||  +--------------------------------------------------------------+ ||
||  | RECENT EVENTS (last 5)                                       | ||
||  |--------------------------------------------------------------| ||
||  | Time     | Entity          | Event            | Duration     | ||
||  |----------|-----------------|------------------|------------- | ||
||  | 1230.00  | Customer#41     | depart()         | 5.00         | ||
||  | 1225.00  | Server#1        | endService()     | 0.01         | ||
||  | 1220.00  | Customer#41     | startService()   | 0.02         | ||
||  | 1215.00  | Customer#41     | arrive()         | 0.01         | ||
||  | 1210.00  | Customer#40     | depart()         | 4.80         | ||
||  +--------------------------------------------------------------+ ||
||                                                                   ||
||  [Set Breakpoint]  [Step Event F8]  [Step Time Shift+F8]          ||
||                                                                   ||
+--------------------------------------------------------------------+
```

---

## Implementation Notes

### Technology Stack

The settings UI should be implemented using:
- **IntelliJ Platform SDK**: Standard configurable and panel classes
- **FormBuilder**: IntelliJ's form layout utility
- **JBCheckBox, JBTextField**: Standard IntelliJ Swing components
- **NotificationGroupManager**: For balloon notifications
- **ToolWindow API**: For custom tool windows (future)

### Code Examples

**Settings Configurable Registration** (plugin.xml):
```xml
<extensions defaultExtensionNs="com.intellij">
  <projectConfigurable
    parentId="build"
    instance="com.hellblazer.primemover.intellij.ui.PrimeMoverSettingsConfigurable"
    id="com.hellblazer.primemover.settings"
    displayName="Prime Mover"
    nonDefaultProject="true"/>
</extensions>
```

**Notification Group Registration** (plugin.xml):
```xml
<extensions defaultExtensionNs="com.intellij">
  <notificationGroup
    id="Prime Mover"
    displayType="BALLOON"
    toolWindowId="Build"/>
</extensions>
```

**Accessible Component Example**:
```java
var checkbox = new JBCheckBox("Enable bytecode transformation");
checkbox.setMnemonic('E');
checkbox.getAccessibleContext().setAccessibleName("Enable transformation");
checkbox.getAccessibleContext().setAccessibleDescription(
    "When checked, classes annotated with @Entity are transformed " +
    "after compilation to support discrete event simulation"
);
```

### Testing Considerations

The UX should be tested for:
1. **Keyboard-only navigation**: All features accessible without mouse
2. **Screen reader compatibility**: Test with NVDA, JAWS, VoiceOver
3. **Theme compatibility**: Light, Dark, High Contrast themes
4. **Font scaling**: Test at 100%, 125%, 150%, 200% scale
5. **Localization readiness**: Text extracted for i18n (future)

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-01-19 | Prime Mover Team | Initial specification |

---

## References

- [IntelliJ Platform SDK Documentation](https://plugins.jetbrains.com/docs/intellij/)
- [IntelliJ UI Guidelines](https://jetbrains.design/intellij/)
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [Prime Mover Documentation](https://github.com/Hellblazer/Prime-Mover/wiki)
- [ERROR_SCENARIOS.md](ERROR_SCENARIOS.md) - Error handling specification
- [FALLBACK_STRATEGY.md](FALLBACK_STRATEGY.md) - Fallback behavior specification
