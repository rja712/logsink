package org.example.logger.orchestrator.async;

import org.example.logger.model.LogEvent;
import org.example.logger.orchestrator.Dispatcher;

public class AsyncDispatcher implements Dispatcher {

    @Override
    public void dispatch(LogEvent event) {
        boolean accepted = LogQueue.getInstance().offer(event);
        if (!accepted) {
            System.err.println("[logger] queue full, dropped: " + event.level() + " " + event.loggerName() + " - " + event.message());
        }
    }
}
