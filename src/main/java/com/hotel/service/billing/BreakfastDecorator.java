package com.hotel.service.billing;

/** Per-night add-on: breakfast is charged for every night of the stay. */
public class BreakfastDecorator extends AddonDecorator {

    private final double perNight;

    public BreakfastDecorator(PricedBooking wrapped, double perNight) {
        super(wrapped);
        this.perNight = perNight;
    }

    @Override
    public double price() {
        return wrapped.price() + perNight * nights();
    }

    @Override
    public String description() {
        return wrapped.description() + " + Breakfast";
    }
}
