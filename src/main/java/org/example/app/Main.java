package org.example.app;

import org.example.logger.LogManager;
import org.example.logger.api.Logger;
import org.example.logger.model.mode.ServiceInfo;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {

    public static void main(String[] args) throws UnknownHostException {
        ServiceInfo serviceInfo = new ServiceInfo(
                "demo-app",
                "1.0.0",
                InetAddress.getLocalHost().getHostAddress());

        LogManager logManager = LogManager.getInstance();
        logManager.initialize("src/main/resources/logger.yaml", serviceInfo);
        Logger log = logManager.getLogger(Main.class);

        try {
            log.info("app started");

            Database database = new Database();
            EmailService emailService = new EmailService();
            UserService userService = new UserService(database, emailService);
            OrderService orderService = new OrderService(database);

            userService.register("ankit", "ankit@gmail.com");
            userService.register("ravi", "no-at-sign");
            orderService.placeOrder("ankit", 250);
            orderService.placeOrder("ankit", 0);

            log.info("app finished its work");
        } finally {
            log.info("app stopping");
            logManager.shutdown();
        }
    }
}
