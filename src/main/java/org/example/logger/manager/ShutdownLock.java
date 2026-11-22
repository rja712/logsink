package org.example.logger.manager;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// One lock for the gap between "still accepting logs" and "shutting down".
//   Read lock:  a dispatcher holds it from the shutdown check until the log is handed over.
//   Write lock: ShutdownManager holds it while it sets the shutdown flag,
//               so it waits for every log that is between the check and the hand-over.
// Because of this, the poison pill is always added after the last accepted log.
public final class ShutdownLock {

    private static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();

    private ShutdownLock() {
    }

    public static Lock readLock() {
        return LOCK.readLock();
    }

    public static Lock writeLock() {
        return LOCK.writeLock();
    }
}
