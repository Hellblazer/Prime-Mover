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
package com.hellblazer.primemover.intellij.ui;

import com.hellblazer.primemover.intellij.settings.PrimeMoverSettings;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Settings UI for Prime Mover plugin.
 * Accessible via Settings > Build, Execution, Deployment > Prime Mover
 */
public final class PrimeMoverSettingsConfigurable implements Configurable {

    private final Project project;
    private JBCheckBox enabledCheckbox;
    private JBCheckBox showNotificationsCheckbox;
    private JBCheckBox warnMavenPluginCheckbox;
    private JBCheckBox autoAddAgentCheckbox;

    public PrimeMoverSettingsConfigurable(Project project) {
        this.project = project;
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Prime Mover";
    }

    @Override
    public @Nullable JComponent createComponent() {
        enabledCheckbox = new JBCheckBox("Enable Prime Mover bytecode transformation");
        showNotificationsCheckbox = new JBCheckBox("Show notifications on project open");
        warnMavenPluginCheckbox = new JBCheckBox("Warn when Maven plugin is also configured (duplicate transformation)");
        autoAddAgentCheckbox = new JBCheckBox("Automatically add -javaagent to run configurations");

        return FormBuilder.createFormBuilder()
            .addComponent(enabledCheckbox)
            .addComponent(showNotificationsCheckbox)
            .addComponent(warnMavenPluginCheckbox)
            .addComponent(autoAddAgentCheckbox)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
    }

    @Override
    public boolean isModified() {
        var settings = PrimeMoverSettings.getInstance(project);
        return enabledCheckbox.isSelected() != settings.isEnabled() ||
               showNotificationsCheckbox.isSelected() != settings.isShowNotifications() ||
               warnMavenPluginCheckbox.isSelected() != settings.isWarnOnMavenPluginPresent() ||
               autoAddAgentCheckbox.isSelected() != settings.isAutoAddAgent();
    }

    @Override
    public void apply() {
        var settings = PrimeMoverSettings.getInstance(project);
        settings.setEnabled(enabledCheckbox.isSelected());
        settings.setShowNotifications(showNotificationsCheckbox.isSelected());
        settings.setWarnOnMavenPluginPresent(warnMavenPluginCheckbox.isSelected());
        settings.setAutoAddAgent(autoAddAgentCheckbox.isSelected());
    }

    @Override
    public void reset() {
        var settings = PrimeMoverSettings.getInstance(project);
        enabledCheckbox.setSelected(settings.isEnabled());
        showNotificationsCheckbox.setSelected(settings.isShowNotifications());
        warnMavenPluginCheckbox.setSelected(settings.isWarnOnMavenPluginPresent());
        autoAddAgentCheckbox.setSelected(settings.isAutoAddAgent());
    }
}
