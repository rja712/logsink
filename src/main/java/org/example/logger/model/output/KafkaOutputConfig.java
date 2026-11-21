package org.example.logger.model.output;

public record KafkaOutputConfig(String brokers, String topic) implements OutputConfig {

    @Override
    public OutputType type() {
        return OutputType.KAFKA;
    }
}
