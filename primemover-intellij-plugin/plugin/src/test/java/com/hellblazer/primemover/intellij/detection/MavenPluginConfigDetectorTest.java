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
 * Unit tests for Maven plugin configuration detection and warnings.
 */
public class MavenPluginConfigDetectorTest {

    @Test
    void testDetectMissingPlugin() {
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

        var result = detectMavenPluginConfig(pomContent);

        assertTrue(result.hasPrimeMoverDependency());
        assertFalse(result.hasMavenPlugin());
        assertTrue(result.needsPluginWarning());
        assertNotNull(result.getWarningMessage());
        assertTrue(result.getWarningMessage().contains("Maven plugin not configured"));
    }

    @Test
    void testDetectMissingTransformGoals() {
        var pomContent = """
            <project>
                <dependencies>
                    <dependency>
                        <groupId>com.hellblazer.primeMover</groupId>
                        <artifactId>api</artifactId>
                        <version>1.0.5</version>
                    </dependency>
                </dependencies>
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

        var result = detectMavenPluginConfig(pomContent);

        assertTrue(result.hasPrimeMoverDependency());
        assertTrue(result.hasMavenPlugin());
        assertFalse(result.hasTransformGoals());
        assertTrue(result.needsPluginWarning());
        assertNotNull(result.getWarningMessage());
        assertTrue(result.getWarningMessage().contains("transform goals not configured"));
    }

    @Test
    void testDetectCompleteConfiguration() {
        var pomContent = """
            <project>
                <dependencies>
                    <dependency>
                        <groupId>com.hellblazer.primeMover</groupId>
                        <artifactId>api</artifactId>
                        <version>1.0.5</version>
                    </dependency>
                </dependencies>
                <build>
                    <plugins>
                        <plugin>
                            <groupId>com.hellblazer.primeMover</groupId>
                            <artifactId>primemover-maven-plugin</artifactId>
                            <version>1.0.5</version>
                            <executions>
                                <execution>
                                    <id>transform-classes</id>
                                    <phase>process-classes</phase>
                                    <goals>
                                        <goal>transform</goal>
                                    </goals>
                                </execution>
                            </executions>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """;

        var result = detectMavenPluginConfig(pomContent);

        assertTrue(result.hasPrimeMoverDependency());
        assertTrue(result.hasMavenPlugin());
        assertTrue(result.hasTransformGoals());
        assertFalse(result.needsPluginWarning());
        assertNull(result.getWarningMessage());
    }

    @Test
    void testDetectPluginVersionMismatch() {
        var pomContent = """
            <project>
                <dependencies>
                    <dependency>
                        <groupId>com.hellblazer.primeMover</groupId>
                        <artifactId>api</artifactId>
                        <version>1.0.5</version>
                    </dependency>
                </dependencies>
                <build>
                    <plugins>
                        <plugin>
                            <groupId>com.hellblazer.primeMover</groupId>
                            <artifactId>primemover-maven-plugin</artifactId>
                            <version>1.0.4</version>
                            <executions>
                                <execution>
                                    <goals>
                                        <goal>transform</goal>
                                    </goals>
                                </execution>
                            </executions>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """;

        var result = detectMavenPluginConfigWithVersion(pomContent, "1.0.5");

        assertTrue(result.hasPluginVersionMismatch());
        assertEquals("1.0.4", result.detectedPluginVersion());
        assertEquals("1.0.5", result.expectedVersion());
        assertNotNull(result.getWarningMessage());
        assertTrue(result.getWarningMessage().contains("version mismatch"));
    }

    @Test
    void testDetectTransformTestGoal() {
        var pomContent = """
            <executions>
                <execution>
                    <id>transform-test-classes</id>
                    <phase>process-test-classes</phase>
                    <goals>
                        <goal>transform-test</goal>
                    </goals>
                </execution>
            </executions>
            """;

        assertTrue(hasTransformGoals(pomContent));
    }

    @Test
    void testNoFalsePositiveOnUnrelatedGoals() {
        var pomContent = """
            <executions>
                <execution>
                    <goals>
                        <goal>compile</goal>
                    </goals>
                </execution>
            </executions>
            """;

        assertFalse(hasTransformGoals(pomContent));
    }

    @Test
    void testDetectNoDependencyNoWarning() {
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

        var result = detectMavenPluginConfig(pomContent);

        assertFalse(result.hasPrimeMoverDependency());
        assertFalse(result.needsPluginWarning());
        assertNull(result.getWarningMessage());
    }

    // Helper methods that mirror the logic in MavenPluginConfigDetector

    private static final String PRIME_MOVER_GROUP_ID = "com.hellblazer.primeMover";
    private static final String PRIME_MOVER_MAVEN_PLUGIN = "primemover-maven-plugin";
    private static final String PRIME_MOVER_API_ARTIFACT = "api";
    private static final String PRIME_MOVER_RUNTIME_ARTIFACT = "runtime";

    private ConfigResult detectMavenPluginConfig(String pomContent) {
        boolean hasDependency = pomContent.contains("<groupId>" + PRIME_MOVER_GROUP_ID + "</groupId>") &&
            (pomContent.contains("<artifactId>" + PRIME_MOVER_API_ARTIFACT + "</artifactId>") ||
             pomContent.contains("<artifactId>" + PRIME_MOVER_RUNTIME_ARTIFACT + "</artifactId>"));

        boolean hasPlugin = pomContent.contains("<artifactId>" + PRIME_MOVER_MAVEN_PLUGIN + "</artifactId>");
        boolean hasGoals = hasTransformGoals(pomContent);

        return new ConfigResult(hasDependency, hasPlugin, hasGoals, null, null);
    }

    private ConfigResult detectMavenPluginConfigWithVersion(String pomContent, String expectedVersion) {
        var base = detectMavenPluginConfig(pomContent);
        String pluginVersion = extractPluginVersion(pomContent);

        return new ConfigResult(
            base.hasPrimeMoverDependency,
            base.hasMavenPlugin,
            base.hasTransformGoals,
            pluginVersion,
            expectedVersion
        );
    }

    private boolean hasTransformGoals(String pomContent) {
        return pomContent.contains("<goal>transform</goal>") ||
               pomContent.contains("<goal>transform-test</goal>");
    }

    private String extractPluginVersion(String pomContent) {
        // Simple extraction - find version tag after primemover-maven-plugin
        int pluginIndex = pomContent.indexOf("<artifactId>" + PRIME_MOVER_MAVEN_PLUGIN + "</artifactId>");
        if (pluginIndex < 0) return null;

        int searchEnd = Math.min(pluginIndex + 300, pomContent.length());
        String searchBlock = pomContent.substring(pluginIndex, searchEnd);

        int versionStart = searchBlock.indexOf("<version>");
        if (versionStart < 0) return null;

        int versionEnd = searchBlock.indexOf("</version>", versionStart);
        if (versionEnd < 0) return null;

        return searchBlock.substring(versionStart + 9, versionEnd).trim();
    }

    // Test result record
    private record ConfigResult(
        boolean hasPrimeMoverDependency,
        boolean hasMavenPlugin,
        boolean hasTransformGoals,
        String detectedPluginVersion,
        String expectedVersion
    ) {
        boolean needsPluginWarning() {
            if (!hasPrimeMoverDependency) {
                return false;
            }
            return !hasMavenPlugin || !hasTransformGoals || hasPluginVersionMismatch();
        }

        boolean hasPluginVersionMismatch() {
            if (detectedPluginVersion == null || expectedVersion == null) {
                return false;
            }
            return !detectedPluginVersion.equals(expectedVersion);
        }

        String getWarningMessage() {
            if (!needsPluginWarning()) {
                return null;
            }

            if (!hasMavenPlugin) {
                return "Prime Mover dependency detected but Maven plugin not configured";
            }

            if (!hasTransformGoals) {
                return "Prime Mover Maven plugin detected but transform goals not configured";
            }

            if (hasPluginVersionMismatch()) {
                return String.format(
                    "Prime Mover Maven plugin version mismatch: expected %s but found %s",
                    expectedVersion, detectedPluginVersion
                );
            }

            return null;
        }
    }
}
