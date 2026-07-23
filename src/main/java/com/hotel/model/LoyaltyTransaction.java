package com.hotel.model;

import com.hotel.model.enums.LoyaltyTransactionType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable audit record of a single loyalty change (EARN/REDEEM/ADJUST). Points balances
 * are reconstructable from these rows — nothing silently mutates a balance without a
 * transaction being written.
 */
@Entity
@Table(name = "loyalty_transaction")
public class LoyaltyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "loyalty_account_id")
    private LoyaltyAccount loyaltyAccount;

    @ManyToOne
    @JoinColumn(name = "config_id")
    private LoyaltyConfig config;

    @ManyToOne
    @JoinColumn(name = "billing_id")
    private Billing billing;

    @ManyToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @ManyToOne
    @JoinColumn(name = "admin_user_id")
    private AdminUser adminUser;

    @Enumerated(EnumType.STRING)
    private LoyaltyTransactionType type;

    @Column(name = "points_delta")
    private int pointsDelta;

    @Column(name = "balance_after")
    private int balanceAfter;

    @Column(name = "paid_amount_basis")
    private double paidAmountBasis;

    @Column(name = "discount_amount")
    private double discountAmount;

    @Column(name = "cap_applied")
    private double capApplied;

    @Column(name = "ts")
    private LocalDateTime timestamp;

    public LoyaltyTransaction() {
    }

    public UUID getId() {
        return id;
    }

    public LoyaltyAccount getLoyaltyAccount() {
        return loyaltyAccount;
    }

    public void setLoyaltyAccount(LoyaltyAccount loyaltyAccount) {
        this.loyaltyAccount = loyaltyAccount;
    }

    public LoyaltyConfig getConfig() {
        return config;
    }

    public void setConfig(LoyaltyConfig config) {
        this.config = config;
    }

    public Billing getBilling() {
        return billing;
    }

    public void setBilling(Billing billing) {
        this.billing = billing;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public AdminUser getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(AdminUser adminUser) {
        this.adminUser = adminUser;
    }

    public LoyaltyTransactionType getType() {
        return type;
    }

    public void setType(LoyaltyTransactionType type) {
        this.type = type;
    }

    public int getPointsDelta() {
        return pointsDelta;
    }

    public void setPointsDelta(int pointsDelta) {
        this.pointsDelta = pointsDelta;
    }

    public int getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(int balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public double getPaidAmountBasis() {
        return paidAmountBasis;
    }

    public void setPaidAmountBasis(double paidAmountBasis) {
        this.paidAmountBasis = paidAmountBasis;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getCapApplied() {
        return capApplied;
    }

    public void setCapApplied(double capApplied) {
        this.capApplied = capApplied;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
