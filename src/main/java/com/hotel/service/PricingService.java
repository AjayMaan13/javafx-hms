package com.hotel.service;

import com.hotel.model.Room;
import com.hotel.service.pricing.PricingStrategy;

import java.time.LocalDate;
import java.util.List;

public class PricingService {

    private static final double HST_RATE = 0.13;

    private final PricingStrategy pricingStrategy;

    public PricingService(PricingStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }

    public double calculateSubtotal(List<Room> rooms, LocalDate checkIn, LocalDate checkOut) {
        double subtotal = 0.0;
        for (Room room : rooms) {
            for (LocalDate night = checkIn; night.isBefore(checkOut); night = night.plusDays(1)) {
                subtotal += pricingStrategy.calculateNightPrice(room, night);
            }
        }
        return subtotal;
    }

    public double calculateTax(double subtotal) {
        return subtotal * HST_RATE;
    }

    public double calculateTotal(List<Room> rooms, LocalDate checkIn, LocalDate checkOut) {
        double subtotal = calculateSubtotal(rooms, checkIn, checkOut);
        return subtotal + calculateTax(subtotal);
    }
}
