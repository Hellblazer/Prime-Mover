package com.hellblazer.primemover.intellij.error;

import com.intellij.notification.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Central error reporting mechanism for Prime Mover plugin.
 * Handles logging, user notifications, and error tracking.
 */
public class ErrorReporter {
    private static final Logger LOG = Logger.getInstance(ErrorReporter.class);
    private static final String NOTIFICATION_GROUP_ID = "Prime Mover Errors";

    private final Project project;
    private final ConcurrentMap<String, PrimeMoverError> errorHistory;
    private final NotificationGroup notificationGroup;

    public ErrorReporter(@NotNull Project project) {
        this.project = project;
        this.errorHistory = new ConcurrentHashMap<>();
        this.notificationGroup = NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP_ID);
    }

    /**
     * Report an error with full context.
     */
    public void reportError(@NotNull PrimeMoverError error) {
        // Log to IDE log
        logError(error);

        // Track in history (keyed by file path + category for deduplication)
        var key = generateErrorKey(error);
        errorHistory.put(key, error);

        // Show user notification if severity requires it
        if (error.getSeverity().requiresNotification()) {
            notifyUser(error);
        }
    }

    /**
     * Report an error from an exception.
     */
    public void reportException(@NotNull ErrorCategory category,
                                 @NotNull String message,
                                 @NotNull Throwable throwable,
                                 @Nullable String filePath) {
        var error = PrimeMoverError.builder(category)
            .message(message)
            .cause(throwable)
            .filePath(filePath)
            .technicalDetails(throwable.getMessage())
            .build();

        reportError(error);
    }

    /**
     * Report a simple error without recovery actions.
     */
    public void reportSimpleError(@NotNull ErrorCategory category,
                                   @NotNull String message,
                                   @Nullable String filePath) {
        var error = PrimeMoverError.builder(category)
            .message(message)
            .filePath(filePath)
            .build();

        reportError(error);
    }

    /**
     * Clear error history (useful after successful rebuild).
     */
    public void clearErrors() {
        errorHistory.clear();
    }

    /**
     * Get all recorded errors.
     */
    public ConcurrentMap<String, PrimeMoverError> getErrorHistory() {
        return errorHistory;
    }

    private void logError(@NotNull PrimeMoverError error) {
        var details = error.getFullDetails();

        switch (error.getSeverity()) {
            case HIGH -> {
                if (error.getCause() != null) {
                    LOG.error(details, error.getCause());
                } else {
                    LOG.error(details);
                }
            }
            case MEDIUM -> LOG.warn(details);
            case LOW -> LOG.info(details);
        }
    }

    private void notifyUser(@NotNull PrimeMoverError error) {
        var notificationType = switch (error.getSeverity()) {
            case HIGH -> NotificationType.ERROR;
            case MEDIUM -> NotificationType.WARNING;
            case LOW -> NotificationType.INFORMATION;
        };

        var notification = notificationGroup.createNotification(
            "Prime Mover: " + error.getCategory().getDisplayName(),
            error.getUserMessage(),
            notificationType
        );

        // Add actions for executable recovery steps
        for (var i = 0; i < error.getRecoveryActions().size(); i++) {
            var recoveryAction = error.getRecoveryActions().get(i);
            if (recoveryAction.isExecutable()) {
                var actionIndex = i;
                notification.addAction(new NotificationAction(recoveryAction.getDescription()) {
                    @Override
                    public void actionPerformed(@NotNull com.intellij.openapi.actionSystem.AnActionEvent e,
                                                @NotNull Notification notification) {
                        try {
                            recoveryAction.execute();
                            notification.expire();
                            LOG.info("Executed recovery action: " + recoveryAction.getDescription());
                        } catch (Exception ex) {
                            LOG.error("Recovery action failed", ex);
                            reportException(
                                ErrorCategory.UNKNOWN,
                                "Recovery action failed: " + recoveryAction.getDescription(),
                                ex,
                                null
                            );
                        }
                    }
                });
            }
        }

        notification.notify(project);
    }

    private String generateErrorKey(@NotNull PrimeMoverError error) {
        var filePart = error.getFilePath() != null ? error.getFilePath() : "global";
        return filePart + ":" + error.getCategory().name();
    }
}
