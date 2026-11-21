package org.example.logger.api;

public interface Logger {

    void debug(String message);

    void info(String message);

    void warn(String message);

    void error(String message);

    void warn(String message, Throwable throwable);

    void error(String message, Throwable throwable);
}
