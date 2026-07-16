package com.hotel.repository;

import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.enums.ReservationStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationRepositoryTest {

    @Test
    void savesAndFindsAllReservations() {
        GuestRepository guestRepository = new GuestRepository();
        ReservationRepository reservationRepository = new ReservationRepository();

        Guest guest = new Guest("Rosalind Franklin", "555-0103", "rosalind@example.com", "1 DNA Way", "X1Y 2Z3");
        guestRepository.save(guest);

        Reservation reservation = new Reservation(guest, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 3),
                2, 0, ReservationStatus.PENDING);
        reservationRepository.save(reservation);

        assertTrue(reservationRepository.findAll().stream().anyMatch(r -> r.getId().equals(reservation.getId())));
    }
}
