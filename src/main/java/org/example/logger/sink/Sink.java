package org.example.logger.sink;

import org.example.logger.model.LogEvent;

public interface Sink {

    void write(LogEvent event);

    // Called once at shutdown, after the last write.
    void close();
}
