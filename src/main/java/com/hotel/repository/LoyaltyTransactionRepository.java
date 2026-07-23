package com.hotel.repository;

import com.hotel.model.AdminUser;
import com.hotel.model.Billing;
import com.hotel.model.LoyaltyAccount;
import com.hotel.model.LoyaltyConfig;
import com.hotel.model.LoyaltyTransaction;
import com.hotel.model.Payment;
import com.hotel.model.enums.LoyaltyTransactionType;
import com.hotel.util.PersistenceManager;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class LoyaltyTransactionRepository extends BaseRepository<LoyaltyTransaction, UUID> {

    public LoyaltyTransactionRepository() {
        super(LoyaltyTransaction.class);
    }

    public List<LoyaltyTransaction> findByAccountId(UUID accountId) {
        EntityManager em = PersistenceManager.getInstance().newEntityManager();
        try {
            return em.createQuery(
                            "SELECT t FROM LoyaltyTransaction t WHERE t.loyaltyAccount.id = :accountId "
                                    + "ORDER BY t.timestamp", LoyaltyTransaction.class)
                    .setParameter("accountId", accountId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Atomically records a loyalty transaction and updates the account's running balance in
     * one transaction, so the balance and its audit row can never disagree. Optional FK ids
     * (config/billing/payment/adminUser) are re-fetched only when non-null.
     */
    public LoyaltyTransaction recordAndUpdateBalance(UUID accountId, UUID configId, UUID billingId, UUID paymentId,
                                                     UUID adminUserId, LoyaltyTransactionType type, int pointsDelta,
                                                     double paidAmountBasis, double discountAmount, double capApplied) {
        return inTransaction(em -> {
            LoyaltyAccount account = em.find(LoyaltyAccount.class, accountId);
            int newBalance = account.getPointsBalance() + pointsDelta;
            account.setPointsBalance(newBalance);

            LoyaltyTransaction tx = new LoyaltyTransaction();
            tx.setLoyaltyAccount(account);
            tx.setConfig(configId == null ? null : em.find(LoyaltyConfig.class, configId));
            tx.setBilling(billingId == null ? null : em.find(Billing.class, billingId));
            tx.setPayment(paymentId == null ? null : em.find(Payment.class, paymentId));
            tx.setAdminUser(adminUserId == null ? null : em.find(AdminUser.class, adminUserId));
            tx.setType(type);
            tx.setPointsDelta(pointsDelta);
            tx.setBalanceAfter(newBalance);
            tx.setPaidAmountBasis(paidAmountBasis);
            tx.setDiscountAmount(discountAmount);
            tx.setCapApplied(capApplied);
            tx.setTimestamp(LocalDateTime.now());
            em.persist(tx);
            return tx;
        });
    }
}
