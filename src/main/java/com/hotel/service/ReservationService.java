package com.hotel.service;

import com.hotel.controller.kiosk.BookingDraft;
import com.hotel.model.Addon;
import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.model.enums.ReservationStatus;
import com.hotel.model.enums.RoomType;
import com.hotel.repository.AddonRepository;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReservationService {

    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final AddonRepository addonRepository;
    private final PricingService pricingService;

    public ReservationService(GuestRepository guestRepository, RoomRepository roomRepository,
                               ReservationRepository reservationRepository, AddonRepository addonRepository,
                               PricingService pricingService) {
        this.guestRepository = guestRepository;
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
        this.addonRepository = addonRepository;
        this.pricingService = pricingService;
    }

    public Reservation createReservation(BookingDraft draft) {
        validateOccupancy(draft);
        validateDates(draft);

        List<Room> selectedRooms = resolveAvailableRooms(draft);
        List<Addon> selectedAddons = resolveAddons(draft);
        Guest guest = findOrCreateGuest(draft);

        double roomSubtotal = pricingService.calculateSubtotal(selectedRooms, draft.getCheckIn(), draft.getCheckOut());
        double addonSubtotal = selectedAddons.stream().mapToDouble(Addon::getPrice).sum();
        double subtotal = roomSubtotal + addonSubtotal;
        double tax = pricingService.calculateTax(subtotal);
        double total = subtotal + tax;

        return reservationRepository.createWithAssociations(guest, selectedRooms, selectedAddons,
                draft.getCheckIn(), draft.getCheckOut(), draft.getAdults(), draft.getChildren(),
                ReservationStatus.CONFIRMED, subtotal, tax, total);
    }

    private void validateOccupancy(BookingDraft draft) {
        if (draft.getAdults() < 1) {
            throw new BookingValidationException("At least 1 adult is required per booking.");
        }
        if (draft.getTotalRoomQuantity() == 0) {
            throw new BookingValidationException("Select at least one room.");
        }

        int totalGuests = draft.getAdults() + draft.getChildren();
        int totalCapacity = draft.getRoomSelections().entrySet().stream()
                .mapToInt(entry -> RoomFactory.capacityFor(entry.getKey()) * entry.getValue())
                .sum();

        if (totalCapacity < totalGuests) {
            throw new BookingValidationException(
                    totalGuests + " guests exceed the selected rooms' capacity (" + totalCapacity
                            + "). Choose a larger room or add another room.");
        }
    }

    private void validateDates(BookingDraft draft) {
        LocalDate checkIn = draft.getCheckIn();
        LocalDate checkOut = draft.getCheckOut();

        if (checkIn == null || checkOut == null) {
            throw new BookingValidationException("Check-in and check-out dates are required.");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new BookingValidationException("Check-in date cannot be in the past.");
        }
        if (!checkIn.isBefore(checkOut)) {
            throw new BookingValidationException("Check-out must be after check-in.");
        }
    }

    private List<Room> resolveAvailableRooms(BookingDraft draft) {
        List<Room> selected = new ArrayList<>();

        for (Map.Entry<RoomType, Integer> entry : draft.getRoomSelections().entrySet()) {
            RoomType type = entry.getKey();
            int quantity = entry.getValue();
            if (quantity <= 0) {
                continue;
            }

            List<Room> available = roomRepository.findAvailable(type, draft.getCheckIn(), draft.getCheckOut());
            if (available.size() < quantity) {
                throw new BookingValidationException(
                        "Only " + available.size() + " " + type + " room(s) available for the selected dates, but "
                                + quantity + " requested.");
            }
            selected.addAll(available.subList(0, quantity));
        }

        return selected;
    }

    private List<Addon> resolveAddons(BookingDraft draft) {
        if (draft.getSelectedAddonIds().isEmpty()) {
            return List.of();
        }
        return addonRepository.findAll().stream()
                .filter(addon -> draft.getSelectedAddonIds().contains(addon.getId()))
                .collect(Collectors.toList());
    }

    private Guest findOrCreateGuest(BookingDraft draft) {
        return guestRepository.findByEmail(draft.getGuestEmail())
                .orElseGet(() -> guestRepository.save(new Guest(
                        draft.getGuestFullName(), draft.getGuestPhone(), draft.getGuestEmail(),
                        draft.getGuestAddress(), draft.getGuestPostalCode())));
    }
}
