package com.hotel.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "loyalty_account")
public class LoyaltyAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "guest_id", unique = true)
    private Guest guest;

    @Column(name = "points_balance")
    private int pointsBalance;

    private String tier;

    private String status;

    public LoyaltyAccount() {
    }

    public LoyaltyAccount(Guest guest, String tier, String status) {
        this.guest = guest;
        this.tier = tier;
        this.status = status;
        this.pointsBalance = 0;
    }

    /**
     * The guest-facing loyalty number. Derived from the account id rather than stored in a
     * separate column, so the schema stays faithful to the M1 ERD (which has no such column).
     */
    public String getLoyaltyNumber() {
        return "ML-" + id.toString().substring(0, 8).toUpperCase();
    }

    public UUID getId() {
        return id;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public int getPointsBalance() {
        return pointsBalance;
    }

    public void setPointsBalance(int pointsBalance) {
        this.pointsBalance = pointsBalance;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
