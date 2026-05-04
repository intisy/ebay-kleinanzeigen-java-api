package io.github.intisy.kleinanzeigen.command;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import io.github.intisy.kleinanzeigen.exception.ScrapingException;
import io.github.intisy.kleinanzeigen.lib.KleinanzeigeExtractor;
import io.github.intisy.kleinanzeigen.model.AdItem;
import io.github.intisy.kleinanzeigen.model.PerformanceMetrics;
import io.github.intisy.kleinanzeigen.model.SearchResponse;
import io.github.intisy.kleinanzeigen.scraper.PlaywrightScraperEngine;
import io.github.intisy.kleinanzeigen.worker.PlaywrightWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fluent command for searching ads on Kleinanzeigen.
 * Distributes page scraping across the Playwright worker pool.
 *
 * @author Finn Birich
 */
public class SearchAdsCmd {
    private static final Logger log = LoggerFactory.getLogger(SearchAdsCmd.class);
    private static final int BATCH_SIZE = 8;
    private static final int RETRY_COUNT = 2;
    private static final Random RANDOM = new Random();

    private final PlaywrightScraperEngine engine;
    private String query;
    private String location;
    private Integer radius;
    private Integer minPrice;
    private Integer maxPrice;
    private int pageCount = 1;

    public SearchAdsCmd(PlaywrightScraperEngine engine) {
        this.engine = engine;
    }

    public SearchAdsCmd withQuery(String query) {
        this.query = query;
        return this;
    }

    public SearchAdsCmd withLocation(String location) {
        this.location = location;
        return this;
    }

    public SearchAdsCmd withRadius(Integer radius) {
        this.radius = radius;
        return this;
    }

    public SearchAdsCmd withMinPrice(Integer minPrice) {
        this.minPrice = minPrice;
        return this;
    }

    public SearchAdsCmd withMaxPrice(Integer maxPrice) {
        this.maxPrice = maxPrice;
        return this;
    }

    public SearchAdsCmd withPageCount(int pageCount) {
        this.pageCount = pageCount;
        return this;
    }

    /**
     * Executes the search command, scraping the requested pages concurrently.
     *
     * @return {@link SearchResponse} with deduplicated results and metrics
     */
    public SearchResponse exec() {
        long startTime = System.currentTimeMillis();
        List<String> urls = buildUrls();

        List<AdItem> allItems = new ArrayList<>();
        int pagesSuccessful = 0;
        List<String> warnings = new ArrayList<>();

        // Process pages in batches
        ExecutorService dispatcher = Executors.newCachedThreadPool();
        try {
            List<List<String>> batches = partition(urls, BATCH_SIZE);
            for (List<String> batch : batches) {
                List<CompletableFuture<List<AdItem>>> futures = new ArrayList<>();
                for (String url : batch) {
                    PlaywrightWorker worker = engine.nextWorker();
                    CompletableFuture<List<AdItem>> future = CompletableFuture.supplyAsync(
                            () -> scrapePage(url, worker), dispatcher);
                    futures.add(future);
                }
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                for (int i = 0; i < futures.size(); i++) {
                    try {
                        List<AdItem> pageItems = futures.get(i).get();
                        if (!pageItems.isEmpty()) {
                            pagesSuccessful++;
                        }
                        allItems.addAll(pageItems);
                    } catch (Exception e) {
                        warnings.add("Page " + (i + 1) + " failed: " + e.getMessage());
                        log.warn("Page scraping failed: {}", e.getMessage());
                    }
                }
            }
        } finally {
            dispatcher.shutdown();
        }

        // Deduplicate by adid preserving order
        Set<String> seen = new LinkedHashSet<>();
        List<AdItem> deduped = new ArrayList<>();
        for (AdItem item : allItems) {
            if (seen.add(item.getAdid())) {
                deduped.add(item);
            }
        }

        double timeTaken = (System.currentTimeMillis() - startTime) / 1000.0;
        double successRate = urls.isEmpty() ? 0.0 : (double) pagesSuccessful / urls.size();

        PerformanceMetrics metrics = new PerformanceMetrics(
                urls.size(), pagesSuccessful, successRate,
                urls.isEmpty() ? 0.0 : timeTaken / urls.size()
        );

        return new SearchResponse(true, timeTaken, deduped.size(), deduped, metrics, warnings);
    }

    private List<AdItem> scrapePage(String url, PlaywrightWorker worker) {
        for (int attempt = 0; attempt <= RETRY_COUNT; attempt++) {
            try {
                return worker.submit(() -> {
                    BrowserContext context = worker.newContext();
                    try {
                        Page page = context.newPage();
                        try {
                            page.navigate(url);
                            try {
                                page.waitForSelector(".ad-listitem", new Page.WaitForSelectorOptions()
                                        .setTimeout(5000));
                            } catch (Exception ignored) {
                                // No results on this page — continue and extract what we can
                            }
                            return KleinanzeigeExtractor.extractAdItems(page);
                        } finally {
                            page.close();
                        }
                    } finally {
                        context.close();
                    }
                }).get();
            } catch (Exception e) {
                if (attempt < RETRY_COUNT) {
                    long delay = (long) (Math.pow(2, attempt) * 1000 + RANDOM.nextInt(500));
                    log.warn("Page scrape attempt {} failed for {}, retrying in {}ms: {}",
                            attempt + 1, url, delay, e.getMessage());
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("All {} attempts failed for {}: {}", RETRY_COUNT + 1, url, e.getMessage());
                }
            }
        }
        return new ArrayList<>();
    }

    private List<String> buildUrls() {
        List<String> urls = new ArrayList<>();
        String baseUrl = "https://www.kleinanzeigen.de";

        for (int page = 1; page <= pageCount; page++) {
            StringBuilder sb = new StringBuilder(baseUrl);

            // Price path segment
            if (minPrice != null || maxPrice != null) {
                String min = minPrice != null ? String.valueOf(minPrice) : "";
                String max = maxPrice != null ? String.valueOf(maxPrice) : "";
                sb.append("/preis:").append(min).append(":").append(max);
            }

            sb.append("/s-seite:").append(page);

            // Query parameters
            boolean first = true;
            if (query != null && !query.isEmpty()) {
                sb.append(first ? "?" : "&").append("keywords=").append(encode(query));
                first = false;
            }
            if (location != null && !location.isEmpty()) {
                sb.append(first ? "?" : "&").append("locationStr=").append(encode(location));
                first = false;
            }
            if (radius != null) {
                sb.append(first ? "?" : "&").append("radius=").append(radius);
            }

            urls.add(sb.toString());
        }
        return urls;
    }

    private static String encode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    private static <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }

    @Override
    public String toString() {
        return "SearchAdsCmd{query='" + query + "', pageCount=" + pageCount + "}";
    }
}
