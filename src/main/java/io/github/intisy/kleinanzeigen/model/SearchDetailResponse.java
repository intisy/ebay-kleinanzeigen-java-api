package io.github.intisy.kleinanzeigen.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Response for the /inserate-detailed combined endpoint.
 *
 * @author Finn Birich
 */
public class SearchDetailResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("time_taken")
    private double timeTaken;

    @SerializedName("unique_results")
    private int uniqueResults;

    @SerializedName("data")
    private List<AdItemWithDetails> data;

    @SerializedName("performance_metrics")
    private PerformanceMetrics performanceMetrics;

    SearchDetailResponse() {}

    public SearchDetailResponse(boolean success, double timeTaken, int uniqueResults,
                                List<AdItemWithDetails> data, PerformanceMetrics performanceMetrics) {
        this.success = success;
        this.timeTaken = timeTaken;
        this.uniqueResults = uniqueResults;
        this.data = data;
        this.performanceMetrics = performanceMetrics;
    }

    public boolean isSuccess() { return success; }
    public double getTimeTaken() { return timeTaken; }
    public int getUniqueResults() { return uniqueResults; }
    public List<AdItemWithDetails> getData() { return data; }
    public PerformanceMetrics getPerformanceMetrics() { return performanceMetrics; }

    @Override
    public String toString() {
        return "SearchDetailResponse{success=" + success + ", uniqueResults=" + uniqueResults + '}';
    }

    /**
     * An AdItem combined with its full detail data.
     */
    public static class AdItemWithDetails {
        @SerializedName("adid")
        private String adid;

        @SerializedName("url")
        private String url;

        @SerializedName("title")
        private String title;

        @SerializedName("price")
        private String price;

        @SerializedName("description")
        private String description;

        @SerializedName("details")
        private AdDetail details;

        @SerializedName("detail_fetch_time")
        private double detailFetchTime;

        AdItemWithDetails() {}

        public AdItemWithDetails(AdItem item, AdDetail details, double detailFetchTime) {
            this.adid = item.getAdid();
            this.url = item.getUrl();
            this.title = item.getTitle();
            this.price = item.getPrice();
            this.description = item.getDescription();
            this.details = details;
            this.detailFetchTime = detailFetchTime;
        }

        public String getAdid() { return adid; }
        public String getUrl() { return url; }
        public String getTitle() { return title; }
        public String getPrice() { return price; }
        public String getDescription() { return description; }
        public AdDetail getDetails() { return details; }
        public double getDetailFetchTime() { return detailFetchTime; }

        @Override
        public String toString() {
            return "AdItemWithDetails{adid='" + adid + "', title='" + title + "'}";
        }
    }
}
