package org.example.logger.model;

public enum LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR;

    public boolean isAtLeast(LogLevel other) {
        return this.ordinal() >= other.ordinal();
    }
}
