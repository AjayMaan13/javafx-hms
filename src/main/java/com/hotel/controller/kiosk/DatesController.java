package com.hotel.controller.kiosk;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import java.time.LocalDate;

public class DatesController implements KioskStepController {

    @FXML
    private DatePicker checkInPicker;

    @FXML
    private DatePicker checkOutPicker;

    @FXML
    private Label errorLabel;

    private KioskShellController shell;
    private BookingDraft draft;

    @Override
    public void init(KioskShellController shell, BookingDraft draft) {
        this.shell = shell;
        this.draft = draft;

        checkInPicker.setValue(draft.getCheckIn());
        checkOutPicker.setValue(draft.getCheckOut());
    }

    @FXML
    private void handleBack() {
        shell.goBack();
    }

    @FXML
    private void handleNext() {
        LocalDate checkIn = checkInPicker.getValue();
        LocalDate checkOut = checkOutPicker.getValue();

        if (checkIn == null || checkOut == null) {
            errorLabel.setText("Please select both a check-in and check-out date.");
            return;
        }
        if (checkIn.isBefore(LocalDate.now())) {
            errorLabel.setText("Check-in date cannot be in the past.");
            return;
        }
        if (!checkIn.isBefore(checkOut)) {
            errorLabel.setText("Check-out must be after check-in.");
            return;
        }

        draft.setCheckIn(checkIn);
        draft.setCheckOut(checkOut);
        errorLabel.setText("");
        shell.goNext();
    }
}
