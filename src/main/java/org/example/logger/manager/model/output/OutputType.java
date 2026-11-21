package org.example.logger.manager.model.output;

import java.util.Set;

/**
 * The kinds of output the library knows about, and which settings each one accepts.
 *
 * To add a new output (e.g. HTTP):
 *   1. add an entry here:          HTTP("url", "format")
 *   2. add a class:                HttpOutputConfig implements OutputConfig
 *   3. add a case in YamlFileReader.readOutput()  (the compiler will point you to it)
 */
public enum OutputType {

    CONSOLE("format"),
    FILE("location", "time-duration", "format"),
    DB("url", "table"),
    KAFKA("brokers", "topic");

    private final Set<String> allowedSettings;

    OutputType(String... allowedSettings) {
        this.allowedSettings = Set.of(allowedSettings);
    }

    /** The setting names this output accepts in the YAML. */
    public Set<String> allowedSettings() {
        return allowedSettings;
    }
}
