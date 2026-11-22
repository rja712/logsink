package org.example.logger;

import org.example.logger.api.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Shared by SyncLoadTest, AsyncLoadTest and LoadScenario.
final class LoadTestSupport {

    static final int THREADS = 50;
    static final int EVENTS_PER_THREAD = 200;
    static final int TOTAL = THREADS * EVENTS_PER_THREAD;   // 10,000

    private LoadTestSupport() {
    }

    // A config that writes every log, as just its message, to one file in the given folder.
    // The queue holds all 10,000 events, so nothing is dropped in async mode.
    static Path writeConfig(Path folder, String mode) throws IOException {
        String yaml = """
                mode:
                  type: %s
                  async:
                    queue-size: 20000
                level: INFO
                output:
                  file:
                    location: %s
                    format: '<message>'
                """.formatted(mode, logFile(folder).toAbsolutePath());
        Path config = folder.resolve("logger.yaml");
        Files.writeString(config, yaml);
        return config;
    }

    static Path logFile(Path folder) {
        return folder.resolve("test.log");
    }

    // 50 threads start at the same moment; thread i logs "t<i> e0", "t<i> e1", ...
    // Returns how long the 10,000 log calls took, in milliseconds.
    static long logFromManyThreads(Logger log) throws InterruptedException {
        CountDownLatch startTogether = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();

        for (int t = 0; t < THREADS; t++) {
            int id = t;
            threads.add(Thread.ofPlatform().start(() -> {
                try {
                    startTogether.await();
                } catch (InterruptedException e) {
                    return;
                }
                for (int n = 0; n < EVENTS_PER_THREAD; n++) {
                    log.info("t" + id + " e" + n);
                }
            }));
        }

        long begin = System.nanoTime();
        startTogether.countDown();
        for (Thread thread : threads) {
            thread.join();
        }
        return (System.nanoTime() - begin) / 1_000_000;
    }

    // Runs LoadScenario in a new Java process, with the same classpath as this test.
    // A new process means a fresh, never-initialized logger, whatever ran before.
    // Returns how long the 10,000 log calls took, in milliseconds.
    static long runInFreshJvm(String mode, Path folder) throws IOException, InterruptedException {
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        Process process = new ProcessBuilder(java, "-cp", System.getProperty("java.class.path"),
                LoadScenario.class.getName(), mode, folder.toString())
                .redirectError(ProcessBuilder.Redirect.INHERIT)   // its stderr shows up in the test output
                .start();

        String output = new String(process.getInputStream().readAllBytes());
        assertTrue(process.waitFor(60, TimeUnit.SECONDS), "scenario did not finish in 60 s");
        assertEquals(0, process.exitValue(), "scenario failed; output: " + output);

        return Long.parseLong(output.strip().replace("millis=", ""));
    }

    // Each thread's lines must appear in the order it logged them: e0, e1, e2, ...
    // That also proves none of a thread's lines are missing or written twice.
    static void assertEveryThreadInOrder(List<String> lines) {
        int[] expectedNext = new int[THREADS];
        for (String line : lines) {
            String[] parts = line.split(" ");                       // "t12 e45"
            int thread = Integer.parseInt(parts[0].substring(1));
            int number = Integer.parseInt(parts[1].substring(1));
            assertEquals(expectedNext[thread], number, "thread t" + thread + " is out of order");
            expectedNext[thread]++;
        }
    }
}
