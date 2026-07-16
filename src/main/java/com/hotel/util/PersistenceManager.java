package com.hotel.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class PersistenceManager {

    private static final String PERSISTENCE_UNIT_NAME = "hotelPU";

    private static PersistenceManager instance;

    private final EntityManagerFactory entityManagerFactory;

    private PersistenceManager() {
        this.entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    }

    public static synchronized PersistenceManager getInstance() {
        if (instance == null) {
            instance = new PersistenceManager();
        }
        return instance;
    }

    public EntityManager newEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    public synchronized void close() {
        if (entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }
}
