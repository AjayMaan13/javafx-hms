package com.hotel.service;

import com.hotel.model.Billing;
import com.hotel.model.Guest;
import com.hotel.model.LoyaltyAccount;
import com.hotel.model.LoyaltyConfig;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.model.enums.ReservationStatus;
import com.hotel.model.enums.RoomStatus;
import com.hotel.model.enums.RoomType;
import com.hotel.repository.BillingRepository;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.LoyaltyAccountRepository;
import com.hotel.repository.LoyaltyConfigRepository;
import com.hotel.repository.LoyaltyTransactionRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoyaltyServiceTest {

    private static final long DAY_OFFSET = 9000 + new Random().nextInt(3000);

    private final GuestRepository guestRepository = new GuestRepository();
    private final RoomRepository roomRepository = new RoomRepository();
    private final ReservationRepository reservationRepository = new ReservationRepository();
    private final BillingRepository billingRepository = new BillingRepository();
    private final LoyaltyAccountRepository accountRepository = new LoyaltyAccountRepository();
    private final LoyaltyConfigRepository configRepository = new LoyaltyConfigRepository();
    private final LoyaltyTransactionRepository transactionRepository = new LoyaltyTransactionRepository();

    private final LoyaltyService loyaltyService = new LoyaltyService(
            accountRepository, configRepository, transactionRepository, billingRepository);

    @BeforeEach
    void ensureActiveConfig() {
        // earn 1 point/$1, redeem $0.05/point, cap 30% — matches DataSeeder defaults.
        if (configRepository.findActive().isEmpty()) {
            configRepository.save(new LoyaltyConfig(1.0, 0.05, 0.30, true));
        }
    }

    private Guest newGuest() {
        Guest guest = new Guest("Loyal Guest", "555-0400", "loyal." + UUID.randomUUID() + "@example.com",
                "1 Rewards Rd", "R3W 4R5");
        return guestRepository.save(guest);
    }

    @Test
    void enrollIssuesAnAccountWithAZeroBalanceAndALoyaltyNumberAndIsIdempotent() {
        Guest guest = newGuest();

        LoyaltyAccount account = loyaltyService.enroll(guest);
        assertEquals(0, account.getPointsBalance());
        assertTrue(account.getLoyaltyNumber().startsWith("ML-"));

        // Enrolling again returns the same account, not a duplicate.
        LoyaltyAccount again = loyaltyService.enroll(guest);
        assertEquals(account.getId(), again.getId());
    }

    @Test
    void earningPointsIncreasesTheBalanceAndWritesATransaction() {
        LoyaltyAccount account = loyaltyService.enroll(newGuest());

        int earned = loyaltyService.earn(account, 200.0, null); // 200 * 1.0 = 200 points
        assertEquals(200, earned);

        LoyaltyAccount reloaded = accountRepository.findById(account.getId()).orElseThrow();
        assertEquals(200, reloaded.getPointsBalance());
        assertEquals(1, loyaltyService.history(reloaded).size());
    }

    @Test
    void redeemingWithinTheCapAppliesADiscountAndDecrementsTheBalance() {
        LoyaltyAccount account = loyaltyService.enroll(newGuest());
        loyaltyService.earn(account, 1000.0, null); // 1000 points
        LoyaltyAccount funded = accountRepository.findById(account.getId()).orElseThrow();

        Billing billing = newBillingWithTotal(funded.getGuest(), 300.0);
        // Redeem 200 points = $10 discount. Cap is 30% of $300 = $90, so this is allowed.
        double discount = loyaltyService.redeem(funded, 200, billing, null);
        assertEquals(10.0, discount, 0.001);

        LoyaltyAccount afterRedeem = accountRepository.findById(account.getId()).orElseThrow();
        assertEquals(800, afterRedeem.getPointsBalance());

        Billing reloadedBilling = billingRepository.findById(billing.getId()).orElseThrow();
        assertEquals(10.0, reloadedBilling.getLoyaltyDiscount(), 0.001);
        assertEquals(290.0, reloadedBilling.getBalance(), 0.001);
    }

    @Test
    void redeemingBeyondTheCapIsRejected() {
        LoyaltyAccount account = loyaltyService.enroll(newGuest());
        loyaltyService.earn(account, 100000.0, null); // plenty of points
        LoyaltyAccount funded = accountRepository.findById(account.getId()).orElseThrow();

        Billing billing = newBillingWithTotal(funded.getGuest(), 100.0);
        // Cap is 30% of $100 = $30 → max 600 points. Redeeming 1000 points ($50) must fail.
        LoyaltyException ex = assertThrows(LoyaltyException.class,
                () -> loyaltyService.redeem(funded, 1000, billing, null));
        assertTrue(ex.getMessage().toLowerCase().contains("capped"));
    }

    private Billing newBillingWithTotal(Guest guest, double total) {
        Room room = roomRepository.save(new Room(RoomType.SINGLE, 100.0, RoomStatus.AVAILABLE));
        LocalDate checkIn = LocalDate.now().plusDays(DAY_OFFSET);
        Reservation reservation = reservationRepository.createWithAssociations(guest, List.of(room), List.of(),
                checkIn, checkIn.plusDays(2), 1, 0, ReservationStatus.CONFIRMED, total, 0.0, total);
        return billingRepository.createFor(reservation.getId(), total);
    }
}
