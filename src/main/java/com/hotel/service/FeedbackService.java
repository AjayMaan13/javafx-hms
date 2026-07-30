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
    // Demo shortcut: when true, feedback can be left on any booking (skips the
    // checked-out + fully-settled requirement) so it can be shown right after booking.
    // Off by default, so the strict rules (and their tests) are unchanged.
    private final boolean demoBypass;

    public FeedbackService(ReservationRepository reservationRepository, GuestRepository guestRepository,
                            FeedbackRepository feedbackRepository, BillingService billingService) {
        this(reservationRepository, guestRepository, feedbackRepository, billingService, false);
    }

    public FeedbackService(ReservationRepository reservationRepository, GuestRepository guestRepository,
                            FeedbackRepository feedbackRepository, BillingService billingService,
                            boolean demoBypass) {
        this.reservationRepository = reservationRepository;
        this.guestRepository = guestRepository;
        this.feedbackRepository = feedbackRepository;
        this.billingService = billingService;
        this.demoBypass = demoBypass;
    }

    /**
     * Reservations eligible for a feedback prompt for the guest with this email: checked
     * out, no feedback submitted yet, and fully settled (brief: "Allow feedback only after
     * checkout and after all reservation balances are settled").
     */
    public List<Reservation> findEligibleReservations(String email) {
        if (demoBypass) {
            return findForDemo(email);
        }
        return guestRepository.findByEmail(email)
                .map(guest -> reservationRepository.findCheckedOutWithoutFeedback(guest.getId()).stream()
                        .filter(billingService::isFullySettled)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    /**
     * Demo path: any reservation without feedback for this guest (no checkout/settlement
     * requirement). If the email matches no guest, falls back to all un-reviewed stays so
     * the prefilled default email always finds something.
     */
    private List<Reservation> findForDemo(String email) {
        List<Reservation> withoutFeedback = reservationRepository.findAllWithRooms().stream()
                .filter(r -> feedbackRepository.findByReservationId(r.getId()).isEmpty())
                .collect(Collectors.toList());
        return guestRepository.findByEmail(email)
                .map(guest -> {
                    List<Reservation> forGuest = withoutFeedback.stream()
                            .filter(r -> r.getGuest().getId().equals(guest.getId()))
                            .collect(Collectors.toList());
                    return forGuest.isEmpty() ? withoutFeedback : forGuest;
                })
                .orElse(withoutFeedback);
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

        if (!demoBypass) {
            if (reservation.getStatus() != ReservationStatus.CHECKED_OUT) {
                throw new FeedbackException("Feedback is only available after checkout.");
            }
            if (!billingService.isFullySettled(reservation)) {
                throw new FeedbackException("Feedback is only available once your balance is fully settled.");
            }
        }
        if (feedbackRepository.findByReservationId(reservation.getId()).isPresent()) {
            throw new FeedbackException("Feedback has already been submitted for this stay.");
        }

        return feedbackRepository.createFor(reservation.getId(), rating, trimmedComment, LocalDate.now());
    }
}
