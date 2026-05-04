package io.github.intisy.kleinanzeigen.model;

import com.google.gson.annotations.SerializedName;

/**
 * Extra metadata from an ad detail page.
 *
 * @author Finn Birich
 */
public class ExtraInfo {
    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("views")
    private String views;

    ExtraInfo() {}

    public ExtraInfo(String createdAt, String views) {
        this.createdAt = createdAt;
        this.views = views;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getViews() {
        return views;
    }

    @Override
    public String toString() {
        return "ExtraInfo{" +
                "createdAt='" + createdAt + '\'' +
                ", views='" + views + '\'' +
                '}';
    }
}
