package io.github.intisy.kleinanzeigen.examples;

import java.util.List;

/**
 * Example: Fetch full details for a single ad ID.
 */
public class SingleAdExample {
    public static void main(String[] args) {
        KleinanzeigeExamples ex = new KleinanzeigeExamples();

        // If an ad ID was passed, fetch it directly
        if (args.length > 0) {
            ex.fetchSingleAd(args[0]);
            return;
        }

        // Otherwise, fetch a list and pick the first one to demonstrate
        System.out.println("No ad ID provided. Fetching a search result to get an ID...");
        KleinanzeigeApiClient client = new KleinanzeigeApiClient();
        List<AdResult> results = client.search(SearchParams.of("laptop"));
        
        if (results != null && !results.isEmpty()) {
            AdResult first = results.get(0);
            ex.fetchSingleAd(first.adId);
        } else {
            System.out.println("Could not find any ads to display.");
        }
    }
}
