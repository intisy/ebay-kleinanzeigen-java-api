package io.github.intisy.kleinanzeigen.examples;

/**
 * Example: Fetch the most viewed listings (requires detail fetching).
 */
public class MostViewedExample {
    public static void main(String[] args) {
        String product = args.length > 0 ? args[0] : "laptop";
        try (KleinanzeigeApiClient client = new KleinanzeigeApiClient()) { KleinanzeigeExamples ex = new KleinanzeigeExamples(client);
        ex.mostViewed(product, 100); }
    }
}
