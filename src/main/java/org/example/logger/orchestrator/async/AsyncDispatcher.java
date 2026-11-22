package org.example.logger.orchestrator.async;

import org.example.logger.manager.ShutdownManager;
import org.example.logger.model.LogEvent;
import org.example.logger.orchestrator.Dispatcher;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AsyncDispatcher implements Dispatcher {

    private static final AsyncDispatcher INSTANCE = new AsyncDispatcher();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private AsyncDispatcher() {
    }

    public static AsyncDispatcher getInstance() {
        return INSTANCE;
    }

    @Override
    public void dispatch(LogEvent event) {
        lock.readLock().lock();
        try {
            if (ShutdownManager.getInstance().isShutDown()) {
                System.err.println("[logger] logging is shut down, dropped: " + event.level() + " " + event.loggerName() + " - " + event.message());
                return;
            }
            boolean accepted = LogQueue.getInstance().offer(event);
            if (!accepted) {
                System.err.println("[logger] queue full, dropped: " + event.level() + " " + event.loggerName() + " - " + event.message());
            }
        } finally {
            lock.readLock().unlock();
        }
    }
}
