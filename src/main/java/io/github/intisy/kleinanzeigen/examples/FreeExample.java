package io.github.intisy.kleinanzeigen.examples;

/**
 * Example: Fetch "Zu verschenken" (free) listings.
 */
public class FreeExample {
    public static void main(String[] args) {
        String product = args.length > 0 ? args[0] : "laptop";
        try (KleinanzeigeApiClient client = new KleinanzeigeApiClient()) { KleinanzeigeExamples ex = new KleinanzeigeExamples(client);
        ex.free(product, 100); }
    }
}
