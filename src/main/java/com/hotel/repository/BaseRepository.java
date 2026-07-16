package com.hotel.repository;

import com.hotel.util.PersistenceManager;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class BaseRepository<T, ID> {

    private final Class<T> entityClass;

    protected BaseRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected <R> R inTransaction(Function<EntityManager, R> work) {
        EntityManager em = PersistenceManager.getInstance().newEntityManager();
        try {
            em.getTransaction().begin();
            R result = work.apply(em);
            em.getTransaction().commit();
            return result;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public T save(T entity) {
        return inTransaction(em -> {
            em.persist(entity);
            return entity;
        });
    }

    public Optional<T> findById(ID id) {
        EntityManager em = PersistenceManager.getInstance().newEntityManager();
        try {
            return Optional.ofNullable(em.find(entityClass, id));
        } finally {
            em.close();
        }
    }

    public List<T> findAll() {
        EntityManager em = PersistenceManager.getInstance().newEntityManager();
        try {
            return em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
