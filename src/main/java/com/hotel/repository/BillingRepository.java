package com.hotel.repository;

import com.hotel.model.Billing;
import com.hotel.model.Reservation;
import com.hotel.util.PersistenceManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.Optional;
import java.util.UUID;

public class BillingRepository extends BaseRepository<Billing, UUID> {

    public BillingRepository() {
        super(Billing.class);
    }

    public Optional<Billing> findByReservation(Reservation reservation) {
        EntityManager em = PersistenceManager.getInstance().newEntityManager();
        try {
            Billing billing = em.createQuery(
                            "SELECT b FROM Billing b WHERE b.reservation.id = :reservationId", Billing.class)
                    .setParameter("reservationId", reservation.getId())
                    .getSingleResult();
            return Optional.of(billing);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    /**
     * Creates a Billing for an existing reservation. Re-fetches the reservation inside the
     * transaction (detached-entity pattern) so the OneToOne FK is written against a managed
     * entity.
     */
    public Billing createFor(UUID reservationId, double totalDue) {
        return inTransaction(em -> {
            Reservation managed = em.find(Reservation.class, reservationId);
            Billing billing = new Billing(managed, totalDue);
            em.persist(billing);
            return billing;
        });
    }

    public Billing updateBalances(UUID billingId, double totalPaid, double balance) {
        return inTransaction(em -> {
            Billing billing = em.find(Billing.class, billingId);
            billing.setTotalPaid(totalPaid);
            billing.setBalance(balance);
            return billing;
        });
    }

    /** Applies a loyalty-point redemption: records the discount + points and lowers the balance. */
    public Billing applyLoyaltyDiscount(UUID billingId, double discountAmount, int pointsRedeemed) {
        return inTransaction(em -> {
            Billing billing = em.find(Billing.class, billingId);
            billing.setLoyaltyDiscount(billing.getLoyaltyDiscount() + discountAmount);
            billing.setPointsRedeemed(billing.getPointsRedeemed() + pointsRedeemed);
            billing.setBalance(billing.getBalance() - discountAmount);
            return billing;
        });
    }

    /** Applies a role-based/courtesy discount: records the discount and lowers the balance. */
    public Billing applyDiscount(UUID billingId, double discountAmount) {
        return inTransaction(em -> {
            Billing billing = em.find(Billing.class, billingId);
            billing.setDiscount(billing.getDiscount() + discountAmount);
            billing.setBalance(billing.getBalance() - discountAmount);
            return billing;
        });
    }
}
