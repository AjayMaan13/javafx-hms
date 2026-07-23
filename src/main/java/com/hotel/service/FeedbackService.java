package com.hotel.service;

import com.hotel.model.Feedback;
import com.hotel.model.Reservation;
import com.hotel.model.enums.ReservationStatus;
import com.hotel.repository.FeedbackRepository;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.ReservationRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class FeedbackService {

    private static final int MAX_COMMENT_LENGTH = 500;

    private final ReservationRepository reservationRepository;
    private final GuestRepository guestRepository;
    private final FeedbackRepository feedbackRepository;
    private final BillingService billingService;

    public FeedbackService(ReservationRepository reservationRepository, GuestRepository guestRepository,
                            FeedbackRepository feedbackRepository, BillingService billingService) {
        this.reservationRepository = reservationRepository;
        this.guestRepository = guestRepository;
        this.feedbackRepository = feedbackRepository;
        this.billingService = billingService;
    }

    /**
     * Reservations eligible for a feedback prompt for the guest with this email: checked
     * out, no feedback submitted yet, and fully settled (brief: "Allow feedback only after
     * checkout and after all reservation balances are settled").
     */
    public List<Reservation> findEligibleReservations(String email) {
        return guestRepository.findByEmail(email)
                .map(guest -> reservationRepository.findCheckedOutWithoutFeedback(guest.getId()).stream()
                        .filter(billingService::isFullySettled)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    /**
     * Submits feedback for a reservation. Re-validates eligibility here (not just in the
     * UI) — checked out, settled, not already reviewed — since this is the actual
     * enforcement point, not the screen that called it.
     */
    public Feedback submit(Reservation reservation, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new FeedbackException("Rating must be between 1 and 5 stars.");
        }

        String trimmedComment = comment == null ? "" : comment.trim();
        if (trimmedComment.length() > MAX_COMMENT_LENGTH) {
            throw new FeedbackException("Comment must be " + MAX_COMMENT_LENGTH + " characters or fewer.");
        }

        if (reservation.getStatus() != ReservationStatus.CHECKED_OUT) {
            throw new FeedbackException("Feedback is only available after checkout.");
        }
        if (!billingService.isFullySettled(reservation)) {
            throw new FeedbackException("Feedback is only available once your balance is fully settled.");
        }
        if (feedbackRepository.findByReservationId(reservation.getId()).isPresent()) {
            throw new FeedbackException("Feedback has already been submitted for this stay.");
        }

        return feedbackRepository.createFor(reservation.getId(), rating, trimmedComment, LocalDate.now());
    }
}
