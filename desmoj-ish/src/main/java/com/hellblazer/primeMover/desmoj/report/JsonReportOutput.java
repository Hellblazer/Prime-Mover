package com.hellblazer.primeMover.desmoj.report;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

/**
 * Outputs reports in JSON format.
 */
public class JsonReportOutput implements ReportOutput {
    
    private final boolean prettyPrint;
    
    public JsonReportOutput() {
        this(true);
    }
    
    public JsonReportOutput(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }
    
    @Override
    public void write(Collection<Reporter> reporters, Writer writer) {
        try {
            var indent = prettyPrint ? "  " : "";
            var newline = prettyPrint ? "\n" : "";
            
            writer.write("{" + newline);
            writer.write(indent + "\"components\": [" + newline);
            
            var first = true;
            for (var reporter : reporters) {
                if (!first) {
                    writer.write("," + newline);
                }
                first = false;
                writeReporter(writer, reporter, indent + indent);
            }
            
            writer.write(newline + indent + "]" + newline);
            writer.write("}" + newline);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write JSON report", e);
        }
    }
    
    private void writeReporter(Writer writer, Reporter reporter, String indent) throws IOException {
        var newline = prettyPrint ? "\n" : "";
        var innerIndent = indent + (prettyPrint ? "  " : "");
        
        writer.write(indent + "{" + newline);
        writer.write(innerIndent + "\"name\": " + quote(reporter.getName()) + "," + newline);
        writer.write(innerIndent + "\"type\": " + quote(reporter.getType()) + "," + newline);
        writer.write(innerIndent + "\"statistics\": {" + newline);
        
        var stats = reporter.getStatistics();
        var statFirst = true;
        for (var entry : stats.entrySet()) {
            if (!statFirst) {
                writer.write("," + newline);
            }
            statFirst = false;
            writer.write(innerIndent + (prettyPrint ? "  " : "") + 
                        quote(entry.getKey()) + ": " + formatValue(entry.getValue()));
        }
        
        writer.write(newline + innerIndent + "}" + newline);
        writer.write(indent + "}");
    }
    
    private String quote(String s) {
        return "\"" + s.replace("\\", "\\\\")
                      .replace("\"", "\\\"")
                      .replace("\n", "\\n")
                      .replace("\r", "\\r")
                      .replace("\t", "\\t")
                      .replace("\b", "\\b")
                      .replace("\f", "\\f") + "\"";
    }
    
    private String formatValue(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return quote((String) value);
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        return quote(value.toString());
    }
}
