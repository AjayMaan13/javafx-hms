package com.hotel.events;

import com.hotel.model.enums.RoomType;

import java.time.LocalDate;

/** Fired when a room of the given type becomes free for the given date range (checkout/cancel). */
public class RoomAvailabilityEvent {

    private final RoomType roomType;
    private final LocalDate checkIn;
    private final LocalDate checkOut;

    public RoomAvailabilityEvent(RoomType roomType, LocalDate checkIn, LocalDate checkOut) {
        this.roomType = roomType;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }
}
