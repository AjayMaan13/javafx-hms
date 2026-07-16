package com.hotel.service.pricing;

import com.hotel.model.Room;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class WeekendPricingStrategy implements PricingStrategy {

    private static final double WEEKEND_MULTIPLIER = 1.25;

    @Override
    public double calculateNightPrice(Room room, LocalDate date) {
        // TODO Final: read the weekend/seasonal multiplier from config/PricingPolicy
        // instead of this hardcoded constant.
        boolean isWeekend = date.getDayOfWeek() == DayOfWeek.FRIDAY || date.getDayOfWeek() == DayOfWeek.SATURDAY;
        return isWeekend ? room.getBasePrice() * WEEKEND_MULTIPLIER : room.getBasePrice();
    }
}
