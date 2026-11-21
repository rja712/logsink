package org.example.logger.manager;

import org.example.logger.model.LoggerConfig;
import org.example.logger.util.YamlFileReader;

import java.nio.file.Path;

/**
 * Helper 1: takes the configuration.
 * Asks YamlFileReader to read the file, stores the LoggerConfig it returns,
 * and hands it out to whoever needs it (e.g. LoggerRegistry).
 *
 * Singleton: there is exactly one, created when the class loads.
 * Get it with ConfigManager.getInstance().
 */
public class ConfigManager {

    private static final ConfigManager INSTANCE = new ConfigManager();

    private final YamlFileReader yamlReader = new YamlFileReader();

    private LoggerConfig config;

    private ConfigManager() {
    }

    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    /** Reads the YAML file at the given path. Can only be done once. */
    public void load(String yamlPath) {
        if (config != null) {
            throw new IllegalStateException("Logging is already initialized");
        }
        if (yamlPath == null || yamlPath.isBlank()) {
            throw new IllegalArgumentException("Config file path must not be empty");
        }

        config = yamlReader.read(Path.of(yamlPath));
    }

    /** Returns the stored config. Fails if load() was never called. */
    public LoggerConfig get() {
        if (config == null) {
            throw new IllegalStateException("Call LogManager.initialize(path) first");
        }
        return config;
    }
}
