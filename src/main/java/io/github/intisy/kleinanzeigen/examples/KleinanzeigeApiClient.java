package io.github.intisy.kleinanzeigen.examples;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight HTTP client for the Kleinanzeigen REST API.
 *
 * <p>Wraps the three main endpoints and returns parsed {@link AdResult} objects
 * ready for sorting and filtering in the example programs.
 *
 * <p>Start the API server first:
 * <pre>
 *   ./gradlew bootRun
 * </pre>
 * then run any example in this package.
 *
 * @author Finn Birich
 */
public class KleinanzeigeApiClient {

    private final String baseUrl;
    private final Gson gson = new Gson();

    /**
     * Creates a client targeting {@code http://localhost:8080}.
     */
    public KleinanzeigeApiClient() {
        this("http://localhost:8080");
    }

    /**
     * Creates a client targeting a custom base URL.
     *
     * @param baseUrl e.g. {@code "http://localhost:8080"}
     */
    public KleinanzeigeApiClient(String baseUrl) {
        this.baseUrl = baseUrl.replaceAll("/$", "");
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Searches ads and returns lightweight {@link AdResult} objects.
     * Sorting happens on the caller side.
     *
     * @param params search parameters
     * @return list of results (may be empty if server returns 0 hits)
     */
    public List<AdResult> search(SearchParams params) {
        String url = buildSearchUrl("/inserate", params);
        JsonObject body = get(url);
        return parseAdItems(body);
    }

    /**
     * Fetches full detail for a single ad by its ID.
     *
     * @param adId the ad identifier
     * @return result with all detail fields populated, or {@code null} on error
     */
    public AdResult detail(String adId) {
        JsonObject body = get(baseUrl + "/inserat/" + adId);
        if (body == null || !body.has("data")) return null;
        JsonObject d = body.getAsJsonObject("data");
        AdResult r = new AdResult();
        r.adId = adId;
        r.title = string(d, "title");
        r.description = string(d, "description");
        if (d.has("price") && d.get("price").isJsonObject()) {
            JsonObject p = d.getAsJsonObject("price");
            r.priceAmount = parseAmount(string(p, "amount"));
            r.priceRaw = string(p, "amount") + " " + string(p, "currency");
            r.negotiable = p.has("negotiable") && p.get("negotiable").getAsBoolean();
        }
        if (d.has("extra_info") && d.get("extra_info").isJsonObject()) {
            JsonObject e = d.getAsJsonObject("extra_info");
            r.createdAt = string(e, "created_at");
            r.views = parseViews(string(e, "views"));
        }
        if (d.has("location") && d.get("location").isJsonObject()) {
            JsonObject l = d.getAsJsonObject("location");
            r.city = string(l, "city");
        }
        if (d.has("seller") && d.get("seller").isJsonObject()) {
            JsonObject s = d.getAsJsonObject("seller");
            r.sellerName = string(s, "name");
        }
        return r;
    }

    /**
     * Searches and fetches details for every result in one round trip
     * (uses the {@code /inserate-detailed} endpoint).
     *
     * @param params search parameters
     * @return list of fully populated results
     */
    public List<AdResult> searchDetailed(SearchParams params) {
        String url = buildSearchUrl("/inserate-detailed", params);
        if (params.maxConcurrentDetails > 0) {
            url += (url.contains("?") ? "&" : "?") + "maxConcurrentDetails=" + params.maxConcurrentDetails;
        }
        JsonObject body = get(url);
        if (body == null || !body.has("data")) return new ArrayList<>();
        JsonArray arr = body.getAsJsonArray("data");
        List<AdResult> results = new ArrayList<>();
        for (JsonElement el : arr) {
            if (!el.isJsonObject()) continue;
            JsonObject entry = el.getAsJsonObject();
            AdResult r = new AdResult();
            // listing fields
            if (entry.has("listing") && entry.get("listing").isJsonObject()) {
                JsonObject listing = entry.getAsJsonObject("listing");
                r.adId = string(listing, "adid");
                r.title = string(listing, "title");
                r.priceRaw = string(listing, "price");
                r.priceAmount = parseAmount(r.priceRaw);
                r.description = string(listing, "description");
                r.url = string(listing, "url");
            }
            // detail fields
            if (entry.has("detail") && entry.get("detail").isJsonObject()) {
                JsonObject detail = entry.getAsJsonObject("detail");
                if (detail.has("price") && detail.get("price").isJsonObject()) {
                    JsonObject p = detail.getAsJsonObject("price");
                    r.negotiable = p.has("negotiable") && p.get("negotiable").getAsBoolean();
                }
                if (detail.has("extra_info") && detail.get("extra_info").isJsonObject()) {
                    JsonObject e = detail.getAsJsonObject("extra_info");
                    r.createdAt = string(e, "created_at");
                    r.views = parseViews(string(e, "views"));
                }
                if (detail.has("location") && detail.get("location").isJsonObject()) {
                    JsonObject l = detail.getAsJsonObject("location");
                    r.city = string(l, "city");
                }
                if (detail.has("seller") && detail.get("seller").isJsonObject()) {
                    JsonObject s = detail.getAsJsonObject("seller");
                    r.sellerName = string(s, "name");
                }
            }
            results.add(r);
        }
        return results;
    }

    // -------------------------------------------------------------------------
    // Internals
    // -------------------------------------------------------------------------

    private List<AdResult> parseAdItems(JsonObject body) {
        List<AdResult> results = new ArrayList<>();
        if (body == null || !body.has("data")) return results;
        JsonArray arr = body.getAsJsonArray("data");
        for (JsonElement el : arr) {
            if (!el.isJsonObject()) continue;
            JsonObject o = el.getAsJsonObject();
            AdResult r = new AdResult();
            r.adId = string(o, "adid");
            r.title = string(o, "title");
            r.priceRaw = string(o, "price");
            r.priceAmount = parseAmount(r.priceRaw);
            r.description = string(o, "description");
            r.url = string(o, "url");
            results.add(r);
        }
        return results;
    }

    private String buildSearchUrl(String endpoint, SearchParams p) {
        StringBuilder sb = new StringBuilder(baseUrl).append(endpoint).append("?");
        boolean first = true;
        if (p.query != null && !p.query.isEmpty()) {
            sb.append("query=").append(encode(p.query));
            first = false;
        }
        if (p.location != null && !p.location.isEmpty()) {
            sb.append(first ? "" : "&").append("location=").append(encode(p.location));
            first = false;
        }
        if (p.radius > 0) {
            sb.append(first ? "" : "&").append("radius=").append(p.radius);
            first = false;
        }
        if (p.minPrice > 0) {
            sb.append(first ? "" : "&").append("minPrice=").append(p.minPrice);
            first = false;
        }
        if (p.maxPrice > 0) {
            sb.append(first ? "" : "&").append("maxPrice=").append(p.maxPrice);
            first = false;
        }
        if (p.pageCount > 1) {
            sb.append(first ? "" : "&").append("pageCount=").append(p.pageCount);
        }
        return sb.toString();
    }

    private JsonObject get(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(120_000);
            conn.setRequestProperty("Accept", "application/json");

            int code = conn.getResponseCode();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(code < 400 ? conn.getInputStream() : conn.getErrorStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            conn.disconnect();

            if (code >= 400) {
                System.err.println("HTTP " + code + " for " + urlStr + ": " + sb);
                return null;
            }
            return gson.fromJson(sb.toString(), JsonObject.class);
        } catch (Exception e) {
            System.err.println("Request failed for " + urlStr + ": " + e.getMessage());
            return null;
        }
    }

    /** Extracts a numeric price from strings like "150 €", "VB", "Zu verschenken". */
    static double parseAmount(String raw) {
        if (raw == null || raw.isBlank()) return Double.MAX_VALUE;
        String cleaned = raw.replaceAll("[^0-9,.]", "").replace(",", ".");
        if (cleaned.isEmpty()) return Double.MAX_VALUE; // non-numeric (VB, free, etc.)
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return Double.MAX_VALUE;
        }
    }

    /** Extracts a numeric view count from strings like "1.234 Mal aufgerufen". */
    static int parseViews(String raw) {
        if (raw == null || raw.isBlank()) return 0;
        String cleaned = raw.replaceAll("[^0-9]", "");
        if (cleaned.isEmpty()) return 0;
        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String string(JsonObject o, String key) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull()) return "";
        return o.get(key).getAsString();
    }

    private static String encode(String v) {
        try {
            return URLEncoder.encode(v, "UTF-8");
        } catch (Exception e) {
            return v;
        }
    }
}
