package org.example.logger.model.output;

public record FileOutputConfig(
        String location,       // file path; <time_range> in it is replaced by the hour or day
        TimeRange timeRange,
        long maxSizeBytes,     // 0 = no size limit
        String format) implements OutputConfig {

    @Override
    public OutputType type() {
        return OutputType.FILE;
    }
}
