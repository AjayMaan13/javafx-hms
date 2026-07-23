package com.hotel.repository;

import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.model.enums.ReservationStatus;
import com.hotel.model.enums.RoomType;
import com.hotel.service.RoomFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationRepositoryUpdateTest {

    // Randomized per run so repeated `mvn test` invocations against the same dev DB file
    // don't collide with rooms booked by earlier runs (same reasoning as ReservationServiceTest).
    private static final long DAY_OFFSET = 30 + new Random().nextInt(3000);

    @Test
    void updateDetailsPersistsNewDatesStatusAndRoomsAndSurvivesAFreshRead() {
        GuestRepository guestRepository = new GuestRepository();
        ReservationRepository reservationRepository = new ReservationRepository();
        RoomRepository roomRepository = new RoomRepository();

        Guest guest = new Guest("Ada Editor", "555-0201", "ada.editor." + UUID.randomUUID() + "@example.com",
                "1 Edit St", "E1D 2T3");
        guestRepository.save(guest);

        LocalDate originalCheckIn = LocalDate.now().plusDays(DAY_OFFSET);
        LocalDate originalCheckOut = originalCheckIn.plusDays(2);
        Room originalRoom = RoomFactory.create(RoomType.SINGLE, "edit-test-" + UUID.randomUUID());
        roomRepository.save(originalRoom);

        Reservation reservation = reservationRepository.createWithAssociations(
                guest, List.of(originalRoom), List.of(), originalCheckIn, originalCheckOut,
                1, 0, ReservationStatus.PENDING, 100.0, 13.0, 113.0);

        LocalDate newCheckIn = originalCheckIn.plusDays(50);
        LocalDate newCheckOut = newCheckIn.plusDays(3);
        Room secondRoom = RoomFactory.create(RoomType.DOUBLE, "edit-test-2-" + UUID.randomUUID());
        roomRepository.save(secondRoom);

        reservationRepository.updateDetails(reservation.getId(), newCheckIn, newCheckOut,
                ReservationStatus.CONFIRMED, List.of(originalRoom.getId(), secondRoom.getId()));

        // Fresh read via a brand-new repository call — proves the change was actually
        // persisted, not just held in the in-memory object from the update call.
        Reservation reloaded = new ReservationRepository().findByIdWithRooms(reservation.getId()).orElseThrow();

        assertEquals(newCheckIn, reloaded.getCheckIn());
        assertEquals(newCheckOut, reloaded.getCheckOut());
        assertEquals(ReservationStatus.CONFIRMED, reloaded.getStatus());
        assertEquals(2, reloaded.getRooms().size());
    }

    @Test
    void cancellingAReservationFreesItsRoomForTheAvailabilityQuery() {
        GuestRepository guestRepository = new GuestRepository();
        ReservationRepository reservationRepository = new ReservationRepository();
        RoomRepository roomRepository = new RoomRepository();

        Guest guest = new Guest("Cancel Test", "555-0202", "cancel." + UUID.randomUUID() + "@example.com",
                "1 Cancel Ave", "C4N 3L4");
        guestRepository.save(guest);

        LocalDate checkIn = LocalDate.now().plusDays(DAY_OFFSET + 200);
        LocalDate checkOut = checkIn.plusDays(2);
        Room room = RoomFactory.create(RoomType.DELUXE, "cancel-test-" + UUID.randomUUID());
        roomRepository.save(room);

        Reservation reservation = reservationRepository.createWithAssociations(
                guest, List.of(room), List.of(), checkIn, checkOut,
                1, 0, ReservationStatus.CONFIRMED, 220.0, 28.6, 248.6);

        // Before cancelling: room correctly shows as unavailable for these dates.
        assertFalse(roomRepository.findAvailable(RoomType.DELUXE, checkIn, checkOut).stream()
                .anyMatch(r -> r.getId().equals(room.getId())));

        reservationRepository.updateStatus(reservation.getId(), ReservationStatus.CANCELLED);

        // After cancelling: the same availability query now includes it again.
        assertTrue(roomRepository.findAvailable(RoomType.DELUXE, checkIn, checkOut).stream()
                .anyMatch(r -> r.getId().equals(room.getId())));
    }

    @Test
    void hasConflictExcludesTheReservationsOwnBookingButCatchesAGenuineOverlapFromAnotherReservation() {
        GuestRepository guestRepository = new GuestRepository();
        ReservationRepository reservationRepository = new ReservationRepository();
        RoomRepository roomRepository = new RoomRepository();

        Guest guestA = new Guest("Conflict A", "555-0203", "conflict.a." + UUID.randomUUID() + "@example.com",
                "1 A St", "A1B 2C3");
        Guest guestB = new Guest("Conflict B", "555-0204", "conflict.b." + UUID.randomUUID() + "@example.com",
                "1 B St", "B1C 2D3");
        guestRepository.save(guestA);
        guestRepository.save(guestB);

        LocalDate checkIn = LocalDate.now().plusDays(DAY_OFFSET + 400);
        LocalDate checkOut = checkIn.plusDays(3);
        Room room = RoomFactory.create(RoomType.PENTHOUSE, "conflict-test-" + UUID.randomUUID());
        roomRepository.save(room);

        Reservation reservationA = reservationRepository.createWithAssociations(
                guestA, List.of(room), List.of(), checkIn, checkOut,
                1, 0, ReservationStatus.CONFIRMED, 400.0, 52.0, 452.0);

        // Re-checking reservationA's own dates against its own room must NOT be a conflict.
        assertFalse(roomRepository.hasConflict(room.getId(), checkIn, checkOut, reservationA.getId()));

        Reservation reservationB = reservationRepository.createWithAssociations(
                guestB, List.of(), List.of(), checkIn.plusDays(60), checkIn.plusDays(63),
                1, 0, ReservationStatus.CONFIRMED, 400.0, 52.0, 452.0);

        // If reservation B tried to take the same room on overlapping dates, that IS a conflict.
        assertTrue(roomRepository.hasConflict(room.getId(), checkIn.plusDays(1), checkIn.plusDays(2),
                reservationB.getId()));
    }
}
