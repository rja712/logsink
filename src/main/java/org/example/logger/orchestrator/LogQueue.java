package org.example.logger.orchestrator;

import org.example.logger.manager.ConfigManager;
import org.example.logger.model.LogEvent;
import org.example.logger.model.mode.AsyncModeConfig;
import org.example.logger.model.mode.ModeConfig;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class LogQueue {

    private LogQueue() {
    }

    // The one queue. Created on the first getInstance() call, which only happens in async mode.
    // volatile so every thread sees the fully built queue once it's set.
    private static volatile BlockingQueue<LogEvent> queue;

    // Double-checked locking, same as LoggerRegistry: no lock once the queue exists.
    public static BlockingQueue<LogEvent> getInstance() {
        if (queue == null) {
            synchronized (LogQueue.class) {
                if (queue == null) {
                    queue = new ArrayBlockingQueue<>(capacity());
                }
            }
        }
        return queue;
    }

    private static int capacity() {
        ModeConfig mode = ConfigManager.getInstance().get().mode();
        if (mode instanceof AsyncModeConfig async) {
            return async.queueSize();
        }
        // Only AsyncOrchestrator and AsyncListener touch the queue, and neither exists
        // in sync mode, so getting here means the mode was changed after startup.
        throw new IllegalStateException(
                "The log queue is only used in async mode, but the mode is " + mode.type());
    }
}
