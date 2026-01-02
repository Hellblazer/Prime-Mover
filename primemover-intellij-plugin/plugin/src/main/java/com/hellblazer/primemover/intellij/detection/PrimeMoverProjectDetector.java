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
 * Detects Prime Mover usage in a project by scanning build files.
 * Checks for:
 * - Prime Mover API/runtime dependencies
 * - Prime Mover Maven plugin configuration
 * - Prime Mover Gradle plugin (future)
 */
public final class PrimeMoverProjectDetector {

    private static final String PRIME_MOVER_GROUP_ID = "com.hellblazer.primeMover";
    private static final String PRIME_MOVER_MAVEN_PLUGIN = "primemover-maven-plugin";
    private static final String PRIME_MOVER_API_ARTIFACT = "api";
    private static final String PRIME_MOVER_RUNTIME_ARTIFACT = "runtime";

    /**
     * Result of project detection scan.
     */
    public record DetectionResult(
        boolean hasPrimeMoverDependency,
        boolean hasMavenPlugin,
        @Nullable String detectedVersion
    ) {
        public boolean isPrimeMoverProject() {
            return hasPrimeMoverDependency;
        }

        public boolean hasDuplicateTransformation() {
            return hasPrimeMoverDependency && hasMavenPlugin;
        }
    }

    /**
     * Detect Prime Mover usage in the given project.
     */
    public static DetectionResult detect(@NotNull Project project) {
        var baseDir = project.getBaseDir();
        if (baseDir == null) {
            return new DetectionResult(false, false, null);
        }

        // Check Maven pom.xml
        var pomFile = baseDir.findChild("pom.xml");
        if (pomFile != null && pomFile.exists()) {
            return detectFromMaven(pomFile);
        }

        // Check Gradle build files
        var buildGradle = baseDir.findChild("build.gradle");
        var buildGradleKts = baseDir.findChild("build.gradle.kts");
        if (buildGradle != null && buildGradle.exists()) {
            return detectFromGradle(buildGradle);
        }
        if (buildGradleKts != null && buildGradleKts.exists()) {
            return detectFromGradle(buildGradleKts);
        }

        return new DetectionResult(false, false, null);
    }

    /**
     * Detect Prime Mover from Maven pom.xml
     */
    private static DetectionResult detectFromMaven(@NotNull VirtualFile pomFile) {
        try {
            var content = new String(pomFile.contentsToByteArray(), StandardCharsets.UTF_8);

            boolean hasDependency = containsPrimeMoverDependency(content);
            boolean hasMavenPlugin = containsMavenPlugin(content);
            String version = extractVersion(content);

            return new DetectionResult(hasDependency, hasMavenPlugin, version);
        } catch (IOException e) {
            return new DetectionResult(false, false, null);
        }
    }

    /**
     * Detect Prime Mover from Gradle build file
     */
    private static DetectionResult detectFromGradle(@NotNull VirtualFile buildFile) {
        try {
            var content = new String(buildFile.contentsToByteArray(), StandardCharsets.UTF_8);

            // Check for Prime Mover dependencies in Gradle format
            boolean hasDependency = content.contains(PRIME_MOVER_GROUP_ID) &&
                (content.contains(PRIME_MOVER_API_ARTIFACT) || content.contains(PRIME_MOVER_RUNTIME_ARTIFACT));

            // Gradle doesn't use Maven plugin, so no duplicate transformation concern
            return new DetectionResult(hasDependency, false, null);
        } catch (IOException e) {
            return new DetectionResult(false, false, null);
        }
    }

    private static boolean containsPrimeMoverDependency(String pomContent) {
        // Check for groupId in dependencies
        return pomContent.contains("<groupId>" + PRIME_MOVER_GROUP_ID + "</groupId>") &&
            (pomContent.contains("<artifactId>" + PRIME_MOVER_API_ARTIFACT + "</artifactId>") ||
             pomContent.contains("<artifactId>" + PRIME_MOVER_RUNTIME_ARTIFACT + "</artifactId>"));
    }

    private static boolean containsMavenPlugin(String pomContent) {
        // Check for Prime Mover Maven plugin in build/plugins section
        return pomContent.contains("<groupId>" + PRIME_MOVER_GROUP_ID + "</groupId>") &&
               pomContent.contains("<artifactId>" + PRIME_MOVER_MAVEN_PLUGIN + "</artifactId>");
    }

    @Nullable
    private static String extractVersion(String pomContent) {
        // Simple version extraction - looks for version tag after Prime Mover groupId
        int groupIdIndex = pomContent.indexOf("<groupId>" + PRIME_MOVER_GROUP_ID + "</groupId>");
        if (groupIdIndex < 0) return null;

        // Look for version tag within next 500 chars (typical dependency block)
        int searchEnd = Math.min(groupIdIndex + 500, pomContent.length());
        String searchBlock = pomContent.substring(groupIdIndex, searchEnd);

        int versionStart = searchBlock.indexOf("<version>");
        if (versionStart < 0) return null;

        int versionEnd = searchBlock.indexOf("</version>", versionStart);
        if (versionEnd < 0) return null;

        return searchBlock.substring(versionStart + 9, versionEnd).trim();
    }
}
