package com.hotel.service;

import com.hotel.controller.kiosk.BookingDraft;
import com.hotel.model.Reservation;
import com.hotel.model.enums.RoomType;
import com.hotel.repository.AddonRepository;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.service.pricing.StandardPricingStrategy;
import com.hotel.util.DataSeeder;
import com.hotel.util.PersistenceManager;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationServiceTest {

    // Randomized per run (rather than a fixed constant) so repeated `mvn test` invocations
    // against the same dev DB file don't eventually exhaust the 5 seeded SINGLE rooms for a
    // fixed date window.
    private static final long DAY_OFFSET = 30 + new Random().nextInt(3000);
    private static final LocalDate CHECK_IN = LocalDate.now().plusDays(DAY_OFFSET);
    private static final LocalDate CHECK_OUT = CHECK_IN.plusDays(2);

    private ReservationService newService() {
        GuestRepository guestRepository = new GuestRepository();
        RoomRepository roomRepository = new RoomRepository();
        ReservationRepository reservationRepository = new ReservationRepository();
        AddonRepository addonRepository = new AddonRepository();
        new DataSeeder(roomRepository, addonRepository, new com.hotel.repository.AdminUserRepository()).seedIfEmpty();

        PricingService pricingService = new PricingService(new StandardPricingStrategy());
        return new ReservationService(guestRepository, roomRepository, reservationRepository, addonRepository,
                pricingService);
    }

    private BookingDraft validSingleGuestDraft(String email, LocalDate checkIn, LocalDate checkOut) {
        BookingDraft draft = new BookingDraft();
        draft.setAdults(1);
        draft.setChildren(0);
        draft.setCheckIn(checkIn);
        draft.setCheckOut(checkOut);
        draft.setRoomQuantity(RoomType.SINGLE, 1);
        draft.setGuestFirstName("Ada");
        draft.setGuestLastName("Lovelace");
        draft.setGuestPhone("555-0199");
        draft.setGuestEmail(email);
        draft.setGuestAddress("1 Analytical Engine Way");
        draft.setGuestPostalCode("A1B 2C3");
        return draft;
    }

    @Test
    void acceptsASinglePersonBookingAndPersistsGuestReservationAndRoomLink() {
        ReservationService service = newService();
        String email = "ada." + UUID.randomUUID() + "@example.com";
        BookingDraft draft = validSingleGuestDraft(email, CHECK_IN, CHECK_OUT);

        Reservation reservation = service.createReservation(draft);

        assertNotNull(reservation.getId());
        // 1 Single room ($100/night) x 2 nights, Standard strategy, 13% tax: 200 + 26 = 226.
        assertEquals(226.0, reservation.getTotal(), 0.001);

        EntityManager em = PersistenceManager.getInstance().newEntityManager();
        try {
            Long roomLinks = em.createQuery(
                            "SELECT COUNT(r) FROM Reservation res JOIN res.rooms r WHERE res.id = :id", Long.class)
                    .setParameter("id", reservation.getId())
                    .getSingleResult();
            assertEquals(1L, roomLinks);

            Long guestCount = em.createQuery(
                            "SELECT COUNT(g) FROM Guest g WHERE g.email = :email", Long.class)
                    .setParameter("email", email)
                    .getSingleResult();
            assertEquals(1L, guestCount);
        } finally {
            em.close();
        }
    }

    @Test
    void rejectsOverCapacityBookingWithAnActionableMessage() {
        ReservationService service = newService();
        BookingDraft draft = validSingleGuestDraft(
                "overcapacity." + UUID.randomUUID() + "@example.com", CHECK_IN.plusDays(100), CHECK_OUT.plusDays(100));
        draft.setAdults(3);
        draft.setRoomQuantity(RoomType.SINGLE, 1); // Single capacity is 2, but 3 adults requested.

        BookingValidationException exception = assertThrows(BookingValidationException.class,
                () -> service.createReservation(draft));

        assertTrue(exception.getMessage().contains("exceed"));
        assertTrue(exception.getMessage().contains("capacity"));
    }

    @Test
    void reusesAnExistingGuestByEmailInsteadOfCreatingADuplicate() {
        ReservationService service = newService();
        String email = "repeat." + UUID.randomUUID() + "@example.com";

        Reservation first = service.createReservation(
                validSingleGuestDraft(email, CHECK_IN.plusDays(200), CHECK_OUT.plusDays(200)));
        Reservation second = service.createReservation(
                validSingleGuestDraft(email, CHECK_IN.plusDays(210), CHECK_OUT.plusDays(210)));

        assertEquals(first.getGuest().getId(), second.getGuest().getId());
    }
}
