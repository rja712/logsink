package org.example.logger.model;

import org.example.logger.model.output.OutputConfig;

import java.util.List;

/**
 * The settings, after ConfigManager has read them from the YAML file.
 *
 * mode    - SYNC or ASYNC
 * level   - messages below this level are ignored
 * outputs - where messages go (console, file, ...)
 */
public record LoggerConfig(
        Mode mode,
        LogLevel level,
        List<OutputConfig> outputs) {
}
