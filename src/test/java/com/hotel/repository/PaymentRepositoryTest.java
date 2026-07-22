package com.hotel.repository;

import com.hotel.model.Guest;
import com.hotel.model.Payment;
import com.hotel.model.Reservation;
import com.hotel.model.enums.PaymentMethod;
import com.hotel.model.enums.ReservationStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentRepositoryTest {

    @Test
    void savesAndFindsPaymentsByReservation() {
        GuestRepository guestRepository = new GuestRepository();
        ReservationRepository reservationRepository = new ReservationRepository();
        PaymentRepository paymentRepository = new PaymentRepository();

        Guest guest = new Guest("Marie Curie", "555-0106", "marie@example.com", "1 Radium Rd", "P0L 1O2");
        guestRepository.save(guest);

        Reservation reservation = new Reservation(guest, LocalDate.now().plusDays(400), LocalDate.now().plusDays(402),
                1, 0, ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        Payment payment = new Payment(reservation, PaymentMethod.CARD, 150.0, LocalDateTime.now());
        paymentRepository.save(payment);

        List<Payment> found = paymentRepository.findByReservation(reservation);
        assertTrue(found.stream().anyMatch(p -> p.getId().equals(payment.getId())));
    }
}
