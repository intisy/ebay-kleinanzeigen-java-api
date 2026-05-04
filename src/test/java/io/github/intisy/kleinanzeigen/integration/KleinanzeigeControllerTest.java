package io.github.intisy.kleinanzeigen.integration;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests that start the full Spring Boot application and hit real endpoints.
 * Requires network access to kleinanzeigen.de for scraping tests.
 *
 * @author Finn Birich
 */
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KleinanzeigeControllerTest {

    @Value("${local.server.port}")
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();
    private final Gson gson = new Gson();

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    @Order(1)
    @DisplayName("GET / returns welcome message with operational status")
    void testRootEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl() + "/", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Welcome to the Kleinanzeigen API"));
        assertTrue(response.getBody().contains("operational"));
    }

    @Test
    @Order(2)
    @DisplayName("GET /inserate returns 400 for pageCount > 20")
    void testInseratePageCountValidation() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl() + "/inserate?pageCount=21", String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(3)
    @DisplayName("GET /inserate returns 400 for pageCount < 1")
    void testInseratePageCountTooLow() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl() + "/inserate?pageCount=0", String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(4)
    @DisplayName("GET /inserate-detailed returns 400 for pageCount > 3")
    void testInserateDetailedPageCountValidation() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl() + "/inserate-detailed?pageCount=4", String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(5)
    @DisplayName("GET /inserate returns valid SearchResponse structure")
    void testInserateEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl() + "/inserate?query=laptop&pageCount=1", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        JsonObject json = gson.fromJson(response.getBody(), JsonObject.class);
        assertTrue(json.has("success"));
        assertTrue(json.has("data"));
        assertTrue(json.has("time_taken"));
        assertTrue(json.get("success").getAsBoolean());
        assertTrue(json.get("data").isJsonArray());
        assertTrue(json.get("time_taken").getAsDouble() >= 0);
    }

    @Test
    @Order(6)
    @DisplayName("GET /inserat/{id} returns 500 or structured error for invalid id")
    void testInseratInvalidId() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl() + "/inserat/invalid-nonexistent-ad-id-xyz", String.class);
        // Either 500 (scraping failed) with error JSON or 200 — not a 400/404 routing error
        assertNotNull(response.getBody());
        // Body should be JSON
        assertTrue(response.getBody().startsWith("{") || response.getBody().startsWith("["),
                "Response should be JSON");
    }

    @Test
    @Order(7)
    @DisplayName("Concurrent requests to /inserate do not cause thread-safety crashes")
    void testConcurrentRequests() throws Exception {
        int concurrency = 5;
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        List<CompletableFuture<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < concurrency; i++) {
            CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                ResponseEntity<String> response = restTemplate.getForEntity(
                        baseUrl() + "/inserate?query=auto&pageCount=1", String.class);
                return response.getStatusCodeValue();
            }, executor);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        for (CompletableFuture<Integer> future : futures) {
            int statusCode = future.get();
            // All responses should be either 200 (success) or 500 (structured error) — never an unhandled crash
            assertTrue(statusCode == 200 || statusCode == 500,
                    "Expected 200 or 500, got: " + statusCode);
        }
    }
}
