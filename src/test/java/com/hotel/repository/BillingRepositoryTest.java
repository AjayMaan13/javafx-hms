package com.hotel.repository;

import com.hotel.model.Billing;
import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.enums.ReservationStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BillingRepositoryTest {

    @Test
    void savesAndFindsBillingByReservation() {
        GuestRepository guestRepository = new GuestRepository();
        ReservationRepository reservationRepository = new ReservationRepository();
        BillingRepository billingRepository = new BillingRepository();

        Guest guest = new Guest("Niels Bohr", "555-0107", "niels@example.com", "1 Quantum Ave", "B0H 2R3");
        guestRepository.save(guest);

        Reservation reservation = new Reservation(guest, LocalDate.now().plusDays(410), LocalDate.now().plusDays(412),
                1, 0, ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        Billing billing = new Billing(reservation, 226.0);
        billingRepository.save(billing);

        Optional<Billing> found = billingRepository.findByReservation(reservation);
        assertTrue(found.isPresent());
        assertEquals(226.0, found.get().getTotalDue(), 0.001);
    }
}
