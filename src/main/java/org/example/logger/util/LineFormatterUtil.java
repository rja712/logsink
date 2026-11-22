package org.example.logger.util;

import org.example.logger.model.LogEvent;
import org.example.logger.model.mode.ServiceInfo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LineFormatterUtil {

    public static final String DEFAULT_FORMAT = "<time_stamp> [<thread_name>] [<log_level>] <class_name> - <message>\n<throwable_trace>";

    // Field names, as placeholders look after case, spaces and underscores are removed,
    // so <Class_name>, <class name> and <className> all become CLASS_NAME.
    private static final String TIME_STAMP = "timestamp";
    private static final String LOG_LEVEL = "loglevel";
    private static final String CLASS_NAME = "classname";
    private static final String THREAD_NAME = "threadname";
    private static final String LINE_NUMBER = "linenumber";
    private static final String MESSAGE = "message";
    private static final String THROWABLE_TRACE = "throwabletrace";
    private static final String IP = "ip";
    private static final String SERVICE_NAME = "servicename";
    private static final String VERSION = "version";

    private static final Set<String> KNOWN = Set.of(
            TIME_STAMP, LOG_LEVEL, CLASS_NAME, THREAD_NAME, LINE_NUMBER,
            MESSAGE, THROWABLE_TRACE, IP, SERVICE_NAME, VERSION);

    private static final Pattern PLACEHOLDER = Pattern.compile("<([^<>]+)>");

    private final String template;
    private final ServiceInfo serviceInfo;

    public LineFormatterUtil(String format, ServiceInfo serviceInfo) {
        this.template = (format == null || format.isBlank()) ? DEFAULT_FORMAT : format;
        this.serviceInfo = serviceInfo;

        // Check every placeholder once, at startup, so a typo fails here and not on the first log.
        Matcher matcher = PLACEHOLDER.matcher(template);
        while (matcher.find()) {
            fieldKey(matcher.group(1));
        }
    }

    private static String fieldKey(String placeholder) {
        String key = placeholder.toLowerCase().replaceAll("[^a-z]", "");
        if (!KNOWN.contains(key)) {
            throw new IllegalArgumentException("Unknown placeholder <" + placeholder + "> in format. Use: "
                    + "<time_stamp> <log_level> <class_name> <thread_name> <message> <throwable_trace> "
                    + "<ip> <service_name> <version> <line_number>");
        }
        return key;
    }

    private static String orDash(String value) {
        return value != null ? value : "-";
    }

    private static String stackTraceOf(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringWriter text = new StringWriter();
        throwable.printStackTrace(new PrintWriter(text));
        return text.toString().stripTrailing();
    }

    // quoteReplacement: a value containing '$' or '\' must be printed as-is, not read as a regex symbol.
    public String format(LogEvent event) {
        return PLACEHOLDER.matcher(template)
                .replaceAll(match -> Matcher.quoteReplacement(valueOf(fieldKey(match.group(1)), event)))
                .stripTrailing();
    }

    private String valueOf(String field, LogEvent event) {
        return switch (field) {
            case TIME_STAMP -> event.timestamp().toString();
            case LOG_LEVEL -> event.level().name();
            case CLASS_NAME -> event.loggerName();
            case THREAD_NAME -> event.threadName();
            case LINE_NUMBER -> event.lineNumber() > 0 ? String.valueOf(event.lineNumber()) : "-";
            case MESSAGE -> event.message();
            case THROWABLE_TRACE -> stackTraceOf(event.throwable());
            case IP -> orDash(serviceInfo.ip());
            case SERVICE_NAME -> orDash(serviceInfo.name());
            case VERSION -> orDash(serviceInfo.version());
            default -> throw new IllegalStateException("Unhandled field: " + field);
        };
    }
}
