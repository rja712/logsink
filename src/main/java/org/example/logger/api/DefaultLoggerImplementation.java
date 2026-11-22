package org.example.logger.api;

import org.example.logger.manager.ShutdownManager;
import org.example.logger.model.LogEvent;
import org.example.logger.model.LogLevel;
import org.example.logger.model.LoggerConfig;
import org.example.logger.orchestrator.Dispatcher;
import org.example.logger.util.LineNumberUitl;

public class DefaultLoggerImplementation implements Logger {

    private final String name;
    private final LoggerConfig config;
    private final ShutdownManager shutdownManager;
    private final Dispatcher dispatcher;

    public DefaultLoggerImplementation(String name, LoggerConfig config, ShutdownManager shutdownManager, Dispatcher dispatcher) {
        this.name = name;
        this.config = config;
        this.shutdownManager = shutdownManager;
        this.dispatcher = dispatcher;
    }

    @Override
    public void debug(String message) {
        log(LogLevel.DEBUG, message, null);
    }

    @Override
    public void info(String message) {
        log(LogLevel.INFO, message, null);
    }

    @Override
    public void warn(String message) {
        log(LogLevel.WARN, message, null);
    }

    @Override
    public void error(String message) {
        log(LogLevel.ERROR, message, null);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        log(LogLevel.WARN, message, throwable);
    }

    @Override
    public void error(String message, Throwable throwable) {
        log(LogLevel.ERROR, message, throwable);
    }

    private void log(LogLevel level, String message, Throwable throwable) {
        if (shutdownManager.isShutDown()) {
            System.err.println("[logger] logging is shut down, dropped: " + level + " " + name + " - " + message);
            return;
        }
        if (!level.isAtLeast(config.level())) {
            return;
        }

        LogEvent logEvent = LogEvent.of(level, name, LineNumberUitl.find(), message, throwable);
        dispatcher.dispatch(logEvent);
    }
}
