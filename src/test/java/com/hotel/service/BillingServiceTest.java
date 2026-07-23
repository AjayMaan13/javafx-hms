package com.hotel.service;

import com.hotel.model.Billing;
import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.model.enums.PaymentMethod;
import com.hotel.model.enums.ReservationStatus;
import com.hotel.model.enums.RoomStatus;
import com.hotel.repository.BillingRepository;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.PaymentRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BillingServiceTest {

    private static final long DAY_OFFSET = 5000 + new Random().nextInt(3000);

    private final GuestRepository guestRepository = new GuestRepository();
    private final RoomRepository roomRepository = new RoomRepository();
    private final ReservationRepository reservationRepository = new ReservationRepository();
    private final BillingRepository billingRepository = new BillingRepository();
    private final PaymentRepository paymentRepository = new PaymentRepository();

    private final BillingService billingService = new BillingService(
            billingRepository, paymentRepository, reservationRepository, roomRepository);

    private Reservation newReservationWithTotal(double total, LocalDate checkIn, LocalDate checkOut, Room room) {
        Guest guest = new Guest("Bill Payer", "555-0300", "bill." + UUID.randomUUID() + "@example.com",
                "1 Ledger Ln", "L3D 4G5");
        guestRepository.save(guest);
        return reservationRepository.createWithAssociations(guest, List.of(room), List.of(),
                checkIn, checkOut, 1, 0, ReservationStatus.CONFIRMED, total, 0.0, total);
    }

    @Test
    void partialPaymentLowersBalanceAndRefundRaisesItBack() {
        Room room = roomRepository.save(new Room(com.hotel.model.enums.RoomType.SINGLE, 100.0, RoomStatus.AVAILABLE));
        LocalDate checkIn = LocalDate.now().plusDays(DAY_OFFSET);
        Reservation reservation = newReservationWithTotal(300.0, checkIn, checkIn.plusDays(2), room);

        billingService.createBillingFor(reservation);

        Billing afterPayment = billingService.recordPayment(reservation, PaymentMethod.CARD, 150.0);
        assertEquals(150.0, afterPayment.getTotalPaid(), 0.001);
        assertEquals(150.0, afterPayment.getBalance(), 0.001);

        Billing afterRefund = billingService.recordPayment(reservation, PaymentMethod.CARD, -50.0);
        assertEquals(100.0, afterRefund.getTotalPaid(), 0.001);
        assertEquals(200.0, afterRefund.getBalance(), 0.001);

        // Both the payment and the refund are recorded as Payment rows (refund = negative).
        assertEquals(2, paymentRepository.findByReservation(reservation).size());
    }

    @Test
    void payingMoreThanTheBalanceIsRejected() {
        Room room = roomRepository.save(new Room(com.hotel.model.enums.RoomType.SINGLE, 100.0, RoomStatus.AVAILABLE));
        LocalDate checkIn = LocalDate.now().plusDays(DAY_OFFSET + 100);
        Reservation reservation = newReservationWithTotal(200.0, checkIn, checkIn.plusDays(2), room);
        billingService.createBillingFor(reservation);

        BillingException ex = assertThrows(BillingException.class,
                () -> billingService.recordPayment(reservation, PaymentMethod.CASH, 250.0));
        assertTrue(ex.getMessage().toLowerCase().contains("exceeds"));
    }

    @Test
    void checkoutIsBlockedWhileABalanceRemains() {
        Room room = roomRepository.save(new Room(com.hotel.model.enums.RoomType.DOUBLE, 160.0, RoomStatus.AVAILABLE));
        LocalDate checkIn = LocalDate.now().plusDays(DAY_OFFSET + 200);
        Reservation reservation = newReservationWithTotal(320.0, checkIn, checkIn.plusDays(2), room);
        billingService.createBillingFor(reservation);
        billingService.recordPayment(reservation, PaymentMethod.CARD, 100.0); // still $220 owing

        BillingException ex = assertThrows(BillingException.class,
                () -> billingService.checkout(reservation));
        assertTrue(ex.getMessage().toLowerCase().contains("outstanding balance"));
    }

    @Test
    void fullySettledCheckoutMarksReservationCheckedOutAndFreesRooms() {
        Room room = roomRepository.save(new Room(com.hotel.model.enums.RoomType.DELUXE, 220.0, RoomStatus.AVAILABLE));
        LocalDate checkIn = LocalDate.now().plusDays(DAY_OFFSET + 300);
        Reservation reservation = newReservationWithTotal(220.0, checkIn, checkIn.plusDays(1), room);
        billingService.createBillingFor(reservation);
        billingService.recordPayment(reservation, PaymentMethod.CASH, 220.0); // paid in full

        billingService.checkout(reservation);

        Reservation reloaded = reservationRepository.findByIdWithRooms(reservation.getId()).orElseThrow();
        assertEquals(ReservationStatus.CHECKED_OUT, reloaded.getStatus());

        Room reloadedRoom = roomRepository.findById(room.getId()).orElseThrow();
        assertEquals(RoomStatus.AVAILABLE, reloadedRoom.getStatus());
    }
}
