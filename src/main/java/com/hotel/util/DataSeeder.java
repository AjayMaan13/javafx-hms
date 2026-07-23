package com.hotel.util;

import com.hotel.model.Addon;
import com.hotel.model.AdminUser;
import com.hotel.model.LoyaltyConfig;
import com.hotel.model.Room;
import com.hotel.model.enums.Role;
import com.hotel.model.enums.RoomType;
import com.hotel.repository.AddonRepository;
import com.hotel.repository.AdminUserRepository;
import com.hotel.repository.LoyaltyConfigRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.security.BCryptPasswordHasher;
import com.hotel.service.RoomFactory;

public class DataSeeder {

    private final RoomRepository roomRepository;
    private final AddonRepository addonRepository;
    private final AdminUserRepository adminUserRepository;
    private final LoyaltyConfigRepository loyaltyConfigRepository;
    private final BCryptPasswordHasher passwordHasher;

    public DataSeeder(RoomRepository roomRepository, AddonRepository addonRepository,
                      AdminUserRepository adminUserRepository, LoyaltyConfigRepository loyaltyConfigRepository,
                      BCryptPasswordHasher passwordHasher) {
        this.roomRepository = roomRepository;
        this.addonRepository = addonRepository;
        this.adminUserRepository = adminUserRepository;
        this.loyaltyConfigRepository = loyaltyConfigRepository;
        this.passwordHasher = passwordHasher;
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

        if (adminUserRepository.findAll().isEmpty()) {
            adminUserRepository.save(new AdminUser("admin", passwordHasher.hash("admin123"), Role.ADMIN));
            adminUserRepository.save(new AdminUser("manager", passwordHasher.hash("manager123"), Role.MANAGER));
        }

        if (loyaltyConfigRepository.findAll().isEmpty()) {
            // 1 point per $1 paid; each point redeems for $0.05; redemption caps at 30% of a bill.
            loyaltyConfigRepository.save(new LoyaltyConfig(1.0, 0.05, 0.30, true));
        }
    }

    private void seedRooms(RoomType type, int startingNumber, int count) {
        for (int i = 0; i < count; i++) {
            Room room = RoomFactory.create(type, String.valueOf(startingNumber + i));
            roomRepository.save(room);
        }
    }
}
