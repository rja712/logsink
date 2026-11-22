package org.example.logger.model.mode;

import org.example.logger.model.Mode;

public record AsyncModeConfig(
        int queueSize,
        int shutdownTimeoutMillis) implements ModeConfig {

    public static final int DEFAULT_QUEUE_SIZE = 10_000;
    public static final int DEFAULT_SHUTDOWN_TIMEOUT_MILLIS = 5_000;

    public AsyncModeConfig {
        if (queueSize <= 0) {
            throw new IllegalArgumentException("queue-size must be more than 0, got " + queueSize);
        }

        if (shutdownTimeoutMillis < 1000){
            throw new IllegalArgumentException("shutdownTimeoutMillis must be more than 1000, got " + shutdownTimeoutMillis);
        }
    }

    @Override
    public Mode type() {
        return Mode.ASYNC;
    }
}
