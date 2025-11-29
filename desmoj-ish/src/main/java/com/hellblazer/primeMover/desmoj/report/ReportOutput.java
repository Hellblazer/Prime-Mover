package com.hellblazer.primeMover.desmoj.report;

import java.io.Writer;
import java.nio.file.Path;
import java.util.Collection;

/**
 * Interface for outputting reports in various formats.
 */
public interface ReportOutput {
    /**
     * Write the report to a Writer.
     */
    void write(Collection<Reporter> reporters, Writer writer);
    
    /**
     * Write the report to a file.
     */
    default void write(Collection<Reporter> reporters, Path path) {
        try (var writer = java.nio.file.Files.newBufferedWriter(path)) {
            write(reporters, writer);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to write report to " + path, e);
        }
    }
    
    /**
     * Write the report to a String.
     */
    default String writeToString(Collection<Reporter> reporters) {
        var writer = new java.io.StringWriter();
        write(reporters, writer);
        return writer.toString();
    }
}
