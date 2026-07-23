package com.hotel.repository;

import com.hotel.model.Waitlist;

import java.util.UUID;

public class WaitlistRepository extends BaseRepository<Waitlist, UUID> {

    public WaitlistRepository() {
        super(Waitlist.class);
    }

    public Waitlist updateStatus(UUID waitlistId, String status) {
        return inTransaction(em -> {
            Waitlist waitlist = em.find(Waitlist.class, waitlistId);
            waitlist.setStatus(status);
            return waitlist;
        });
    }
}
