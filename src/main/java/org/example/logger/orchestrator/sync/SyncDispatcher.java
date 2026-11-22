package org.example.logger.orchestrator.sync;

import org.example.logger.model.LogEvent;
import org.example.logger.orchestrator.Dispatcher;
import org.example.logger.sink.Sink;
import org.example.logger.sink.SinkFactory;

import java.util.List;

public class SyncDispatcher implements Dispatcher {

    private final List<Sink> sinks = SinkFactory.getInstance().getSinks();

    @Override
    public void dispatch(LogEvent event) {
        for (Sink sink : sinks) {
            try {
                sink.write(event);
            } catch (RuntimeException e) {
                System.err.println("[logger] sink " + sink.getClass().getSimpleName() + " failed: " + e);
            }
        }
    }
}
