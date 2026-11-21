package org.example.logger.util;

import org.example.logger.model.LogLevel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalTime;

/**
 * Builds the text of one log line, with the stack trace underneath if there is one.
 *
 *   03:29:47.280071 [ERROR] OrderService - payment failed
 *   java.lang.IllegalStateException: boom
 *       at ...
 *
 * TODO: replace with the formatter built from the "format" setting in the YAML.
 */
public final class LineFormatter {

    private LineFormatter() {
    }

    public static String format(String loggerName, LogLevel level, String message, Throwable throwable) {
        String line = LocalTime.now() + " [" + level + "] " + loggerName + " - " + message;
        if (throwable == null) {
            return line;
        }
        return line + System.lineSeparator() + stackTraceOf(throwable);
    }

    /**
     * printStackTrace() prints straight to the screen and returns nothing,
     * so we point it at a StringWriter to get the text back as a String.
     */
    private static String stackTraceOf(Throwable throwable) {
        StringWriter text = new StringWriter();
        throwable.printStackTrace(new PrintWriter(text));
        return text.toString().stripTrailing();
    }
}
