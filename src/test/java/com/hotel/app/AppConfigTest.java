package com.hotel.app;

import com.hotel.model.Room;
import com.hotel.model.enums.RoomStatus;
import com.hotel.model.enums.RoomType;
import com.hotel.service.PricingService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppConfigTest {

    @Test
    void wiresAPricingServiceUsingTheDefaultStrategy() {
        AppConfig appConfig = new AppConfig();
        PricingService pricingService = appConfig.getPricingService();

        Room doubleRoom = new Room(RoomType.DOUBLE, 160.0, RoomStatus.AVAILABLE);

        // Default strategy is Standard, so a Thu-Fri stay ignores the weekend entirely.
        double subtotal = pricingService.calculateSubtotal(
                List.of(doubleRoom), LocalDate.of(2026, 7, 16), LocalDate.of(2026, 7, 18));

        assertEquals(320.0, subtotal, 0.001);
    }
}
