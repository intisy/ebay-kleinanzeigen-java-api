package io.github.intisy.kleinanzeigen.examples;

/**
 * Example: Fetch the most expensive listings for a product.
 */
public class MostExpensiveExample {
    public static void main(String[] args) {
        String product = args.length > 0 ? args[0] : "laptop";
        try (KleinanzeigeApiClient client = new KleinanzeigeApiClient()) { KleinanzeigeExamples ex = new KleinanzeigeExamples(client);
        ex.mostExpensive(product, 100); }
    }
}
