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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Detects Maven plugin configuration issues and provides warnings.
 * Checks for:
 * - Missing Maven plugin when Prime Mover dependencies exist
 * - Missing transform goals in plugin configuration
 * - Plugin version mismatches
 * - Classpath issues
 */
public final class MavenPluginConfigDetector {

    private static final String PRIME_MOVER_GROUP_ID = "com.hellblazer.primeMover";
    private static final String PRIME_MOVER_MAVEN_PLUGIN = "primemover-maven-plugin";
    private static final String PRIME_MOVER_API_ARTIFACT = "api";
    private static final String PRIME_MOVER_RUNTIME_ARTIFACT = "runtime";

    /**
     * Result of Maven plugin configuration detection.
     */
    public record ConfigResult(
        boolean hasPrimeMoverDependency,
        boolean hasMavenPlugin,
        boolean hasTransformGoals,
        @Nullable String detectedPluginVersion,
        @Nullable String expectedVersion
    ) {
        public static ConfigResult notApplicable() {
            return new ConfigResult(false, false, false, null, null);
        }

        public static ConfigResult create(
            boolean hasDependency,
            boolean hasPlugin,
            boolean hasGoals,
            String pluginVersion,
            String expectedVersion
        ) {
            return new ConfigResult(hasDependency, hasPlugin, hasGoals, pluginVersion, expectedVersion);
        }

        /**
         * Whether this configuration needs a warning to the user.
         */
        public boolean needsPluginWarning() {
            // Only warn if Prime Mover is actually used
            if (!hasPrimeMoverDependency) {
                return false;
            }

            // Warn if plugin missing, goals missing, or version mismatch
            return !hasMavenPlugin || !hasTransformGoals || hasPluginVersionMismatch();
        }

        /**
         * Whether the plugin version mismatches the expected version.
         */
        public boolean hasPluginVersionMismatch() {
            if (detectedPluginVersion == null || expectedVersion == null) {
                return false;
            }
            return !detectedPluginVersion.equals(expectedVersion);
        }

        /**
         * Get a user-facing warning message, or null if no warning needed.
         */
        @Nullable
        public String getWarningMessage() {
            if (!needsPluginWarning()) {
                return null;
            }

            if (!hasMavenPlugin) {
                return "Prime Mover dependency detected but Maven plugin not configured.\n" +
                       "Add primemover-maven-plugin to your build plugins.";
            }

            if (!hasTransformGoals) {
                return "Prime Mover Maven plugin detected but transform goals not configured.\n" +
                       "Add transform and transform-test goals to plugin executions.";
            }

            if (hasPluginVersionMismatch()) {
                return String.format(
                    "Prime Mover Maven plugin version mismatch: expected %s but found %s.\n" +
                    "Update plugin version to match dependency version.",
                    expectedVersion, detectedPluginVersion
                );
            }

            return null;
        }
    }

    /**
     * Detect Maven plugin configuration in the given project.
     * @param project the IntelliJ project
     * @param expectedVersion the expected Prime Mover version (from classpath or IDE plugin)
     */
    public static ConfigResult detect(@NotNull Project project, @Nullable String expectedVersion) {
        var baseDir = project.getBaseDir();
        if (baseDir == null) {
            return ConfigResult.notApplicable();
        }

        // Check Maven pom.xml
        var pomFile = baseDir.findChild("pom.xml");
        if (pomFile != null && pomFile.exists()) {
            return detectFromMaven(pomFile, expectedVersion);
        }

        // Gradle projects don't use Maven plugin, so no warning needed
        return ConfigResult.notApplicable();
    }

    /**
     * Detect configuration from Maven pom.xml file.
     * Package-private for testing.
     */
    static ConfigResult detectFromMaven(@NotNull VirtualFile pomFile, @Nullable String expectedVersion) {
        try {
            var content = new String(pomFile.contentsToByteArray(), StandardCharsets.UTF_8);
            return detectFromMavenContent(content, expectedVersion);
        } catch (IOException e) {
            return ConfigResult.notApplicable();
        }
    }

    /**
     * Detect configuration from Maven pom.xml content.
     * Package-private for testing.
     */
    static ConfigResult detectFromMavenContent(String pomContent, @Nullable String expectedVersion) {
        boolean hasDependency = containsPrimeMoverDependency(pomContent);
        boolean hasPlugin = containsMavenPlugin(pomContent);
        boolean hasGoals = containsTransformGoals(pomContent);
        String pluginVersion = extractPluginVersion(pomContent);

        return ConfigResult.create(hasDependency, hasPlugin, hasGoals, pluginVersion, expectedVersion);
    }

    /**
     * Check if pom.xml contains Prime Mover dependencies.
     * Package-private for testing.
     */
    static boolean containsPrimeMoverDependency(String pomContent) {
        return pomContent.contains("<groupId>" + PRIME_MOVER_GROUP_ID + "</groupId>") &&
            (pomContent.contains("<artifactId>" + PRIME_MOVER_API_ARTIFACT + "</artifactId>") ||
             pomContent.contains("<artifactId>" + PRIME_MOVER_RUNTIME_ARTIFACT + "</artifactId>"));
    }

    /**
     * Check if pom.xml contains Prime Mover Maven plugin.
     * Package-private for testing.
     */
    static boolean containsMavenPlugin(String pomContent) {
        return pomContent.contains("<groupId>" + PRIME_MOVER_GROUP_ID + "</groupId>") &&
               pomContent.contains("<artifactId>" + PRIME_MOVER_MAVEN_PLUGIN + "</artifactId>");
    }

    /**
     * Check if pom.xml contains transform goals (transform or transform-test).
     * Package-private for testing.
     */
    static boolean containsTransformGoals(String pomContent) {
        return pomContent.contains("<goal>transform</goal>") ||
               pomContent.contains("<goal>transform-test</goal>");
    }

    /**
     * Extract Maven plugin version from pom.xml content.
     * Package-private for testing.
     */
    @Nullable
    static String extractPluginVersion(String pomContent) {
        // Find the plugin block
        int pluginIndex = pomContent.indexOf("<artifactId>" + PRIME_MOVER_MAVEN_PLUGIN + "</artifactId>");
        if (pluginIndex < 0) {
            return null;
        }

        // Search for version tag within plugin block (typically within 300 chars)
        int searchEnd = Math.min(pluginIndex + 300, pomContent.length());
        String searchBlock = pomContent.substring(pluginIndex, searchEnd);

        int versionStart = searchBlock.indexOf("<version>");
        if (versionStart < 0) {
            return null;
        }

        int versionEnd = searchBlock.indexOf("</version>", versionStart);
        if (versionEnd < 0) {
            return null;
        }

        return searchBlock.substring(versionStart + 9, versionEnd).trim();
    }
}
