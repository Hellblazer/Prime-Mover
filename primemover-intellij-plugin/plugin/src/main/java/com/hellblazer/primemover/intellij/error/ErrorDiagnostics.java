package com.hellblazer.primemover.intellij.error;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Diagnostic logging and troubleshooting support for Prime Mover errors.
 * Provides detailed error reports for bug reports and support.
 */
public class ErrorDiagnostics {
    private static final Logger LOG = Logger.getInstance(ErrorDiagnostics.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private final Project project;
    private final List<PrimeMoverError> diagnosticHistory;

    public ErrorDiagnostics(@NotNull Project project) {
        this.project = project;
        this.diagnosticHistory = new ArrayList<>();
    }

    /**
     * Record an error for diagnostic purposes.
     */
    public void recordError(@NotNull PrimeMoverError error) {
        diagnosticHistory.add(error);

        // Keep history bounded (last 100 errors)
        if (diagnosticHistory.size() > 100) {
            diagnosticHistory.remove(0);
        }
    }

    /**
     * Generate a comprehensive diagnostic report.
     */
    public @NotNull String generateDiagnosticReport() {
        var report = new StringBuilder();

        report.append("=== Prime Mover Diagnostic Report ===\n");
        report.append("Generated: ").append(LocalDateTime.now()).append("\n");
        report.append("Project: ").append(project.getName()).append("\n");
        report.append("Project Path: ").append(project.getBasePath()).append("\n\n");

        // System information
        report.append("=== System Information ===\n");
        report.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
        report.append("OS: ").append(System.getProperty("os.name")).append(" ")
              .append(System.getProperty("os.version")).append("\n");
        report.append("IDE Version: ").append(getIdeVersion()).append("\n\n");

        // Error summary
        report.append("=== Error Summary ===\n");
        report.append("Total Errors Recorded: ").append(diagnosticHistory.size()).append("\n");

        var errorsByCategory = diagnosticHistory.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                PrimeMoverError::getCategory,
                java.util.stream.Collectors.counting()
            ));

        report.append("Errors by Category:\n");
        errorsByCategory.forEach((category, count) ->
            report.append("  ").append(category.getDisplayName())
                  .append(": ").append(count).append("\n")
        );

        report.append("\n");

        // Detailed error list
        report.append("=== Error Details ===\n\n");
        for (int i = 0; i < diagnosticHistory.size(); i++) {
            var error = diagnosticHistory.get(i);
            report.append("Error #").append(i + 1).append("\n");
            report.append(error.getFullDetails());
            report.append("\n");

            if (error.getCause() != null) {
                report.append("Stack Trace:\n");
                report.append(getStackTrace(error.getCause()));
                report.append("\n");
            }

            report.append("---\n\n");
        }

        return report.toString();
    }

    /**
     * Export diagnostic report to a file.
     */
    public @NotNull Path exportDiagnosticReport() throws Exception {
        var report = generateDiagnosticReport();

        var timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        var filename = "primemover-diagnostics-" + timestamp + ".txt";

        var outputPath = getDefaultExportPath().resolve(filename);
        Files.createDirectories(outputPath.getParent());
        Files.writeString(outputPath, report);

        LOG.info("Exported diagnostic report to: " + outputPath);
        return outputPath;
    }

    /**
     * Clear diagnostic history.
     */
    public void clearHistory() {
        diagnosticHistory.clear();
    }

    /**
     * Get recent errors (last N).
     */
    public @NotNull List<PrimeMoverError> getRecentErrors(int count) {
        var size = diagnosticHistory.size();
        var start = Math.max(0, size - count);
        return new ArrayList<>(diagnosticHistory.subList(start, size));
    }

    private String getStackTrace(@NotNull Throwable throwable) {
        var sw = new StringWriter();
        var pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    private String getIdeVersion() {
        try {
            var appInfo = com.intellij.openapi.application.ApplicationInfo.getInstance();
            return appInfo.getFullApplicationName() + " " + appInfo.getFullVersion();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private Path getDefaultExportPath() {
        var projectPath = project.getBasePath();
        if (projectPath != null) {
            return Paths.get(projectPath, "build", "primemover-diagnostics");
        }

        // Fallback to user home
        return Paths.get(System.getProperty("user.home"), ".primemover", "diagnostics");
    }
}
