package com.hotel.repository;

import com.hotel.model.LoyaltyConfig;
import com.hotel.util.PersistenceManager;
import jakarta.persistence.EntityManager;

import java.util.Optional;
import java.util.UUID;

public class LoyaltyConfigRepository extends BaseRepository<LoyaltyConfig, UUID> {

    public LoyaltyConfigRepository() {
        super(LoyaltyConfig.class);
    }

    public Optional<LoyaltyConfig> findActive() {
        EntityManager em = PersistenceManager.getInstance().newEntityManager();
        try {
            return em.createQuery(
                            "SELECT c FROM LoyaltyConfig c WHERE c.active = true", LoyaltyConfig.class)
                    .getResultStream()
                    .findFirst();
        } finally {
            em.close();
        }
    }
}
