package org.example.logger.manager;

import org.example.logger.orchestrator.AsyncListener;
import org.example.logger.sink.SinkFactory;

public class ShutdownManager {

    private static final ShutdownManager INSTANCE = new ShutdownManager();

    private volatile boolean shutDown = false;

    private AsyncListener asyncListener;   // null in sync mode

    private ShutdownManager() {
    }

    public static ShutdownManager getInstance() {
        return INSTANCE;
    }

    public void setAsyncListener(AsyncListener asyncListener) {
        this.asyncListener = asyncListener;
    }

    public void shutdown() {
        if (shutDown) {
            return;
        }
        shutDown = true;   // 1. loggers stop accepting new messages

        if (asyncListener != null) {
            asyncListener.stop();   // 2. write out what's still in the queue, then end the thread
        }

        SinkFactory.getInstance().closeAll();   // 3. close the sinks (e.g. close open files)
    }

    public boolean isShutDown() {
        return shutDown;
    }
}
