package io.github.intisy.kleinanzeigen.exception;

/**
 * Exception thrown when the Playwright scraper fails to extract data.
 *
 * @author Finn Birich
 */
public class ScrapingException extends KleinanzeigeException {

    public ScrapingException(String message) {
        super(message, ErrorCategory.BROWSER, ErrorSeverity.HIGH);
    }

    public ScrapingException(String message, Throwable cause) {
        super(message, ErrorCategory.BROWSER, ErrorSeverity.HIGH, cause);
    }
}
