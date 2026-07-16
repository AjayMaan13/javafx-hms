package com.hotel.service.pricing;

import com.hotel.model.Room;

import java.time.LocalDate;

public class StandardPricingStrategy implements PricingStrategy {

    @Override
    public double calculateNightPrice(Room room, LocalDate date) {
        return room.getBasePrice();
    }
}
