package org.example.logger.model;

import org.example.logger.model.mode.ModeConfig;
import org.example.logger.model.output.OutputConfig;

import java.util.List;

public record LoggerConfig(
        ModeConfig mode,
        LogLevel level,
        List<OutputConfig> outputs) {
}
