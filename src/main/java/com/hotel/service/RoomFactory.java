package com.hotel.service;

import com.hotel.model.Room;
import com.hotel.model.enums.RoomStatus;
import com.hotel.model.enums.RoomType;

public class RoomFactory {

    public static Room create(RoomType type, String roomNumber) {
        Room room = new Room(type, basePriceFor(type), RoomStatus.AVAILABLE);
        room.setRoomNumber(roomNumber);
        return room;
    }

    public static double basePriceFor(RoomType type) {
        switch (type) {
            case SINGLE:
                return 100.0;
            case DOUBLE:
                return 160.0;
            case DELUXE:
                return 220.0;
            case PENTHOUSE:
                return 400.0;
            default:
                throw new IllegalArgumentException("Unknown room type: " + type);
        }
    }

    public static int capacityFor(RoomType type) {
        switch (type) {
            case SINGLE:
                return 2;
            case DOUBLE:
                return 4;
            case DELUXE:
                return 2;
            case PENTHOUSE:
                return 2;
            default:
                throw new IllegalArgumentException("Unknown room type: " + type);
        }
    }
}
