package org.example.logger.model.output;

public interface OutputConfig {

    OutputType type();

    default String format() {
        return null;
    }
}
