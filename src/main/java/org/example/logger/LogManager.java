package org.example.logger;

import org.example.logger.api.Logger;
import org.example.logger.model.mode.ServiceInfo;
import org.example.logger.manager.ConfigManager;
import org.example.logger.manager.LoggerRegistry;
import org.example.logger.manager.ShutdownManager;
import org.example.logger.orchestrator.AsyncListener;

public final class LogManager {

    private static final LogManager INSTANCE = new LogManager();

    private final ConfigManager configManager = ConfigManager.getInstance();
    private final ShutdownManager shutdownManager = ShutdownManager.getInstance();

    private LogManager() {
    }

    public static LogManager getInstance() {
        return INSTANCE;
    }

    public synchronized void initialize(String yamlPath, ServiceInfo serviceInfo) {
        configManager.load(yamlPath, serviceInfo);

        // Build the registry now, so sync/async is fixed at startup.
        LoggerRegistry registry = LoggerRegistry.getInstance();

        // Async mode: start the background thread, and tell ShutdownManager about it.
        AsyncListener listener = registry.getAsyncListener();
        if (listener != null) {
            listener.start();
            shutdownManager.setAsyncListener(listener);
        }
    }

    public Logger getLogger(Class<?> type) {
        return LoggerRegistry.getInstance().getLogger(type);
    }

    public synchronized void shutdown() {
        shutdownManager.shutdown();
    }
}
