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

import com.hellblazer.primemover.intellij.detection.PrimeMoverProjectDetector;
import com.hellblazer.primemover.intellij.settings.PrimeMoverSettings;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.runners.JavaProgramPatcher;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Automatically adds -javaagent:sim-agent.jar to Java run configurations
 * for Prime Mover projects when enabled in settings.
 *
 * The sim-agent provides runtime bytecode transformation for @Entity classes,
 * useful for:
 * - Hot-reload scenarios (classes compiled outside IDE)
 * - Fallback when JPS transformation fails
 * - Debugging with runtime transformation
 * - Hybrid builds mixing Maven and IDE compilation
 */
public final class PrimeMoverJavaProgramPatcher extends JavaProgramPatcher {

    private static final Logger LOG = Logger.getInstance(PrimeMoverJavaProgramPatcher.class);
    private static final String JAVAAGENT_PREFIX = "-javaagent:";

    @Override
    public void patchJavaParameters(@NotNull Executor executor,
                                    @NotNull RunProfile configuration,
                                    @NotNull JavaParameters javaParameters) {
        // Get project from configuration
        var project = getProject(configuration);
        if (project == null) {
            return;
        }

        // Check if auto-agent is enabled in settings
        var settings = PrimeMoverSettings.getInstance(project);
        if (!settings.isEnabled() || !settings.isAutoAddAgent()) {
            return;
        }

        // Check if this is a Prime Mover project
        var detection = PrimeMoverProjectDetector.detect(project);
        if (!detection.isPrimeMoverProject()) {
            return;
        }

        // Check if javaagent is already configured
        if (hasJavaAgentConfigured(javaParameters)) {
            LOG.debug("Prime Mover: javaagent already configured, skipping");
            return;
        }

        // Try to find sim-agent.jar in module dependencies first, then project
        var module = getModule(configuration);
        var agentDetection = module != null
            ? SimAgentDetector.detect(module)
            : SimAgentDetector.detect(project);

        if (!agentDetection.found()) {
            LOG.debug("Prime Mover: sim-agent.jar not found in dependencies");
            return;
        }

        // Add -javaagent to VM parameters
        var agentArg = SimAgentDetector.buildJavaAgentArg(agentDetection.jarPath());
        javaParameters.getVMParametersList().add(agentArg);

        LOG.info("Prime Mover: Added " + agentArg + " to run configuration");
    }

    /**
     * Check if a -javaagent for sim-agent is already configured.
     */
    private boolean hasJavaAgentConfigured(JavaParameters javaParameters) {
        var vmParams = javaParameters.getVMParametersList().getParameters();
        for (var param : vmParams) {
            if (param.startsWith(JAVAAGENT_PREFIX) && param.contains("sim-agent")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extract Project from RunProfile.
     */
    private Project getProject(RunProfile configuration) {
        if (configuration instanceof ModuleBasedConfiguration<?, ?> moduleConfig) {
            var module = moduleConfig.getConfigurationModule().getModule();
            if (module != null) {
                return module.getProject();
            }
        }
        return null;
    }

    /**
     * Extract Module from RunProfile.
     */
    private Module getModule(RunProfile configuration) {
        if (configuration instanceof ModuleBasedConfiguration<?, ?> moduleConfig) {
            return moduleConfig.getConfigurationModule().getModule();
        }
        return null;
    }
}
