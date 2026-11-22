package org.example.logger.orchestrator.async;

import org.example.logger.model.LogEvent;
import org.example.logger.sink.Sink;
import org.example.logger.sink.SinkFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class AsyncListener implements Runnable {

    private static final long STOP_WAIT_MILLIS = 5_000;

    private final List<Sink> sinks = SinkFactory.getInstance().getSinks();
    private final Thread thread = new Thread(this, "logger-async-listener");

    public AsyncListener() {
        // Daemon: the JVM can exit even if shutdown() was never called.
        // TODO: add a JVM shutdown hook that calls shutdown(), so queued events aren't lost then.
        thread.setDaemon(true);
    }

    public void start() {
        thread.start();
    }

    public void stop() {
        try {
            LogQueue.getInstance().put(LogQueue.POISON_PILL);
            thread.join(STOP_WAIT_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (thread.isAlive()) {
            thread.interrupt();
            System.err.println("[logger] listener did not finish in " + STOP_WAIT_MILLIS + " ms, stopped it; " + LogQueue.getInstance().size() + " events not written");
        }
    }

    @Override
    public void run() {
        while (true) {
            LogEvent event;
            try {
                event = LogQueue.getInstance().take();
            } catch (InterruptedException e) {
                return;
            }

            if (event == LogQueue.POISON_PILL) {
                return;
            }
            writeToSinks(event);
        }
    }

    private void writeToSinks(LogEvent event) {
        for (Sink sink : sinks) {
            try {
                sink.write(event);
            } catch (RuntimeException e) {
                System.err.println("[logger] sink " + sink.getClass().getSimpleName() + " failed: " + e);
            }
        }
    }
}
