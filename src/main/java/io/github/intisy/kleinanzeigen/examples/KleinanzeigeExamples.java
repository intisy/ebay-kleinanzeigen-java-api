package io.github.intisy.kleinanzeigen.examples;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Runnable examples demonstrating every major API feature.
 *
 * <p><b>Prerequisites</b>: the API server must be running on port 8080.
 * Start it with:
 * <pre>
 *   ./gradlew bootRun
 * </pre>
 *
 * <p>Run {@link #main(String[])} to execute every example in sequence,
 * or call individual methods from your own code.
 *
 * @author Finn Birich
 */
public class KleinanzeigeExamples {

    private static final int LIMIT = 100;
    private final KleinanzeigeApiClient client;

    public KleinanzeigeExamples() {
        this(new KleinanzeigeApiClient());
    }

    public KleinanzeigeExamples(KleinanzeigeApiClient client) {
        this.client = client;
    }

    /**
     * Top N <em>newest</em> listings for a product.
     *
     * <p>Uses {@code /inserate-detailed} so each result carries a
     * {@code created_at} timestamp from the detail page.
     * Results are sorted descending by that timestamp string
     * (ISO-8601 strings sort lexicographically = chronologically).
     *
     * @param product search keyword
     * @param limit   maximum number of results to return
     */
    public List<AdResult> newest(String product, int limit) {
        SearchParams p = SearchParams.of(product);
        p.pageCount = 3;
        p.maxConcurrentDetails = 10;

        List<AdResult> results = client.searchDetailed(p);
        results.sort(Comparator.comparing(
                (AdResult r) -> r.createdAt == null ? "" : r.createdAt
        ).reversed());

        List<AdResult> top = limit(results, limit);
        print("Top " + top.size() + " NEWEST \"" + product + "\" listings", top, false);
        return top;
    }

    /**
     * Top N <em>oldest</em> listings still active on the platform.
     *
     * <p>Same as {@link #newest} but sorted ascending — oldest first.
     *
     * @param product search keyword
     * @param limit   maximum number of results to return
     */
    public List<AdResult> oldest(String product, int limit) {
        SearchParams p = SearchParams.of(product);
        p.pageCount = 3;
        p.maxConcurrentDetails = 10;

        List<AdResult> results = client.searchDetailed(p);
        results.sort(Comparator.comparing(
                (AdResult r) -> r.createdAt == null ? "" : r.createdAt
        ));

        List<AdResult> top = limit(results, limit);
        print("Top " + top.size() + " OLDEST \"" + product + "\" listings", top, false);
        return top;
    }

    /**
     * Top N <em>cheapest</em> listings for a product.
     *
     * <p>Uses {@code /inserate} (no detail fetch needed) and sorts ascending
     * by parsed numeric price. Ads without a numeric price (VB, Zu verschenken, …)
     * are placed last.
     *
     * @param product search keyword
     * @param limit   maximum number of results to return
     */
    public List<AdResult> cheapest(String product, int limit) {
        SearchParams p = SearchParams.of(product);
        p.pageCount = 4;

        List<AdResult> results = client.search(p);
        results.sort(Comparator.comparingDouble(r -> r.priceAmount));

        List<AdResult> top = limit(results, limit);
        print("Top " + top.size() + " CHEAPEST \"" + product + "\" listings", top, true);
        return top;
    }

    /**
     * Top N <em>most expensive</em> listings for a product.
     *
     * <p>Ads with non-numeric prices (VB, free) are excluded since they have
     * no comparable value.
     *
     * @param product search keyword
     * @param limit   maximum number of results to return
     */
    public List<AdResult> mostExpensive(String product, int limit) {
        SearchParams p = SearchParams.of(product);
        p.pageCount = 4;

        List<AdResult> results = client.search(p);
        results = results.stream()
                .filter(r -> r.priceAmount < Double.MAX_VALUE)
                .collect(Collectors.toList());
        results.sort(Comparator.comparingDouble((AdResult r) -> r.priceAmount).reversed());

        List<AdResult> top = limit(results, limit);
        print("Top " + top.size() + " MOST EXPENSIVE \"" + product + "\" listings", top, true);
        return top;
    }

    /**
     * Top N <em>most viewed</em> listings — a rough proxy for popularity.
     *
     * <p>View counts come from the detail page, so this uses
     * {@code /inserate-detailed}.
     *
     * @param product search keyword
     * @param limit   maximum number of results to return
     */
    public List<AdResult> mostViewed(String product, int limit) {
        SearchParams p = SearchParams.of(product);
        p.pageCount = 3;
        p.maxConcurrentDetails = 10;

        List<AdResult> results = client.searchDetailed(p);
        results.sort(Comparator.comparingInt((AdResult r) -> r.views).reversed());

        List<AdResult> top = limit(results, limit);
        print("Top " + top.size() + " MOST VIEWED \"" + product + "\" listings", top, false);
        return top;
    }

    /**
     * Top N cheapest listings within a specific price band.
     *
     * <p>The price filter is applied <em>server-side</em> via the API's
     * {@code minPrice}/{@code maxPrice} parameters, which builds the correct
     * Kleinanzeigen URL segment {@code /preis:{min}:{max}}.
     *
     * @param product  search keyword
     * @param minPrice lower bound in euros (inclusive)
     * @param maxPrice upper bound in euros (inclusive)
     * @param limit    maximum number of results to return
     */
    public List<AdResult> cheapestInRange(String product, int minPrice, int maxPrice, int limit) {
        SearchParams p = SearchParams.of(product);
        p.pageCount = 4;
        p.minPrice = minPrice;
        p.maxPrice = maxPrice;

        List<AdResult> results = client.search(p);
        results.sort(Comparator.comparingDouble(r -> r.priceAmount));

        List<AdResult> top = limit(results, limit);
        print(String.format("Top %d CHEAPEST \"%s\" listings (€%d–€%d)",
                top.size(), product, minPrice, maxPrice), top, true);
        return top;
    }

    /**
     * Top N cheapest listings near a given city.
     *
     * <p>Uses the {@code location} and {@code radius} parameters to restrict
     * results geographically.
     *
     * @param product  search keyword
     * @param location city name, e.g. {@code "München"}
     * @param radius   search radius in km
     * @param limit    maximum number of results to return
     */
    public List<AdResult> nearLocation(String product, String location, int radius, int limit) {
        SearchParams p = SearchParams.of(product);
        p.pageCount = 4;
        p.location = location;
        p.radius = radius;

        List<AdResult> results = client.search(p);
        results.sort(Comparator.comparingDouble(r -> r.priceAmount));

        List<AdResult> top = limit(results, limit);
        print(String.format("Top %d CHEAPEST \"%s\" within %d km of %s",
                top.size(), product, radius, location), top, true);
        return top;
    }

    /**
     * Top N free ("Zu verschenken") listings for a keyword.
     *
     * <p>Kleinanzeigen represents free items with a price of {@code 0} or the
     * string {@code "Zu verschenken"}. This example keeps only zero-price ads.
     *
     * @param product search keyword, or {@code ""} for all categories
     * @param limit   maximum number of results to return
     */
    public List<AdResult> free(String product, int limit) {
        SearchParams p = SearchParams.of(product);
        p.pageCount = 4;
        p.maxPrice = 0;

        List<AdResult> results = client.search(p);
        results = results.stream()
                .filter(r -> r.priceAmount == 0.0
                        || (r.priceRaw != null && r.priceRaw.toLowerCase().contains("verschenken")))
                .collect(Collectors.toList());

        List<AdResult> top = limit(results, limit);
        print("Top " + top.size() + " FREE \"" + product + "\" listings", top, true);
        return top;
    }

    /**
     * Top N listings where the seller marked the price as negotiable (VB),
     * sorted cheapest first.
     *
     * <p>The negotiable flag comes from the detail page, so this uses
     * {@code /inserate-detailed}.
     *
     * @param product search keyword
     * @param limit   maximum number of results to return
     */
    public List<AdResult> negotiable(String product, int limit) {
        SearchParams p = SearchParams.of(product);
        p.pageCount = 3;
        p.maxConcurrentDetails = 10;

        List<AdResult> results = client.searchDetailed(p);
        results = results.stream()
                .filter(r -> r.negotiable)
                .collect(Collectors.toList());
        results.sort(Comparator.comparingDouble(r -> r.priceAmount));

        List<AdResult> top = limit(results, limit);
        print("Top " + top.size() + " NEGOTIABLE (VB) \"" + product + "\" listings", top, true);
        return top;
    }

    /**
     * Fetches and prints the full detail for a single known ad ID.
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

    /**
     * Runs every example in sequence using {@code "laptop"} as the demo product.
     *
     * <p>The server must be running before calling this method.
     */
    public static void main(String[] args) {
        String product = args.length > 0 ? args[0] : "laptop";
        KleinanzeigeExamples ex = new KleinanzeigeExamples();

        System.out.println("=================================================");
        System.out.println("  Kleinanzeigen API Examples — product: " + product);
        System.out.println("=================================================\n");

        ex.cheapest(product, LIMIT);
        ex.mostExpensive(product, LIMIT);
        ex.cheapestInRange(product, 50, 300, LIMIT);
        ex.free(product, LIMIT);
        ex.nearLocation(product, "Berlin", 50, LIMIT);
        ex.newest(product, LIMIT);
        ex.oldest(product, LIMIT);
        ex.mostViewed(product, LIMIT);
        ex.negotiable(product, LIMIT);

        System.out.println("\nAll examples completed.");
    }

    private static List<AdResult> limit(List<AdResult> list, int n) {
        return list.size() <= n ? new ArrayList<>(list) : new ArrayList<>(list.subList(0, n));
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
