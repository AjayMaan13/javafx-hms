package com.hotel.repository;

import com.hotel.model.Waitlist;

import java.util.UUID;

public class WaitlistRepository extends BaseRepository<Waitlist, UUID> {

    public WaitlistRepository() {
        super(Waitlist.class);
    }
}
