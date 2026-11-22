package org.example.logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncLoadTest {

    @Test
    void fiftyThreadsLogTenThousandEventsInOrderAtTenThousandPerSecond(@TempDir Path folder) throws Exception {
        long millis = LoadTestSupport.runInFreshJvm("async", folder);

        List<String> lines = Files.readAllLines(LoadTestSupport.logFile(folder));

        assertEquals(LoadTestSupport.TOTAL, lines.size(), "every event is written");
        LoadTestSupport.assertEveryThreadInOrder(lines);
        assertTrue(millis < 1000, LoadTestSupport.TOTAL + " events took " + millis + " ms; need 10,000 per second");
    }
}
