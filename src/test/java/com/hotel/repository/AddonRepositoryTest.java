package com.hotel.repository;

import com.hotel.model.Addon;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AddonRepositoryTest {

    @Test
    void savesAndFindsAllAddons() {
        AddonRepository repository = new AddonRepository();

        Addon addon = new Addon("Wi-Fi", 5.0);
        repository.save(addon);

        assertTrue(repository.findAll().stream().anyMatch(a -> a.getId().equals(addon.getId())));
    }
}
