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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Detects Prime Mover framework version in classpath and compares with IDE plugin version.
 * Scans for version information in:
 * - META-INF/maven/com.hellblazer.primeMover/{artifact}/pom.properties
 * - META-INF/MANIFEST.MF (Implementation-Version)
 *
 * Used to warn users when IDE plugin version does not match framework version.
 */
public final class PrimeMoverVersionDetector {

    private static final String PRIME_MOVER_GROUP_ID = "com.hellblazer.primeMover";
    private static final String[] KNOWN_ARTIFACTS = {"api", "runtime", "sim-agent", "transform"};
    private static final String POM_PROPERTIES_PATH = "META-INF/maven/com.hellblazer.primeMover";
    private static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";

    /**
     * Result of version detection.
     */
    public record VersionResult(
        boolean found,
        @Nullable String classpathVersion,
        @Nullable String pluginVersion
    ) {
        public static VersionResult notFound() {
            return new VersionResult(false, null, null);
        }

        public static VersionResult found(String classpathVersion, String pluginVersion) {
            return new VersionResult(true, classpathVersion, pluginVersion);
        }

        public boolean hasMismatch() {
            if (!found || classpathVersion == null || pluginVersion == null) {
                return false;
            }
            return !classpathVersion.equals(pluginVersion);
        }

        public String getMismatchMessage() {
            if (!hasMismatch()) {
                return null;
            }
            return String.format(
                "Prime Mover version mismatch: IDE plugin is %s but classpath has %s",
                pluginVersion, classpathVersion
            );
        }
    }

    /**
     * Detect Prime Mover version in project classpath.
     * @param project the IntelliJ project
     * @param pluginVersion the IDE plugin version to compare against
     */
    public static VersionResult detect(@NotNull Project project, @NotNull String pluginVersion) {
        var roots = ProjectRootManager.getInstance(project)
            .orderEntries()
            .librariesOnly()
            .classes()
            .getRoots();

        return detectInRoots(roots, pluginVersion);
    }

    /**
     * Detect Prime Mover version in module classpath.
     * @param module the IntelliJ module
     * @param pluginVersion the IDE plugin version to compare against
     */
    public static VersionResult detect(@NotNull Module module, @NotNull String pluginVersion) {
        var roots = OrderEnumerator.orderEntries(module)
            .librariesOnly()
            .classes()
            .getRoots();

        return detectInRoots(roots, pluginVersion);
    }

    /**
     * Search through library roots to find Prime Mover version.
     * Package-private for testing.
     */
    static VersionResult detectInRoots(VirtualFile[] roots, String pluginVersion) {
        for (var root : roots) {
            // Try to find pom.properties in Prime Mover JARs
            var version = findVersionInJar(root);
            if (version != null) {
                return VersionResult.found(version, pluginVersion);
            }
        }

        return VersionResult.notFound();
    }

    /**
     * Find version in a JAR root by checking pom.properties or MANIFEST.MF.
     * Package-private for testing.
     */
    @Nullable
    static String findVersionInJar(VirtualFile jarRoot) {
        // First try pom.properties (most reliable)
        for (var artifact : KNOWN_ARTIFACTS) {
            var pomPropertiesPath = POM_PROPERTIES_PATH + "/" + artifact + "/pom.properties";
            var pomProperties = jarRoot.findFileByRelativePath(pomPropertiesPath);
            if (pomProperties != null && pomProperties.exists()) {
                var version = parseVersionFromPomProperties(pomProperties);
                if (version != null) {
                    return version;
                }
            }
        }

        // Fall back to MANIFEST.MF
        var manifest = jarRoot.findFileByRelativePath(MANIFEST_PATH);
        if (manifest != null && manifest.exists()) {
            return parseVersionFromManifest(manifest);
        }

        return null;
    }

    /**
     * Parse version from pom.properties file.
     * Package-private for testing.
     */
    @Nullable
    static String parseVersionFromPomProperties(@NotNull VirtualFile pomProperties) {
        try {
            var content = new String(pomProperties.contentsToByteArray(), StandardCharsets.UTF_8);
            return parseVersionFromPomPropertiesContent(content);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Parse version from pom.properties content.
     * Package-private for testing.
     */
    @Nullable
    static String parseVersionFromPomPropertiesContent(String content) {
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

    /**
     * Parse version from MANIFEST.MF file.
     * Package-private for testing.
     */
    @Nullable
    static String parseVersionFromManifest(@NotNull VirtualFile manifest) {
        try {
            var content = new String(manifest.contentsToByteArray(), StandardCharsets.UTF_8);
            return parseVersionFromManifestContent(content);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Parse version from MANIFEST.MF content.
     * Package-private for testing.
     */
    @Nullable
    static String parseVersionFromManifestContent(String content) {
        for (var line : content.split("\n")) {
            var trimmed = line.trim();
            if (trimmed.startsWith("Implementation-Version:")) {
                return trimmed.substring("Implementation-Version:".length()).trim();
            }
        }
        return null;
    }

    /**
     * Check if groupId and artifactId represent a Prime Mover artifact.
     * Package-private for testing.
     */
    static boolean isPrimeMoverArtifact(String groupId, String artifactId) {
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
}
