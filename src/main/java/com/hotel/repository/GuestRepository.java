package com.hotel.repository;

import com.hotel.model.Guest;
import com.hotel.util.PersistenceManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.Optional;
import java.util.UUID;

public class GuestRepository extends BaseRepository<Guest, UUID> {

    public GuestRepository() {
        super(Guest.class);
    }

    public Optional<Guest> findByEmail(String email) {
        EntityManager em = PersistenceManager.getInstance().newEntityManager();
        try {
            Guest guest = em.createQuery("SELECT g FROM Guest g WHERE g.email = :email", Guest.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.of(guest);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }
}
