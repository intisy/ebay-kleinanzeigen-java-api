package io.github.intisy.kleinanzeigen.lib;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import io.github.intisy.kleinanzeigen.model.AdItem;
import io.github.intisy.kleinanzeigen.model.ExtraInfo;
import io.github.intisy.kleinanzeigen.model.Location;
import io.github.intisy.kleinanzeigen.model.PriceInfo;
import io.github.intisy.kleinanzeigen.model.SellerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Static utility class that extracts structured data from Kleinanzeigen pages
 * using CSS selectors. Mirrors the Python kleinanzeigen.py extractor logic.
 * All methods fail silently (log warning, return empty/default) on error.
 *
 * @author Finn Birich
 */
public class KleinanzeigeExtractor {
    private static final Logger log = LoggerFactory.getLogger(KleinanzeigeExtractor.class);

    private KleinanzeigeExtractor() {}

    /**
     * Extracts a list of ad items from a search result page.
     *
     * @param page the Playwright page loaded with a search result
     * @return list of {@link AdItem}, never null
     */
    public static List<AdItem> extractAdItems(Page page) {
        List<AdItem> results = new ArrayList<>();
        try {
            List<ElementHandle> articles = page.querySelectorAll(
                    ".ad-listitem:not(.is-topad):not(.badge-hint-pro-small-srp) article[data-adid]");
            for (ElementHandle article : articles) {
                try {
                    String adid = article.getAttribute("data-adid");
                    String href = article.getAttribute("data-href");
                    if (adid == null || adid.isEmpty() || href == null || href.isEmpty()) {
                        continue;
                    }
                    String url = "https://www.kleinanzeigen.de" + href;

                    String title = "";
                    ElementHandle titleEl = article.querySelector("h2.text-module-begin a.ellipsis");
                    if (titleEl != null) {
                        title = titleEl.innerText().trim();
                    }

                    String price = "";
                    ElementHandle priceEl = article.querySelector("p.aditem-main--middle--price-shipping--price");
                    if (priceEl != null) {
                        price = priceEl.innerText()
                                .replace("€", "")
                                .replace("VB", "")
                                .replace(".", "")
                                .trim();
                    }

                    String description = "";
                    ElementHandle descEl = article.querySelector("p.aditem-main--middle--description");
                    if (descEl != null) {
                        description = descEl.innerText().trim();
                    }

                    results.add(new AdItem(adid, url, title, price, description));
                } catch (Exception e) {
                    log.warn("Failed to extract ad item: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("extractAdItems failed: {}", e.getMessage());
        }
        return results;
    }

    /**
     * Extracts seller information from an ad detail page.
     *
     * @param page the Playwright page loaded with the ad detail
     * @return {@link SellerInfo}, never null
     */
    public static SellerInfo extractSellerInfo(Page page) {
        try {
            String name = "";
            ElementHandle nameEl = page.querySelector(".userprofile-vip");
            if (nameEl != null) {
                name = nameEl.innerText().trim();
            }

            String type = "private";
            String since = "";
            ElementHandle detailsEl = page.querySelector(".userprofile-vip-details-text");
            if (detailsEl != null) {
                String detailsText = detailsEl.innerText();
                if (detailsText.contains("Gewerblicher")) {
                    type = "business";
                }
                if (detailsText.contains("Aktiv seit")) {
                    since = detailsText.replace("Aktiv seit", "").trim();
                }
            }

            List<String> badges = new ArrayList<>();
            List<ElementHandle> badgeEls = page.querySelectorAll(".userprofile-vip-badges .userbadge-tag");
            for (ElementHandle badgeEl : badgeEls) {
                String badge = badgeEl.innerText().trim();
                if (!badge.isEmpty()) {
                    badges.add(badge);
                }
            }

            return new SellerInfo(name, since, type, badges);
        } catch (Exception e) {
            log.warn("extractSellerInfo failed: {}", e.getMessage());
            return new SellerInfo("", "", "private", new ArrayList<>());
        }
    }

    /**
     * Extracts ad detail key-value pairs from an ad detail page.
     *
     * @param page the Playwright page loaded with the ad detail
     * @return map of label to value, never null
     */
    public static Map<String, String> extractDetails(Page page) {
        Map<String, String> details = new HashMap<>();
        try {
            List<ElementHandle> detailEls = page.querySelectorAll(
                    "#viewad-details .addetailslist--detail");
            for (ElementHandle detailEl : detailEls) {
                try {
                    String value = "";
                    ElementHandle valueEl = detailEl.querySelector(".addetailslist--detail--value");
                    if (valueEl != null) {
                        value = valueEl.innerText().trim();
                    }
                    String fullText = detailEl.innerText().trim();
                    String label = fullText.replace(value, "").trim();
                    if (!label.isEmpty()) {
                        details.put(label, value);
                    }
                } catch (Exception e) {
                    log.warn("Failed to extract detail entry: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("extractDetails failed: {}", e.getMessage());
        }
        return details;
    }

    /**
     * Extracts feature tags from an ad detail page.
     *
     * @param page the Playwright page loaded with the ad detail
     * @return list of feature strings, never null
     */
    public static List<String> extractFeatures(Page page) {
        List<String> features = new ArrayList<>();
        try {
            List<ElementHandle> featureEls = page.querySelectorAll(
                    "#viewad-configuration .checktaglist .checktag");
            for (ElementHandle featureEl : featureEls) {
                String text = featureEl.innerText().trim();
                if (!text.isEmpty()) {
                    features.add(text);
                }
            }
        } catch (Exception e) {
            log.warn("extractFeatures failed: {}", e.getMessage());
        }
        return features;
    }

    /**
     * Extracts the location from an ad detail page.
     * Parses format: "12345 Berlin - Mitte" → zip="12345", state="Berlin", city="Mitte"
     *
     * @param page the Playwright page loaded with the ad detail
     * @return {@link Location}, never null
     */
    public static Location extractLocation(Page page) {
        try {
            ElementHandle localityEl = page.querySelector("#viewad-locality");
            if (localityEl == null) {
                return new Location("", "", "");
            }
            String raw = localityEl.innerText().trim();
            // Format: "12345 StateName - CityName"
            String zip = "";
            String state = "";
            String city = "";
            String[] dashParts = raw.split(" - ", 2);
            if (dashParts.length == 2) {
                city = dashParts[1].trim();
                String[] spaceParts = dashParts[0].trim().split(" ", 2);
                if (spaceParts.length == 2) {
                    zip = spaceParts[0].trim();
                    state = spaceParts[1].trim();
                } else {
                    zip = spaceParts[0].trim();
                }
            } else {
                String[] spaceParts = raw.split(" ", 2);
                if (spaceParts.length == 2) {
                    zip = spaceParts[0].trim();
                    state = spaceParts[1].trim();
                }
            }
            return new Location(zip, city, state);
        } catch (Exception e) {
            log.warn("extractLocation failed: {}", e.getMessage());
            return new Location("", "", "");
        }
    }

    /**
     * Extracts extra info (creation date, view count) from an ad detail page.
     *
     * @param page the Playwright page loaded with the ad detail
     * @return {@link ExtraInfo}, never null
     */
    public static ExtraInfo extractExtraInfo(Page page) {
        try {
            String createdAt = "";
            ElementHandle createdEl = page.querySelector("#viewad-extra-info > div:nth-child(1) > span");
            if (createdEl != null) {
                createdAt = createdEl.innerText().trim();
            }

            String views = "";
            ElementHandle viewsEl = page.querySelector("#viewad-cntr-num");
            if (viewsEl != null) {
                views = viewsEl.innerText().trim();
            }

            return new ExtraInfo(createdAt, views);
        } catch (Exception e) {
            log.warn("extractExtraInfo failed: {}", e.getMessage());
            return new ExtraInfo("", "");
        }
    }

    /**
     * Parses a raw price string into a structured {@link PriceInfo}.
     *
     * @param priceText raw price text (may be null)
     * @return {@link PriceInfo}, never null
     */
    public static PriceInfo parsePrice(String priceText) {
        if (priceText == null || priceText.trim().isEmpty()) {
            return new PriceInfo("0", "EUR", false);
        }
        boolean negotiable = priceText.contains("VB");
        String amount = priceText
                .replace("VB", "")
                .replace("€", "")
                .replace(".", "")
                .replace(",", ".")
                .trim();
        // Remove any remaining non-numeric characters except dot
        amount = amount.replaceAll("[^0-9.]", "").trim();
        if (amount.isEmpty()) {
            amount = "0";
        }
        // Strip trailing dot
        if (amount.endsWith(".")) {
            amount = amount.substring(0, amount.length() - 1);
        }
        return new PriceInfo(amount, "EUR", negotiable);
    }
}
