package org.example.logger.api;

/** What the app uses to send log messages. */
public interface Logger {

    void debug(String message);

    void info(String message);

    void warn(String message);

    void error(String message);

    void warn(String message, Throwable throwable);

    void error(String message, Throwable throwable);
}
