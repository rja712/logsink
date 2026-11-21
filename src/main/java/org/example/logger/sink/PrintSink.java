package org.example.logger.sink;

import org.example.logger.model.LogEvent;
import org.example.logger.util.LineFormatterUtil;

// TODO: temporary, until ConsoleSink exists
public class PrintSink implements Sink {

    private final LineFormatterUtil formatter;

    public PrintSink(LineFormatterUtil formatter) {
        this.formatter = formatter;
    }

    @Override
    public void write(LogEvent event) {
        System.out.println(formatter.format(event));
    }
}
