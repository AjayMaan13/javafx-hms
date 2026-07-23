package com.hotel.controller.kiosk;

import javafx.fxml.FXML;

public class WelcomeController implements KioskStepController {

    private KioskShellController shell;

    @Override
    public void init(KioskShellController shell, BookingDraft draft) {
        this.shell = shell;
    }

    @FXML
    private void handleStartBooking() {
        shell.getDraft().reset();
        shell.goNext();
    }

    @FXML
    private void handleLeaveFeedback() {
        shell.showFeedback();
    }
}
