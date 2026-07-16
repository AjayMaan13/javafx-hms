package com.hotel.repository;

import com.hotel.model.Addon;

import java.util.UUID;

public class AddonRepository extends BaseRepository<Addon, UUID> {

    public AddonRepository() {
        super(Addon.class);
    }
}
