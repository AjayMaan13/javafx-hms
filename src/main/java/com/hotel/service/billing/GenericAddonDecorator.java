package com.hotel.service.billing;

/**
 * Fallback decorator for any add-on that isn't one of the specifically-modelled ones
 * (Wi-Fi/Breakfast/Parking/Spa). Charged once per stay, labelled with the add-on's name.
 */
public class GenericAddonDecorator extends AddonDecorator {

    private final String name;
    private final double perStay;

    public GenericAddonDecorator(PricedBooking wrapped, String name, double perStay) {
        super(wrapped);
        this.name = name;
        this.perStay = perStay;
    }

    @Override
    public double price() {
        return wrapped.price() + perStay;
    }

    @Override
    public String description() {
        return wrapped.description() + " + " + name;
    }
}
