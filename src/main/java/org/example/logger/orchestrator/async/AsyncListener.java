package org.example.logger.orchestrator.async;

import org.example.logger.manager.ConfigManager;
import org.example.logger.model.LogEvent;
import org.example.logger.sink.Sink;
import org.example.logger.sink.SinkFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class AsyncListener implements Runnable {

     private static volatile AsyncListener instance;

    private final List<Sink> sinks = SinkFactory.getInstance().getSinks();
    private final long stopWaitMillis = ConfigManager.getInstance().getAsyncConfig().shutdownTimeoutMillis();
    private final Thread asyncListenerThread = Thread.ofPlatform()
            .name("logger-async-listener")
            .daemon(true)
            .unstarted(this);

    private AsyncListener() {
    }

    public static AsyncListener getInstance() {
        if (instance == null) {
            synchronized (AsyncListener.class) {
                if (instance == null) {
                    instance = new AsyncListener();
                }
            }
        }
        return instance;
    }


    public synchronized void start() {
        if (asyncListenerThread.getState() != Thread.State.NEW) {
            return;
        }
        asyncListenerThread.start();
    }

    public void stop() {
        try {
            boolean stopSignalAdded = LogQueue.getInstance().offer(LogEvent.getPoisonPill(), stopWaitMillis, TimeUnit.MILLISECONDS);
            if (!stopSignalAdded) {
                System.err.println("[logger] could not add stop signal, queue is full");
            }
            asyncListenerThread.join(stopWaitMillis); // Joins asyncListenerThread with the main app thread
        } catch (InterruptedException e) {
            System.err.println("[logger] listener did not finish shutdown: main thread interrupted " + e.getMessage());
            Thread.currentThread().interrupt();
        }

        if (asyncListenerThread.isAlive()) {
            asyncListenerThread.interrupt();
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
            } catch (Throwable t) {
                System.err.println("[logger] sink " + sink.getClass().getSimpleName() + " failed: " + t);
            }
        }
    }
}
