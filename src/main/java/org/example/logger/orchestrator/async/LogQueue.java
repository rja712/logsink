package org.example.logger.orchestrator.async;

import org.example.logger.manager.ConfigManager;
import org.example.logger.model.LogEvent;
import org.example.logger.model.mode.AsyncModeConfig;
import org.example.logger.model.mode.ModeConfig;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class LogQueue {

    // volatile so every thread sees the fully built queue once it's set.
    private static volatile BlockingQueue<LogEvent> queue;

    private LogQueue() {
    }

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
        if (mode instanceof AsyncModeConfig(int queueSize)) {
            return queueSize;
        }
        throw new IllegalStateException("The log queue is only used in async mode, but the mode is " + mode.type());
    }
}
