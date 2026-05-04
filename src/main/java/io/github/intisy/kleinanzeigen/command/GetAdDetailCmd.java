package io.github.intisy.kleinanzeigen.command;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import io.github.intisy.kleinanzeigen.exception.ScrapingException;
import io.github.intisy.kleinanzeigen.lib.KleinanzeigeExtractor;
import io.github.intisy.kleinanzeigen.model.AdDetail;
import io.github.intisy.kleinanzeigen.model.DetailResponse;
import io.github.intisy.kleinanzeigen.model.ExtraInfo;
import io.github.intisy.kleinanzeigen.model.Location;
import io.github.intisy.kleinanzeigen.model.PerformanceMetrics;
import io.github.intisy.kleinanzeigen.model.PriceInfo;
import io.github.intisy.kleinanzeigen.model.SellerInfo;
import io.github.intisy.kleinanzeigen.scraper.PlaywrightScraperEngine;
import io.github.intisy.kleinanzeigen.worker.PlaywrightWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Fluent command for fetching detailed information for a single ad.
 *
 * @author Finn Birich
 */
public class GetAdDetailCmd {
    private static final Logger log = LoggerFactory.getLogger(GetAdDetailCmd.class);

    private final PlaywrightScraperEngine engine;
    private final String adId;

    public GetAdDetailCmd(PlaywrightScraperEngine engine, String adId) {
        this.engine = engine;
        this.adId = adId;
    }

    /**
     * Executes the detail fetch command.
     *
     * @return {@link DetailResponse} with full ad detail and metrics
     * @throws ScrapingException if the page does not load the details section
     */
    public DetailResponse exec() {
        long startTime = System.currentTimeMillis();
        String url = "https://www.kleinanzeigen.de/s-anzeige/" + adId;
        PlaywrightWorker worker = engine.nextWorker();

        try {
            AdDetail detail = worker.submit(() -> {
                BrowserContext context = worker.newContext();
                try {
                    Page page = context.newPage();
                    try {
                        page.navigate(url);
                        try {
                            page.waitForSelector("#viewad-details", new Page.WaitForSelectorOptions()
                                    .setTimeout(10000));
                        } catch (Exception e) {
                            throw new ScrapingException("Page did not load details section for adId=" + adId, e);
                        }

                        Map<String, String> details = KleinanzeigeExtractor.extractDetails(page);
                        List<String> features = KleinanzeigeExtractor.extractFeatures(page);
                        Location location = KleinanzeigeExtractor.extractLocation(page);
                        SellerInfo seller = KleinanzeigeExtractor.extractSellerInfo(page);
                        ExtraInfo extraInfo = KleinanzeigeExtractor.extractExtraInfo(page);

                        // Extract description from page
                        String description = "";
                        com.microsoft.playwright.ElementHandle descEl = page.querySelector("#viewad-description-text");
                        if (descEl != null) {
                            description = descEl.innerText().trim();
                        }

                        // Extract title
                        String title = "";
                        com.microsoft.playwright.ElementHandle titleEl = page.querySelector("#viewad-title");
                        if (titleEl != null) {
                            title = titleEl.innerText().trim();
                        }

                        // Extract price
                        String rawPrice = "";
                        com.microsoft.playwright.ElementHandle priceEl = page.querySelector("#viewad-price");
                        if (priceEl != null) {
                            rawPrice = priceEl.innerText().trim();
                        }
                        PriceInfo priceInfo = KleinanzeigeExtractor.parsePrice(rawPrice);

                        return new AdDetail(title, description, priceInfo, details, features, location, seller, extraInfo);
                    } finally {
                        page.close();
                    }
                } finally {
                    context.close();
                }
            }).get();

            double timeTaken = (System.currentTimeMillis() - startTime) / 1000.0;
            PerformanceMetrics metrics = new PerformanceMetrics(1, 1, 1.0, timeTaken);
            return new DetailResponse(true, timeTaken, detail, metrics);

        } catch (ScrapingException e) {
            throw e;
        } catch (Exception e) {
            throw new ScrapingException("Failed to fetch ad detail for adId=" + adId, e);
        }
    }

    @Override
    public String toString() {
        return "GetAdDetailCmd{adId='" + adId + "'}";
    }
}
