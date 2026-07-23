package com.hotel.service;

import com.hotel.model.Billing;
import com.hotel.model.Payment;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.model.enums.PaymentMethod;
import com.hotel.model.enums.ReservationStatus;
import com.hotel.model.enums.RoomStatus;
import com.hotel.repository.BillingRepository;
import com.hotel.repository.PaymentRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;

import java.util.List;

public class BillingService {

    // Money comparisons use a small epsilon so floating-point noise (e.g. 0.0000001)
    // isn't treated as a real outstanding balance.
    private static final double EPSILON = 0.005;

    private final BillingRepository billingRepository;
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    public BillingService(BillingRepository billingRepository, PaymentRepository paymentRepository,
                           ReservationRepository reservationRepository, RoomRepository roomRepository) {
        this.billingRepository = billingRepository;
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
    }

    /** Called when a reservation is confirmed: total_due = total, nothing paid yet. */
    public Billing createBillingFor(Reservation reservation) {
        return billingRepository.createFor(reservation.getId(), reservation.getTotal());
    }

    /** Returns the reservation's billing, lazily creating one for reservations that predate billing. */
    public Billing getOrCreateBilling(Reservation reservation) {
        return billingRepository.findByReservation(reservation)
                .orElseGet(() -> billingRepository.createFor(reservation.getId(), reservation.getTotal()));
    }

    public List<Payment> paymentHistory(Reservation reservation) {
        return paymentRepository.findByReservation(reservation);
    }

    /**
     * Records a payment (positive amount) or refund (negative amount).
     * Guards both directions: a payment can't exceed the outstanding balance, and a refund
     * can't exceed what has actually been paid (which would produce a negative total-paid).
     */
    public Billing recordPayment(Reservation reservation, PaymentMethod method, double amount) {
        if (amount == 0) {
            throw new BillingException("Enter a non-zero amount.");
        }

        Billing billing = getOrCreateBilling(reservation);

        double newTotalPaid = billing.getTotalPaid() + amount;
        double newBalance = billing.getTotalDue() - newTotalPaid;

        if (newBalance < -EPSILON) {
            throw new BillingException(String.format(
                    "Payment of $%.2f exceeds the outstanding balance of $%.2f.", amount, billing.getBalance()));
        }
        if (newTotalPaid < -EPSILON) {
            throw new BillingException(String.format(
                    "Refund of $%.2f exceeds the amount paid so far ($%.2f).", -amount, billing.getTotalPaid()));
        }

        paymentRepository.recordFor(reservation.getId(), method, amount);
        return billingRepository.updateBalances(billing.getId(), newTotalPaid, newBalance);
    }

    public boolean isFullySettled(Reservation reservation) {
        return getOrCreateBilling(reservation).getBalance() <= EPSILON;
    }

    /**
     * Settles checkout: blocks while any balance remains, else marks the reservation
     * CHECKED_OUT and its rooms AVAILABLE.
     */
    public void checkout(Reservation reservation) {
        Billing billing = getOrCreateBilling(reservation);

        if (billing.getBalance() > EPSILON) {
            throw new BillingException(String.format(
                    "Cannot check out — an outstanding balance of $%.2f must be settled first.",
                    billing.getBalance()));
        }

        reservationRepository.updateStatus(reservation.getId(), ReservationStatus.CHECKED_OUT);

        List<Room> rooms = reservationRepository.findByIdWithRooms(reservation.getId())
                .map(Reservation::getRooms)
                .orElse(List.of());
        for (Room room : rooms) {
            roomRepository.updateStatus(room.getId(), RoomStatus.AVAILABLE);
        }

        // TODO Phase 8 (Observer): fire a room-availability event here so subscribed
        // admins / waitlist entries are notified.
    }
}
