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

public class FileSink implements Sink {

    private final Path path;
    private final LineFormatterUtil formatter;
    private final BufferedWriter writer;

    public FileSink(FileOutputConfig config, LineFormatterUtil formatter) {
        this.path = Path.of(config.location());
        this.formatter = formatter;
        try {
            Path folder = path.toAbsolutePath().getParent();
            Files.createDirectories(folder);
            this.writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);   // keep what's already there
        } catch (IOException e) {
            throw new IllegalArgumentException("Output 'FILE': cannot open " + path.toAbsolutePath() + " (" + e + ")");
        }
    }

    // synchronized: in sync mode many app threads write at the same time.
    @Override
    public synchronized void write(LogEvent event) {
        try {
            writer.write(formatter.format(event));
            writer.newLine();
            writer.flush();   // on disk now, not lost if the app crashes
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write to " + path, e);
        }
    }

    @Override
    public synchronized void close() {
        try {
            writer.close();
        } catch (IOException e) {
            System.err.println("[logger] could not close " + path + ": " + e);
        }
    }
}
