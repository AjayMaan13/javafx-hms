package com.hotel.controller.kiosk;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class GuestDetailsController implements KioskStepController {

    private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField postalCodeField;
    @FXML
    private Label errorLabel;

    private KioskShellController shell;
    private BookingDraft draft;

    @Override
    public void init(KioskShellController shell, BookingDraft draft) {
        this.shell = shell;
        this.draft = draft;

        firstNameField.setText(nullToEmpty(draft.getGuestFirstName()));
        lastNameField.setText(nullToEmpty(draft.getGuestLastName()));
        phoneField.setText(nullToEmpty(draft.getGuestPhone()));
        emailField.setText(nullToEmpty(draft.getGuestEmail()));
        addressField.setText(nullToEmpty(draft.getGuestAddress()));
        postalCodeField.setText(nullToEmpty(draft.getGuestPostalCode()));
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    @FXML
    private void handleBack() {
        shell.goBack();
    }

    @FXML
    private void handleNext() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            errorLabel.setText("First and last name are required.");
            return;
        }
        if (phone.isEmpty()) {
            errorLabel.setText("Phone number is required.");
            return;
        }
        if (!email.matches(EMAIL_PATTERN)) {
            errorLabel.setText("Enter a valid email address.");
            return;
        }

        draft.setGuestFirstName(firstName);
        draft.setGuestLastName(lastName);
        draft.setGuestPhone(phone);
        draft.setGuestEmail(email);
        draft.setGuestAddress(addressField.getText().trim());
        draft.setGuestPostalCode(postalCodeField.getText().trim());

        errorLabel.setText("");
        shell.goNext();
    }
}
