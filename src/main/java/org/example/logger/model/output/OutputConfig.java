package org.example.logger.model.output;

/**
 * Settings for one output destination.
 *
 * Each kind of output has its own class with only the settings it needs:
 *   ConsoleOutputConfig - format
 *   FileOutputConfig    - location, time-duration, format
 *   DbOutputConfig      - url, table
 *   KafkaOutputConfig   - brokers, topic
 */
public interface OutputConfig {

    OutputType type();
}
