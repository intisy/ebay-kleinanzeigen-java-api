package io.github.intisy.kleinanzeigen.unit;

import com.google.gson.Gson;
import io.github.intisy.kleinanzeigen.exception.ErrorCategory;
import io.github.intisy.kleinanzeigen.exception.ErrorSeverity;
import io.github.intisy.kleinanzeigen.model.AdDetail;
import io.github.intisy.kleinanzeigen.model.AdItem;
import io.github.intisy.kleinanzeigen.model.DetailResponse;
import io.github.intisy.kleinanzeigen.model.ErrorResponse;
import io.github.intisy.kleinanzeigen.model.ExtraInfo;
import io.github.intisy.kleinanzeigen.model.Location;
import io.github.intisy.kleinanzeigen.model.PerformanceMetrics;
import io.github.intisy.kleinanzeigen.model.PriceInfo;
import io.github.intisy.kleinanzeigen.model.SearchDetailResponse;
import io.github.intisy.kleinanzeigen.model.SearchResponse;
import io.github.intisy.kleinanzeigen.model.SellerInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for model Gson round-trip serialization.
 *
 * @author Finn Birich
 */
@Tag("unit")
public class ModelTest {

    private final Gson gson = new Gson();

    @Test
    @DisplayName("AdItem - Gson round-trip")
    void testAdItemRoundTrip() {
        AdItem item = new AdItem("123", "https://example.com", "Test Title", "100", "A great item");
        String json = gson.toJson(item);
        AdItem deserialized = gson.fromJson(json, AdItem.class);
        assertEquals("123", deserialized.getAdid());
        assertEquals("https://example.com", deserialized.getUrl());
        assertEquals("Test Title", deserialized.getTitle());
        assertEquals("100", deserialized.getPrice());
        assertEquals("A great item", deserialized.getDescription());
    }

    @Test
    @DisplayName("SellerInfo - Gson round-trip")
    void testSellerInfoRoundTrip() {
        SellerInfo seller = new SellerInfo("Hans Müller", "2020", "private", Arrays.asList("Verified", "Trusted"));
        String json = gson.toJson(seller);
        SellerInfo deserialized = gson.fromJson(json, SellerInfo.class);
        assertEquals("Hans Müller", deserialized.getName());
        assertEquals("private", deserialized.getType());
        assertEquals("2020", deserialized.getSince());
        assertEquals(2, deserialized.getBadges().size());
    }

    @Test
    @DisplayName("Location - Gson round-trip")
    void testLocationRoundTrip() {
        Location loc = new Location("10115", "Mitte", "Berlin");
        String json = gson.toJson(loc);
        Location deserialized = gson.fromJson(json, Location.class);
        assertEquals("10115", deserialized.getZip());
        assertEquals("Mitte", deserialized.getCity());
        assertEquals("Berlin", deserialized.getState());
    }

    @Test
    @DisplayName("PriceInfo - negotiable flag preserved in round-trip")
    void testPriceInfoNegotiable() {
        PriceInfo price = new PriceInfo("350", "EUR", true);
        String json = gson.toJson(price);
        PriceInfo deserialized = gson.fromJson(json, PriceInfo.class);
        assertTrue(deserialized.isNegotiable());
        assertEquals("350", deserialized.getAmount());
        assertEquals("EUR", deserialized.getCurrency());
    }

    @Test
    @DisplayName("SearchResponse - full structure round-trip")
    void testSearchResponseRoundTrip() {
        AdItem item = new AdItem("456", "https://example.com/ad", "Laptop", "500", "Laptop in good condition");
        PerformanceMetrics metrics = new PerformanceMetrics(1, 1, 1.0, 2.5);
        SearchResponse response = new SearchResponse(true, 3.0, 1,
                Arrays.asList(item), metrics, Collections.emptyList());
        String json = gson.toJson(response);
        SearchResponse deserialized = gson.fromJson(json, SearchResponse.class);
        assertTrue(deserialized.isSuccess());
        assertEquals(1, deserialized.getUniqueResults());
        assertNotNull(deserialized.getData());
        assertEquals(1, deserialized.getData().size());
        assertEquals("456", deserialized.getData().get(0).getAdid());
    }

    @Test
    @DisplayName("ErrorResponse - round-trip with recovery suggestions")
    void testErrorResponseRoundTrip() {
        ErrorResponse err = new ErrorResponse("Something failed", "BROWSER", "HIGH",
                Arrays.asList("Retry", "Check logs"));
        String json = gson.toJson(err);
        ErrorResponse deserialized = gson.fromJson(json, ErrorResponse.class);
        assertEquals("Something failed", deserialized.getError());
        assertEquals("BROWSER", deserialized.getCategory());
        assertEquals(2, deserialized.getRecoverySuggestions().size());
    }

    @Test
    @DisplayName("ErrorCategory - all 7 values present")
    void testErrorCategoryValues() {
        assertEquals(7, ErrorCategory.values().length);
    }

    @Test
    @DisplayName("ErrorSeverity - all 4 values present")
    void testErrorSeverityValues() {
        assertEquals(4, ErrorSeverity.values().length);
    }

    @Test
    @DisplayName("ExtraInfo - serialized_name createdAt maps to created_at")
    void testExtraInfoSerializedName() {
        ExtraInfo info = new ExtraInfo("01.01.2024", "1234");
        String json = gson.toJson(info);
        assertTrue(json.contains("created_at"), "JSON should contain 'created_at' not 'createdAt'");
    }

    @Test
    @DisplayName("AdDetail - full round-trip")
    void testAdDetailRoundTrip() {
        Map<String, String> details = new HashMap<>();
        details.put("Zustand", "Gebraucht");
        AdDetail detail = new AdDetail("Laptop", "Good condition", new PriceInfo("500", "EUR", false),
                details, Arrays.asList("WLAN"), new Location("10115", "Mitte", "Berlin"),
                new SellerInfo("Max", "2021", "private", Collections.emptyList()),
                new ExtraInfo("01.01.2024", "100"));
        String json = gson.toJson(detail);
        assertNotNull(json);
        assertTrue(json.contains("Laptop"));
        assertTrue(json.contains("extra_info"));
    }

    @Test
    @DisplayName("PerformanceMetrics - snake_case field names in JSON")
    void testPerformanceMetricsSnakeCase() {
        PerformanceMetrics m = new PerformanceMetrics(5, 4, 0.8, 1.5);
        String json = gson.toJson(m);
        assertTrue(json.contains("pages_requested"));
        assertTrue(json.contains("pages_successful"));
        assertTrue(json.contains("success_rate"));
        assertTrue(json.contains("average_page_time"));
        assertFalse(json.contains("pagesRequested"));
    }
}
