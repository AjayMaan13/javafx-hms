package com.hotel.util;

import com.hotel.model.Addon;
import com.hotel.model.Room;
import com.hotel.model.enums.RoomType;
import com.hotel.repository.AddonRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.service.RoomFactory;

public class DataSeeder {

    private final RoomRepository roomRepository;
    private final AddonRepository addonRepository;

    public DataSeeder(RoomRepository roomRepository, AddonRepository addonRepository) {
        this.roomRepository = roomRepository;
        this.addonRepository = addonRepository;
    }

    public void seedIfEmpty() {
        if (roomRepository.findAll().isEmpty()) {
            seedRooms(RoomType.SINGLE, 101, 5);
            seedRooms(RoomType.DOUBLE, 201, 5);
            seedRooms(RoomType.DELUXE, 301, 3);
            seedRooms(RoomType.PENTHOUSE, 401, 2);
        }

        if (addonRepository.findAll().isEmpty()) {
            addonRepository.save(new Addon("Wi-Fi", 5.0));
            addonRepository.save(new Addon("Breakfast", 12.0));
            addonRepository.save(new Addon("Parking", 8.0));
            addonRepository.save(new Addon("Spa access", 20.0));
        }

        // TODO Final: seed one AdminUser (BCrypt-hashed password) here once Phase 1B's
        // AdminUser entity/repository exist. That entity is teammate-owned (Phase 1B).
    }

    private void seedRooms(RoomType type, int startingNumber, int count) {
        for (int i = 0; i < count; i++) {
            Room room = RoomFactory.create(type, String.valueOf(startingNumber + i));
            roomRepository.save(room);
        }
    }
}
