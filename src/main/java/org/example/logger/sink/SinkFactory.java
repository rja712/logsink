package org.example.logger.sink;

import org.example.logger.manager.ConfigManager;
import org.example.logger.model.LoggerConfig;
import org.example.logger.model.output.ConsoleOutputConfig;
import org.example.logger.model.output.OutputConfig;
import org.example.logger.util.LineFormatterUtil;

import java.util.List;

public class SinkFactory {

    private static final SinkFactory INSTANCE = new SinkFactory();

    private List<Sink> sinks;

    private SinkFactory() {
    }

    public static SinkFactory getInstance() {
        return INSTANCE;
    }

    public synchronized List<Sink> getSinks() {
        if (sinks == null) {
            sinks = createSinks(ConfigManager.getInstance().get());
        }
        return sinks;
    }

    private List<Sink> createSinks(LoggerConfig config) {
        // TODO: build one real sink per output, by its type:
        //         CONSOLE -> ConsoleSink, FILE -> FileSink, DB -> DbSink, KAFKA -> KafkaSink
        //       Until then, one temporary sink prints every event to the console,
        //       using the console output's format (or the default format if there is none).
        String format = null;
        for (OutputConfig output : config.outputs()) {
            if (output instanceof ConsoleOutputConfig console) {
                format = console.format();
            }
        }
        return List.of(new PrintSink(new LineFormatterUtil(format, ConfigManager.getInstance().getServiceInfo())));
    }
}
