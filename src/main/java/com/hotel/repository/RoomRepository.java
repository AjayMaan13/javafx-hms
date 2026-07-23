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

    private static final String HAS_CONFLICT_EXCLUDING_RESERVATION_JPQL =
            "SELECT COUNT(res) FROM Reservation res JOIN res.rooms r " +
            "WHERE r.id = :roomId " +
            "AND res.id <> :excludeReservationId " +
            "AND res.status <> com.hotel.model.enums.ReservationStatus.CANCELLED " +
            "AND res.checkIn < :checkOut " +
            "AND :checkIn < res.checkOut";

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

    public Room updateStatus(UUID roomId, com.hotel.model.enums.RoomStatus status) {
        return inTransaction(em -> {
            Room room = em.find(Room.class, roomId);
            room.setStatus(status);
            return room;
        });
    }

    /**
     * Same overlap check as findAvailable, but scoped to one specific room and excluding
     * one reservation's own existing booking — needed when re-saving a reservation's own
     * dates, since otherwise it would conflict with itself.
     */
    public boolean hasConflict(UUID roomId, LocalDate checkIn, LocalDate checkOut, UUID excludeReservationId) {
        EntityManager em = PersistenceManager.getInstance().newEntityManager();
        try {
            Long conflicts = em.createQuery(HAS_CONFLICT_EXCLUDING_RESERVATION_JPQL, Long.class)
                    .setParameter("roomId", roomId)
                    .setParameter("excludeReservationId", excludeReservationId)
                    .setParameter("checkIn", checkIn)
                    .setParameter("checkOut", checkOut)
                    .getSingleResult();
            return conflicts > 0;
        } finally {
            em.close();
        }
    }
}
