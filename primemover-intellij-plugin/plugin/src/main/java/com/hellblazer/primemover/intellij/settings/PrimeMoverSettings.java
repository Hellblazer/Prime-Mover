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
package com.hellblazer.primemover.intellij.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Project-level settings for Prime Mover plugin.
 * Persisted in .idea/primemover.xml
 */
@Service(Service.Level.PROJECT)
@State(
    name = "PrimeMoverSettings",
    storages = @Storage("primemover.xml")
)
public final class PrimeMoverSettings implements PersistentStateComponent<PrimeMoverSettings.State> {

    private State state = new State();

    public static PrimeMoverSettings getInstance(@NotNull Project project) {
        return project.getService(PrimeMoverSettings.class);
    }

    @Override
    public @Nullable State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    // Accessor methods for settings

    public boolean isEnabled() {
        return state.enabled;
    }

    public void setEnabled(boolean enabled) {
        state.enabled = enabled;
    }

    public boolean isShowNotifications() {
        return state.showNotifications;
    }

    public void setShowNotifications(boolean show) {
        state.showNotifications = show;
    }

    public boolean isWarnOnMavenPluginPresent() {
        return state.warnOnMavenPluginPresent;
    }

    public void setWarnOnMavenPluginPresent(boolean warn) {
        state.warnOnMavenPluginPresent = warn;
    }

    public boolean isAutoAddAgent() {
        return state.autoAddAgent;
    }

    public void setAutoAddAgent(boolean auto) {
        state.autoAddAgent = auto;
    }

    /**
     * Settings state class for XML serialization.
     */
    public static class State {
        /** Whether Prime Mover transformation is enabled for this project */
        public boolean enabled = true;

        /** Whether to show notifications on project open */
        public boolean showNotifications = true;

        /** Whether to warn when Maven plugin is also present (duplicate transformation) */
        public boolean warnOnMavenPluginPresent = true;

        /** Whether to automatically add -javaagent to run configurations */
        public boolean autoAddAgent = true;
    }
}
