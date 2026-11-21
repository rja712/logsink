package org.example.logger.sink;

import org.example.logger.model.LogEvent;
import org.example.logger.util.LineFormatterUtil;

public class ConsoleSink implements Sink {

    private final LineFormatterUtil formatter;

    public ConsoleSink(LineFormatterUtil formatter) {
        this.formatter = formatter;
    }

    @Override
    public void write(LogEvent event) {
        System.out.println(formatter.format(event));   // println is already thread-safe
    }

    @Override
    public void close() {
        System.out.flush();
    }
}
