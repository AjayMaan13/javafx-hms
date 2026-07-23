package com.hotel.service;

import com.hotel.config.DiscountPolicy;
import com.hotel.model.AdminUser;
import com.hotel.model.Billing;
import com.hotel.model.Reservation;
import com.hotel.repository.BillingRepository;

public class DiscountService {

    private static final double EPSILON = 0.005;

    private final BillingRepository billingRepository;
    private final DiscountPolicy discountPolicy;

    public DiscountService(BillingRepository billingRepository, DiscountPolicy discountPolicy) {
        this.billingRepository = billingRepository;
        this.discountPolicy = discountPolicy;
    }

    /**
     * Applies a percentage discount to a reservation's bill, enforcing the applying admin's
     * role cap. The cap is checked here in the service — not in the UI — so a tampered or
     * bypassed screen can never exceed it. Returns the dollar discount applied.
     */
    public double apply(Reservation reservation, double percent, AdminUser admin) {
        if (admin == null) {
            throw new DiscountException("You must be logged in to apply a discount.");
        }
        if (percent <= 0) {
            throw new DiscountException("Enter a positive discount percentage.");
        }

        double cap = discountPolicy.maxPercentFor(admin.getRole());
        if (percent > cap + EPSILON) {
            throw new DiscountException(String.format(
                    "%s discounts are capped at %.0f%% — %.1f%% was requested.", admin.getRole(), cap, percent));
        }

        Billing billing = billingRepository.findByReservation(reservation)
                .orElseThrow(() -> new DiscountException("No bill exists for this reservation yet."));

        double discountAmount = (percent / 100.0) * billing.getTotalDue();
        if (billing.getBalance() - discountAmount < -EPSILON) {
            throw new DiscountException(String.format(
                    "A %.1f%% discount ($%.2f) exceeds the remaining balance of $%.2f.",
                    percent, discountAmount, billing.getBalance()));
        }

        billingRepository.applyDiscount(billing.getId(), discountAmount);
        return discountAmount;
    }
}
