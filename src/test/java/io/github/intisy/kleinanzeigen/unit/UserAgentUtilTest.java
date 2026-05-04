package io.github.intisy.kleinanzeigen.unit;

import io.github.intisy.kleinanzeigen.util.UserAgentUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link UserAgentUtil}.
 *
 * @author Finn Birich
 */
@Tag("unit")
public class UserAgentUtilTest {

    @Test
    @DisplayName("getRandom() returns non-null, non-empty user agent 100 times")
    void testGetRandomAlwaysReturnsValidAgent() {
        for (int i = 0; i < 100; i++) {
            String ua = UserAgentUtil.getRandom();
            assertNotNull(ua, "User agent must not be null at iteration " + i);
            assertFalse(ua.trim().isEmpty(), "User agent must not be empty at iteration " + i);
        }
    }

    @Test
    @DisplayName("getRandom() returns a string containing 'Mozilla'")
    void testGetRandomContainsMozilla() {
        // All valid modern user agents start with Mozilla
        for (int i = 0; i < 20; i++) {
            String ua = UserAgentUtil.getRandom();
            assert ua.contains("Mozilla") : "Expected 'Mozilla' in user agent: " + ua;
        }
    }
}
