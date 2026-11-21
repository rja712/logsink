package org.example.logger.manager.model.output;

/**
 * File output settings.
 *
 * location            - where the files go (required)
 * timeDurationSeconds - start a new file after this many seconds
 * format              - how each line looks. null means "use the default format".
 */
public record FileOutputConfig(String location, long timeDurationSeconds, String format) implements OutputConfig {

    @Override
    public OutputType type() {
        return OutputType.FILE;
    }
}
