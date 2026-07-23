package com.hotel.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "loyalty_config")
public class LoyaltyConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Points earned per $1 paid. */
    @Column(name = "earn_rate")
    private double earnRate;

    /** Discount dollars granted per point redeemed. */
    @Column(name = "redeem_rate")
    private double redeemRate;

    /** Maximum share of a bill (0..1) that point redemption may discount. */
    @Column(name = "redemption_cap_percent")
    private double redemptionCapPercent;

    private boolean active;

    public LoyaltyConfig() {
    }

    public LoyaltyConfig(double earnRate, double redeemRate, double redemptionCapPercent, boolean active) {
        this.earnRate = earnRate;
        this.redeemRate = redeemRate;
        this.redemptionCapPercent = redemptionCapPercent;
        this.active = active;
    }

    public UUID getId() {
        return id;
    }

    public double getEarnRate() {
        return earnRate;
    }

    public void setEarnRate(double earnRate) {
        this.earnRate = earnRate;
    }

    public double getRedeemRate() {
        return redeemRate;
    }

    public void setRedeemRate(double redeemRate) {
        this.redeemRate = redeemRate;
    }

    public double getRedemptionCapPercent() {
        return redemptionCapPercent;
    }

    public void setRedemptionCapPercent(double redemptionCapPercent) {
        this.redemptionCapPercent = redemptionCapPercent;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
