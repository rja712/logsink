package org.example.logger.manager;

import org.example.logger.api.impl.AsyncLogger;
import org.example.logger.api.Logger;
import org.example.logger.api.impl.SyncLogger;
import org.example.logger.model.LoggerConfig;
import org.example.logger.model.Mode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper 2: gives out loggers.
 * Creates one logger per class and reuses it, so asking twice gives the same object.
 *
 * Sync or async is decided ONCE, in the constructor, from the config.
 * It is stored in a final field, so it can't change while the app runs:
 * every logger this registry ever gives out is the same kind.
 *
 * Singleton, but created lazily: it needs the config, which only exists after
 * LogManager.initialize() has loaded the YAML. So it is built on the first
 * getInstance() call, and the same one is returned forever after.
 */
public class LoggerRegistry {

    // volatile so every thread sees the fully built registry once it's set
    private static volatile LoggerRegistry instance;

    private final Map<String, Logger> loggers = new ConcurrentHashMap<>();
    private final Mode mode;
    private final LoggerConfig config;
    private final ShutdownManager shutdownManager;

    private LoggerRegistry(LoggerConfig config, ShutdownManager shutdownManager) {
        this.mode = config.mode();
        this.config = config;
        this.shutdownManager = shutdownManager;
    }

    /**
     * Double-checked locking:
     *   1st check (no lock)   - fast path, used on every call after the first
     *   lock                  - only one thread may build it
     *   2nd check (with lock) - another thread may have built it while we waited
     *
     * Fails with "Call LogManager.initialize(path) first" if the config isn't loaded yet.
     */
    public static LoggerRegistry getInstance() {
        if (instance == null) {
            synchronized (LoggerRegistry.class) {
                if (instance == null) {
                    instance = new LoggerRegistry(
                            ConfigManager.getInstance().get(),
                            ShutdownManager.getInstance());
                }
            }
        }
        return instance;
    }

    public Logger getLogger(Class<?> type) {
        return loggers.computeIfAbsent(type.getSimpleName(), this::createLogger);
    }

    private Logger createLogger(String name) {
        return switch (mode) {
            case SYNC -> new SyncLogger(name, config, shutdownManager);
            case ASYNC -> new AsyncLogger(name, config, shutdownManager);
        };
    }
}
