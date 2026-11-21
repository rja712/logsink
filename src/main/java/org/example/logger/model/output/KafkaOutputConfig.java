package org.example.logger.model.output;

/**
 * Kafka output settings.
 *
 * brokers - Kafka servers, e.g. "localhost:9092"
 * topic   - which topic to send to
 *
 * TODO: these fields are a first guess; adjust once the Kafka output is built.
 */
public record KafkaOutputConfig(String brokers, String topic) implements OutputConfig {

    @Override
    public OutputType type() {
        return OutputType.KAFKA;
    }
}
