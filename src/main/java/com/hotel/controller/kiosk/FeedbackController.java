package com.hotel.controller.kiosk;

import com.hotel.model.Reservation;
import com.hotel.service.FeedbackException;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.List;

public class FeedbackController implements KioskStepController {

    @FXML
    private TextField emailField;
    @FXML
    private VBox stayBox;
    @FXML
    private ComboBox<Reservation> reservationCombo;
    @FXML
    private ComboBox<Integer> ratingCombo;
    @FXML
    private TextArea commentArea;
    @FXML
    private VBox confirmationBox;
    @FXML
    private Label messageLabel;

    private static final String DEFAULT_EMAIL = "guest@demo.com";

    private KioskShellController shell;
    private BookingDraft draft;

    @Override
    public void init(KioskShellController shell, BookingDraft draft) {
        this.shell = shell;
        this.draft = draft;
    }

    @Override
    public void onShow() {
        // Prefill with the email just booked with (or a default) so feedback is easy to try.
        String prefill = draft != null && draft.getGuestEmail() != null && !draft.getGuestEmail().isBlank()
                ? draft.getGuestEmail() : DEFAULT_EMAIL;
        emailField.setText(prefill);
        hide(stayBox);
        hide(confirmationBox);
        messageLabel.setText("");

        ratingCombo.getItems().setAll(5, 4, 3, 2, 1);
        reservationCombo.setConverter(new StringConverter<Reservation>() {
            @Override
            public String toString(Reservation reservation) {
                return reservation == null ? "" : reservation.getCheckIn() + " to " + reservation.getCheckOut();
            }

            @Override
            public Reservation fromString(String string) {
                return null;
            }
        });
    }

    @FXML
    private void handleFindStay() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        if (email.isEmpty()) {
            messageLabel.setText("Enter the email you booked with.");
            return;
        }

        List<Reservation> eligible = shell.getAppConfig().getFeedbackService().findEligibleReservations(email);
        if (eligible.isEmpty()) {
            messageLabel.setText("No checked-out, fully-settled stays found for that email — "
                    + "or feedback was already submitted for all of them.");
            hide(stayBox);
            return;
        }

        reservationCombo.getItems().setAll(eligible);
        reservationCombo.getSelectionModel().selectFirst();
        ratingCombo.getSelectionModel().selectFirst();
        commentArea.clear();
        messageLabel.setText("");
        show(stayBox);
    }

    @FXML
    private void handleSubmit() {
        Reservation reservation = reservationCombo.getValue();
        Integer rating = ratingCombo.getValue();

        if (reservation == null) {
            messageLabel.setText("Select a stay.");
            return;
        }
        if (rating == null) {
            messageLabel.setText("Select a rating.");
            return;
        }

        try {
            shell.getAppConfig().getFeedbackService().submit(reservation, rating, commentArea.getText());
            hide(stayBox);
            show(confirmationBox);
            messageLabel.setText("");
        } catch (FeedbackException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        shell.returnToWelcome();
    }

    private void show(VBox box) {
        box.setManaged(true);
        box.setVisible(true);
    }

    private void hide(VBox box) {
        box.setManaged(false);
        box.setVisible(false);
    }
}
