package io.github.intisy.kleinanzeigen.exception;

/**
 * Categorization of errors for proper handling and recovery strategies.
 *
 * @author Finn Birich
 */
public enum ErrorCategory {
    RECOVERABLE,
    NON_RECOVERABLE,
    RESOURCE,
    NETWORK,
    PARSING,
    VALIDATION,
    BROWSER
}
