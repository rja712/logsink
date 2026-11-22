package org.example.logger.model.output;

public record FileOutputConfig(
        String location,
        TimeRange timeRange,
        long maxSizeBytes,
        String format) implements OutputConfig {

    @Override
    public OutputType type() {
        return OutputType.FILE;
    }
}
