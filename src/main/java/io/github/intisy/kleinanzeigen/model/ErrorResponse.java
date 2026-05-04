package io.github.intisy.kleinanzeigen.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Structured error response returned by the API on failures.
 *
 * @author Finn Birich
 */
public class ErrorResponse {
    @SerializedName("error")
    private String error;

    @SerializedName("category")
    private String category;

    @SerializedName("severity")
    private String severity;

    @SerializedName("recovery_suggestions")
    private List<String> recoverySuggestions;

    ErrorResponse() {}

    public ErrorResponse(String error, String category, String severity, List<String> recoverySuggestions) {
        this.error = error;
        this.category = category;
        this.severity = severity;
        this.recoverySuggestions = recoverySuggestions;
    }

    public String getError() { return error; }
    public String getCategory() { return category; }
    public String getSeverity() { return severity; }
    public List<String> getRecoverySuggestions() { return recoverySuggestions; }

    @Override
    public String toString() {
        return "ErrorResponse{error='" + error + "', category='" + category + "'}";
    }
}
