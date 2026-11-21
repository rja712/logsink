package org.example.logger.api.impl;

import org.example.logger.api.Logger;
import org.example.logger.manager.ShutdownManager;
import org.example.logger.model.LogLevel;
import org.example.logger.model.LoggerConfig;
import org.example.logger.util.LineFormatter;

/**
 * Used when the YAML says "mode: async".
 *
 * The log call puts the message on a queue and returns straight away.
 * A background thread takes messages off the queue and writes them.
 * The app never waits for slow outputs, but messages still in the queue
 * must be written out at shutdown.
 */
public class AsyncLogger implements Logger {

    private final String name;
    private final LoggerConfig config;
    private final ShutdownManager shutdownManager;

    public AsyncLogger(String name, LoggerConfig config, ShutdownManager shutdownManager) {
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

        // TODO: put the message on a shared BlockingQueue and return.
        //       A background thread takes messages off the queue and sends them
        //       to every output destination. Until then, print it like sync does.
        System.out.println(LineFormatter.format(name, level, message, throwable));
    }
}
