package org.example.logger.manager;

import org.example.logger.api.DefaultLoggerImplementation;
import org.example.logger.api.Logger;
import org.example.logger.model.LoggerConfig;
import org.example.logger.model.Mode;
import org.example.logger.orchestrator.Dispatcher;
import org.example.logger.orchestrator.async.AsyncDispatcher;
import org.example.logger.orchestrator.sync.SyncDispatcher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoggerRegistry {

    // volatile so every thread sees the fully built registry once it's set
    private static volatile LoggerRegistry instance;

    private final Map<String, Logger> loggers = new ConcurrentHashMap<>();
    private final LoggerConfig config;
    private final ShutdownManager shutdownManager;

    private final Dispatcher dispatcher;

    private LoggerRegistry(LoggerConfig config, ShutdownManager shutdownManager) {
        this.config = config;
        this.shutdownManager = shutdownManager;

        if (config.mode().type() == Mode.SYNC) {
            this.dispatcher = new SyncDispatcher();
        } else {
            this.dispatcher = new AsyncDispatcher();
        }
    }

    public static LoggerRegistry getInstance() {
        if (instance == null) {
            synchronized (LoggerRegistry.class) {
                if (instance == null) {
                    instance = new LoggerRegistry(ConfigManager.getInstance().getLoggerConfig(), ShutdownManager.getInstance());
                }
            }
        }
        return instance;
    }

    public Logger getLogger(Class<?> type) {
        return loggers.computeIfAbsent(type.getSimpleName(), this::createLogger);
    }

    private Logger createLogger(String name) {
        return new DefaultLoggerImplementation(name, config, shutdownManager, dispatcher);
    }
}
