package io.github.intisy.kleinanzeigen.examples;

/**
 * Example: Fetch the cheapest listings for a product.
 */
public class CheapestExample {
    public static void main(String[] args) {
        String product = args.length > 0 ? args[0] : "laptop";
        try (KleinanzeigeApiClient client = new KleinanzeigeApiClient()) { KleinanzeigeExamples ex = new KleinanzeigeExamples(client);
        ex.cheapest(product, 100); }
    }
}
