package com.hotel.repository;

import com.hotel.model.Feedback;
import com.hotel.model.Reservation;
import com.hotel.util.PersistenceManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public class FeedbackRepository extends BaseRepository<Feedback, UUID> {

    public FeedbackRepository() {
        super(Feedback.class);
    }

    public Optional<Feedback> findByReservationId(UUID reservationId) {
        EntityManager em = PersistenceManager.getInstance().newEntityManager();
        try {
            Feedback feedback = em.createQuery(
                            "SELECT f FROM Feedback f WHERE f.reservation.id = :reservationId", Feedback.class)
                    .setParameter("reservationId", reservationId)
                    .getSingleResult();
            return Optional.of(feedback);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    /** Creates feedback for an existing reservation (detached-entity-safe). */
    public Feedback createFor(UUID reservationId, int rating, String comment, LocalDate createdAt) {
        return inTransaction(em -> {
            Reservation managed = em.find(Reservation.class, reservationId);
            Feedback feedback = new Feedback(managed, rating, comment, createdAt);
            em.persist(feedback);
            return feedback;
        });
    }
}
