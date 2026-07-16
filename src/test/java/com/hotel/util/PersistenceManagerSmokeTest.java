package com.hotel.util;

import com.hotel.model.Guest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PersistenceManagerSmokeTest {

    @Test
    void persistsAndReadsBackAGuest() {
        PersistenceManager manager = PersistenceManager.getInstance();

        Guest guest = new Guest("Ada Lovelace", "555-0100", "ada@example.com", "1 Analytical Engine Way", "A1B 2C3");

        EntityManager em = manager.newEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(guest);
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        UUID savedId = guest.getId();
        assertNotNull(savedId);

        EntityManager readEm = manager.newEntityManager();
        try {
            Guest found = readEm.find(Guest.class, savedId);
            assertNotNull(found);
            assertEquals("Ada Lovelace", found.getName());
        } finally {
            readEm.close();
        }
    }
}
