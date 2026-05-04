package io.github.intisy.kleinanzeigen.worker;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import io.github.intisy.kleinanzeigen.util.UserAgentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Owns a single Playwright + Browser instance on a dedicated daemon thread.
 * All Playwright operations are submitted to this worker's single thread.
 * Thread safety: Playwright Java objects must never be shared across threads.
 *
 * @author Finn Birich
 */
public class PlaywrightWorker {
    private static final Logger log = LoggerFactory.getLogger(PlaywrightWorker.class);

    private final ExecutorService singleThread;
    private Playwright playwright;
    private Browser browser;
    private final int workerId;

    public PlaywrightWorker(int workerId) {
        this.workerId = workerId;
        this.singleThread = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "playwright-worker-" + workerId);
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Initializes Playwright and Browser on the dedicated thread.
     * Blocks until the browser is ready. Must be called once before submitting tasks.
     *
     * @throws Exception if browser initialization fails
     */
    public void start() throws Exception {
        singleThread.submit(() -> {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setArgs(Arrays.asList(
                            "--no-sandbox",
                            "--disable-setuid-sandbox",
                            "--disable-dev-shm-usage",
                            "--disable-blink-features=AutomationControlled"
                    )));
            log.debug("PlaywrightWorker {} started", workerId);
        }).get();
    }

    /**
     * Submits a task to run on this worker's dedicated thread.
     *
     * @param task the callable to execute on the worker thread
     * @param <T>  the return type
     * @return a Future representing the pending result
     */
    public <T> Future<T> submit(Callable<T> task) {
        return singleThread.submit(task);
    }

    /**
     * Creates a new BrowserContext on this worker's thread.
     * MUST be called from within a task already submitted to this worker.
     *
     * @return a fresh BrowserContext with a rotated user agent
     */
    public BrowserContext newContext() {
        String userAgent = UserAgentUtil.getRandom();
        return browser.newContext(new Browser.NewContextOptions()
                .setUserAgent(userAgent)
                .setExtraHTTPHeaders(Collections.singletonMap(
                        "Accept-Language", "de-DE,de;q=0.9,en;q=0.8"
                )));
    }

    /**
     * Shuts down the browser and Playwright instance on the worker thread,
     * then terminates the executor.
     */
    public void stop() {
        singleThread.submit(() -> {
            try {
                if (browser != null) browser.close();
                if (playwright != null) playwright.close();
            } catch (Exception e) {
                log.warn("Worker {} shutdown error: {}", workerId, e.getMessage());
            }
        });
        singleThread.shutdown();
        log.debug("PlaywrightWorker {} stopped", workerId);
    }

    public int getWorkerId() {
        return workerId;
    }

    @Override
    public String toString() {
        return "PlaywrightWorker{workerId=" + workerId + "}";
    }
}
