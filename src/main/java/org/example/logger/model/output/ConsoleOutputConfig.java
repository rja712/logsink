package org.example.logger.model.output;

public record ConsoleOutputConfig(String format) implements OutputConfig {

    @Override
    public OutputType type() {
        return OutputType.CONSOLE;
    }
}
