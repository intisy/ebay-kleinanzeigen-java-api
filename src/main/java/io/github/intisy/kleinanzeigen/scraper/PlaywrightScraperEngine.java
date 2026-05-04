package io.github.intisy.kleinanzeigen.scraper;

import io.github.intisy.kleinanzeigen.worker.PlaywrightWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages a pool of {@link PlaywrightWorker} instances.
 * Distributes work via round-robin. Lifecycle bound to Spring context.
 *
 * @author Finn Birich
 */
@Component
public class PlaywrightScraperEngine {
    private static final Logger log = LoggerFactory.getLogger(PlaywrightScraperEngine.class);

    @Value("${kleinanzeigen.browser.max-concurrent:10}")
    private int maxConcurrent;

    private final List<PlaywrightWorker> workers = new ArrayList<>();
    private final AtomicInteger roundRobin = new AtomicInteger(0);

    /**
     * Starts all Playwright workers. Called automatically by Spring after construction.
     *
     * @throws Exception if any worker fails to start
     */
    @PostConstruct
    public void start() throws Exception {
        for (int i = 0; i < maxConcurrent; i++) {
            PlaywrightWorker worker = new PlaywrightWorker(i);
            worker.start();
            workers.add(worker);
        }
        log.info("PlaywrightScraperEngine started with {} workers", maxConcurrent);
    }

    /**
     * Stops all Playwright workers. Called automatically by Spring before destruction.
     */
    @PreDestroy
    public void stop() {
        for (PlaywrightWorker worker : workers) {
            worker.stop();
        }
        log.info("PlaywrightScraperEngine stopped");
    }

    /**
     * Returns the next worker in round-robin order.
     *
     * @return the next {@link PlaywrightWorker}
     */
    public PlaywrightWorker nextWorker() {
        int size = workers.size();
        int idx = Math.abs(roundRobin.getAndIncrement() % size);
        return workers.get(idx);
    }

    public int getWorkerCount() {
        return workers.size();
    }

    @Override
    public String toString() {
        return "PlaywrightScraperEngine{workerCount=" + workers.size() + "}";
    }
}
