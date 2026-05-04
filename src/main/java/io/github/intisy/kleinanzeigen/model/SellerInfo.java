package io.github.intisy.kleinanzeigen.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Seller information extracted from an ad detail page.
 *
 * @author Finn Birich
 */
public class SellerInfo {
    @SerializedName("name")
    private String name;

    @SerializedName("since")
    private String since;

    @SerializedName("type")
    private String type;

    @SerializedName("badges")
    private List<String> badges;

    SellerInfo() {}

    public SellerInfo(String name, String since, String type, List<String> badges) {
        this.name = name;
        this.since = since;
        this.type = type;
        this.badges = badges;
    }

    public String getName() {
        return name;
    }

    public String getSince() {
        return since;
    }

    public String getType() {
        return type;
    }

    public List<String> getBadges() {
        return badges;
    }

    @Override
    public String toString() {
        return "SellerInfo{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
