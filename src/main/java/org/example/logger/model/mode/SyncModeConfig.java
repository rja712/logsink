package org.example.logger.model.mode;

import org.example.logger.model.Mode;

public record SyncModeConfig() implements ModeConfig {

    @Override
    public Mode type() {
        return Mode.SYNC;
    }
}
