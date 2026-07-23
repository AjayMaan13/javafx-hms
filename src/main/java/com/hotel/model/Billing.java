package com.hotel.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "billing")
public class Billing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "reservation_id", unique = true)
    private Reservation reservation;

    @Column(name = "total_due")
    private double totalDue;

    @Column(name = "loyalty_discount")
    private double loyaltyDiscount;

    /** Role-based / courtesy discount applied by an admin (separate from loyalty redemption). */
    private double discount;

    @Column(name = "points_redeemed")
    private int pointsRedeemed;

    @Column(name = "points_earned")
    private int pointsEarned;

    @Column(name = "total_paid")
    private double totalPaid;

    private double balance;

    public Billing() {
    }

    public Billing(Reservation reservation, double totalDue) {
        this.reservation = reservation;
        this.totalDue = totalDue;
        this.balance = totalDue;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public UUID getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public double getTotalDue() {
        return totalDue;
    }

    public void setTotalDue(double totalDue) {
        this.totalDue = totalDue;
    }

    public double getLoyaltyDiscount() {
        return loyaltyDiscount;
    }

    public void setLoyaltyDiscount(double loyaltyDiscount) {
        this.loyaltyDiscount = loyaltyDiscount;
    }

    public int getPointsRedeemed() {
        return pointsRedeemed;
    }

    public void setPointsRedeemed(int pointsRedeemed) {
        this.pointsRedeemed = pointsRedeemed;
    }

    public int getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(int pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public double getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(double totalPaid) {
        this.totalPaid = totalPaid;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
