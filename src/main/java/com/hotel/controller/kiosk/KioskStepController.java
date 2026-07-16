package com.hotel.controller.kiosk;

public interface KioskStepController {

    void init(KioskShellController shell, BookingDraft draft);

    default void onShow() {
    }
}
