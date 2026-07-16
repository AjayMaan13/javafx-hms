package com.hotel.repository;

import com.hotel.model.Reservation;

import java.util.UUID;

public class ReservationRepository extends BaseRepository<Reservation, UUID> {

    public ReservationRepository() {
        super(Reservation.class);
    }
}
