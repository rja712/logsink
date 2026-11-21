package org.example.logger.manager;

/**
 * Helper 3: shuts logging down.
 * Remembers whether shutdown has happened, so loggers can ignore late messages.
 *
 * Singleton: there is exactly one, created when the class loads.
 * Get it with ShutdownManager.getInstance().
 */
public class ShutdownManager {

    private static final ShutdownManager INSTANCE = new ShutdownManager();

    private volatile boolean shutDown = false;

    private ShutdownManager() {
    }

    public static ShutdownManager getInstance() {
        return INSTANCE;
    }

    /** Stops logging. Calling it a second time does nothing. */
    public void shutdown() {
        if (shutDown) {
            return;
        }
        shutDown = true;

        // TODO: write out any messages still waiting (needed once async exists)
        // TODO: close the output destinations (e.g. close open files)
    }

    public boolean isShutDown() {
        return shutDown;
    }
}
