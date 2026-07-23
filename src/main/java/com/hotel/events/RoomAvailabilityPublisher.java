package com.hotel.events;

/** Concrete Subject for room-availability changes. Services call publish(); they don't
 *  know or care who's listening. */
public class RoomAvailabilityPublisher extends Subject<RoomAvailabilityEvent> {

    public void publish(RoomAvailabilityEvent event) {
        notifyObservers(event);
    }
}
