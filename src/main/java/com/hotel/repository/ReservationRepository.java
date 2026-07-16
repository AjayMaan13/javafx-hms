package com.hotel.repository;

import com.hotel.model.Addon;
import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.model.enums.ReservationStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ReservationRepository extends BaseRepository<Reservation, UUID> {

    public ReservationRepository() {
        super(Reservation.class);
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
