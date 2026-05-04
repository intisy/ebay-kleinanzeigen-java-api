package io.github.intisy.kleinanzeigen.examples;

/**
 * Example: Fetch the newest listings (requires detail fetching).
 */
public class NewestExample {
    public static void main(String[] args) {
        String product = args.length > 0 ? args[0] : "laptop";
        try (KleinanzeigeApiClient client = new KleinanzeigeApiClient()) { KleinanzeigeExamples ex = new KleinanzeigeExamples(client);
        ex.newest(product, 100); }
    }
}
