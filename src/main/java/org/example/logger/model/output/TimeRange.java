package org.example.logger.model.output;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public enum TimeRange {

    HOURLY("yyyy-MM-dd_HH"),
    DAILY("yyyy-MM-dd");

    private final DateTimeFormatter labelFormat;

    TimeRange(String pattern) {
        this.labelFormat = DateTimeFormatter.ofPattern(pattern);
    }

    public String labelFor(LocalDateTime time) {
        return labelFormat.format(time);
    }
}
