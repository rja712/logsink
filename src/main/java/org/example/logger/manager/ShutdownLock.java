package org.example.logger.manager;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
