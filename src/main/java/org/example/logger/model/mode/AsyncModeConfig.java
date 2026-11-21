package org.example.logger.model.mode;

import org.example.logger.model.Mode;

public record AsyncModeConfig(int queueSize) implements ModeConfig {

    public static final int DEFAULT_QUEUE_SIZE = 10_000;

    public AsyncModeConfig {
        if (queueSize <= 0) {
            throw new IllegalArgumentException("queue-size must be more than 0, got " + queueSize);
        }
    }

    @Override
    public Mode type() {
        return Mode.ASYNC;
    }
}
