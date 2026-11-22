package org.example.logger.sink;

import org.example.logger.model.LogEvent;
import org.example.logger.model.output.FileOutputConfig;
import org.example.logger.util.LineFormatterUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileSink implements Sink {

    private static final Pattern TIME_RANGE = Pattern.compile("<time[_ ]?range>", Pattern.CASE_INSENSITIVE);

    private final FileOutputConfig config;
    private final LineFormatterUtil formatter;

    private Path currentPath;
    private BufferedWriter writer;
    private long currentSize;

    public FileSink(FileOutputConfig config, LineFormatterUtil formatter) {
        this.config = config;
        this.formatter = formatter;

        // Create the folder now, so a bad location fails at startup, not on the first log.
        createFolderFor(pathFor(LocalDateTime.now()));
    }

    private static void createFolderFor(Path path) {
        Path folder = path.toAbsolutePath().getParent();
        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            throw new IllegalArgumentException("Output 'FILE': cannot create folder " + folder + " (" + e + ")");
        }
    }

    // synchronized: in sync mode many app threads write at the same time.
    @Override
    public synchronized void write(LogEvent event) {
        String line = formatter.format(event) + System.lineSeparator();
        long lineSize = line.getBytes(StandardCharsets.UTF_8).length;

        try {
            Path path = pathFor(event.timestamp());
            if (!path.equals(currentPath)) {
                open(path);
            } else if (isFull(lineSize)) {
                moveToBackup();
                open(path);
            }

            writer.write(line);
            writer.flush();
            currentSize += lineSize;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write to " + currentPath, e);
        }
    }

    @Override
    public synchronized void close() {
        if (writer == null) {
            return;
        }
        try {
            writer.close();
        } catch (IOException e) {
            System.err.println("[logger] could not close " + currentPath + ": " + e);
        }
        writer = null;
        currentPath = null;
    }

    private Path pathFor(LocalDateTime time) {
        String label = config.timeRange().labelFor(time);
        return Path.of(TIME_RANGE.matcher(config.location()).replaceAll(Matcher.quoteReplacement(label)));
    }

    private boolean isFull(long lineSize) {
        return config.maxSizeBytes() > 0
                && currentSize > 0
                && currentSize + lineSize > config.maxSizeBytes();
    }

    private void open(Path path) throws IOException {
        if (writer != null) {
            writer.close();
        }
        createFolderFor(path);
        writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        currentPath = path;
        currentSize = Files.size(path);
    }

    private void moveToBackup() throws IOException {
        writer.close();
        writer = null;

        int number = 1;
        Path backup = Path.of(currentPath + "." + number);
        while (Files.exists(backup)) {
            number++;
            backup = Path.of(currentPath + "." + number);
        }
        Files.move(currentPath, backup);
        // TODO: max-backups setting, to delete the oldest backups
    }
}
