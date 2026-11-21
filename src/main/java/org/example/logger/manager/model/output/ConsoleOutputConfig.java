package org.example.logger.manager.model.output;

/**
 * Console output settings.
 *
 * format - how each line looks. null means "use the default format".
 */
public record ConsoleOutputConfig(String format) implements OutputConfig {

    @Override
    public OutputType type() {
        return OutputType.CONSOLE;
    }
}
