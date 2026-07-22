package com.hotel.repository;

import com.hotel.model.AdminUser;
import com.hotel.util.PersistenceManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.Optional;
import java.util.UUID;

public class AdminUserRepository extends BaseRepository<AdminUser, UUID> {

    public AdminUserRepository() {
        super(AdminUser.class);
    }

    public Optional<AdminUser> findByUsername(String username) {
        EntityManager em = PersistenceManager.getInstance().newEntityManager();
        try {
            AdminUser adminUser = em.createQuery(
                            "SELECT a FROM AdminUser a WHERE a.username = :username", AdminUser.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(adminUser);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }
}
