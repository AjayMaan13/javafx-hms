package com.hotel.controller.kiosk;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

public class OccupancyController implements KioskStepController {

    @FXML
    private Spinner<Integer> adultsSpinner;

    @FXML
    private Spinner<Integer> childrenSpinner;

    @FXML
    private Label errorLabel;

    private KioskShellController shell;
    private BookingDraft draft;

    @Override
    public void init(KioskShellController shell, BookingDraft draft) {
        this.shell = shell;
        this.draft = draft;

        adultsSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, Math.max(1, draft.getAdults())));
        childrenSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 12, draft.getChildren()));
    }

    @FXML
    private void handleBack() {
        shell.goBack();
    }

    @FXML
    private void handleNext() {
        draft.setAdults(adultsSpinner.getValue());
        draft.setChildren(childrenSpinner.getValue());

        if (draft.getAdults() < 1) {
            errorLabel.setText("At least 1 adult is required per booking.");
            return;
        }

        errorLabel.setText("");
        shell.goNext();
    }
}
