package com.hotel.controller.kiosk;

import com.hotel.model.enums.RoomType;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Accumulates the guest's in-progress choices across kiosk screens. Not an entity —
 * it only becomes a persisted Reservation when ReservationService.createReservation
 * is called on confirm.
 */
public class BookingDraft {

    private int adults;
    private int children;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private final Map<RoomType, Integer> roomSelections = new LinkedHashMap<>();
    private final Set<UUID> selectedAddonIds = new LinkedHashSet<>();

    private String guestFirstName;
    private String guestLastName;
    private String guestPhone;
    private String guestEmail;
    private String guestAddress;
    private String guestPostalCode;

    public void reset() {
        adults = 0;
        children = 0;
        checkIn = null;
        checkOut = null;
        roomSelections.clear();
        selectedAddonIds.clear();
        guestFirstName = null;
        guestLastName = null;
        guestPhone = null;
        guestEmail = null;
        guestAddress = null;
        guestPostalCode = null;
    }

    public int getAdults() {
        return adults;
    }

    public void setAdults(int adults) {
        this.adults = adults;
    }

    public int getChildren() {
        return children;
    }

    public void setChildren(int children) {
        this.children = children;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalDate checkIn) {
        this.checkIn = checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalDate checkOut) {
        this.checkOut = checkOut;
    }

    public Map<RoomType, Integer> getRoomSelections() {
        return roomSelections;
    }

    public void setRoomQuantity(RoomType type, int quantity) {
        roomSelections.put(type, quantity);
    }

    public int getTotalRoomQuantity() {
        return roomSelections.values().stream().mapToInt(Integer::intValue).sum();
    }

    public Set<UUID> getSelectedAddonIds() {
        return selectedAddonIds;
    }

    public String getGuestFirstName() {
        return guestFirstName;
    }

    public void setGuestFirstName(String guestFirstName) {
        this.guestFirstName = guestFirstName;
    }

    public String getGuestLastName() {
        return guestLastName;
    }

    public void setGuestLastName(String guestLastName) {
        this.guestLastName = guestLastName;
    }

    public String getGuestFullName() {
        String first = guestFirstName == null ? "" : guestFirstName.trim();
        String last = guestLastName == null ? "" : guestLastName.trim();
        return (first + " " + last).trim();
    }

    public String getGuestPhone() {
        return guestPhone;
    }

    public void setGuestPhone(String guestPhone) {
        this.guestPhone = guestPhone;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }

    public String getGuestAddress() {
        return guestAddress;
    }

    public void setGuestAddress(String guestAddress) {
        this.guestAddress = guestAddress;
    }

    public String getGuestPostalCode() {
        return guestPostalCode;
    }

    public void setGuestPostalCode(String guestPostalCode) {
        this.guestPostalCode = guestPostalCode;
    }
}
