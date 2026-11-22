package org.example.logger.manager;

import org.example.logger.orchestrator.async.AsyncListener;
import org.example.logger.sink.SinkFactory;

public class ShutdownManager {

    private static final ShutdownManager INSTANCE = new ShutdownManager();

    private volatile boolean shutDown = false;

    private ShutdownManager() {
    }

    public static ShutdownManager getInstance() {
        return INSTANCE;
    }

    public synchronized void shutdown() {
        if (shutDown) {
            return;
        }
        ShutdownLock.writeLock().lock();
        try {
            shutDown = true;
        } finally {
            ShutdownLock.writeLock().unlock();
        }

        if (ConfigManager.getInstance().getAsyncConfig() != null) {
            AsyncListener.getInstance().stop();
        }

        SinkFactory.getInstance().closeAll();
    }

    public boolean isShutDown() {
        return shutDown;
    }
}
