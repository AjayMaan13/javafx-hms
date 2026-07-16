package com.hotel.repository;

import com.hotel.model.Feedback;
import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.enums.ReservationStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FeedbackRepositoryTest {

    @Test
    void savesAndFindsAllFeedback() {
        GuestRepository guestRepository = new GuestRepository();
        ReservationRepository reservationRepository = new ReservationRepository();
        FeedbackRepository feedbackRepository = new FeedbackRepository();

        Guest guest = new Guest("Katherine Johnson", "555-0104", "katherine@example.com", "1 Orbit Ave", "K9L 8M7");
        guestRepository.save(guest);

        Reservation reservation = new Reservation(guest, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 4),
                1, 0, ReservationStatus.CHECKED_OUT);
        reservationRepository.save(reservation);

        Feedback feedback = new Feedback(reservation, 5, "Great stay!", LocalDate.of(2026, 6, 4));
        feedbackRepository.save(feedback);

        assertTrue(feedbackRepository.findAll().stream().anyMatch(f -> f.getId().equals(feedback.getId())));
    }
}
