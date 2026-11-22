package org.example.logger.orchestrator.async;

import org.example.logger.manager.ConfigManager;
import org.example.logger.model.LogEvent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class LogQueue {

    private static volatile BlockingQueue<LogEvent> queue;
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private LogQueue() {
    }

    private static BlockingQueue<LogEvent> getInstance() {
        if (queue == null) {
            synchronized (LogQueue.class) {
                if (queue == null) {
                    queue = new ArrayBlockingQueue<>(ConfigManager.getInstance().getAsyncConfig().queueSize());
                }
            }
        }
        return queue;
    }

    public static int size(){
        return getInstance().size();
    }

    public static void safePut(LogEvent event) {
        if (lock.writeLock().tryLock()) {
            try {
                getInstance().put(event);
            } catch (InterruptedException e) {
                System.err.println("[logger] queue take interrupted: " + e.getMessage());
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    public static boolean safeOffer(LogEvent event) {
        boolean accepted = false;
        if (lock.readLock().tryLock()) {
            accepted = getInstance().offer(event);
            lock.readLock().unlock();
        }
        return accepted;
    }

    public static LogEvent safeTake() {
        if (lock.readLock().tryLock()) {
            try {
                return getInstance().take();
            } catch (InterruptedException e) {
                System.err.println("[logger] queue take interrupted: " + e.getMessage());
            } finally {
                lock.readLock().unlock();
            }
        }
        return null;

    }


}
