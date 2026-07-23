package com.hotel.service.pricing;

import com.hotel.config.PricingPolicy;
import com.hotel.model.Room;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class WeekendPricingStrategy implements PricingStrategy {

    private final PricingPolicy pricingPolicy;

    public WeekendPricingStrategy(PricingPolicy pricingPolicy) {
        this.pricingPolicy = pricingPolicy;
    }

    @Override
    public double calculateNightPrice(Room room, LocalDate date) {
        boolean isWeekend = date.getDayOfWeek() == DayOfWeek.FRIDAY || date.getDayOfWeek() == DayOfWeek.SATURDAY;
        return isWeekend ? room.getBasePrice() * pricingPolicy.getWeekendMultiplier() : room.getBasePrice();
    }
}
