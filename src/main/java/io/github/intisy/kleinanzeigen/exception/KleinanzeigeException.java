package io.github.intisy.kleinanzeigen.exception;

/**
 * Base exception for Kleinanzeigen API errors.
 *
 * @author Finn Birich
 */
public class KleinanzeigeException extends RuntimeException {
    private final ErrorCategory category;
    private final ErrorSeverity severity;

    public KleinanzeigeException(String message, ErrorCategory category, ErrorSeverity severity) {
        super(message);
        this.category = category;
        this.severity = severity;
    }

    public KleinanzeigeException(String message, ErrorCategory category, ErrorSeverity severity, Throwable cause) {
        super(message, cause);
        this.category = category;
        this.severity = severity;
    }

    public ErrorCategory getCategory() {
        return category;
    }

    public ErrorSeverity getSeverity() {
        return severity;
    }
}
