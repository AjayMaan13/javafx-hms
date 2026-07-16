package com.hotel.service.pricing;

import com.hotel.model.Room;

import java.time.LocalDate;

public interface PricingStrategy {

    double calculateNightPrice(Room room, LocalDate date);
}
