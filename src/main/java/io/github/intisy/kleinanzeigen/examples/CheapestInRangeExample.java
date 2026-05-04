package io.github.intisy.kleinanzeigen.examples;

/**
 * Example: Fetch the cheapest listings within a specific price range.
 */
public class CheapestInRangeExample {
    public static void main(String[] args) {
        String product = args.length > 0 ? args[0] : "laptop";
        try (KleinanzeigeApiClient client = new KleinanzeigeApiClient()) { KleinanzeigeExamples ex = new KleinanzeigeExamples(client);
        ex.cheapestInRange(product, 50, 300, 100); }
    }
}
