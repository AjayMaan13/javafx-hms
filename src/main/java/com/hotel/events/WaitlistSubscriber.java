package com.hotel.events;

import com.hotel.model.Waitlist;
import com.hotel.repository.WaitlistRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Concrete Observer: when a room becomes available, flags any matching WAITING entries so
 * the admin waitlist screen surfaces them for conversion.
 */
public class WaitlistSubscriber implements Observer<RoomAvailabilityEvent> {

    public static final String STATUS_WAITING = "WAITING";
    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_CONVERTED = "CONVERTED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private final WaitlistRepository waitlistRepository;

    public WaitlistSubscriber(WaitlistRepository waitlistRepository) {
        this.waitlistRepository = waitlistRepository;
    }

    @Override
    public void onEvent(RoomAvailabilityEvent event) {
        List<Waitlist> matches = waitlistRepository.findAll().stream()
                .filter(entry -> STATUS_WAITING.equals(entry.getStatus()))
                .filter(entry -> entry.getRequestedType() == event.getRoomType())
                .filter(entry -> entry.getStartDate().isBefore(event.getCheckOut())
                        && event.getCheckIn().isBefore(entry.getEndDate()))
                .collect(Collectors.toList());

        for (Waitlist entry : matches) {
            waitlistRepository.updateStatus(entry.getId(), STATUS_AVAILABLE);
        }
    }
}
