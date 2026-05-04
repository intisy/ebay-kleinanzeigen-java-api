package io.github.intisy.kleinanzeigen.examples;

/**
 * Example: Fetch the oldest listings (requires detail fetching).
 */
public class OldestExample {
    public static void main(String[] args) {
        String product = args.length > 0 ? args[0] : "laptop";
        KleinanzeigeExamples ex = new KleinanzeigeExamples();
        ex.oldest(product, 100);
    }
}
