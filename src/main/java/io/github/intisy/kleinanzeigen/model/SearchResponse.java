package io.github.intisy.kleinanzeigen.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Response for the /inserate search endpoint.
 *
 * @author Finn Birich
 */
public class SearchResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("time_taken")
    private double timeTaken;

    @SerializedName("unique_results")
    private int uniqueResults;

    @SerializedName("data")
    private List<AdItem> data;

    @SerializedName("performance_metrics")
    private PerformanceMetrics performanceMetrics;

    @SerializedName("warnings")
    private List<String> warnings;

    SearchResponse() {}

    public SearchResponse(boolean success, double timeTaken, int uniqueResults, List<AdItem> data,
                          PerformanceMetrics performanceMetrics, List<String> warnings) {
        this.success = success;
        this.timeTaken = timeTaken;
        this.uniqueResults = uniqueResults;
        this.data = data;
        this.performanceMetrics = performanceMetrics;
        this.warnings = warnings;
    }

    public boolean isSuccess() { return success; }
    public double getTimeTaken() { return timeTaken; }
    public int getUniqueResults() { return uniqueResults; }
    public List<AdItem> getData() { return data; }
    public PerformanceMetrics getPerformanceMetrics() { return performanceMetrics; }
    public List<String> getWarnings() { return warnings; }

    @Override
    public String toString() {
        return "SearchResponse{success=" + success + ", uniqueResults=" + uniqueResults + '}';
    }
}
