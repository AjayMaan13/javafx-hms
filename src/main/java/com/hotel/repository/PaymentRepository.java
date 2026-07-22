package com.hotel.repository;

import com.hotel.model.Payment;
import com.hotel.model.Reservation;
import com.hotel.util.PersistenceManager;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.UUID;

public class PaymentRepository extends BaseRepository<Payment, UUID> {

    public PaymentRepository() {
        super(Payment.class);
    }

    public List<Payment> findByReservation(Reservation reservation) {
        EntityManager em = PersistenceManager.getInstance().newEntityManager();
        try {
            return em.createQuery(
                            "SELECT p FROM Payment p WHERE p.reservation.id = :reservationId", Payment.class)
                    .setParameter("reservationId", reservation.getId())
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
