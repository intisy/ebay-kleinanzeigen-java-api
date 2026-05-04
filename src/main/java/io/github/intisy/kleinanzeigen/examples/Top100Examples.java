package io.github.intisy.kleinanzeigen.examples;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Runnable examples that demonstrate every major API feature.
 *
 * <p><b>Prerequisites</b>: the API server must be running on port 8080.
 * Start it with:
 * <pre>
 *   ./gradlew bootRun
 * </pre>
 *
 * <p>Each public {@code top100*} method is a self-contained example that:
 * <ol>
 *   <li>Builds a {@link SearchParams} object describing the query</li>
 *   <li>Calls the appropriate API endpoint</li>
 *   <li>Sorts the results client-side</li>
 *   <li>Trims to at most 100 entries</li>
 *   <li>Prints a formatted table to stdout</li>
 * </ol>
 *
 * <p>Run {@link #main(String[])} to execute every example in sequence,
 * or call individual methods from your own code.
 *
 * @author Finn Birich
 */
public class Top100Examples {

    private static final int TOP = 100;
    private final KleinanzeigeApiClient client;

    public Top100Examples() {
        this(new KleinanzeigeApiClient());
    }

    public Top100Examples(KleinanzeigeApiClient client) {
        this.client = client;
    }

    // =========================================================================
    // EXAMPLE 1 — Top 100 newest listings (requires detail endpoint for date)
    // =========================================================================

    /**
     * Top 100 <em>newest</em> laptop listings.
     *
     * <p>Uses {@code /inserate-detailed} so each result carries a
     * {@code created_at} timestamp from the detail page.
     * Results are sorted descending by that timestamp string
     * (ISO-8601 strings sort lexicographically = chronologically).
     */
    public List<AdResult> top100Newest(String product) {
        SearchParams p = SearchParams.of(product);
        p.pageCount = 3; // detailed endpoint is limited to 1–3 pages
        p.maxConcurrentDetails = 10;

        List<AdResult> results = client.searchDetailed(p);

        results.sort(Comparator.comparing(
                (AdResult r) -> r.createdAt == null ? "" : r.createdAt
        ).reversed());

        List<AdResult> top = top(results);
        print("Top " + top.size() + " NEWEST \"" + product + "\" listings", top, false);
        return top;
    }

    // =========================================================================
    // EXAMPLE 2 — Top 100 oldest listings
    // =========================================================================

    /**
     * Top 100 <em>oldest</em> laptop listings still active on the platform.
     *
     * <p>Same as {@link #top100Newest} but sorted ascending — oldest first.
     * These are often long-standing, hard-to-sell items or collectibles.
     */
    public List<AdResult> top100Oldest(String product) {
        SearchParams p = SearchParams.of(product);
        p.pageCount = 3;
        p.maxConcurrentDetails = 10;

        List<AdResult> results = client.searchDetailed(p);

        results.sort(Comparator.comparing(
                (AdResult r) -> r.createdAt == null ? "" : r.createdAt
        ));

        List<AdResult> top = top(results);
        print("Top " + top.size() + " OLDEST \"" + product + "\" listings", top, false);
        return top;
    }

    // =========================================================================
    // EXAMPLE 3 — Top 100 cheapest listings
    // =========================================================================

    /**
     * Top 100 <em>cheapest</em> listings for a product.
     *
     * <p>Uses {@code /inserate} (lightweight, no detail fetch needed) and
     * sorts ascending by the parsed numeric price.
     * Ads without a numeric price (VB, Zu verschenken, …) are placed last.
     */
    public List<AdResult> top100Cheapest(String product) {
        SearchParams p = SearchParams.of(product);
        p.pageCount = 4;

        List<AdResult> results = client.search(p);
        results.sort(Comparator.comparingDouble(r -> r.priceAmount));

        List<AdResult> top = top(results);
        print("Top " + top.size() + " CHEAPEST \"" + product + "\" listings", top, true);
        return top;
    }

    // =========================================================================
    // EXAMPLE 4 — Top 100 most expensive listings
    // =========================================================================

    /**
     * Top 100 <em>most expensive</em> listings for a product.
     *
     * <p>Ads with non-numeric prices (VB, free) are excluded since they have
     * no comparable value.
     */
    public List<AdResult> top100MostExpensive(String product) {
        SearchParams p = SearchParams.of(product);
        p.pageCount = 4;

        List<AdResult> results = client.search(p);

        // exclude non-numeric prices
        results = results.stream()
                .filter(r -> r.priceAmount < Double.MAX_VALUE)
                .collect(Collectors.toList());

        results.sort(Comparator.comparingDouble((AdResult r) -> r.priceAmount).reversed());

        List<AdResult> top = top(results);
        print("Top " + top.size() + " MOST EXPENSIVE \"" + product + "\" listings", top, true);
        return top;
    }

    // =========================================================================
    // EXAMPLE 5 — Top 100 most viewed listings
    // =========================================================================

    /**
     * Top 100 <em>most viewed</em> listings — a rough proxy for popularity.
     *
     * <p>View counts come from the detail page, so this uses
     * {@code /inserate-detailed}.
     */
    public List<AdResult> top100MostViewed(String product) {
        SearchParams p = SearchParams.of(product);
        p.pageCount = 3;
        p.maxConcurrentDetails = 10;

        List<AdResult> results = client.searchDetailed(p);
        results.sort(Comparator.comparingInt((AdResult r) -> r.views).reversed());

        List<AdResult> top = top(results);
        print("Top " + top.size() + " MOST VIEWED \"" + product + "\" listings", top, false);
        return top;
    }

    // =========================================================================
    // EXAMPLE 6 — Top 100 cheapest in a price band (e.g. €50–€200 laptops)
    // =========================================================================

    /**
     * Top 100 cheapest listings within a specific price band.
     *
     * <p>The price filter is applied <em>server-side</em> via the API's
     * {@code minPrice}/{@code maxPrice} parameters, which builds the correct
     * Kleinanzeigen URL segment {@code /preis:{min}:{max}}.
     *
     * @param product  search keyword
     * @param minPrice lower bound in euros (inclusive)
     * @param maxPrice upper bound in euros (inclusive)
     */
    public List<AdResult> top100CheapestInRange(String product, int minPrice, int maxPrice) {
        SearchParams p = SearchParams.of(product);
        p.pageCount = 4;
        p.minPrice = minPrice;
        p.maxPrice = maxPrice;

        List<AdResult> results = client.search(p);
        results.sort(Comparator.comparingDouble(r -> r.priceAmount));

        List<AdResult> top = top(results);
        print(String.format("Top %d CHEAPEST \"%s\" listings (€%d–€%d)",
                top.size(), product, minPrice, maxPrice), top, true);
        return top;
    }

    // =========================================================================
    // EXAMPLE 7 — Top 100 listings near a location
    // =========================================================================

    /**
     * Top 100 listings near a given city, sorted by price ascending.
     *
     * <p>Uses the {@code location} and {@code radius} parameters to restrict
     * results geographically. Useful for finding local deals.
     *
     * @param product  search keyword
     * @param location city name, e.g. {@code "München"}
     * @param radius   search radius in km
     */
    public List<AdResult> top100NearLocation(String product, String location, int radius) {
        SearchParams p = SearchParams.of(product);
        p.pageCount = 4;
        p.location = location;
        p.radius = radius;

        List<AdResult> results = client.search(p);
        results.sort(Comparator.comparingDouble(r -> r.priceAmount));

        List<AdResult> top = top(results);
        print(String.format("Top %d CHEAPEST \"%s\" within %d km of %s",
                top.size(), product, radius, location), top, true);
        return top;
    }

    // =========================================================================
    // EXAMPLE 8 — Top 100 free / give-away listings
    // =========================================================================

    /**
     * Top 100 free ("Zu verschenken") listings for a keyword.
     *
     * <p>Kleinanzeigen represents free items with a price of {@code 0} or the
     * string {@code "Zu verschenken"}.  This example keeps only zero-price ads.
     *
     * @param product search keyword, or {@code ""} for all categories
     */
    public List<AdResult> top100Free(String product) {
        SearchParams p = SearchParams.of(product);
        p.pageCount = 4;
        p.maxPrice = 0; // "Zu verschenken" filter

        List<AdResult> results = client.search(p);

        // Belt-and-suspenders: also include any ad the parser resolved to 0.0
        results = results.stream()
                .filter(r -> r.priceAmount == 0.0
                        || (r.priceRaw != null && r.priceRaw.toLowerCase().contains("verschenken")))
                .collect(Collectors.toList());

        List<AdResult> top = top(results);
        print("Top " + top.size() + " FREE \"" + product + "\" listings", top, true);
        return top;
    }

    // =========================================================================
    // EXAMPLE 9 — Top 100 negotiable listings sorted by price
    // =========================================================================

    /**
     * Top 100 listings where the seller marked the price as negotiable (VB).
     *
     * <p>Negotiable flag comes from the detail page, so this uses
     * {@code /inserate-detailed}. Results are sorted cheapest first —
     * the real price may be even lower after negotiation.
     */
    public List<AdResult> top100Negotiable(String product) {
        SearchParams p = SearchParams.of(product);
        p.pageCount = 3;
        p.maxConcurrentDetails = 10;

        List<AdResult> results = client.searchDetailed(p);

        results = results.stream()
                .filter(r -> r.negotiable)
                .collect(Collectors.toList());

        results.sort(Comparator.comparingDouble(r -> r.priceAmount));

        List<AdResult> top = top(results);
        print("Top " + top.size() + " NEGOTIABLE (VB) \"" + product + "\" listings", top, true);
        return top;
    }

    // =========================================================================
    // EXAMPLE 10 — Single ad detail lookup
    // =========================================================================

    /**
     * Fetches and prints the full detail for a single known ad ID.
     *
     * <p>Use this when you already have an {@code adId} from a search result
     * and want all fields (description, features, seller, location, date, views).
     *
     * @param adId Kleinanzeigen ad ID, e.g. {@code "1234567890"}
     */
    public AdResult fetchSingleAd(String adId) {
        AdResult r = client.detail(adId);
        if (r == null) {
            System.out.println("Ad " + adId + " not found or server error.");
            return null;
        }
        System.out.println("\n=== Ad Detail: " + adId + " ===");
        System.out.printf("  Title      : %s%n", r.title);
        System.out.printf("  Price      : %s%s%n", r.priceRaw, r.negotiable ? " (VB)" : "");
        System.out.printf("  City       : %s%n", r.city);
        System.out.printf("  Seller     : %s%n", r.sellerName);
        System.out.printf("  Created at : %s%n", r.createdAt);
        System.out.printf("  Views      : %d%n", r.views);
        System.out.printf("  Description: %s%n",
                r.description != null && r.description.length() > 120
                        ? r.description.substring(0, 120) + "…" : r.description);
        return r;
    }

    // =========================================================================
    // Main — run all examples
    // =========================================================================

    /**
     * Runs every example in sequence using "laptop" as the demo product.
     *
     * <p>The server must be running before calling this method.
     * Each example prints its own header and result table.
     */
    public static void main(String[] args) {
        String product = args.length > 0 ? args[0] : "laptop";
        Top100Examples ex = new Top100Examples();

        System.out.println("=================================================");
        System.out.println("  Kleinanzeigen API Examples — product: " + product);
        System.out.println("=================================================\n");

        // 1. Cheapest (no detail needed — fastest)
        ex.top100Cheapest(product);

        // 2. Most expensive
        ex.top100MostExpensive(product);

        // 3. In a price band (€50–€300)
        ex.top100CheapestInRange(product, 50, 300);

        // 4. Free give-aways
        ex.top100Free(product);

        // 5. Near a location (Berlin, 50 km radius)
        ex.top100NearLocation(product, "Berlin", 50);

        // 6. Newest (detail endpoint needed for dates)
        ex.top100Newest(product);

        // 7. Oldest
        ex.top100Oldest(product);

        // 8. Most viewed
        ex.top100MostViewed(product);

        // 9. Negotiable / VB
        ex.top100Negotiable(product);

        System.out.println("\nAll examples completed.");
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private static List<AdResult> top(List<AdResult> list) {
        return list.size() <= TOP ? new ArrayList<>(list) : list.subList(0, TOP);
    }

    private static void print(String header, List<AdResult> results, boolean showPrice) {
        System.out.println("\n--- " + header + " ---");
        System.out.printf("%-6s  %-55s  %s%n",
                "Rank", "Title", showPrice ? "Price" : "Created / Views");
        System.out.println("-".repeat(80));
        int rank = 1;
        for (AdResult r : results) {
            String col2 = showPrice
                    ? (r.priceRaw != null ? r.priceRaw : "—")
                    : (r.createdAt != null && !r.createdAt.isEmpty()
                            ? r.createdAt + " (" + r.views + " views)"
                            : "—");
            String title = r.title != null && r.title.length() > 55
                    ? r.title.substring(0, 52) + "…" : r.title;
            System.out.printf("%-6d  %-55s  %s%n", rank++, title, col2);
        }
        System.out.println("-".repeat(80));
        System.out.printf("Total shown: %d%n", results.size());
    }
}
