package org.example.logger.util;

import org.example.logger.manager.model.LogLevel;
import org.example.logger.manager.model.LoggerConfig;
import org.example.logger.manager.model.Mode;
import org.example.logger.manager.model.output.ConsoleOutputConfig;
import org.example.logger.manager.model.output.DbOutputConfig;
import org.example.logger.manager.model.output.FileOutputConfig;
import org.example.logger.manager.model.output.KafkaOutputConfig;
import org.example.logger.manager.model.output.OutputConfig;
import org.example.logger.manager.model.output.OutputType;

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

/**
 * Reads the YAML config file and returns a LoggerConfig.
 * All reading, parsing and checking of the file happens here.
 *
 * It works in two steps:
 *   1. file -> flat key/value map    (nested keys joined with dots)
 *   2. map  -> LoggerConfig          (mode, level, outputs, with checks)
 *
 * Rules:
 *   mode   - optional, default sync
 *   level  - required
 *   output - optional, default console. Each one must be an OutputType.
 *
 * We can't use a YAML library (JDK only), so step 1 handles just what we need:
 * "key: value" lines, nesting by indentation (spaces only), and # comments.
 */
public class YamlFileReader {

    /** Reads the file at the given path and turns it into a LoggerConfig. */
    public LoggerConfig read(Path file) {
        Map<String, String> values = readKeyValues(file);

        Mode mode = readMode(values);
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

    /** A section we are currently inside, like "output" or "output.file". */
    private record Section(int indent, String fullKey) {
    }

    private Map<String, String> readKeyValues(Path file) {
        List<String> lines = readLines(file);

        Map<String, String> values = new LinkedHashMap<>();
        Deque<Section> openSections = new ArrayDeque<>();

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
            while (!openSections.isEmpty() && openSections.peek().indent() >= indent) {
                openSections.pop();
            }

            String fullKey = openSections.isEmpty() ? key : openSections.peek().fullKey() + "." + key;
            values.put(fullKey, value);

            // "key:" with nothing after it starts a new section. "key: ''" does not:
            // that is an empty value.
            if (value.isEmpty() && !quoted) {
                openSections.push(new Section(indent, fullKey));
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

    /**
     * Returns the text inside the quotes the value starts with.
     * Anything after the closing quote is a comment.
     *
     * A value starting with '[' or '{' has to be quoted to stay valid YAML,
     * which is why the format lines in logger.yaml are written that way.
     */
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

    private Mode readMode(Map<String, String> values) {
        String text = values.get("mode");
        if (text == null || text.isEmpty()) {
            return Mode.SYNC;
        }
        try {
            return Mode.valueOf(text.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown mode '" + text + "'. Use sync or async");
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

    /**
     * Turns the "output" section into one OutputConfig per destination:
     *
     *   output:
     *     console:                     ->  ConsoleOutputConfig(format)
     *       format: ...
     *     file:                        ->  FileOutputConfig(location, timeDurationSeconds, format)
     *       location: ...
     */
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
            checkSettingsAllowed(type, settings);
            outputs.add(readOutput(type, settings));
        }
        return outputs;
    }

    /**
     * Builds the right config class for the type.
     * Adding a new OutputType makes this switch fail to compile until you add its case here.
     */
    private OutputConfig readOutput(OutputType type, Map<String, String> settings) {
        // TODO: check the format text once the formatter exists
        return switch (type) {
            case CONSOLE -> new ConsoleOutputConfig(
                    settings.get("format"));

            case FILE -> new FileOutputConfig(
                    required(type, settings, "location"),
                    positiveNumber(type, settings, "time-duration", 3600),
                    settings.get("format"));

            case DB -> new DbOutputConfig(
                    required(type, settings, "url"),
                    required(type, settings, "table"));

            case KAFKA -> new KafkaOutputConfig(
                    required(type, settings, "brokers"),
                    required(type, settings, "topic"));
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

    /** All keys that start with the prefix, with the prefix removed. */
    private Map<String, String> settingsUnder(Map<String, String> values, String prefix) {
        Map<String, String> settings = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                settings.put(entry.getKey().substring(prefix.length()), entry.getValue());
            }
        }
        return settings;
    }

    /** Fails if a setting doesn't belong to this output, e.g. "format" under db. */
    private void checkSettingsAllowed(OutputType type, Map<String, String> settings) {
        for (String key : settings.keySet()) {
            if (!type.allowedSettings().contains(key)) {
                throw new IllegalArgumentException("Output '" + type + "' doesn't use '" + key
                        + "'. It uses: " + type.allowedSettings());
            }
        }
    }

    private String required(OutputType type, Map<String, String> settings, String key) {
        String value = settings.get(key);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Output '" + type + "' needs a '" + key + "'");
        }
        return value;
    }

    private long positiveNumber(OutputType type, Map<String, String> settings, String key, long defaultValue) {
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
        throw new IllegalArgumentException("Output '" + type + "': " + key
                + " must be a positive number, got '" + value + "'");
    }
}
