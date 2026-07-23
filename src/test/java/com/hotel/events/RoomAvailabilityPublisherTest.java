package com.hotel.events;

import com.hotel.model.enums.RoomType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomAvailabilityPublisherTest {

    @Test
    void everyAttachedObserverReceivesAPublishedEvent() {
        RoomAvailabilityPublisher publisher = new RoomAvailabilityPublisher();
        List<RoomAvailabilityEvent> receivedByFirst = new ArrayList<>();
        List<RoomAvailabilityEvent> receivedBySecond = new ArrayList<>();

        publisher.attach(receivedByFirst::add);
        publisher.attach(receivedBySecond::add);

        RoomAvailabilityEvent event = new RoomAvailabilityEvent(
                RoomType.DOUBLE, LocalDate.now(), LocalDate.now().plusDays(2));
        publisher.publish(event);

        assertEquals(1, receivedByFirst.size());
        assertEquals(1, receivedBySecond.size());
        assertTrue(receivedByFirst.get(0) == event);
    }

    @Test
    void aDetachedObserverStopsReceivingEvents() {
        RoomAvailabilityPublisher publisher = new RoomAvailabilityPublisher();
        List<RoomAvailabilityEvent> received = new ArrayList<>();
        Observer<RoomAvailabilityEvent> observer = received::add;

        publisher.attach(observer);
        publisher.publish(new RoomAvailabilityEvent(RoomType.SINGLE, LocalDate.now(), LocalDate.now().plusDays(1)));
        publisher.detach(observer);
        publisher.publish(new RoomAvailabilityEvent(RoomType.SINGLE, LocalDate.now(), LocalDate.now().plusDays(1)));

        assertEquals(1, received.size());
    }

    @Test
    void publishingWithNoObserversAttachedDoesNothingAndDoesNotThrow() {
        RoomAvailabilityPublisher publisher = new RoomAvailabilityPublisher();

        publisher.publish(new RoomAvailabilityEvent(RoomType.PENTHOUSE, LocalDate.now(), LocalDate.now().plusDays(1)));
        // No assertion needed beyond "this didn't throw" — the publisher has zero knowledge
        // of any concrete observer type, which is the whole point of the pattern.
    }
}
