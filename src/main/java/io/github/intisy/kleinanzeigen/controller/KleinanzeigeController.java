package io.github.intisy.kleinanzeigen.controller;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import io.github.intisy.kleinanzeigen.command.GetAdDetailCmd;
import io.github.intisy.kleinanzeigen.command.SearchAdsCmd;
import io.github.intisy.kleinanzeigen.exception.KleinanzeigeException;
import io.github.intisy.kleinanzeigen.lib.KleinanzeigeExtractor;
import io.github.intisy.kleinanzeigen.model.AdDetail;
import io.github.intisy.kleinanzeigen.model.AdItem;
import io.github.intisy.kleinanzeigen.model.DetailResponse;
import io.github.intisy.kleinanzeigen.model.ErrorResponse;
import io.github.intisy.kleinanzeigen.model.ExtraInfo;
import io.github.intisy.kleinanzeigen.model.Location;
import io.github.intisy.kleinanzeigen.model.PerformanceMetrics;
import io.github.intisy.kleinanzeigen.model.PriceInfo;
import io.github.intisy.kleinanzeigen.model.SearchDetailResponse;
import io.github.intisy.kleinanzeigen.model.SearchResponse;
import io.github.intisy.kleinanzeigen.model.SellerInfo;
import io.github.intisy.kleinanzeigen.scraper.PlaywrightScraperEngine;
import io.github.intisy.kleinanzeigen.worker.PlaywrightWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * REST controller exposing all Kleinanzeigen scraper endpoints.
 *
 * @author Finn Birich
 */
@RestController
public class KleinanzeigeController {
    private static final Logger log = LoggerFactory.getLogger(KleinanzeigeController.class);

    private final PlaywrightScraperEngine engine;

    public KleinanzeigeController(PlaywrightScraperEngine engine) {
        this.engine = engine;
    }

    /**
     * Root endpoint — API welcome and endpoint listing.
     *
     * @return welcome map
     */
    @GetMapping("/")
    public Map<String, Object> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to the Kleinanzeigen API");
        response.put("endpoints", Arrays.asList("/inserate", "/inserat/{id}", "/inserate-detailed"));
        response.put("status", "operational");
        return response;
    }

    /**
     * Search ads endpoint.
     *
     * @param query      search keywords (optional)
     * @param location   location string (optional)
     * @param radius     search radius in km (optional)
     * @param minPrice   minimum price filter (optional)
     * @param maxPrice   maximum price filter (optional)
     * @param pageCount  number of result pages to scrape (1–20, default 1)
     * @return search results or error response
     */
    @GetMapping("/inserate")
    public ResponseEntity<?> getInserate(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer radius,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(defaultValue = "1") int pageCount
    ) {
        if (pageCount < 1 || pageCount > 20) {
            ErrorResponse err = new ErrorResponse(
                    "pageCount must be between 1 and 20",
                    "VALIDATION", "LOW",
                    Arrays.asList("Provide pageCount between 1 and 20"));
            return ResponseEntity.badRequest().body(err);
        }

        log.info("GET /inserate query={} location={} pageCount={}", query, location, pageCount);

        SearchResponse result = new SearchAdsCmd(engine)
                .withQuery(query)
                .withLocation(location)
                .withRadius(radius)
                .withMinPrice(minPrice)
                .withMaxPrice(maxPrice)
                .withPageCount(pageCount)
                .exec();

        return ResponseEntity.ok(result);
    }

    /**
     * Single ad detail endpoint.
     *
     * @param id the ad ID
     * @return ad detail or error response
     */
    @GetMapping("/inserat/{id}")
    public ResponseEntity<?> getInserat(@PathVariable String id) {
        if (id == null || id.trim().isEmpty()) {
            ErrorResponse err = new ErrorResponse(
                    "Ad ID must not be blank",
                    "VALIDATION", "LOW",
                    Arrays.asList("Provide a valid ad ID in the path"));
            return ResponseEntity.badRequest().body(err);
        }

        log.info("GET /inserat/{}", id);

        DetailResponse result = new GetAdDetailCmd(engine, id).exec();
        return ResponseEntity.ok(result);
    }

    /**
     * Combined search + detail endpoint.
     *
     * @param query                 search keywords (optional)
     * @param location              location string (optional)
     * @param radius                search radius in km (optional)
     * @param minPrice              minimum price filter (optional)
     * @param maxPrice              maximum price filter (optional)
     * @param pageCount             number of search pages to scrape (1–3, default 1)
     * @param maxConcurrentDetails  max parallel detail fetches (1–10, default 5)
     * @return combined search + detail results or error response
     */
    @GetMapping("/inserate-detailed")
    public ResponseEntity<?> getInserateDetailed(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer radius,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(defaultValue = "1") int pageCount,
            @RequestParam(defaultValue = "5") int maxConcurrentDetails
    ) {
        if (pageCount < 1 || pageCount > 3) {
            ErrorResponse err = new ErrorResponse(
                    "pageCount must be between 1 and 3 for detailed search",
                    "VALIDATION", "LOW",
                    Arrays.asList("Provide pageCount between 1 and 3"));
            return ResponseEntity.badRequest().body(err);
        }
        if (maxConcurrentDetails < 1 || maxConcurrentDetails > 10) {
            ErrorResponse err = new ErrorResponse(
                    "maxConcurrentDetails must be between 1 and 10",
                    "VALIDATION", "LOW",
                    Arrays.asList("Provide maxConcurrentDetails between 1 and 10"));
            return ResponseEntity.badRequest().body(err);
        }

        log.info("GET /inserate-detailed query={} pageCount={} maxConcurrentDetails={}",
                query, pageCount, maxConcurrentDetails);

        long startTime = System.currentTimeMillis();

        // Phase 1: search
        SearchResponse searchResult = new SearchAdsCmd(engine)
                .withQuery(query)
                .withLocation(location)
                .withRadius(radius)
                .withMinPrice(minPrice)
                .withMaxPrice(maxPrice)
                .withPageCount(pageCount)
                .exec();

        List<AdItem> listings = searchResult.getData();

        // Phase 2: fetch details concurrently
        Semaphore semaphore = new Semaphore(maxConcurrentDetails);
        ExecutorService detailExecutor = Executors.newCachedThreadPool();
        List<CompletableFuture<SearchDetailResponse.AdItemWithDetails>> detailFutures = new ArrayList<>();

        for (AdItem item : listings) {
            CompletableFuture<SearchDetailResponse.AdItemWithDetails> future =
                    CompletableFuture.supplyAsync(() -> {
                        long detailStart = System.currentTimeMillis();
                        try {
                            semaphore.acquire();
                            try {
                                PlaywrightWorker worker = engine.nextWorker();
                                AdDetail detail = worker.submit(() -> {
                                    BrowserContext context = worker.newContext();
                                    try {
                                        Page page = context.newPage();
                                        try {
                                            page.navigate("https://www.kleinanzeigen.de/s-anzeige/" + item.getAdid());
                                            try {
                                                page.waitForSelector("#viewad-details",
                                                        new Page.WaitForSelectorOptions().setTimeout(10000));
                                            } catch (Exception ignored) {
                                                // continue with partial data
                                            }
                                            Map<String, String> detailMap = KleinanzeigeExtractor.extractDetails(page);
                                            List<String> featureList = KleinanzeigeExtractor.extractFeatures(page);
                                            Location loc = KleinanzeigeExtractor.extractLocation(page);
                                            SellerInfo sel = KleinanzeigeExtractor.extractSellerInfo(page);
                                            ExtraInfo extra = KleinanzeigeExtractor.extractExtraInfo(page);
                                            PriceInfo pInfo = KleinanzeigeExtractor.parsePrice(item.getPrice());
                                            return new AdDetail(item.getTitle(), "", pInfo, detailMap, featureList, loc, sel, extra);
                                        } finally {
                                            page.close();
                                        }
                                    } finally {
                                        context.close();
                                    }
                                }).get();
                                double detailTime = (System.currentTimeMillis() - detailStart) / 1000.0;
                                return new SearchDetailResponse.AdItemWithDetails(item, detail, detailTime);
                            } finally {
                                semaphore.release();
                            }
                        } catch (Exception e) {
                            log.warn("Detail fetch failed for adid={}: {}", item.getAdid(), e.getMessage());
                            double detailTime = (System.currentTimeMillis() - detailStart) / 1000.0;
                            return new SearchDetailResponse.AdItemWithDetails(item, null, detailTime);
                        }
                    }, detailExecutor);

            detailFutures.add(future);
        }

        CompletableFuture.allOf(detailFutures.toArray(new CompletableFuture[0])).join();
        detailExecutor.shutdown();

        List<SearchDetailResponse.AdItemWithDetails> detailedItems = new ArrayList<>();
        for (CompletableFuture<SearchDetailResponse.AdItemWithDetails> future : detailFutures) {
            try {
                detailedItems.add(future.get());
            } catch (Exception e) {
                log.warn("Failed to collect detail future: {}", e.getMessage());
            }
        }

        double timeTaken = (System.currentTimeMillis() - startTime) / 1000.0;
        PerformanceMetrics metrics = new PerformanceMetrics(
                listings.size(), detailedItems.size(),
                listings.isEmpty() ? 0.0 : (double) detailedItems.size() / listings.size(),
                listings.isEmpty() ? 0.0 : timeTaken / listings.size()
        );

        SearchDetailResponse response = new SearchDetailResponse(
                true, timeTaken, detailedItems.size(), detailedItems, metrics);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(KleinanzeigeException.class)
    public ResponseEntity<ErrorResponse> handleKleinanzeigeException(KleinanzeigeException e) {
        log.error("KleinanzeigeException: {}", e.getMessage(), e);
        ErrorResponse err = new ErrorResponse(
                e.getMessage(),
                e.getCategory().name(),
                e.getSeverity().name(),
                Arrays.asList("Check logs for details", "Retry the request")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("Unhandled exception: {}", e.getMessage(), e);
        ErrorResponse err = new ErrorResponse(
                "An unexpected error occurred: " + e.getMessage(),
                "NON_RECOVERABLE", "HIGH",
                Arrays.asList("Check server logs")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }
}
