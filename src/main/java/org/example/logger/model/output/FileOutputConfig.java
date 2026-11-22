package org.example.logger.model.output;

public record FileOutputConfig(
        String location,
        String format) implements OutputConfig {

    @Override
    public OutputType type() {
        return OutputType.FILE;
    }
}
