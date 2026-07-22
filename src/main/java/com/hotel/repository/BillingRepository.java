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
}
