package org.example.app;

import org.example.logger.LogManager;
import org.example.logger.api.Logger;

/**
 * Start here.
 *   1. initialize logging with the path of the YAML config file
 *   2. create the app's objects
 *   3. do some work
 *   4. shut logging down (in finally, so it always happens)
 */
public class Main {

    public static void main(String[] args) {
        // 1. initialize logging: the app only gives the path, the library reads the file
        LogManager logManager = LogManager.getInstance();
        logManager.initialize("src/main/resources/logger.yaml");
        Logger log = logManager.getLogger(Main.class);

        try {
            log.info("app started");

            // 2. create objects
            //    Database and EmailService don't need anything.
            //    UserService needs both. OrderService needs Database.
            Database database = new Database();
            EmailService emailService = new EmailService();
            UserService userService = new UserService(database, emailService);
            OrderService orderService = new OrderService(database);

            // 3. do some work
            userService.register("ankit", "ankit@mail.com");
            userService.register("ravi", "no-at-sign");
            orderService.placeOrder("ankit", 250);
            orderService.placeOrder("ankit", 0);

            log.info("app finished its work");
        } finally {
            // 4. shut down logging
            log.info("app stopping");
            logManager.shutdown();
        }
    }
}
