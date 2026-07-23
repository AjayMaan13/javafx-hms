package com.hotel.repository;

import com.hotel.model.Guest;
import com.hotel.model.LoyaltyAccount;
import com.hotel.util.PersistenceManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.Optional;
import java.util.UUID;

public class LoyaltyAccountRepository extends BaseRepository<LoyaltyAccount, UUID> {

    public LoyaltyAccountRepository() {
        super(LoyaltyAccount.class);
    }

    public Optional<LoyaltyAccount> findByGuestId(UUID guestId) {
        EntityManager em = PersistenceManager.getInstance().newEntityManager();
        try {
            LoyaltyAccount account = em.createQuery(
                            "SELECT a FROM LoyaltyAccount a WHERE a.guest.id = :guestId", LoyaltyAccount.class)
                    .setParameter("guestId", guestId)
                    .getSingleResult();
            return Optional.of(account);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    /** Creates a loyalty account for an existing guest (detached-entity-safe). */
    public LoyaltyAccount createFor(UUID guestId, String tier, String status) {
        return inTransaction(em -> {
            Guest managed = em.find(Guest.class, guestId);
            LoyaltyAccount account = new LoyaltyAccount(managed, tier, status);
            em.persist(account);
            return account;
        });
    }
}
