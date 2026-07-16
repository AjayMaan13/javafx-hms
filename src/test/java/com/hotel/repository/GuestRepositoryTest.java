package com.hotel.repository;

import com.hotel.model.Guest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GuestRepositoryTest {

    @Test
    void savesAndFindsAllGuests() {
        GuestRepository repository = new GuestRepository();

        Guest guest = new Guest("Alan Turing", "555-0102", "alan@example.com", "1 Bletchley Park", "M11 1A1");
        repository.save(guest);

        assertTrue(repository.findAll().stream().anyMatch(g -> g.getId().equals(guest.getId())));
    }
}
