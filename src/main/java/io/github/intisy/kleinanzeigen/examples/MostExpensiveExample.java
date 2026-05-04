package io.github.intisy.kleinanzeigen.examples;

/**
 * Example: Fetch the most expensive listings for a product.
 */
public class MostExpensiveExample {
    public static void main(String[] args) {
        String product = args.length > 0 ? args[0] : "laptop";
        KleinanzeigeExamples ex = new KleinanzeigeExamples();
        ex.mostExpensive(product, 100);
    }
}
