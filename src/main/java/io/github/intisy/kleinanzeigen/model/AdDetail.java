package io.github.intisy.kleinanzeigen.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Full detail information for a single ad.
 *
 * @author Finn Birich
 */
public class AdDetail {
    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("price")
    private PriceInfo price;

    @SerializedName("details")
    private Map<String, String> details;

    @SerializedName("features")
    private List<String> features;

    @SerializedName("location")
    private Location location;

    @SerializedName("seller")
    private SellerInfo seller;

    @SerializedName("extra_info")
    private ExtraInfo extraInfo;

    AdDetail() {}

    public AdDetail(String title, String description, PriceInfo price, Map<String, String> details,
                    List<String> features, Location location, SellerInfo seller, ExtraInfo extraInfo) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.details = details;
        this.features = features;
        this.location = location;
        this.seller = seller;
        this.extraInfo = extraInfo;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public PriceInfo getPrice() { return price; }
    public Map<String, String> getDetails() { return details; }
    public List<String> getFeatures() { return features; }
    public Location getLocation() { return location; }
    public SellerInfo getSeller() { return seller; }
    public ExtraInfo getExtraInfo() { return extraInfo; }

    @Override
    public String toString() {
        return "AdDetail{" +
                "title='" + title + '\'' +
                ", location=" + location +
                '}';
    }
}
