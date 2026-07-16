package com.hotel.repository;

import com.hotel.model.Room;
import com.hotel.model.enums.RoomType;
import com.hotel.util.PersistenceManager;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class RoomRepository extends BaseRepository<Room, UUID> {

    private static final String FIND_AVAILABLE_JPQL =
            "SELECT r FROM Room r " +
            "WHERE r.type = :type " +
            "AND NOT EXISTS (" +
            "    SELECT res FROM Reservation res JOIN res.rooms bookedRoom " +
            "    WHERE bookedRoom = r " +
            "    AND res.status <> com.hotel.model.enums.ReservationStatus.CANCELLED " +
            "    AND res.checkIn < :checkOut " +
            "    AND :checkIn < res.checkOut" +
            ")";

    public RoomRepository() {
        super(Room.class);
    }

    public List<Room> findAvailable(RoomType type, LocalDate checkIn, LocalDate checkOut) {
        EntityManager em = PersistenceManager.getInstance().newEntityManager();
        try {
            return em.createQuery(FIND_AVAILABLE_JPQL, Room.class)
                    .setParameter("type", type)
                    .setParameter("checkIn", checkIn)
                    .setParameter("checkOut", checkOut)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
