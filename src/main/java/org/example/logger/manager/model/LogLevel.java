package org.example.logger.manager.model;

/** Log levels, from least to most important. */
public enum LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR;

    /** True if this level is the same as, or more important than, the other one. */
    public boolean isAtLeast(LogLevel other) {
        return this.ordinal() >= other.ordinal();
    }
}
