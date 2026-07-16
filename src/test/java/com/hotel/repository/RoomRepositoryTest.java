package com.hotel.repository;

import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.model.enums.ReservationStatus;
import com.hotel.model.enums.RoomStatus;
import com.hotel.model.enums.RoomType;
import com.hotel.util.PersistenceManager;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomRepositoryTest {

    @Test
    void savesAndFindsAllRooms() {
        RoomRepository repository = new RoomRepository();

        Room room = new Room(RoomType.SINGLE, 100.0, RoomStatus.AVAILABLE);
        repository.save(room);

        assertTrue(repository.findAll().stream().anyMatch(r -> r.getId().equals(room.getId())));
    }

    @Test
    void findAvailableExcludesOnlyTheRoomWithAnOverlappingReservation() {
        RoomRepository roomRepository = new RoomRepository();

        Room bookedRoom = new Room(RoomType.DOUBLE, 160.0, RoomStatus.AVAILABLE);
        Room freeRoom = new Room(RoomType.DOUBLE, 160.0, RoomStatus.AVAILABLE);
        Guest guest = new Guest("Grace Hopper", "555-0105", "grace@example.com", "1 Compiler Ln", "H0P 3R2");

        // Existing reservation books `bookedRoom` for Aug 1 - Aug 5, 2026.
        EntityManager em = PersistenceManager.getInstance().newEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(bookedRoom);
            em.persist(freeRoom);
            em.persist(guest);

            Reservation reservation = new Reservation(guest, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5),
                    2, 0, ReservationStatus.CONFIRMED);
            reservation.getRooms().add(bookedRoom);
            em.persist(reservation);

            em.getTransaction().commit();
        } finally {
            em.close();
        }

        // A date range that overlaps the existing booking (Aug 3 - Aug 4 falls inside Aug 1 - Aug 5):
        // bookedRoom must be excluded, freeRoom must still come back.
        List<Room> duringOverlap = roomRepository.findAvailable(RoomType.DOUBLE,
                LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 4));
        assertTrue(duringOverlap.stream().noneMatch(r -> r.getId().equals(bookedRoom.getId())));
        assertTrue(duringOverlap.stream().anyMatch(r -> r.getId().equals(freeRoom.getId())));

        // A date range that starts exactly when the booking ends (Aug 5 - Aug 8): no overlap,
        // so both rooms should be available again.
        List<Room> afterCheckout = roomRepository.findAvailable(RoomType.DOUBLE,
                LocalDate.of(2026, 8, 5), LocalDate.of(2026, 8, 8));
        assertTrue(afterCheckout.stream().anyMatch(r -> r.getId().equals(bookedRoom.getId())));
        assertTrue(afterCheckout.stream().anyMatch(r -> r.getId().equals(freeRoom.getId())));
    }
}
