package io.github.intisy.kleinanzeigen.examples;

/**
 * Fluent parameters object for API search calls.
 *
 * <p>All fields have safe defaults (0 / null / empty) so only the fields you
 * care about need to be set.
 *
 * @author Finn Birich
 */
public class SearchParams {

    /** Free-text search keyword(s), e.g. {@code "laptop"}. */
    public String query;

    /** Location string forwarded to Kleinanzeigen, e.g. {@code "Berlin"}. */
    public String location;

    /** Search radius in kilometres around {@link #location}. 0 = no radius. */
    public int radius;

    /** Minimum price in euros (inclusive). 0 = no lower bound. */
    public int minPrice;

    /** Maximum price in euros (inclusive). 0 = no upper bound. */
    public int maxPrice;

    /**
     * Number of result pages to scrape (each page ≈ 25 listings).
     * Defaults to 4 so 100 results can be obtained in one call.
     */
    public int pageCount = 4;

    /**
     * Maximum number of concurrent detail fetches for
     * {@link KleinanzeigeApiClient#searchDetailed}.
     * 0 = use server default (5).
     */
    public int maxConcurrentDetails;

    /** Creates a params object with only the keyword set. */
    public static SearchParams of(String query) {
        SearchParams p = new SearchParams();
        p.query = query;
        return p;
    }

    /** Returns a copy of this params object limited to {@code pages} pages. */
    public SearchParams withPages(int pages) {
        SearchParams copy = new SearchParams();
        copy.query = this.query;
        copy.location = this.location;
        copy.radius = this.radius;
        copy.minPrice = this.minPrice;
        copy.maxPrice = this.maxPrice;
        copy.pageCount = pages;
        copy.maxConcurrentDetails = this.maxConcurrentDetails;
        return copy;
    }
}
