package com.hotel.service.billing;

/**
 * Per-stay add-on: the spa package is a one-time charge for the whole stay, not per night.
 * Having one per-stay decorator alongside the per-night ones is deliberate — it shows the
 * Decorator pattern composing add-ons with different cost models transparently.
 */
public class SpaDecorator extends AddonDecorator {

    private final double perStay;

    public SpaDecorator(PricedBooking wrapped, double perStay) {
        super(wrapped);
        this.perStay = perStay;
    }

    @Override
    public double price() {
        return wrapped.price() + perStay;
    }

    @Override
    public String description() {
        return wrapped.description() + " + Spa";
    }
}
