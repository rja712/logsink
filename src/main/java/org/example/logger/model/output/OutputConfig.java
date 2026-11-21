package org.example.logger.model.output;

public interface OutputConfig {

    OutputType type();

    // The line format, for outputs that write text lines (console, file).
    // Records with a "format" field return it automatically.
    default String format() {
        return null;
    }
}
