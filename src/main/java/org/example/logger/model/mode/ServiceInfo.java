package org.example.logger.model.mode;

// Who is logging. The app fills this in, however it likes, and passes it to
// LogManager.initialize(). Any field can be null; it then prints as "-".
public record ServiceInfo(String name, String version, String ip) {
}
