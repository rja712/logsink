package org.example.logger.api;

import org.example.logger.manager.ShutdownManager;
import org.example.logger.model.LogEvent;
import org.example.logger.model.LogLevel;
import org.example.logger.model.LoggerConfig;
import org.example.logger.orchestrator.Orchestrator;
import org.example.logger.util.LineNumberUitl;

public class DefaultLoggerImplementation implements Logger {

    private final String name;
    private final LoggerConfig config;
    private final ShutdownManager shutdownManager;
    private final Orchestrator orchestrator;
    private final boolean findLineNumber;   // true only if some format uses <line_number>

    public DefaultLoggerImplementation(String name, LoggerConfig config, ShutdownManager shutdownManager, Orchestrator orchestrator, boolean findLineNumber) {
        this.name = name;
        this.config = config;
        this.shutdownManager = shutdownManager;
        this.orchestrator = orchestrator;
        this.findLineNumber = findLineNumber;
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
            return;     // shut down: ignore
        }
        if (!level.isAtLeast(config.level())) {
            return;     // below the configured level: ignore
        }

        // Must happen here, on the app's thread: in async mode the app's stack is gone
        // by the time the background thread writes the event.
        int lineNumber = findLineNumber ? LineNumberUitl.find() : 0;

        orchestrator.dispatch(LogEvent.of(level, name, lineNumber, message, throwable));
    }
}
