package org.example.logger.sink;

import org.example.logger.model.LogEvent;

public interface Sink {

    void write(LogEvent event);
}
