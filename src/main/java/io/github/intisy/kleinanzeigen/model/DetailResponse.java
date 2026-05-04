package io.github.intisy.kleinanzeigen.model;

import com.google.gson.annotations.SerializedName;

/**
 * Response for the /inserat/{id} detail endpoint.
 *
 * @author Finn Birich
 */
public class DetailResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("time_taken")
    private double timeTaken;

    @SerializedName("data")
    private AdDetail data;

    @SerializedName("performance_metrics")
    private PerformanceMetrics performanceMetrics;

    DetailResponse() {}

    public DetailResponse(boolean success, double timeTaken, AdDetail data, PerformanceMetrics performanceMetrics) {
        this.success = success;
        this.timeTaken = timeTaken;
        this.data = data;
        this.performanceMetrics = performanceMetrics;
    }

    public boolean isSuccess() { return success; }
    public double getTimeTaken() { return timeTaken; }
    public AdDetail getData() { return data; }
    public PerformanceMetrics getPerformanceMetrics() { return performanceMetrics; }

    @Override
    public String toString() {
        return "DetailResponse{success=" + success + '}';
    }
}
