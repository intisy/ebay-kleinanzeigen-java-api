package io.github.intisy.kleinanzeigen.model;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a single ad listing from the search results.
 *
 * @author Finn Birich
 */
public class AdItem {
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

    AdItem() {}

    public AdItem(String adid, String url, String title, String price, String description) {
        this.adid = adid;
        this.url = url;
        this.title = title;
        this.price = price;
        this.description = description;
    }

    public String getAdid() {
        return adid;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "AdItem{" +
                "adid='" + adid + '\'' +
                ", title='" + title + '\'' +
                ", price='" + price + '\'' +
                '}';
    }
}
