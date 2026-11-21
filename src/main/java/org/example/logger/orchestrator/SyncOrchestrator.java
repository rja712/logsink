package org.example.logger.orchestrator;

import org.example.logger.model.LogEvent;
import org.example.logger.sink.Sink;
import org.example.logger.sink.SinkFactory;

import java.util.List;

public class SyncOrchestrator implements Orchestrator {

    private final List<Sink> sinks = SinkFactory.getInstance().getSinks();

    @Override
    public void dispatch(LogEvent event) {
        for (Sink sink : sinks) {
            try {
                sink.write(event);
            } catch (RuntimeException e) {
                // one broken sink must not stop the others, or crash the app
                System.err.println("[logger] sink " + sink.getClass().getSimpleName() + " failed: " + e);
            }
        }
    }
}
