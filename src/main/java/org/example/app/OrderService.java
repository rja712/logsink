package org.example.app;

import org.example.logger.LogManager;
import org.example.logger.api.Logger;

/** Places orders. Needs Database. */
public class OrderService {

    private final Logger log = LogManager.getInstance().getLogger(OrderService.class);

    private final Database database;

    public OrderService(Database database) {
        this.database = database;
    }

    public void placeOrder(String user, int amount) {
        if (amount <= 0) {
            log.error("order rejected for " + user + ": amount must be more than 0, got " + amount);
            return;
        }
        database.save("orders", user + " paid " + amount);
        log.info("order placed for " + user);
    }
}
