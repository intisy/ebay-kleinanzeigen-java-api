package io.github.intisy.kleinanzeigen.examples;

/**
 * Example: Fetch the newest listings (requires detail fetching).
 */
public class NewestExample {
    public static void main(String[] args) {
        String product = args.length > 0 ? args[0] : "laptop";
        KleinanzeigeExamples ex = new KleinanzeigeExamples();
        ex.newest(product, 100);
    }
}
