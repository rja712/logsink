package org.example.logger;

import org.example.logger.api.Logger;

import java.nio.file.Path;

// The work one test does, as a program of its own: initialize the logger once,
// have 50 threads log 10,000 events, shut down. Each test runs this in a NEW Java
// process (see LoadTestSupport.runInFreshJvm), because the logger is initialized only
// once per process, the same as in a real app.
//
// args: mode ("sync" or "async"), folder for the config and the log file
public class LoadScenario {

    public static void main(String[] args) throws Exception {
        String mode = args[0];
        Path folder = Path.of(args[1]);

        LogManager logManager = LogManager.getInstance();
        logManager.initialize(LoadTestSupport.writeConfig(folder, mode).toString(), null);
        Logger log = logManager.getLogger(LoadScenario.class);

        long millis = LoadTestSupport.logFromManyThreads(log);
        logManager.shutdown();

        System.out.println("millis=" + millis);   // read back by the test
    }
}
