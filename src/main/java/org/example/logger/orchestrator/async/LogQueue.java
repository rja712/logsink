package org.example.logger.orchestrator.async;

import org.example.logger.manager.ConfigManager;
import org.example.logger.model.LogEvent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class LogQueue {

     public static final LogEvent POISON_PILL = new LogEvent(null, null, "POISON_PILL", null, 0, null, null);

    // volatile so every thread sees the fully built queue once it's set.
    private static volatile BlockingQueue<LogEvent> queue;

    private LogQueue() {
    }

    public static BlockingQueue<LogEvent> getInstance() {
        if (queue == null) {
            synchronized (LogQueue.class) {
                if (queue == null) {
                    queue = new ArrayBlockingQueue<>(ConfigManager.getInstance().getQueueSize());
                }
            }
        }
        return queue;
    }
}
