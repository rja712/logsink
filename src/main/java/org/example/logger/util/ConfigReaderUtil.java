package org.example.logger.util;

import org.example.logger.model.LogLevel;
import org.example.logger.model.LoggerConfig;
import org.example.logger.model.Mode;
import org.example.logger.model.mode.AsyncModeConfig;
import org.example.logger.model.mode.ModeConfig;
import org.example.logger.model.mode.SyncModeConfig;
import org.example.logger.model.output.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;

public class ConfigReaderUtil {

    public LoggerConfig read(Path file) {
        Map<String, String> values = readKeyValues(file);

        ModeConfig mode = readMode(values);
        LogLevel level = readLevel(values);
        List<OutputConfig> outputs = readOutputs(values);

        return new LoggerConfig(mode, level, outputs);
    }

    //   level: INFO                 ->  "level"                = "INFO"
    //   output:                     ->  "output"               = ""
    //     file:                     ->  "output.file"          = ""
    //       location: /logs         ->  "output.file.location" = "/logs"
    //
    // TODO: support lists ("- item") if the config ever needs them.

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
                continue;
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

    private ModeConfig readMode(Map<String, String> values) {
        String text = values.get("mode");

        if (text == null) {
            return new SyncModeConfig();
        }
        if (!text.isEmpty()) {
            return readModeConfig(readModeType(text), Map.of());
        }

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
            case ASYNC -> readAsyncMode(settings);
        };
    }

    private AsyncModeConfig readAsyncMode(Map<String, String> settings) {
        String what = "Mode 'ASYNC'";
        int queueSize = positiveInt(what, settings, "queue-size", AsyncModeConfig.DEFAULT_QUEUE_SIZE);
        int timeout = positiveInt(what, settings, "shutdown-timeout-ms", AsyncModeConfig.DEFAULT_SHUTDOWN_TIMEOUT_MILLIS);
        return new AsyncModeConfig(queueSize, timeout);
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
            throw new IllegalArgumentException("Unknown level '" + text + "'. Use one of " + Arrays.toString(LogLevel.values()));
        }
    }

    private List<OutputConfig> readOutputs(Map<String, String> values) {
        Set<String> names = new LinkedHashSet<>();
        for (String key : values.keySet()) {
            if (key.startsWith("output.")) {
                String rest = key.substring("output.".length());
                names.add(rest.split("\\.")[0]);
            }
        }

        if (names.isEmpty()) {
            return List.of(new ConsoleOutputConfig(null));
        }

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

        return switch (type) {
            case CONSOLE -> new ConsoleOutputConfig(
                    settings.get("format"));

            case FILE -> new FileOutputConfig(
                    required(what, settings, "location"),
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
            throw new IllegalArgumentException("Unknown output '" + name + "'. Use one of " + Arrays.toString(OutputType.values()));
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
                throw new IllegalArgumentException(what + " doesn't use '" + key + "'. It uses: " + allowed);
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

    // The setting as a whole number above 0, or the default if it's left out.
    private int positiveInt(String what, Map<String, String> settings, String key, int defaultValue) {
        String value = settings.get(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            int number = Integer.parseInt(value);
            if (number > 0) {
                return number;
            }
        } catch (NumberFormatException ignored) {
            // falls through to the error below
        }
        throw new IllegalArgumentException(what + ": " + key + " must be a positive number, got '" + value + "'");
    }
}
