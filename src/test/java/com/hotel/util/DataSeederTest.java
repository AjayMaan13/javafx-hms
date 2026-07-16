package com.hotel.util;

import com.hotel.repository.AddonRepository;
import com.hotel.repository.RoomRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataSeederTest {

    @Test
    void seedsRoomsAndAddonsOnlyWhenEmptyAndNeverDuplicatesOnRepeatCalls() {
        RoomRepository roomRepository = new RoomRepository();
        AddonRepository addonRepository = new AddonRepository();
        DataSeeder seeder = new DataSeeder(roomRepository, addonRepository);

        int roomCountBefore = roomRepository.findAll().size();
        int addonCountBefore = addonRepository.findAll().size();

        seeder.seedIfEmpty();
        int roomCountAfterFirst = roomRepository.findAll().size();
        int addonCountAfterFirst = addonRepository.findAll().size();

        if (roomCountBefore == 0) {
            // Only true against a genuinely fresh database (e.g. `rm -rf data && mvn test
            // -Dtest=DataSeederTest`) — other test classes share this dev DB file and may
            // have already inserted rooms by the time the full suite reaches this test.
            assertEquals(15, roomCountAfterFirst);
        } else {
            assertEquals(roomCountBefore, roomCountAfterFirst);
        }

        if (addonCountBefore == 0) {
            assertEquals(4, addonCountAfterFirst);
        } else {
            assertEquals(addonCountBefore, addonCountAfterFirst);
        }

        seeder.seedIfEmpty();
        assertEquals(roomCountAfterFirst, roomRepository.findAll().size());
        assertEquals(addonCountAfterFirst, addonRepository.findAll().size());
    }
}
