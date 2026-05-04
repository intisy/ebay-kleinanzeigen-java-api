package io.github.intisy.kleinanzeigen.examples;

/**
 * Example: Fetch the most viewed listings (requires detail fetching).
 */
public class MostViewedExample {
    public static void main(String[] args) {
        String product = args.length > 0 ? args[0] : "laptop";
        KleinanzeigeExamples ex = new KleinanzeigeExamples();
        ex.mostViewed(product, 100);
    }
}
