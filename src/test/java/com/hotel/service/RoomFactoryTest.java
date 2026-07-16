package com.hotel.service;

import com.hotel.model.Room;
import com.hotel.model.enums.RoomStatus;
import com.hotel.model.enums.RoomType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoomFactoryTest {

    @Test
    void createsEachRoomTypeWithItsConfiguredBasePriceAndDefaultsToAvailable() {
        assertRoom(RoomFactory.create(RoomType.SINGLE, "101"), RoomType.SINGLE, 100.0, "101");
        assertRoom(RoomFactory.create(RoomType.DOUBLE, "201"), RoomType.DOUBLE, 160.0, "201");
        assertRoom(RoomFactory.create(RoomType.DELUXE, "301"), RoomType.DELUXE, 220.0, "301");
        assertRoom(RoomFactory.create(RoomType.PENTHOUSE, "401"), RoomType.PENTHOUSE, 400.0, "401");
    }

    @Test
    void capacityMatchesTheBriefsOccupancyRules() {
        assertEquals(2, RoomFactory.capacityFor(RoomType.SINGLE));
        assertEquals(4, RoomFactory.capacityFor(RoomType.DOUBLE));
        assertEquals(2, RoomFactory.capacityFor(RoomType.DELUXE));
        assertEquals(2, RoomFactory.capacityFor(RoomType.PENTHOUSE));
    }

    private void assertRoom(Room room, RoomType type, double expectedPrice, String expectedRoomNumber) {
        assertEquals(type, room.getType());
        assertEquals(expectedPrice, room.getBasePrice());
        assertEquals(RoomStatus.AVAILABLE, room.getStatus());
        assertEquals(expectedRoomNumber, room.getRoomNumber());
    }
}
