package com.hotel.service.billing;

import com.hotel.model.Addon;

import java.util.List;

/**
 * Builds a decorated {@link PricedBooking} chain from the selected add-ons. Factory + Decorator
 * together: it starts from a plain {@link BaseBooking} and wraps one decorator per add-on, so
 * the final object's price() and description() reflect the room-nights plus every add-on.
 *
 * Static like {@link com.hotel.service.RoomFactory}, since the decorators are stateless.
 */
public final class AddonDecoratorFactory {

    private AddonDecoratorFactory() {
    }

    public static PricedBooking priceBooking(double roomSubtotal, long nights, List<Addon> addons) {
        PricedBooking booking = new BaseBooking(roomSubtotal, nights);
        for (Addon addon : addons) {
            booking = decorate(booking, addon);
        }
        return booking;
    }

    private static PricedBooking decorate(PricedBooking booking, Addon addon) {
        switch (addon.getName()) {
            case "Wi-Fi":
                return new WifiDecorator(booking, addon.getPrice());
            case "Breakfast":
                return new BreakfastDecorator(booking, addon.getPrice());
            case "Parking":
                return new ParkingDecorator(booking, addon.getPrice());
            case "Spa access":
                return new SpaDecorator(booking, addon.getPrice());
            default:
                return new GenericAddonDecorator(booking, addon.getName(), addon.getPrice());
        }
    }
}
