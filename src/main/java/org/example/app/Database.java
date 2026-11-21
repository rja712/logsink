package org.example.app;

import org.example.logger.LogManager;
import org.example.logger.api.Logger;

public class Database {

    private final Logger log = LogManager.getInstance().getLogger(Database.class);

    public Database() {
        log.info("connected");
    }

    public void save(String table, String row) {
        log.debug("about to save into " + table);   // hidden: level is INFO
        log.info("saved into " + table + ": " + row);
    }
}
