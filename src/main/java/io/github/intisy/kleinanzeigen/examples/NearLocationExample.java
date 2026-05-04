package io.github.intisy.kleinanzeigen.examples;

/**
 * Example: Fetch listings near a specific location.
 */
public class NearLocationExample {
    public static void main(String[] args) {
        String product = args.length > 0 ? args[0] : "laptop";
        try (KleinanzeigeApiClient client = new KleinanzeigeApiClient()) { KleinanzeigeExamples ex = new KleinanzeigeExamples(client);
        ex.nearLocation(product, "Berlin", 50, 100); }
    }
}
