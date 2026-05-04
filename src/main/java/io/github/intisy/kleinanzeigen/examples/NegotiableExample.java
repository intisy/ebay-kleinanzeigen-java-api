package io.github.intisy.kleinanzeigen.examples;

/**
 * Example: Fetch negotiable (VB) listings sorted by price.
 */
public class NegotiableExample {
    public static void main(String[] args) {
        String product = args.length > 0 ? args[0] : "laptop";
        try (KleinanzeigeApiClient client = new KleinanzeigeApiClient()) { KleinanzeigeExamples ex = new KleinanzeigeExamples(client);
        ex.negotiable(product, 100); }
    }
}
