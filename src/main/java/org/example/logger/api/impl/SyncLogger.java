package org.example.logger.api.impl;

import org.example.logger.api.Logger;
import org.example.logger.manager.ShutdownManager;
import org.example.logger.manager.model.LogLevel;
import org.example.logger.manager.model.LoggerConfig;
import org.example.logger.util.LineFormatter;

/**
 * Used when the YAML says "mode: sync".
 *
 * The app's own thread writes the message, and the log call returns only after
 * it is written. Nothing is lost if the app crashes right after, but the app waits
 * for slow outputs (like a file or a database).
 */
public class SyncLogger implements Logger {

    private final String name;
    private final LoggerConfig config;
    private final ShutdownManager shutdownManager;

    public SyncLogger(String name, LoggerConfig config, ShutdownManager shutdownManager) {
        this.name = name;
        this.config = config;
        this.shutdownManager = shutdownManager;
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

        // TODO: send the line to every output destination (console, file, ...) right here,
        //       on the caller's thread, instead of printing it.
        System.out.println(LineFormatter.format(name, level, message, throwable));
    }
}
