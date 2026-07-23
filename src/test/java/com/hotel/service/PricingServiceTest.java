package com.hotel.service;

import com.hotel.config.PricingPolicy;
import com.hotel.model.Room;
import com.hotel.model.enums.RoomStatus;
import com.hotel.model.enums.RoomType;
import com.hotel.service.pricing.StandardPricingStrategy;
import com.hotel.service.pricing.WeekendPricingStrategy;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingServiceTest {

    // 2026-07-16 is a Thursday, 2026-07-18 is a Saturday: two nights stayed, Thu + Fri.
    private static final LocalDate THURSDAY_CHECK_IN = LocalDate.of(2026, 7, 16);
    private static final LocalDate SATURDAY_CHECK_OUT = LocalDate.of(2026, 7, 18);

    private final PricingPolicy pricingPolicy = new PricingPolicy();

    @Test
    void weekendStrategyChargesThursdayAtBaseAndFridayAtOneAndAQuarter() {
        Room doubleRoom = new Room(RoomType.DOUBLE, 160.0, RoomStatus.AVAILABLE);
        PricingService pricingService = new PricingService(new WeekendPricingStrategy(pricingPolicy), pricingPolicy);

        double subtotal = pricingService.calculateSubtotal(List.of(doubleRoom), THURSDAY_CHECK_IN, SATURDAY_CHECK_OUT);

        assertEquals(360.0, subtotal, 0.001);
    }

    @Test
    void standardStrategyIgnoresTheDayOfWeekEntirely() {
        Room doubleRoom = new Room(RoomType.DOUBLE, 160.0, RoomStatus.AVAILABLE);
        PricingService pricingService = new PricingService(new StandardPricingStrategy(), pricingPolicy);

        double subtotal = pricingService.calculateSubtotal(List.of(doubleRoom), THURSDAY_CHECK_IN, SATURDAY_CHECK_OUT);

        assertEquals(320.0, subtotal, 0.001);
    }

    @Test
    void taxIsThirteenPercentOnTopOfSubtotal() {
        PricingService pricingService = new PricingService(new StandardPricingStrategy(), pricingPolicy);

        assertEquals(13.0, pricingService.calculateTax(100.0), 0.001);
    }

    @Test
    void changingThePolicyVisiblyChangesPricing() {
        // Proves the M3 Phase 10 gate: a PricingPolicy change alone changes pricing,
        // with zero edits to PricingService or WeekendPricingStrategy.
        PricingPolicy doubledWeekend = new PricingPolicy(1.5, 0.13);
        Room singleRoom = new Room(RoomType.SINGLE, 100.0, RoomStatus.AVAILABLE);
        PricingService pricingService = new PricingService(new WeekendPricingStrategy(doubledWeekend), doubledWeekend);

        // Thu + Fri: 100 (Thu, standard) + 100*1.5 (Fri, weekend) = 250.
        double subtotal = pricingService.calculateSubtotal(List.of(singleRoom), THURSDAY_CHECK_IN, SATURDAY_CHECK_OUT);
        assertEquals(250.0, subtotal, 0.001);
    }
}
