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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Detects the sim-agent.jar in project dependencies.
 * The sim-agent provides runtime bytecode transformation for @Entity classes
 * when build-time transformation hasn't been applied.
 */
public final class SimAgentDetector {

    private static final String SIM_AGENT_GROUP_ID = "com.hellblazer.primeMover";
    private static final String SIM_AGENT_ARTIFACT_ID = "sim-agent";
    private static final String SIM_AGENT_JAR_PATTERN = "sim-agent";

    /**
     * Result of sim-agent detection.
     */
    public record DetectionResult(
        boolean found,
        @Nullable String jarPath,
        @Nullable String version
    ) {
        public static DetectionResult notFound() {
            return new DetectionResult(false, null, null);
        }

        public static DetectionResult found(String jarPath, String version) {
            return new DetectionResult(true, jarPath, version);
        }
    }

    /**
     * Detect sim-agent.jar in the given project's dependencies.
     */
    public static DetectionResult detect(@NotNull Project project) {
        var roots = ProjectRootManager.getInstance(project)
            .orderEntries()
            .librariesOnly()
            .classes()
            .getRoots();

        return findSimAgentInRoots(roots);
    }

    /**
     * Detect sim-agent.jar in the given module's dependencies.
     */
    public static DetectionResult detect(@NotNull Module module) {
        var roots = OrderEnumerator.orderEntries(module)
            .librariesOnly()
            .classes()
            .getRoots();

        return findSimAgentInRoots(roots);
    }

    /**
     * Search through library roots to find sim-agent JAR.
     */
    private static DetectionResult findSimAgentInRoots(VirtualFile[] roots) {
        for (var root : roots) {
            var path = root.getPath();

            // Check if this is a sim-agent JAR
            // Paths look like: ~/.m2/repository/com/hellblazer/primeMover/sim-agent/1.0.5/sim-agent-1.0.5.jar
            // Or in Gradle cache: ~/.gradle/caches/.../sim-agent-1.0.5.jar
            if (isSimAgentJar(path)) {
                var version = extractVersion(path);
                // VirtualFile paths for JARs end with !/ - strip it
                var cleanPath = path.endsWith("!/") ? path.substring(0, path.length() - 2) : path;
                return DetectionResult.found(cleanPath, version);
            }
        }

        return DetectionResult.notFound();
    }

    /**
     * Check if a path represents a sim-agent JAR.
     * Package-private for testing.
     */
    static boolean isSimAgentJar(String path) {
        // Normalize path for comparison
        var normalizedPath = path.replace('\\', '/').toLowerCase();

        // Check for Maven repository path pattern
        // com/hellblazer/primeMover/sim-agent/VERSION/sim-agent-VERSION.jar
        if (normalizedPath.contains("/com/hellblazer/primemover/sim-agent/") ||
            normalizedPath.contains("/com/hellblazer/primemover/sim-agent/")) {
            return true;
        }

        // Check for JAR filename pattern (works for Gradle cache)
        var fileName = new File(path).getName().toLowerCase();
        return fileName.startsWith("sim-agent-") && fileName.endsWith(".jar");
    }

    /**
     * Extract version from sim-agent JAR path.
     * Package-private for testing.
     * Examples:
     * - sim-agent-1.0.5.jar -> 1.0.5
     * - sim-agent-1.0.5-SNAPSHOT.jar -> 1.0.5-SNAPSHOT
     */
    @Nullable
    static String extractVersion(String path) {
        var fileName = new File(path.replace("!/", "")).getName();

        // sim-agent-VERSION.jar
        if (fileName.startsWith("sim-agent-") && fileName.endsWith(".jar")) {
            return fileName.substring("sim-agent-".length(), fileName.length() - ".jar".length());
        }

        return null;
    }

    /**
     * Construct the -javaagent argument for the given JAR path.
     */
    public static String buildJavaAgentArg(String jarPath) {
        // Handle paths with spaces by quoting
        if (jarPath.contains(" ")) {
            return "-javaagent:\"" + jarPath + "\"";
        }
        return "-javaagent:" + jarPath;
    }
}
