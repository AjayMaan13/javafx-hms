package com.hotel.service.billing;

/**
 * Concrete component: the room-nights subtotal with no add-ons. Decorators wrap this.
 * Its price never changes when new add-on decorators are introduced — that's the point
 * of the pattern.
 */
public class BaseBooking implements PricedBooking {

    private final double roomSubtotal;
    private final long nights;

    public BaseBooking(double roomSubtotal, long nights) {
        this.roomSubtotal = roomSubtotal;
        this.nights = nights;
    }

    @Override
    public double price() {
        return roomSubtotal;
    }

    @Override
    public String description() {
        return String.format("Rooms (%d night%s)", nights, nights == 1 ? "" : "s");
    }

    @Override
    public long nights() {
        return nights;
    }
}
