package org.example.logger.model;

import java.util.Set;

public enum Mode {

    SYNC(),
    ASYNC("queue-size");

    private final Set<String> allowedSettings;

    Mode(String... allowedSettings) {
        this.allowedSettings = Set.of(allowedSettings);
    }

    public Set<String> allowedSettings() {
        return allowedSettings;
    }

    public String yamlName() {
        return name().toLowerCase();
    }
}
