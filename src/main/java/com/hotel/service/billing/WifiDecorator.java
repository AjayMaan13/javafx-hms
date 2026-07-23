package com.hotel.service.billing;

/** Per-night add-on: Wi-Fi is charged for every night of the stay. */
public class WifiDecorator extends AddonDecorator {

    private final double perNight;

    public WifiDecorator(PricedBooking wrapped, double perNight) {
        super(wrapped);
        this.perNight = perNight;
    }

    @Override
    public double price() {
        return wrapped.price() + perNight * nights();
    }

    @Override
    public String description() {
        return wrapped.description() + " + Wi-Fi";
    }
}
