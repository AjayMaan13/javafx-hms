package com.hotel.repository;

import com.hotel.model.Addon;
import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.model.enums.ReservationStatus;
import com.hotel.util.PersistenceManager;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ReservationRepository extends BaseRepository<Reservation, UUID> {

    public ReservationRepository() {
        super(Reservation.class);
    }

    /**
     * Same LazyInitializationException reasoning as findByIdWithRooms below, but for
     * listing screens (Dashboard search) that need to show a room count per reservation.
     */
    public List<Reservation> findAllWithRooms() {
        EntityManager em = PersistenceManager.getInstance().newEntityManager();
        try {
            return em.createQuery(
                            "SELECT DISTINCT r FROM Reservation r LEFT JOIN FETCH r.rooms", Reservation.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Reservation.rooms is @ManyToMany (LAZY by default), so a plain findById would throw
     * LazyInitializationException the moment the caller touches getRooms() after the
     * EntityManager that loaded it has closed. This eagerly joins the rooms so the
     * collection is fully populated before the EntityManager closes.
     */
    public Optional<Reservation> findByIdWithRooms(UUID id) {
        EntityManager em = PersistenceManager.getInstance().newEntityManager();
        try {
            return em.createQuery(
                            "SELECT DISTINCT r FROM Reservation r LEFT JOIN FETCH r.rooms WHERE r.id = :id",
                            Reservation.class)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst();
        } finally {
            em.close();
        }
    }

    /**
     * Updates dates/status/room assignment for an existing reservation. Re-fetches the
     * reservation and each room inside one transaction (same reasoning as
     * createWithAssociations) so the join table is rebuilt against managed entities rather
     * than whatever detached objects the caller happened to be holding.
     */
    public Reservation updateDetails(UUID reservationId, LocalDate checkIn, LocalDate checkOut,
                                      ReservationStatus status, List<UUID> roomIds) {
        return inTransaction(em -> {
            Reservation reservation = em.find(Reservation.class, reservationId);
            reservation.setCheckIn(checkIn);
            reservation.setCheckOut(checkOut);
            reservation.setStatus(status);

            reservation.getRooms().clear();
            for (UUID roomId : roomIds) {
                reservation.getRooms().add(em.find(Room.class, roomId));
            }
            return reservation;
        });
    }

    public Reservation updateStatus(UUID reservationId, ReservationStatus status) {
        return inTransaction(em -> {
            Reservation reservation = em.find(Reservation.class, reservationId);
            reservation.setStatus(status);
            return reservation;
        });
    }

    /**
     * Builds and persists a Reservation from already-persisted guest/room/addon IDs.
     * Re-fetches each one inside this single transaction so they're all managed by the
     * same EntityManager — required for the ManyToMany join tables to populate correctly,
     * since the objects passed in may have come from separate, now-closed EntityManagers.
     */
    public Reservation createWithAssociations(Guest guest, List<Room> rooms, List<Addon> addons,
                                               LocalDate checkIn, LocalDate checkOut,
                                               int adults, int children, ReservationStatus status,
                                               double subtotal, double tax, double total) {
        return inTransaction(em -> {
            Guest managedGuest = em.find(Guest.class, guest.getId());

            Reservation reservation = new Reservation(managedGuest, checkIn, checkOut, adults, children, status);
            for (Room room : rooms) {
                reservation.getRooms().add(em.find(Room.class, room.getId()));
            }
            for (Addon addon : addons) {
                reservation.getAddons().add(em.find(Addon.class, addon.getId()));
            }
            reservation.setSubtotal(subtotal);
            reservation.setTax(tax);
            reservation.setTotal(total);

            em.persist(reservation);
            return reservation;
        });
    }
}
