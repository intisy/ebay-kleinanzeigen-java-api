package io.github.intisy.kleinanzeigen.model;

import com.google.gson.annotations.SerializedName;

/**
 * Performance metrics for a scraping operation.
 *
 * @author Finn Birich
 */
public class PerformanceMetrics {
    @SerializedName("pages_requested")
    private int pagesRequested;

    @SerializedName("pages_successful")
    private int pagesSuccessful;

    @SerializedName("success_rate")
    private double successRate;

    @SerializedName("average_page_time")
    private double averagePageTime;

    PerformanceMetrics() {}

    public PerformanceMetrics(int pagesRequested, int pagesSuccessful, double successRate, double averagePageTime) {
        this.pagesRequested = pagesRequested;
        this.pagesSuccessful = pagesSuccessful;
        this.successRate = successRate;
        this.averagePageTime = averagePageTime;
    }

    public int getPagesRequested() { return pagesRequested; }
    public int getPagesSuccessful() { return pagesSuccessful; }
    public double getSuccessRate() { return successRate; }
    public double getAveragePageTime() { return averagePageTime; }

    @Override
    public String toString() {
        return "PerformanceMetrics{" +
                "pagesRequested=" + pagesRequested +
                ", pagesSuccessful=" + pagesSuccessful +
                ", successRate=" + successRate +
                '}';
    }
}
