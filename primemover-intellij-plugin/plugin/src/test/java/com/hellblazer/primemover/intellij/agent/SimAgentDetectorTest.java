/*
 * Copyright (C) 2026 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover IntelliJ Plugin.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 */
package com.hellblazer.primemover.intellij.agent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SimAgentDetector path parsing logic.
 */
public class SimAgentDetectorTest {

    @Test
    void testIsSimAgentJarMavenPath() {
        // Maven repository path pattern
        var mavenPath = "/Users/test/.m2/repository/com/hellblazer/primeMover/sim-agent/1.0.5/sim-agent-1.0.5.jar";
        assertTrue(SimAgentDetector.isSimAgentJar(mavenPath),
            "Should detect sim-agent in Maven repository path");
    }

    @Test
    void testIsSimAgentJarGradlePath() {
        // Gradle cache path pattern
        var gradlePath = "/Users/test/.gradle/caches/modules-2/files-2.1/com.hellblazer.primeMover/sim-agent/1.0.5/abc123/sim-agent-1.0.5.jar";
        assertTrue(SimAgentDetector.isSimAgentJar(gradlePath),
            "Should detect sim-agent in Gradle cache path");
    }

    @Test
    void testIsSimAgentJarSnapshotVersion() {
        var snapshotPath = "/Users/test/.m2/repository/com/hellblazer/primeMover/sim-agent/1.0.5-SNAPSHOT/sim-agent-1.0.5-SNAPSHOT.jar";
        assertTrue(SimAgentDetector.isSimAgentJar(snapshotPath),
            "Should detect sim-agent SNAPSHOT version");
    }

    @Test
    void testIsSimAgentJarWindowsPath() {
        var windowsPath = "C:\\Users\\test\\.m2\\repository\\com\\hellblazer\\primeMover\\sim-agent\\1.0.5\\sim-agent-1.0.5.jar";
        assertTrue(SimAgentDetector.isSimAgentJar(windowsPath),
            "Should detect sim-agent on Windows path");
    }

    @Test
    void testIsSimAgentJarWithBangSuffix() {
        // VirtualFile paths for JARs often end with !/
        var jarPath = "/Users/test/.m2/repository/com/hellblazer/primeMover/sim-agent/1.0.5/sim-agent-1.0.5.jar!/";
        assertTrue(SimAgentDetector.isSimAgentJar(jarPath),
            "Should detect sim-agent with !/ suffix");
    }

    @Test
    void testIsNotSimAgentJarOtherDependency() {
        var otherPath = "/Users/test/.m2/repository/org/junit/jupiter/junit-jupiter/5.11.0/junit-jupiter-5.11.0.jar";
        assertFalse(SimAgentDetector.isSimAgentJar(otherPath),
            "Should not match other dependencies");
    }

    @Test
    void testIsNotSimAgentJarApiArtifact() {
        // Should not match the api artifact, only sim-agent
        var apiPath = "/Users/test/.m2/repository/com/hellblazer/primeMover/api/1.0.5/api-1.0.5.jar";
        assertFalse(SimAgentDetector.isSimAgentJar(apiPath),
            "Should not match api artifact");
    }

    @Test
    void testExtractVersionRelease() {
        var path = "/path/to/sim-agent-1.0.5.jar";
        assertEquals("1.0.5", SimAgentDetector.extractVersion(path),
            "Should extract release version");
    }

    @Test
    void testExtractVersionSnapshot() {
        var path = "/path/to/sim-agent-1.0.5-SNAPSHOT.jar";
        assertEquals("1.0.5-SNAPSHOT", SimAgentDetector.extractVersion(path),
            "Should extract SNAPSHOT version");
    }

    @Test
    void testExtractVersionWithBangSuffix() {
        var path = "/path/to/sim-agent-2.0.0.jar!/";
        assertEquals("2.0.0", SimAgentDetector.extractVersion(path),
            "Should extract version ignoring !/ suffix");
    }

    @Test
    void testExtractVersionNonSimAgent() {
        var path = "/path/to/other-library-1.0.0.jar";
        assertNull(SimAgentDetector.extractVersion(path),
            "Should return null for non-sim-agent JARs");
    }

    @Test
    void testBuildJavaAgentArgSimplePath() {
        var path = "/path/to/sim-agent-1.0.5.jar";
        var result = SimAgentDetector.buildJavaAgentArg(path);
        assertEquals("-javaagent:/path/to/sim-agent-1.0.5.jar", result,
            "Should build simple -javaagent arg");
    }

    @Test
    void testBuildJavaAgentArgPathWithSpaces() {
        var path = "/path/with spaces/sim-agent-1.0.5.jar";
        var result = SimAgentDetector.buildJavaAgentArg(path);
        assertEquals("-javaagent:\"/path/with spaces/sim-agent-1.0.5.jar\"", result,
            "Should quote paths with spaces");
    }

    @Test
    void testDetectionResultNotFound() {
        var result = SimAgentDetector.DetectionResult.notFound();
        assertFalse(result.found());
        assertNull(result.jarPath());
        assertNull(result.version());
    }

    @Test
    void testDetectionResultFound() {
        var result = SimAgentDetector.DetectionResult.found("/path/to/sim-agent.jar", "1.0.5");
        assertTrue(result.found());
        assertEquals("/path/to/sim-agent.jar", result.jarPath());
        assertEquals("1.0.5", result.version());
    }
}
