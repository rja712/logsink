package org.example.logger.sink;

import org.example.logger.manager.ConfigManager;
import org.example.logger.model.mode.ServiceInfo;
import org.example.logger.model.output.FileOutputConfig;
import org.example.logger.model.output.OutputConfig;
import org.example.logger.util.LineFormatterUtil;

import java.util.ArrayList;
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
            ConfigManager configManager = ConfigManager.getInstance();
            List<Sink> built = new ArrayList<>();
            for (OutputConfig output : configManager.get().outputs()) {
                built.add(createSink(output, configManager.getServiceInfo()));
            }
            sinks = List.copyOf(built);
        }
        return sinks;
    }

    private Sink createSink(OutputConfig output, ServiceInfo serviceInfo) {
        LineFormatterUtil formatter = new LineFormatterUtil(output.format(), serviceInfo);

        return switch (output.type()) {
            case CONSOLE -> new ConsoleSink(formatter);
            case FILE -> new FileSink((FileOutputConfig) output, formatter);
            case DB, KAFKA -> throw new IllegalArgumentException("TBD");
        };
    }


    public synchronized void closeAll() {
        if (sinks == null) {
            return;
        }
        for (Sink sink : sinks) {
            try {
                sink.close();
            } catch (RuntimeException e) {
                System.err.println("[logger] could not close " + sink.getClass().getSimpleName() + ": " + e);
            }
        }
    }


}
