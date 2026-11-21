package org.example.logger.model.output;

public record DbOutputConfig(String url, String table) implements OutputConfig {

    @Override
    public OutputType type() {
        return OutputType.DB;
    }
}
