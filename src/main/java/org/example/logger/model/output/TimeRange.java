package org.example.logger.model.output;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// How often the file sink starts a new file. The label replaces <time_range> in the file's location.
public enum TimeRange {

    HOURLY("yyyy-MM-dd_HH"),   // 2026-11-22_04
    DAILY("yyyy-MM-dd");       // 2026-11-22

    private final DateTimeFormatter labelFormat;

    TimeRange(String pattern) {
        this.labelFormat = DateTimeFormatter.ofPattern(pattern);
    }

    public String labelFor(LocalDateTime time) {
        return labelFormat.format(time);
    }
}
