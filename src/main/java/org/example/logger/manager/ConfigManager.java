package org.example.logger.manager;

import org.example.logger.model.LoggerConfig;
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

    public void load(String yamlPath, ServiceInfo serviceInfo) {
        if (config != null) {
            throw new IllegalStateException("Logging is already initialized");
        }
        if (yamlPath == null || yamlPath.isBlank()) {
            throw new IllegalArgumentException("Config file path must not be empty");
        }

        config = yamlReader.read(Path.of(yamlPath));
        this.serviceInfo = serviceInfo != null ? serviceInfo : new ServiceInfo(null, null, null);
    }

    public LoggerConfig get() {
        if (config == null) {
            throw new IllegalStateException("Call LogManager.initialize(path, serviceInfo) first");
        }
        return config;
    }

    public ServiceInfo getServiceInfo() {
        get();
        return serviceInfo;
    }
}
