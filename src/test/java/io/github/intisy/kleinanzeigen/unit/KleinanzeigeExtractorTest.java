package io.github.intisy.kleinanzeigen.unit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.github.intisy.kleinanzeigen.lib.KleinanzeigeExtractor;
import io.github.intisy.kleinanzeigen.model.AdItem;
import io.github.intisy.kleinanzeigen.model.Location;
import io.github.intisy.kleinanzeigen.model.PriceInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link KleinanzeigeExtractor} using static HTML fixtures.
 * No network calls to kleinanzeigen.de are made.
 *
 * @author Finn Birich
 */
@Tag("unit")
public class KleinanzeigeExtractorTest {

    private static Playwright playwright;
    private static Browser browser;

    @BeforeAll
    static void startBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    static void stopBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    private Page newPageWithContent(String html) {
        Page page = browser.newContext().newPage();
        page.setContent(html);
        return page;
    }

    @Test
    @DisplayName("extractAdItems - parses ad list from fixture HTML")
    void testExtractAdItems() {
        String html = "<html><body>" +
                "<div class=\"ad-listitem\">" +
                "  <article data-adid=\"12345\" data-href=\"/s-anzeige/test-item/12345\">" +
                "    <h2 class=\"text-module-begin\"><a class=\"ellipsis\">Test Laptop</a></h2>" +
                "    <p class=\"aditem-main--middle--price-shipping--price\">500 €</p>" +
                "    <p class=\"aditem-main--middle--description\">Great condition</p>" +
                "  </article>" +
                "</div>" +
                "</body></html>";
        Page page = newPageWithContent(html);
        try {
            List<AdItem> items = KleinanzeigeExtractor.extractAdItems(page);
            assertFalse(items.isEmpty(), "Should extract at least one ad item");
            AdItem item = items.get(0);
            assertEquals("12345", item.getAdid());
            assertTrue(item.getUrl().contains("12345"));
            assertEquals("Test Laptop", item.getTitle());
        } finally {
            page.close();
        }
    }

    @Test
    @DisplayName("extractAdItems - skips topad articles")
    void testExtractAdItemsSkipsTopad() {
        String html = "<html><body>" +
                "<div class=\"ad-listitem is-topad\">" +
                "  <article data-adid=\"99999\" data-href=\"/s-anzeige/topad/99999\">" +
                "    <h2 class=\"text-module-begin\"><a class=\"ellipsis\">Sponsored</a></h2>" +
                "  </article>" +
                "</div>" +
                "<div class=\"ad-listitem\">" +
                "  <article data-adid=\"11111\" data-href=\"/s-anzeige/real/11111\">" +
                "    <h2 class=\"text-module-begin\"><a class=\"ellipsis\">Real Item</a></h2>" +
                "    <p class=\"aditem-main--middle--price-shipping--price\">100 €</p>" +
                "    <p class=\"aditem-main--middle--description\">desc</p>" +
                "  </article>" +
                "</div>" +
                "</body></html>";
        Page page = newPageWithContent(html);
        try {
            List<AdItem> items = KleinanzeigeExtractor.extractAdItems(page);
            assertEquals(1, items.size());
            assertEquals("11111", items.get(0).getAdid());
        } finally {
            page.close();
        }
    }

    @Test
    @DisplayName("extractLocation - parses zip/city/state correctly")
    void testExtractLocation() {
        String html = "<html><body>" +
                "<span id=\"viewad-locality\">12345 Berlin - Mitte</span>" +
                "</body></html>";
        Page page = newPageWithContent(html);
        try {
            Location loc = KleinanzeigeExtractor.extractLocation(page);
            assertEquals("12345", loc.getZip());
            assertEquals("Berlin", loc.getState());
            assertEquals("Mitte", loc.getCity());
        } finally {
            page.close();
        }
    }

    @Test
    @DisplayName("parsePrice - handles VB flag")
    void testParsePriceNegotiable() {
        PriceInfo p = KleinanzeigeExtractor.parsePrice("350 € VB");
        assertTrue(p.isNegotiable());
        assertEquals("350", p.getAmount());
        assertEquals("EUR", p.getCurrency());
    }

    @Test
    @DisplayName("parsePrice - handles clean price without VB")
    void testParsePriceFixed() {
        PriceInfo p = KleinanzeigeExtractor.parsePrice("1.200 €");
        assertFalse(p.isNegotiable());
        assertEquals("1200", p.getAmount());
    }

    @Test
    @DisplayName("parsePrice - handles null input")
    void testParsePriceNull() {
        PriceInfo p = KleinanzeigeExtractor.parsePrice(null);
        assertEquals("0", p.getAmount());
        assertFalse(p.isNegotiable());
    }

    @Test
    @DisplayName("parsePrice - handles empty string")
    void testParsePriceEmpty() {
        PriceInfo p = KleinanzeigeExtractor.parsePrice("");
        assertEquals("0", p.getAmount());
        assertFalse(p.isNegotiable());
    }

    @Test
    @DisplayName("extractFeatures - returns checktag items")
    void testExtractFeatures() {
        String html = "<html><body>" +
                "<div id=\"viewad-configuration\">" +
                "  <ul class=\"checktaglist\">" +
                "    <li class=\"checktag\">WLAN</li>" +
                "    <li class=\"checktag\">Bluetooth</li>" +
                "  </ul>" +
                "</div>" +
                "</body></html>";
        Page page = newPageWithContent(html);
        try {
            List<String> features = KleinanzeigeExtractor.extractFeatures(page);
            assertEquals(2, features.size());
            assertTrue(features.contains("WLAN"));
            assertTrue(features.contains("Bluetooth"));
        } finally {
            page.close();
        }
    }
}
