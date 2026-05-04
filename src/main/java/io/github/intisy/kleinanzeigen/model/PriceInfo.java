package io.github.intisy.kleinanzeigen.model;

import com.google.gson.annotations.SerializedName;

/**
 * Parsed price information from an ad.
 *
 * @author Finn Birich
 */
public class PriceInfo {
    @SerializedName("amount")
    private String amount;

    @SerializedName("currency")
    private String currency;

    @SerializedName("negotiable")
    private boolean negotiable;

    PriceInfo() {}

    public PriceInfo(String amount, String currency, boolean negotiable) {
        this.amount = amount;
        this.currency = currency;
        this.negotiable = negotiable;
    }

    public String getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public boolean isNegotiable() {
        return negotiable;
    }

    @Override
    public String toString() {
        return "PriceInfo{" +
                "amount='" + amount + '\'' +
                ", currency='" + currency + '\'' +
                ", negotiable=" + negotiable +
                '}';
    }
}
