package org.example.logger.manager;

import org.example.logger.model.LoggerConfig;
import org.example.logger.model.Mode;
import org.example.logger.model.mode.AsyncModeConfig;
import org.example.logger.model.mode.ServiceInfo;
import org.example.logger.util.ConfigReaderUtil;

import java.nio.file.Path;

public class ConfigManager {

    private static final ConfigManager INSTANCE = new ConfigManager();

    private final ConfigReaderUtil yamlReader = new ConfigReaderUtil();

    private LoggerConfig config;
    private ServiceInfo serviceInfo;

    private ConfigManager() {
    }

    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    public void loadConfiguration(String yamlPath, ServiceInfo serviceInfo) {
        if (config != null) {
            throw new IllegalStateException("Logging is already initialized");
        }
        if (yamlPath == null || yamlPath.isBlank()) {
            throw new IllegalArgumentException("Config file path must not be empty");
        }

        config = yamlReader.read(Path.of(yamlPath));
        this.serviceInfo = serviceInfo != null ? serviceInfo : new ServiceInfo(null, null, null);
    }

    public LoggerConfig getLoggerConfig() {
        if (config == null) {
            throw new IllegalStateException("Call LogManager.initialize(path, serviceInfo) first");
        }
        return config;
    }

   public AsyncModeConfig getAsyncConfig() {
        if (getLoggerConfig().mode().type() == Mode.ASYNC) {
            return (AsyncModeConfig) getLoggerConfig().mode();
        }
        return null;
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }
}
