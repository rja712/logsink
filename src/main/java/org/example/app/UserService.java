package org.example.app;

import org.example.logger.LogManager;
import org.example.logger.api.Logger;

public class UserService {

    private final Logger log = LogManager.getInstance().getLogger(UserService.class);

    private final Database database;
    private final EmailService emailService;

    public UserService(Database database, EmailService emailService) {
        this.database = database;
        this.emailService = emailService;
    }

    public void register(String name, String email) {
        log.info("registering " + name);
        database.save("users", name);

        boolean sent = emailService.send(email, "Welcome!");
        if (!sent) {
            log.warn(name + " is registered, but got no welcome email");
        }
    }
}
