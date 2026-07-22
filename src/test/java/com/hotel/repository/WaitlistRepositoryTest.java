package com.hotel.repository;

import com.hotel.model.Guest;
import com.hotel.model.Waitlist;
import com.hotel.model.enums.RoomType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WaitlistRepositoryTest {

    @Test
    void savesAndFindsAllWaitlistEntries() {
        GuestRepository guestRepository = new GuestRepository();
        WaitlistRepository waitlistRepository = new WaitlistRepository();

        Guest guest = new Guest("Rosa Parks", "555-0108", "rosa@example.com", "1 Freedom Way", "R0S 3A4");
        guestRepository.save(guest);

        Waitlist entry = new Waitlist(guest, RoomType.DOUBLE,
                LocalDate.now().plusDays(420), LocalDate.now().plusDays(423), "WAITING");
        waitlistRepository.save(entry);

        assertTrue(waitlistRepository.findAll().stream().anyMatch(w -> w.getId().equals(entry.getId())));
    }
}
