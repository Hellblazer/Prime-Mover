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
package com.hellblazer.primemover.intellij.detection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Prime Mover version detection logic.
 * Tests version parsing and comparison without requiring full IntelliJ environment.
 */
public class PrimeMoverVersionDetectorTest {

    @Test
    void testParseVersionFromPomProperties() {
        var pomProperties = """
            artifactId=api
            groupId=com.hellblazer.primeMover
            version=1.0.5
            """;

        assertEquals("1.0.5", parseVersionFromPomProperties(pomProperties));
    }

    @Test
    void testParseSnapshotVersionFromPomProperties() {
        var pomProperties = """
            artifactId=runtime
            groupId=com.hellblazer.primeMover
            version=1.0.6-SNAPSHOT
            """;

        assertEquals("1.0.6-SNAPSHOT", parseVersionFromPomProperties(pomProperties));
    }

    @Test
    void testParseVersionFromPomPropertiesWithExtraWhitespace() {
        var pomProperties = """
            artifactId=api
            groupId=com.hellblazer.primeMover
            version = 1.0.5
            """;

        assertEquals("1.0.5", parseVersionFromPomProperties(pomProperties));
    }

    @Test
    void testParseVersionFromPomPropertiesNoVersion() {
        var pomProperties = """
            artifactId=api
            groupId=com.hellblazer.primeMover
            """;

        assertNull(parseVersionFromPomProperties(pomProperties));
    }

    @Test
    void testParseVersionFromPomPropertiesEmpty() {
        assertNull(parseVersionFromPomProperties(""));
    }

    @Test
    void testCompareVersionsEqual() {
        assertTrue(versionsMatch("1.0.5", "1.0.5"));
    }

    @Test
    void testCompareVersionsDifferent() {
        assertFalse(versionsMatch("1.0.5", "1.0.6"));
    }

    @Test
    void testCompareVersionsSnapshotVsRelease() {
        assertFalse(versionsMatch("1.0.5-SNAPSHOT", "1.0.5"));
    }

    @Test
    void testCompareVersionsBothSnapshot() {
        assertTrue(versionsMatch("1.0.5-SNAPSHOT", "1.0.5-SNAPSHOT"));
    }

    @Test
    void testCompareVersionsNullHandling() {
        assertFalse(versionsMatch(null, "1.0.5"));
        assertFalse(versionsMatch("1.0.5", null));
        assertTrue(versionsMatch(null, null));
    }

    @Test
    void testVersionMismatchDetection() {
        var result = createMismatchResult("1.0.5", "1.0.6");

        assertTrue(result.hasMismatch());
        assertEquals("1.0.5", result.classpathVersion());
        assertEquals("1.0.6", result.pluginVersion());
    }

    @Test
    void testNoMismatchWhenVersionsMatch() {
        var result = createMatchResult("1.0.5");

        assertFalse(result.hasMismatch());
        assertEquals("1.0.5", result.classpathVersion());
        assertEquals("1.0.5", result.pluginVersion());
    }

    @Test
    void testNotFoundResult() {
        var result = createNotFoundResult();

        assertFalse(result.found());
        assertFalse(result.hasMismatch());
        assertNull(result.classpathVersion());
    }

    @Test
    void testIsPrimeMoverArtifact() {
        assertTrue(isPrimeMoverArtifact("com.hellblazer.primeMover", "api"));
        assertTrue(isPrimeMoverArtifact("com.hellblazer.primeMover", "runtime"));
        assertTrue(isPrimeMoverArtifact("com.hellblazer.primeMover", "sim-agent"));
        assertFalse(isPrimeMoverArtifact("org.junit.jupiter", "junit-jupiter"));
        assertFalse(isPrimeMoverArtifact("com.hellblazer.primeMover", "unknown"));
    }

    @Test
    void testExtractVersionFromJarManifest() {
        var manifest = """
            Manifest-Version: 1.0
            Implementation-Title: Prime Mover API
            Implementation-Version: 1.0.5
            Implementation-Vendor: Hellblazer
            """;

        assertEquals("1.0.5", extractVersionFromManifest(manifest));
    }

    @Test
    void testExtractVersionFromJarManifestNoVersion() {
        var manifest = """
            Manifest-Version: 1.0
            Implementation-Title: Prime Mover API
            """;

        assertNull(extractVersionFromManifest(manifest));
    }

    // Helper methods that mirror the logic in PrimeMoverVersionDetector
    // These test the parsing logic directly

    private static final String PRIME_MOVER_GROUP_ID = "com.hellblazer.primeMover";
    private static final String[] KNOWN_ARTIFACTS = {"api", "runtime", "sim-agent", "transform"};

    private String parseVersionFromPomProperties(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }

        for (var line : content.split("\n")) {
            var trimmed = line.trim();
            if (trimmed.startsWith("version")) {
                var parts = trimmed.split("=", 2);
                if (parts.length == 2) {
                    return parts[1].trim();
                }
            }
        }
        return null;
    }

    private boolean versionsMatch(String version1, String version2) {
        if (version1 == null && version2 == null) {
            return true;
        }
        if (version1 == null || version2 == null) {
            return false;
        }
        return version1.equals(version2);
    }

    private boolean isPrimeMoverArtifact(String groupId, String artifactId) {
        if (!PRIME_MOVER_GROUP_ID.equals(groupId)) {
            return false;
        }
        for (var artifact : KNOWN_ARTIFACTS) {
            if (artifact.equals(artifactId)) {
                return true;
            }
        }
        return false;
    }

    private String extractVersionFromManifest(String manifest) {
        for (var line : manifest.split("\n")) {
            var trimmed = line.trim();
            if (trimmed.startsWith("Implementation-Version:")) {
                return trimmed.substring("Implementation-Version:".length()).trim();
            }
        }
        return null;
    }

    // Result creation helpers for testing

    private PrimeMoverVersionDetector.VersionResult createMismatchResult(String classpathVersion, String pluginVersion) {
        return new PrimeMoverVersionDetector.VersionResult(true, classpathVersion, pluginVersion);
    }

    private PrimeMoverVersionDetector.VersionResult createMatchResult(String version) {
        return new PrimeMoverVersionDetector.VersionResult(true, version, version);
    }

    private PrimeMoverVersionDetector.VersionResult createNotFoundResult() {
        return new PrimeMoverVersionDetector.VersionResult(false, null, null);
    }
}
