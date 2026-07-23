package com.hotel.service.reporting;

import java.time.LocalDate;

public class RevenueReportRow {

    private final LocalDate date;
    private final int reservationCount;
    private final double subtotal;
    private final double tax;
    private final double discounts;
    private final double total;

    public RevenueReportRow(LocalDate date, int reservationCount, double subtotal, double tax, double discounts,
                             double total) {
        this.date = date;
        this.reservationCount = reservationCount;
        this.subtotal = subtotal;
        this.tax = tax;
        this.discounts = discounts;
        this.total = total;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getReservationCount() {
        return reservationCount;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getTax() {
        return tax;
    }

    public double getDiscounts() {
        return discounts;
    }

    public double getTotal() {
        return total;
    }
}
