package com.hotel.controller.kiosk;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ConfirmationController implements KioskStepController {

    @FXML
    private Label confirmationLabel;

    private KioskShellController shell;

    @Override
    public void init(KioskShellController shell, BookingDraft draft) {
        this.shell = shell;
    }

    @Override
    public void onShow() {
        confirmationLabel.setText("Confirmation #: " + shell.getLastReservationId());
    }

    @FXML
    private void handleDone() {
        shell.restart();
    }
}
