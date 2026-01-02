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
package com.hellblazer.primemover.intellij;

import com.hellblazer.primemover.intellij.detection.PrimeMoverProjectDetector;
import com.hellblazer.primemover.intellij.settings.PrimeMoverSettings;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Startup activity that runs when a project is opened.
 * Detects Prime Mover projects and shows relevant notifications.
 */
public final class PrimeMoverStartupActivity implements ProjectActivity {

    private static final String NOTIFICATION_GROUP = "Prime Mover";

    @Override
    public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        var settings = PrimeMoverSettings.getInstance(project);

        // Skip if notifications are disabled
        if (!settings.isShowNotifications()) {
            return Unit.INSTANCE;
        }

        // Detect Prime Mover usage in project
        var detection = PrimeMoverProjectDetector.detect(project);

        if (detection.isPrimeMoverProject()) {
            if (settings.isEnabled()) {
                showInfoNotification(project,
                    "Prime Mover detected",
                    "Post-compile bytecode transformation is enabled for @Entity classes.");
            }

            // Warn about duplicate transformation if Maven plugin is also present
            if (detection.hasDuplicateTransformation() && settings.isWarnOnMavenPluginPresent()) {
                showWarningNotification(project,
                    "Duplicate transformation detected",
                    "Both the IDE plugin and Maven plugin are configured. " +
                    "This may cause classes to be transformed twice. " +
                    "Consider disabling one of them.");
            }
        }

        return Unit.INSTANCE;
    }

    private void showInfoNotification(Project project, String title, String content) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP)
            .createNotification(title, content, NotificationType.INFORMATION)
            .notify(project);
    }

    private void showWarningNotification(Project project, String title, String content) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP)
            .createNotification(title, content, NotificationType.WARNING)
            .notify(project);
    }
}
