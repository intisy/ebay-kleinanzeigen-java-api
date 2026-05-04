package io.github.intisy.kleinanzeigen.exception;

/**
 * Exception thrown when the scraper engine is not available.
 *
 * @author Finn Birich
 */
public class ServiceUnavailableException extends KleinanzeigeException {

    public ServiceUnavailableException(String message) {
        super(message, ErrorCategory.RESOURCE, ErrorSeverity.CRITICAL);
    }
}
