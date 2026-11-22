package org.example.logger.orchestrator;

import org.example.logger.model.LogEvent;

public interface Dispatcher {

    void dispatch(LogEvent event);
}
