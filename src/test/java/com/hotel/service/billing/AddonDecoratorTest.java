package com.hotel.service.billing;

import com.hotel.model.Addon;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddonDecoratorTest {

    @Test
    void baseBookingWithNoAddonsIsJustTheRoomSubtotal() {
        PricedBooking booking = AddonDecoratorFactory.priceBooking(320.0, 2, List.of());

        assertEquals(320.0, booking.price(), 0.001);
        assertEquals("Rooms (2 nights)", booking.description());
    }

    @Test
    void perNightAddonsAreMultipliedByNightsAndItemisedInTheDescription() {
        // 2-night stay, $320 rooms, Wi-Fi $5/night + Breakfast $12/night.
        // Expected: 320 + (5*2) + (12*2) = 320 + 10 + 24 = 354.
        List<Addon> addons = List.of(new Addon("Wi-Fi", 5.0), new Addon("Breakfast", 12.0));
        PricedBooking booking = AddonDecoratorFactory.priceBooking(320.0, 2, addons);

        assertEquals(354.0, booking.price(), 0.001);
        assertTrue(booking.description().contains("Wi-Fi"));
        assertTrue(booking.description().contains("Breakfast"));
    }

    @Test
    void spaIsChargedOncePerStayNotPerNight() {
        // 3-night stay, $600 rooms, Spa $20 as a one-time package.
        // Expected: 600 + 20 = 620 (NOT 600 + 20*3).
        List<Addon> addons = List.of(new Addon("Spa access", 20.0));
        PricedBooking booking = AddonDecoratorFactory.priceBooking(600.0, 3, addons);

        assertEquals(620.0, booking.price(), 0.001);
        assertTrue(booking.description().contains("Spa"));
    }

    @Test
    void wrappingAnExtraDecoratorChangesThePriceWithoutTouchingBaseBooking() {
        BaseBooking base = new BaseBooking(100.0, 2);
        double baseOnly = base.price();

        PricedBooking withWifi = new WifiDecorator(base, 5.0);

        // The same BaseBooking instance still reports its original price — the decorator
        // added cost on top rather than mutating the component.
        assertEquals(100.0, base.price(), 0.001);
        assertEquals(baseOnly, base.price(), 0.001);
        assertEquals(110.0, withWifi.price(), 0.001);
    }
}
