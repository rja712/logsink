package org.example.logger;

import org.example.logger.api.Logger;
import org.example.logger.manager.ConfigManager;
import org.example.logger.manager.LoggerRegistry;
import org.example.logger.manager.ShutdownManager;

/**
 * The library's entry point, and the orchestrator.
 * It does no work itself. Each call is passed to one helper:
 *
 *   initialize(path)    ->  ConfigManager    (helper 1)
 *   getLogger(type)     ->  LoggerRegistry   (helper 2)
 *   shutdown()          ->  ShutdownManager  (helper 3)
 *
 * Singleton: get it with LogManager.getInstance().
 * All three helpers are singletons too.
 */
public final class LogManager {

    private static final LogManager INSTANCE = new LogManager();

    private final ConfigManager configManager = ConfigManager.getInstance();
    private final ShutdownManager shutdownManager = ShutdownManager.getInstance();

    private LogManager() {
    }

    public static LogManager getInstance() {
        return INSTANCE;
    }

    /**
     * Sets up logging from a YAML config file. Call once, before anything logs.
     * The path is passed straight to ConfigManager, which reads the file.
     */
    public synchronized void initialize(String yamlPath) {
        configManager.load(yamlPath);

        // Build the registry now, so sync/async is fixed at startup.
        LoggerRegistry.getInstance();

        // TODO: create the output destinations (console, file) from config
        // TODO: create the formatter from config
        // TODO: if mode is async, start the background thread that empties the queue
    }

    /** Returns the logger for a class. Fails if initialize() hasn't been called. */
    public Logger getLogger(Class<?> type) {
        return LoggerRegistry.getInstance().getLogger(type);
    }

    /** Stops logging cleanly. Messages logged after this are ignored. */
    public synchronized void shutdown() {
        shutdownManager.shutdown();
    }
}
