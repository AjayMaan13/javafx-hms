package com.hotel.util;

import com.hotel.repository.RoomRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataSeederTest {

    @Test
    void seedsFifteenRoomsOnlyWhenTheTableIsEmptyAndNeverDuplicatesOnRepeatCalls() {
        RoomRepository roomRepository = new RoomRepository();
        DataSeeder seeder = new DataSeeder(roomRepository);

        int countBeforeSeeding = roomRepository.findAll().size();

        seeder.seedIfEmpty();
        int countAfterFirstCall = roomRepository.findAll().size();

        if (countBeforeSeeding == 0) {
            // Only true against a genuinely fresh database (e.g. `rm -rf data && mvn test
            // -Dtest=DataSeederTest`) — other test classes share this dev DB file and may
            // have already inserted rooms by the time the full suite reaches this test.
            assertEquals(15, countAfterFirstCall);
        } else {
            assertEquals(countBeforeSeeding, countAfterFirstCall);
        }

        seeder.seedIfEmpty();
        int countAfterSecondCall = roomRepository.findAll().size();
        assertEquals(countAfterFirstCall, countAfterSecondCall);
    }
}
