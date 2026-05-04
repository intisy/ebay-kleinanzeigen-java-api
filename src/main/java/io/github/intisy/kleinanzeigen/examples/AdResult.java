package io.github.intisy.kleinanzeigen.examples;

/**
 * A single search or detail result returned by {@link KleinanzeigeApiClient}.
 *
 * <p>Price, date, and view count fields are pre-parsed into sortable types so
 * the example programs can sort with simple {@link java.util.Comparator}s.
 *
 * @author Finn Birich
 */
public class AdResult {

    /** Kleinanzeigen ad ID (numeric string). */
    public String adId;

    /** Ad title. */
    public String title;

    /** Raw price string as returned by the API, e.g. {@code "150 €"} or {@code "VB"}. */
    public String priceRaw;

    /**
     * Numeric price in euros parsed from {@link #priceRaw}.
     * Ads without a numeric price (VB, free, …) get {@link Double#MAX_VALUE}
     * so they sort last in ascending order.
     */
    public double priceAmount;

    /** {@code true} if the seller marked the price as negotiable. */
    public boolean negotiable;

    /** Short description snippet from the listing card. */
    public String description;

    /** Relative URL path of the ad on kleinanzeigen.de. */
    public String url;

    /** ISO-8601-like creation date string, e.g. {@code "2024-03-15"}. */
    public String createdAt;

    /** Number of page views (parsed from the detail page). */
    public int views;

    /** City / district of the seller. */
    public String city;

    /** Display name of the seller. */
    public String sellerName;

    @Override
    public String toString() {
        return String.format("AdResult{adId='%s', title='%s', price='%s', createdAt='%s', views=%d}",
                adId, title, priceRaw, createdAt, views);
    }
}
