package org.example.logger.orchestrator.async;

import org.example.logger.model.LogEvent;
import org.example.logger.orchestrator.Dispatcher;

public class AsyncDispatcher implements Dispatcher {

    @Override
    public void dispatch(LogEvent event) {
        // offer, not put: never make the app wait. If the queue is full the event is dropped
        // (accepted: async is best effort; use sync mode when every line must be kept).
        LogQueue.getInstance().offer(event);
    }
}
