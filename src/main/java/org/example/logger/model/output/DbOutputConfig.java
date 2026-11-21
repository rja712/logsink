package org.example.logger.model.output;

/**
 * Database output settings. No format: rows are stored as fields, not as text lines.
 *
 * url   - where the database is
 * table - which table to write into
 *
 * TODO: these fields are a first guess; adjust once the DB output is built.
 */
public record DbOutputConfig(String url, String table) implements OutputConfig {

    @Override
    public OutputType type() {
        return OutputType.DB;
    }
}
