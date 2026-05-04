package io.github.intisy.kleinanzeigen.model;

import com.google.gson.annotations.SerializedName;

/**
 * Location information parsed from an ad detail page.
 *
 * @author Finn Birich
 */
public class Location {
    @SerializedName("zip")
    private String zip;

    @SerializedName("city")
    private String city;

    @SerializedName("state")
    private String state;

    Location() {}

    public Location(String zip, String city, String state) {
        this.zip = zip;
        this.city = city;
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    @Override
    public String toString() {
        return "Location{" +
                "zip='" + zip + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
