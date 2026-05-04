package io.github.intisy.kleinanzeigen.examples;

import io.github.intisy.kleinanzeigen.command.GetAdDetailCmd;
import io.github.intisy.kleinanzeigen.command.SearchAdsCmd;
import io.github.intisy.kleinanzeigen.model.AdDetail;
import io.github.intisy.kleinanzeigen.model.AdItem;
import io.github.intisy.kleinanzeigen.model.DetailResponse;
import io.github.intisy.kleinanzeigen.model.SearchResponse;
import io.github.intisy.kleinanzeigen.scraper.PlaywrightScraperEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * Direct Java client for the Kleinanzeigen scraper.
 *
 * <p>Unlike the previous HTTP client, this bypasses the local port entirely
 * and invokes the scraper engine directly in the same JVM.
 *
 * <p>Implements {@link AutoCloseable} so you can run it in a try-with-resources
 * block to ensure the Playwright browsers are properly shut down.
 *
 * @author Finn Birich
 */
public class KleinanzeigeApiClient implements AutoCloseable {

    private final PlaywrightScraperEngine engine;

    /**
     * Initializes the underlying Playwright engine.
     */
    public KleinanzeigeApiClient() {
        // Since we aren't running inside Spring Boot, we instantiate the engine directly
        this.engine = new PlaywrightScraperEngine();
        // and manually call its lifecycle init method
        try {
            this.engine.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start Playwright engine", e);
        }
    }

    /**
     * Searches ads and returns lightweight {@link AdResult} objects.
     * Sorting happens on the caller side.
     *
     * @param params search parameters
     * @return list of results
     */
    public List<AdResult> search(SearchParams params) {
        SearchResponse response = new SearchAdsCmd(engine)
                .withQuery(params.query)
                .withLocation(params.location)
                .withRadius(params.radius > 0 ? params.radius : null)
                .withMinPrice(params.minPrice > 0 ? params.minPrice : null)
                .withMaxPrice(params.maxPrice > 0 ? params.maxPrice : null)
                .withPageCount(params.pageCount > 0 ? params.pageCount : 1)
                .exec();

        List<AdResult> results = new ArrayList<>();
        if (response.getData() != null) {
            for (AdItem item : response.getData()) {
                results.add(mapAdItem(item));
            }
        }
        return results;
    }

    /**
     * Fetches full detail for a single ad by its ID.
     *
     * @param adId the ad identifier
     * @return result with all detail fields populated, or {@code null} on error
     */
    public AdResult detail(String adId) {
        DetailResponse response = new GetAdDetailCmd(engine, adId).exec();
        if (response == null || response.getData() == null) return null;
        
        AdResult r = new AdResult();
        r.adId = adId;
        mapAdDetail(response.getData(), r);
        return r;
    }

    /**
     * Searches and fetches details for every result in one round trip.
     *
     * @param params search parameters
     * @return list of fully populated results
     */
    public List<AdResult> searchDetailed(SearchParams params) {
        // 1. Search
        List<AdResult> results = search(params);

        // 2. Fetch details for each (sequentially in this simple example client, 
        // to avoid duplicating the complex Semaphore logic from the Controller)
        for (AdResult r : results) {
            DetailResponse detailResp = new GetAdDetailCmd(engine, r.adId).exec();
            if (detailResp != null && detailResp.getData() != null) {
                mapAdDetail(detailResp.getData(), r);
            }
        }
        return results;
    }

    @Override
    public void close() {
        if (engine != null) {
            engine.stop();
        }
    }

    // -------------------------------------------------------------------------
    // Internals
    // -------------------------------------------------------------------------

    private AdResult mapAdItem(AdItem item) {
        AdResult r = new AdResult();
        r.adId = item.getAdid();
        r.title = item.getTitle();
        r.priceRaw = item.getPrice();
        r.priceAmount = parseAmount(r.priceRaw);
        r.description = item.getDescription();
        r.url = item.getUrl();
        return r;
    }

    private void mapAdDetail(AdDetail d, AdResult r) {
        if (d.getTitle() != null && !d.getTitle().isEmpty()) {
            r.title = d.getTitle();
        }
        if (d.getDescription() != null && !d.getDescription().isEmpty()) {
            r.description = d.getDescription();
        }
        if (d.getPrice() != null) {
            r.priceAmount = parseAmount(d.getPrice().getAmount());
            r.priceRaw = d.getPrice().getAmount() + " " + d.getPrice().getCurrency();
            r.negotiable = d.getPrice().isNegotiable();
        }
        if (d.getExtraInfo() != null) {
            r.createdAt = d.getExtraInfo().getCreatedAt();
            r.views = parseViews(d.getExtraInfo().getViews());
        }
        if (d.getLocation() != null) {
            r.city = d.getLocation().getCity();
        }
        if (d.getSeller() != null) {
            r.sellerName = d.getSeller().getName();
        }
    }

    /** Extracts a numeric price from strings like "150 €", "VB", "Zu verschenken". */
    static double parseAmount(String raw) {
        if (raw == null || raw.isBlank()) return Double.MAX_VALUE;
        String cleaned = raw.replaceAll("[^0-9,.]", "").replace(",", ".");
        if (cleaned.isEmpty()) return Double.MAX_VALUE;
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return Double.MAX_VALUE;
        }
    }

    /** Extracts a numeric view count from strings like "1.234 Mal aufgerufen". */
    static int parseViews(String raw) {
        if (raw == null || raw.isBlank()) return 0;
        String cleaned = raw.replaceAll("[^0-9]", "");
        if (cleaned.isEmpty()) return 0;
        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
