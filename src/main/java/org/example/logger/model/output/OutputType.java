package org.example.logger.model.output;

import java.util.Set;

public enum OutputType {

    CONSOLE("format"),
    FILE("location", "time-range", "max-size", "format"),
    DB("url", "table"),
    KAFKA("brokers", "topic");

    private final Set<String> allowedSettings;

    OutputType(String... allowedSettings) {
        this.allowedSettings = Set.of(allowedSettings);
    }

    public Set<String> allowedSettings() {
        return allowedSettings;
    }
}
