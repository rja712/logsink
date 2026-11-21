package org.example.app;

import org.example.logger.LogManager;
import org.example.logger.api.Logger;

/** Pretend email sender. Needs nothing. */
public class EmailService {

    private final Logger log = LogManager.getInstance().getLogger(EmailService.class);

    /** Returns false if the address has no @. */
    public boolean send(String to, String subject) {
        if (!to.contains("@")) {
            log.warn("cannot send to '" + to + "': not an email address");
            return false;
        }
        log.info("sent '" + subject + "' to " + to);
        return true;
    }
}
