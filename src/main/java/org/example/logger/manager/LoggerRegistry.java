package org.example.logger.manager;

import org.example.logger.api.Logger;
import org.example.logger.api.DefaultLoggerImplementation;
import org.example.logger.model.LoggerConfig;
import org.example.logger.model.Mode;
import org.example.logger.orchestrator.AsyncListener;
import org.example.logger.orchestrator.AsyncOrchestrator;
import org.example.logger.orchestrator.Orchestrator;
import org.example.logger.orchestrator.SyncOrchestrator;
import org.example.logger.util.LineFormatterUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoggerRegistry {

    // volatile so every thread sees the fully built registry once it's set
    private static volatile LoggerRegistry instance;

    private final Map<String, Logger> loggers = new ConcurrentHashMap<>();
    private final LoggerConfig config;
    private final ShutdownManager shutdownManager;

    private final Orchestrator orchestrator;    // fixed for the life of the registry
    private final AsyncListener asyncListener;  // only in async mode; null in sync mode
    private final boolean findLineNumber;       // looking it up costs time, so only if a format uses it

    private LoggerRegistry(LoggerConfig config, ShutdownManager shutdownManager) {
        this.config = config;
        this.shutdownManager = shutdownManager;
        this.findLineNumber = config.outputs().stream()
                .anyMatch(output -> LineFormatterUtil.usesLineNumber(output.format()));

        if (config.mode().type() == Mode.SYNC) {
            this.orchestrator = new SyncOrchestrator();
            this.asyncListener = null;
        } else {
            // Both use the one shared LogQueue: the orchestrator puts, the listener takes.
            this.orchestrator = new AsyncOrchestrator();
            this.asyncListener = new AsyncListener();
        }
    }

    public static LoggerRegistry getInstance() {
        if (instance == null) {
            synchronized (LoggerRegistry.class) {
                if (instance == null) {
                    instance = new LoggerRegistry(ConfigManager.getInstance().get(), ShutdownManager.getInstance());
                }
            }
        }
        return instance;
    }

    public Logger getLogger(Class<?> type) {
        return loggers.computeIfAbsent(type.getSimpleName(), this::createLogger);
    }

    public AsyncListener getAsyncListener() {
        return asyncListener;
    }

    private Logger createLogger(String name) {
        return new DefaultLoggerImplementation(name, config, shutdownManager, orchestrator, findLineNumber);
    }
}
