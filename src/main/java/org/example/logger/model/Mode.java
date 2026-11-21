package org.example.logger.model;

/**
 * How log messages are written.
 *
 * SYNC  - the app's thread writes the message before carrying on.
 * ASYNC - the message is queued and a background thread writes it.
 */
public enum Mode {
    SYNC,
    ASYNC
}
