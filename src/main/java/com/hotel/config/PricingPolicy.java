package com.hotel.config;

/**
 * Centralises the pricing constants that used to be hardcoded inside PricingService and
 * WeekendPricingStrategy, so changing a rate is a one-line edit here rather than a hunt
 * through pricing logic.
 */
public class PricingPolicy {

    private final double weekendMultiplier;
    private final double taxRate;

    public PricingPolicy() {
        this(1.25, 0.13);
    }

    public PricingPolicy(double weekendMultiplier, double taxRate) {
        this.weekendMultiplier = weekendMultiplier;
        this.taxRate = taxRate;
    }

    public double getWeekendMultiplier() {
        return weekendMultiplier;
    }

    /** HST rate applied to every reservation subtotal. */
    public double getTaxRate() {
        return taxRate;
    }
}
