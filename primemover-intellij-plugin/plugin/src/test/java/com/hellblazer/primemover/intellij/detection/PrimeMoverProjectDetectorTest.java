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
 * Unit tests for Prime Mover project detection logic.
 * Tests the string parsing logic without requiring full IntelliJ environment.
 */
public class PrimeMoverProjectDetectorTest {

    @Test
    void testDetectPrimeMoverDependencyInPom() {
        var pomContent = """
            <project>
                <dependencies>
                    <dependency>
                        <groupId>com.hellblazer.primeMover</groupId>
                        <artifactId>api</artifactId>
                        <version>1.0.5</version>
                    </dependency>
                </dependencies>
            </project>
            """;

        assertTrue(containsPrimeMoverDependency(pomContent),
            "Should detect Prime Mover API dependency");
    }

    @Test
    void testDetectPrimeMoverRuntimeDependency() {
        var pomContent = """
            <project>
                <dependencies>
                    <dependency>
                        <groupId>com.hellblazer.primeMover</groupId>
                        <artifactId>runtime</artifactId>
                        <version>1.0.5</version>
                    </dependency>
                </dependencies>
            </project>
            """;

        assertTrue(containsPrimeMoverDependency(pomContent),
            "Should detect Prime Mover runtime dependency");
    }

    @Test
    void testDetectMavenPlugin() {
        var pomContent = """
            <project>
                <build>
                    <plugins>
                        <plugin>
                            <groupId>com.hellblazer.primeMover</groupId>
                            <artifactId>primemover-maven-plugin</artifactId>
                            <version>1.0.5</version>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """;

        assertTrue(containsMavenPlugin(pomContent),
            "Should detect Prime Mover Maven plugin");
    }

    @Test
    void testNoFalsePositiveOnUnrelatedDependency() {
        var pomContent = """
            <project>
                <dependencies>
                    <dependency>
                        <groupId>org.junit.jupiter</groupId>
                        <artifactId>junit-jupiter</artifactId>
                        <version>5.11.0</version>
                    </dependency>
                </dependencies>
            </project>
            """;

        assertFalse(containsPrimeMoverDependency(pomContent),
            "Should not detect Prime Mover in unrelated pom");
        assertFalse(containsMavenPlugin(pomContent),
            "Should not detect Maven plugin in unrelated pom");
    }

    @Test
    void testExtractVersion() {
        var pomContent = """
            <dependency>
                <groupId>com.hellblazer.primeMover</groupId>
                <artifactId>api</artifactId>
                <version>1.0.5-SNAPSHOT</version>
            </dependency>
            """;

        assertEquals("1.0.5-SNAPSHOT", extractVersion(pomContent),
            "Should extract version from dependency");
    }

    @Test
    void testDetectGradleDependency() {
        var buildGradle = """
            dependencies {
                implementation 'com.hellblazer.primeMover:api:1.0.5'
                implementation 'com.hellblazer.primeMover:runtime:1.0.5'
            }
            """;

        assertTrue(containsGradleDependency(buildGradle),
            "Should detect Prime Mover in Gradle build file");
    }

    @Test
    void testDetectGradleKotlinDependency() {
        var buildGradleKts = """
            dependencies {
                implementation("com.hellblazer.primeMover:api:1.0.5")
            }
            """;

        assertTrue(containsGradleDependency(buildGradleKts),
            "Should detect Prime Mover in Kotlin DSL build file");
    }

    // Helper methods that mirror the logic in PrimeMoverProjectDetector
    // These test the parsing logic directly without needing VirtualFile

    private static final String PRIME_MOVER_GROUP_ID = "com.hellblazer.primeMover";
    private static final String PRIME_MOVER_MAVEN_PLUGIN = "primemover-maven-plugin";
    private static final String PRIME_MOVER_API_ARTIFACT = "api";
    private static final String PRIME_MOVER_RUNTIME_ARTIFACT = "runtime";

    private boolean containsPrimeMoverDependency(String pomContent) {
        return pomContent.contains("<groupId>" + PRIME_MOVER_GROUP_ID + "</groupId>") &&
            (pomContent.contains("<artifactId>" + PRIME_MOVER_API_ARTIFACT + "</artifactId>") ||
             pomContent.contains("<artifactId>" + PRIME_MOVER_RUNTIME_ARTIFACT + "</artifactId>"));
    }

    private boolean containsMavenPlugin(String pomContent) {
        return pomContent.contains("<groupId>" + PRIME_MOVER_GROUP_ID + "</groupId>") &&
               pomContent.contains("<artifactId>" + PRIME_MOVER_MAVEN_PLUGIN + "</artifactId>");
    }

    private boolean containsGradleDependency(String buildContent) {
        return buildContent.contains(PRIME_MOVER_GROUP_ID) &&
            (buildContent.contains(PRIME_MOVER_API_ARTIFACT) || buildContent.contains(PRIME_MOVER_RUNTIME_ARTIFACT));
    }

    private String extractVersion(String pomContent) {
        int groupIdIndex = pomContent.indexOf("<groupId>" + PRIME_MOVER_GROUP_ID + "</groupId>");
        if (groupIdIndex < 0) return null;

        int searchEnd = Math.min(groupIdIndex + 500, pomContent.length());
        String searchBlock = pomContent.substring(groupIdIndex, searchEnd);

        int versionStart = searchBlock.indexOf("<version>");
        if (versionStart < 0) return null;

        int versionEnd = searchBlock.indexOf("</version>", versionStart);
        if (versionEnd < 0) return null;

        return searchBlock.substring(versionStart + 9, versionEnd).trim();
    }
}
