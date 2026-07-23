package com.hotel.controller.kiosk;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ConfirmationController implements KioskStepController {

    @FXML
    private Label confirmationLabel;
    @FXML
    private Label loyaltyLabel;

    private KioskShellController shell;

    @Override
    public void init(KioskShellController shell, BookingDraft draft) {
        this.shell = shell;
    }

    @Override
    public void onShow() {
        confirmationLabel.setText("Confirmation #: " + shell.getLastReservationId());

        String loyaltyNumber = shell.getLastLoyaltyNumber();
        boolean enrolled = loyaltyNumber != null;
        loyaltyLabel.setManaged(enrolled);
        loyaltyLabel.setVisible(enrolled);
        if (enrolled) {
            loyaltyLabel.setText("Maple Rewards number: " + loyaltyNumber);
        }
    }

    @FXML
    private void handleDone() {
        shell.restart();
    }
}
