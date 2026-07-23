package com.hotel.service;

import com.hotel.config.DiscountPolicy;
import com.hotel.model.AdminUser;
import com.hotel.model.Billing;
import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.model.enums.ReservationStatus;
import com.hotel.model.enums.Role;
import com.hotel.model.enums.RoomStatus;
import com.hotel.model.enums.RoomType;
import com.hotel.repository.BillingRepository;
import com.hotel.repository.GuestRepository;
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

class DiscountServiceTest {

    private static final long DAY_OFFSET = 12000 + new Random().nextInt(3000);

    private final GuestRepository guestRepository = new GuestRepository();
    private final RoomRepository roomRepository = new RoomRepository();
    private final ReservationRepository reservationRepository = new ReservationRepository();
    private final BillingRepository billingRepository = new BillingRepository();

    private final DiscountService discountService = new DiscountService(billingRepository, new DiscountPolicy());

    private Reservation newReservationWithBill(double total) {
        Guest guest = new Guest("Discount Guest", "555-0500", "disc." + UUID.randomUUID() + "@example.com",
                "1 Deal St", "D3A 4L5");
        guestRepository.save(guest);
        Room room = roomRepository.save(new Room(RoomType.DOUBLE, 160.0, RoomStatus.AVAILABLE));
        LocalDate checkIn = LocalDate.now().plusDays(DAY_OFFSET);
        Reservation reservation = reservationRepository.createWithAssociations(guest, List.of(room), List.of(),
                checkIn, checkIn.plusDays(2), 1, 0, ReservationStatus.CONFIRMED, total, 0.0, total);
        billingRepository.createFor(reservation.getId(), total);
        return reservation;
    }

    @Test
    void adminExceedingFifteenPercentIsRejected() {
        Reservation reservation = newReservationWithBill(300.0);
        AdminUser admin = new AdminUser("admin-cap-" + UUID.randomUUID(), "hash", Role.ADMIN);

        DiscountException ex = assertThrows(DiscountException.class,
                () -> discountService.apply(reservation, 20.0, admin));
        assertTrue(ex.getMessage().toLowerCase().contains("capped"));
    }

    @Test
    void managerApplyingTwentyFivePercentSucceedsAndReducesTheBalance() {
        Reservation reservation = newReservationWithBill(300.0);
        AdminUser manager = new AdminUser("manager-cap-" + UUID.randomUUID(), "hash", Role.MANAGER);

        double discount = discountService.apply(reservation, 25.0, manager);
        assertEquals(75.0, discount, 0.001); // 25% of $300

        Billing billing = billingRepository.findByReservation(reservation).orElseThrow();
        assertEquals(75.0, billing.getDiscount(), 0.001);
        assertEquals(225.0, billing.getBalance(), 0.001);
    }

    @Test
    void adminAtExactlyFifteenPercentIsAllowed() {
        Reservation reservation = newReservationWithBill(200.0);
        AdminUser admin = new AdminUser("admin-edge-" + UUID.randomUUID(), "hash", Role.ADMIN);

        double discount = discountService.apply(reservation, 15.0, admin);
        assertEquals(30.0, discount, 0.001);
    }
}
