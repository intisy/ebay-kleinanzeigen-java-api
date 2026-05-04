package io.github.intisy.kleinanzeigen.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Utility for rotating user agents to reduce bot detection.
 *
 * @author Finn Birich
 */
public class UserAgentUtil {
    private static final Logger log = LoggerFactory.getLogger(UserAgentUtil.class);
    private static final Random RANDOM = new Random();

    private static final List<String> USER_AGENTS = Arrays.asList(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:125.0) Gecko/20100101 Firefox/125.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_4_1) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4.1 Safari/605.1.15",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 Edg/123.0.0.0",
            "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:125.0) Gecko/20100101 Firefox/125.0"
    );

    private UserAgentUtil() {
        // Utility class - no instantiation
    }

    /**
     * Returns a random user agent string from the rotation list.
     *
     * @return user agent string
     */
    public static String getRandom() {
        String ua = USER_AGENTS.get(RANDOM.nextInt(USER_AGENTS.size()));
        log.trace("Selected user agent: {}", ua);
        return ua;
    }

    /**
     * Returns the number of available user agents.
     *
     * @return count of user agents
     */
    public static int getCount() {
        return USER_AGENTS.size();
    }
}
