package org.example.logger.orchestrator;

import org.example.logger.model.LogEvent;

public interface Orchestrator {

    void dispatch(LogEvent event);
}
