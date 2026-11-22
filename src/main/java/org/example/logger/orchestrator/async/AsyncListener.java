package org.example.logger.orchestrator.async;

import org.example.logger.manager.ConfigManager;
import org.example.logger.model.LogEvent;
import org.example.logger.sink.Sink;
import org.example.logger.sink.SinkFactory;

import java.util.List;

public final class AsyncListener implements Runnable {

    private final List<Sink> sinks = SinkFactory.getInstance().getSinks();
    private final Thread thread = new Thread(this, "logger-async-listener");

    private AsyncListener() {
        thread.setDaemon(true);
    }

    public static void start() {
        thread.start();
    }

    public void stop() {

        long stopWaitMillis = ConfigManager.getInstance().getAsyncConfig().shutdownTimeoutMillis();

        try {
            LogQueue.getInstance().put(LogEvent.poisonPill());
            thread.join(stopWaitMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (thread.isAlive()) {
            thread.interrupt();
            System.err.println("[logger] listener did not finish in " + stopWaitMillis + " ms, stopped it");
        }

        if (!LogQueue.getInstance().isEmpty()){
            System.err.println("[logger] logging is shut down, dropped: " + LogQueue.getInstance().size() + " log lines");
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

            if (LogEvent.isPoisonPill(event)) {
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
