package com.hotel.repository;

import com.hotel.model.Payment;
import com.hotel.model.Reservation;
import com.hotel.model.enums.PaymentMethod;
import com.hotel.util.PersistenceManager;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PaymentRepository extends BaseRepository<Payment, UUID> {

    public PaymentRepository() {
        super(Payment.class);
    }

    public List<Payment> findByReservation(Reservation reservation) {
        return findByReservationId(reservation.getId());
    }

    public List<Payment> findByReservationId(UUID reservationId) {
        EntityManager em = PersistenceManager.getInstance().newEntityManager();
        try {
            return em.createQuery(
                            "SELECT p FROM Payment p WHERE p.reservation.id = :reservationId "
                                    + "ORDER BY p.timestamp", Payment.class)
                    .setParameter("reservationId", reservationId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Records a payment (positive amount) or refund (negative amount) against a reservation.
     * Re-fetches the reservation inside the transaction (detached-entity pattern) so the
     * ManyToOne FK is written against a managed entity.
     */
    public Payment recordFor(UUID reservationId, PaymentMethod method, double amount) {
        return inTransaction(em -> {
            Reservation managed = em.find(Reservation.class, reservationId);
            Payment payment = new Payment(managed, method, amount, LocalDateTime.now());
            em.persist(payment);
            return payment;
        });
    }
}
