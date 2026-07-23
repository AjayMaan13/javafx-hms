package com.hotel.service;

import com.hotel.config.PricingPolicy;
import com.hotel.model.Room;
import com.hotel.service.pricing.PricingStrategy;

import java.time.LocalDate;
import java.util.List;

public class PricingService {

    private final PricingStrategy pricingStrategy;
    private final PricingPolicy pricingPolicy;

    public PricingService(PricingStrategy pricingStrategy, PricingPolicy pricingPolicy) {
        this.pricingStrategy = pricingStrategy;
        this.pricingPolicy = pricingPolicy;
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
        return subtotal * pricingPolicy.getTaxRate();
    }

    public double calculateTotal(List<Room> rooms, LocalDate checkIn, LocalDate checkOut) {
        double subtotal = calculateSubtotal(rooms, checkIn, checkOut);
        return subtotal + calculateTax(subtotal);
    }
}
