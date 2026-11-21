package org.example.logger.util;

import org.example.logger.model.LogLevel;
import org.example.logger.model.LoggerConfig;
import org.example.logger.model.Mode;
import org.example.logger.model.mode.AsyncModeConfig;
import org.example.logger.model.mode.ModeConfig;
import org.example.logger.model.mode.SyncModeConfig;
import org.example.logger.model.output.ConsoleOutputConfig;
import org.example.logger.model.output.DbOutputConfig;
import org.example.logger.model.output.FileOutputConfig;
import org.example.logger.model.output.KafkaOutputConfig;
import org.example.logger.model.output.OutputConfig;
import org.example.logger.model.output.OutputType;
import org.example.logger.model.output.TimeRange;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigReaderUtil {

    public LoggerConfig read(Path file) {
        Map<String, String> values = readKeyValues(file);

        ModeConfig mode = readMode(values);
        LogLevel level = readLevel(values);
        List<OutputConfig> outputs = readOutputs(values);

        return new LoggerConfig(mode, level, outputs);
    }

    // ------------------------------------------------------------------
    // Step 1: file -> flat key/value map
    //
    //   level: INFO                 ->  "level"                = "INFO"
    //   output:                     ->  "output"               = ""
    //     file:                     ->  "output.file"          = ""
    //       location: /logs         ->  "output.file.location" = "/logs"
    //
    // TODO: support lists ("- item") if the config ever needs them.
    // ------------------------------------------------------------------

    private Map<String, String> readKeyValues(Path file) {
        List<String> lines = readLines(file);

        Map<String, String> values = new LinkedHashMap<>();
        // The sections the current line can be inside, innermost on top.
        // Two stacks, always pushed and popped together: one for the indent, one for the full key.
        Deque<Integer> sectionIndents = new ArrayDeque<>();
        Deque<String> sectionKeys = new ArrayDeque<>();

        for (int i = 0; i < lines.size(); i++) {
            String raw = lines.get(i);
            String line = raw.strip();
            int lineNumber = i + 1;

            if (line.isEmpty() || line.startsWith("#")) {
                continue;   // skip blank lines and comments
            }

            int indent = raw.indexOf(line);
            if (raw.substring(0, indent).contains("\t")) {
                throw new IllegalArgumentException(
                        "Line " + lineNumber + " in " + file + " is indented with a tab. Use spaces.");
            }

            int colon = line.indexOf(':');
            if (colon < 1) {
                throw new IllegalArgumentException(
                        "Line " + lineNumber + " in " + file + " is not 'key: value': " + line);
            }

            String key = line.substring(0, colon).strip();

            // A quoted value keeps everything inside the quotes, so a '#' or a '['
            // in the text is not mistaken for a comment or a list.
            String rest = line.substring(colon + 1).strip();
            boolean quoted = rest.startsWith("'") || rest.startsWith("\"");
            String value = quoted
                    ? readQuoted(rest, file, lineNumber)
                    : removeTrailingComment(rest).strip();

            // Leave the sections this line is not inside of (same or smaller indent).
            while (!sectionIndents.isEmpty() && sectionIndents.peek() >= indent) {
                sectionIndents.pop();
                sectionKeys.pop();
            }

            String fullKey = sectionKeys.isEmpty() ? key : sectionKeys.peek() + "." + key;
            values.put(fullKey, value);

            // "key:" with nothing after it starts a new section. "key: ''" does not:
            // that is an empty value.
            if (value.isEmpty() && !quoted) {
                sectionIndents.push(indent);
                sectionKeys.push(fullKey);
            }
        }
        return values;
    }

    private List<String> readLines(Path file) {
        try {
            return Files.readAllLines(file);
        } catch (NoSuchFileException e) {
            throw new IllegalArgumentException("Config file not found: " + file.toAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read config file: " + file, e);
        }
    }

    private String removeTrailingComment(String value) {
        int hash = value.indexOf(" #");
        return hash >= 0 ? value.substring(0, hash) : value;
    }

    private String readQuoted(String text, Path file, int lineNumber) {
        char quote = text.charAt(0);
        int end = text.indexOf(quote, 1);
        if (end < 0) {
            throw new IllegalArgumentException("Line " + lineNumber + " in " + file
                    + " has an unclosed " + quote + " quote: " + text);
        }
        return text.substring(1, end);
    }

    // ------------------------------------------------------------------
    // Step 2: map -> LoggerConfig
    // ------------------------------------------------------------------

    private ModeConfig readMode(Map<String, String> values) {
        String text = values.get("mode");

        if (text == null) {
            return new SyncModeConfig();
        }
        if (!text.isEmpty()) {
            return readModeConfig(readModeType(text), Map.of());
        }

        // "mode:" opened a section, so "type" has to say which mode is in use.
        String typeText = values.get("mode.type");
        if (typeText == null || typeText.isEmpty()) {
            throw new IllegalArgumentException("The 'mode' section needs a 'type'. Use sync or async");
        }
        Mode type = readModeType(typeText);

        checkModeNames(values);

        Map<String, String> settings = settingsUnder(values, "mode." + type.yamlName() + ".");
        checkSettingsAllowed("Mode '" + type + "'", type.allowedSettings(), settings);
        return readModeConfig(type, settings);
    }

    private ModeConfig readModeConfig(Mode type, Map<String, String> settings) {
        return switch (type) {
            case SYNC -> new SyncModeConfig();

            case ASYNC -> new AsyncModeConfig(
                    positiveInt("Mode '" + type + "'", settings, "queue-size",
                            AsyncModeConfig.DEFAULT_QUEUE_SIZE));
        };
    }

    private Mode readModeType(String text) {
        try {
            return Mode.valueOf(text.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown mode '" + text + "'. Use sync or async");
        }
    }

    private void checkModeNames(Map<String, String> values) {
        for (String key : values.keySet()) {
            if (!key.startsWith("mode.")) {
                continue;
            }
            String name = key.substring("mode.".length()).split("\\.")[0];
            if (!name.equals("type")) {
                readModeType(name);
            }
        }
    }

    private LogLevel readLevel(Map<String, String> values) {
        String text = values.get("level");
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Config file is missing 'level'");
        }
        try {
            return LogLevel.valueOf(text.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown level '" + text + "'. Use one of "
                    + Arrays.toString(LogLevel.values()));
        }
    }

    private List<OutputConfig> readOutputs(Map<String, String> values) {
        // 1. find the names directly under "output" (console, file, ...)
        Set<String> names = new LinkedHashSet<>();
        for (String key : values.keySet()) {
            if (key.startsWith("output.")) {
                String rest = key.substring("output.".length());
                names.add(rest.split("\\.")[0]);
            }
        }

        if (names.isEmpty()) {
            return List.of(new ConsoleOutputConfig(null));   // default
        }

        // 2. build one config object per name
        List<OutputConfig> outputs = new ArrayList<>();
        for (String name : names) {
            OutputType type = readOutputType(name);
            Map<String, String> settings = settingsUnder(values, "output." + name + ".");
            checkSettingsAllowed("Output '" + type + "'", type.allowedSettings(), settings);
            outputs.add(readOutput(type, settings));
        }
        return outputs;
    }

    private OutputConfig readOutput(OutputType type, Map<String, String> settings) {
        String what = "Output '" + type + "'";

        // TODO: check the format text once the formatter exists
        return switch (type) {
            case CONSOLE -> new ConsoleOutputConfig(
                    settings.get("format"));

            case FILE -> new FileOutputConfig(
                    required(what, settings, "location"),
                    readTimeRange(what, settings.get("time-range")),
                    readSize(what, settings.get("max-size")),
                    settings.get("format"));

            case DB -> new DbOutputConfig(
                    required(what, settings, "url"),
                    required(what, settings, "table"));

            case KAFKA -> new KafkaOutputConfig(
                    required(what, settings, "brokers"),
                    required(what, settings, "topic"));
        };
    }

    private OutputType readOutputType(String name) {
        try {
            return OutputType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown output '" + name + "'. Use one of "
                    + Arrays.toString(OutputType.values()));
        }
    }

    private Map<String, String> settingsUnder(Map<String, String> values, String prefix) {
        Map<String, String> settings = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                settings.put(entry.getKey().substring(prefix.length()), entry.getValue());
            }
        }
        return settings;
    }

    // The checks below are shared by outputs and modes, so "what" is the subject
    // of the message, e.g. "Output 'FILE'" or "Mode 'ASYNC'".

    private void checkSettingsAllowed(String what, Set<String> allowed, Map<String, String> settings) {
        for (String key : settings.keySet()) {
            if (!allowed.contains(key)) {
                throw new IllegalArgumentException(what + " doesn't use '" + key
                        + "'. It uses: " + allowed);
            }
        }
    }

    private String required(String what, Map<String, String> settings, String key) {
        String value = settings.get(key);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(what + " needs a '" + key + "'");
        }
        return value;
    }

    // "hourly" or "daily". Left out means daily.
    private TimeRange readTimeRange(String what, String text) {
        if (text == null || text.isEmpty()) {
            return TimeRange.DAILY;
        }
        try {
            return TimeRange.valueOf(text.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(what + ": time-range must be one of "
                    + Arrays.toString(TimeRange.values()) + ", got '" + text + "'");
        }
    }

    // "500KB", "10MB", "1GB" or plain bytes -> bytes. Left out means no limit (0).
    private long readSize(String what, String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        String upper = text.toUpperCase().replace(" ", "");
        long unit = 1;
        if (upper.endsWith("GB")) {
            unit = 1024L * 1024 * 1024;
        } else if (upper.endsWith("MB")) {
            unit = 1024L * 1024;
        } else if (upper.endsWith("KB")) {
            unit = 1024L;
        }
        String number = unit == 1 ? upper.replace("B", "") : upper.substring(0, upper.length() - 2);
        try {
            long size = Long.parseLong(number) * unit;
            if (size > 0) {
                return size;
            }
        } catch (NumberFormatException ignored) {
            // falls through to the error below
        }
        throw new IllegalArgumentException(what + ": max-size must look like 500KB, 10MB or 1GB, got '" + text + "'");
    }

    private long positiveNumber(String what, Map<String, String> settings, String key, long defaultValue) {
        String value = settings.get(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            long number = Long.parseLong(value);
            if (number > 0) {
                return number;
            }
        } catch (NumberFormatException ignored) {
            // falls through to the error below
        }
        throw new IllegalArgumentException(what + ": " + key
                + " must be a positive number, got '" + value + "'");
    }

    private int positiveInt(String what, Map<String, String> settings, String key, int defaultValue) {
        long number = positiveNumber(what, settings, key, defaultValue);
        if (number > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(what + ": " + key
                    + " must not be more than " + Integer.MAX_VALUE + ", got " + number);
        }
        return (int) number;
    }
}
