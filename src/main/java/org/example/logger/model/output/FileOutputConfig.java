package org.example.logger.model.output;

public record FileOutputConfig(String location, long timeDurationSeconds, String format) implements OutputConfig {

    @Override
    public OutputType type() {
        return OutputType.FILE;
    }
}
