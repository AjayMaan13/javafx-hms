package com.hotel.service;

import com.hotel.events.RoomAvailabilityPublisher;
import com.hotel.model.Feedback;
import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.model.enums.PaymentMethod;
import com.hotel.model.enums.ReservationStatus;
import com.hotel.model.enums.RoomStatus;
import com.hotel.model.enums.RoomType;
import com.hotel.repository.BillingRepository;
import com.hotel.repository.FeedbackRepository;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.LoyaltyAccountRepository;
import com.hotel.repository.LoyaltyConfigRepository;
import com.hotel.repository.LoyaltyTransactionRepository;
import com.hotel.repository.PaymentRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeedbackServiceTest {

    private static final long DAY_OFFSET = 15000 + new Random().nextInt(3000);

    private final GuestRepository guestRepository = new GuestRepository();
    private final RoomRepository roomRepository = new RoomRepository();
    private final ReservationRepository reservationRepository = new ReservationRepository();
    private final FeedbackRepository feedbackRepository = new FeedbackRepository();

    private final LoyaltyService loyaltyService = new LoyaltyService(
            new LoyaltyAccountRepository(), new LoyaltyConfigRepository(),
            new LoyaltyTransactionRepository(), new BillingRepository());
    private final BillingService billingService = new BillingService(new BillingRepository(),
            new PaymentRepository(), reservationRepository, roomRepository, loyaltyService,
            new RoomAvailabilityPublisher());
    private final FeedbackService feedbackService = new FeedbackService(
            reservationRepository, guestRepository, feedbackRepository, billingService);

    private Reservation newReservation(String email, ReservationStatus status, double total) {
        Guest guest = new Guest("Feedback Guest", "555-0700", email, "1 Review Rd", "R3V 4W5");
        guestRepository.save(guest);
        return newReservationForGuest(guest, status, total);
    }

    /** Multiple reservations sharing one Guest — findByEmail requires a single row per email. */
    private Reservation newReservationForGuest(Guest guest, ReservationStatus status, double total) {
        Room room = roomRepository.save(new Room(RoomType.SINGLE, 100.0, RoomStatus.AVAILABLE));
        LocalDate checkIn = LocalDate.now().plusDays(DAY_OFFSET);
        return reservationRepository.createWithAssociations(guest, List.of(room), List.of(),
                checkIn, checkIn.plusDays(2), 1, 0, status, total, 0.0, total);
    }

    @Test
    void rejectsFeedbackForAReservationThatHasNotCheckedOutYet() {
        Reservation reservation = newReservation(
                "notcheckedout." + UUID.randomUUID() + "@example.com", ReservationStatus.CONFIRMED, 200.0);

        FeedbackException ex = assertThrows(FeedbackException.class,
                () -> feedbackService.submit(reservation, 5, "Great stay"));
        assertTrue(ex.getMessage().toLowerCase().contains("checkout"));
    }

    @Test
    void rejectsFeedbackWhileABalanceRemainsEvenIfCheckedOut() {
        Reservation reservation = newReservation(
                "unsettled." + UUID.randomUUID() + "@example.com", ReservationStatus.CHECKED_OUT, 200.0);
        billingService.createBillingFor(reservation); // balance = 200, nothing paid

        FeedbackException ex = assertThrows(FeedbackException.class,
                () -> feedbackService.submit(reservation, 5, "Great stay"));
        assertTrue(ex.getMessage().toLowerCase().contains("settled"));
    }

    @Test
    void acceptsFeedbackOnceCheckedOutAndFullySettledAndItPersists() {
        Reservation reservation = newReservation(
                "settled." + UUID.randomUUID() + "@example.com", ReservationStatus.CHECKED_OUT, 200.0);
        billingService.createBillingFor(reservation);
        billingService.recordPayment(reservation, PaymentMethod.CASH, 200.0); // fully paid

        Feedback feedback = feedbackService.submit(reservation, 4, "Comfortable room.");
        assertEquals(4, feedback.getRating());

        // Fresh repository call — proves it actually persisted, not just held in memory.
        Feedback reloaded = new FeedbackRepository().findByReservationId(reservation.getId()).orElseThrow();
        assertEquals("Comfortable room.", reloaded.getComment());
    }

    @Test
    void rejectsASecondFeedbackSubmissionForTheSameStay() {
        Reservation reservation = newReservation(
                "duplicate." + UUID.randomUUID() + "@example.com", ReservationStatus.CHECKED_OUT, 150.0);
        billingService.createBillingFor(reservation);
        billingService.recordPayment(reservation, PaymentMethod.CARD, 150.0);

        feedbackService.submit(reservation, 5, "First review.");

        FeedbackException ex = assertThrows(FeedbackException.class,
                () -> feedbackService.submit(reservation, 3, "Second review."));
        assertTrue(ex.getMessage().toLowerCase().contains("already"));
    }

    @Test
    void rejectsAnOutOfRangeRating() {
        Reservation reservation = newReservation(
                "badrating." + UUID.randomUUID() + "@example.com", ReservationStatus.CHECKED_OUT, 100.0);
        billingService.createBillingFor(reservation);
        billingService.recordPayment(reservation, PaymentMethod.CASH, 100.0);

        assertThrows(FeedbackException.class, () -> feedbackService.submit(reservation, 6, "Too high"));
        assertThrows(FeedbackException.class, () -> feedbackService.submit(reservation, 0, "Too low"));
    }

    @Test
    void findEligibleReservationsOnlyReturnsCheckedOutSettledReservationsWithoutExistingFeedback() {
        String email = "eligible." + UUID.randomUUID() + "@example.com";
        Guest guest = new Guest("Repeat Guest", "555-0701", email, "1 Repeat Rd", "R3P 8E9");
        guestRepository.save(guest);

        Reservation notCheckedOut = newReservationForGuest(guest, ReservationStatus.CONFIRMED, 100.0);

        Reservation eligible = newReservationForGuest(guest, ReservationStatus.CHECKED_OUT, 100.0);
        billingService.createBillingFor(eligible);
        billingService.recordPayment(eligible, PaymentMethod.CASH, 100.0);

        Reservation alreadyReviewed = newReservationForGuest(guest, ReservationStatus.CHECKED_OUT, 100.0);
        billingService.createBillingFor(alreadyReviewed);
        billingService.recordPayment(alreadyReviewed, PaymentMethod.CASH, 100.0);
        feedbackService.submit(alreadyReviewed, 5, "Already reviewed this one.");

        List<Reservation> results = feedbackService.findEligibleReservations(email);

        assertTrue(results.stream().anyMatch(r -> r.getId().equals(eligible.getId())));
        assertFalse(results.stream().anyMatch(r -> r.getId().equals(notCheckedOut.getId())));
        assertFalse(results.stream().anyMatch(r -> r.getId().equals(alreadyReviewed.getId())));
    }
}
