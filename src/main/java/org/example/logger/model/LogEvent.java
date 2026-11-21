package org.example.logger.model;

import java.time.LocalDateTime;

public record LogEvent(
        LocalDateTime timestamp,
        LogLevel level,
        String loggerName,
        String threadName,
        int lineNumber,          // 0 if not looked up
        String message,
        Throwable throwable) {

    public static LogEvent of(LogLevel level, String loggerName, int lineNumber, String message, Throwable throwable) {
        return new LogEvent(
                LocalDateTime.now(),
                level,
                loggerName,
                Thread.currentThread().getName(),
                lineNumber,
                message,
                throwable);
    }
}
