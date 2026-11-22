package org.example.logger.model;

import java.time.LocalDateTime;

public record LogEvent(
        LocalDateTime timestamp,
        LogLevel level,
        String loggerName,
        String threadName,
        int lineNumber,
        String message,
        Throwable throwable) {


    private static final LogEvent POISON_PILL = new LogEvent(null, null, "POISON_PILL", null, 0, null, null);

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

    public static LogEvent getPoisonPill() {
        return POISON_PILL;
    }

    public static boolean isPoisonPill(LogEvent event){
        return POISON_PILL.equals(event);
    }

}
