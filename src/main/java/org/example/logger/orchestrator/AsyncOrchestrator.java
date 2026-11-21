package org.example.logger.orchestrator;

import org.example.logger.model.LogEvent;

public class AsyncOrchestrator implements Orchestrator {

    @Override
    public void dispatch(LogEvent event) {
        try {
            LogQueue.getInstance().put(event);   // waits only if the queue is full
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();   // the app's thread was interrupted: drop this event
        }
    }
}
