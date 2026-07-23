package com.hotel.service.billing;

/** Per-night add-on: parking is charged for every night of the stay. */
public class ParkingDecorator extends AddonDecorator {

    private final double perNight;

    public ParkingDecorator(PricedBooking wrapped, double perNight) {
        super(wrapped);
        this.perNight = perNight;
    }

    @Override
    public double price() {
        return wrapped.price() + perNight * nights();
    }

    @Override
    public String description() {
        return wrapped.description() + " + Parking";
    }
}
