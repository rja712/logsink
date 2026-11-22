package org.example.logger.orchestrator;

import org.example.logger.model.LogEvent;
import org.example.logger.sink.Sink;
import org.example.logger.sink.SinkFactory;

import java.util.List;

import java.util.concurrent.TimeUnit;

public final class AsyncListener implements Runnable {

    private static final long STOP_WAIT_MILLIS = 5_000;

    private final List<Sink> sinks = SinkFactory.getInstance().getSinks();
    private final Thread thread = new Thread(this, "logger-async-listener");

    private volatile boolean running = true;

    public AsyncListener() {
        // Daemon: the JVM can exit even if shutdown() was never called.
        // TODO: add a JVM shutdown hook that calls shutdown(), so queued events aren't lost then.
        thread.setDaemon(true);
    }

    public void start() {
        thread.start();
    }

    public void stop() {
        running = false;
        try {
            thread.join(STOP_WAIT_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        // Normal work: wait for events. poll() wakes up every 100 ms to check "running".
        while (running) {
            try {
                LogEvent event = LogQueue.getInstance().poll(100, TimeUnit.MILLISECONDS);
                if (event != null) {
                    writeToSinks(event);
                }
            } catch (InterruptedException e) {
                break;
            }
        }

        // Stopping: write out whatever is still in the queue.
        LogEvent event;
        while ((event = LogQueue.getInstance().poll()) != null) {
            writeToSinks(event);
        }
    }

    // One sink after another, in queue order.
    private void writeToSinks(LogEvent event) {
        for (Sink sink : sinks) {
            try {
                sink.write(event);
            } catch (RuntimeException e) {
                // one broken sink must not stop the others, or kill this thread
                System.err.println("[logger] sink " + sink.getClass().getSimpleName() + " failed: " + e);
            }
        }
    }
}
