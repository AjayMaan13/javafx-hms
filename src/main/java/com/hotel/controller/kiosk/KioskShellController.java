package com.hotel.controller.kiosk;

import com.hotel.app.AppConfig;
import com.hotel.app.SceneRouter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class KioskShellController {

    private static final List<String> STEP_FILES = List.of(
            "welcome.fxml",
            "occupancy.fxml",
            "dates.fxml",
            "room-selection.fxml",
            "addons.fxml",
            "guest-details.fxml",
            "estimate.fxml",
            "confirmation.fxml"
    );

    private static final List<String> STEP_LABELS = List.of(
            "Welcome",
            "Step 1 of 7 · Occupancy",
            "Step 2 of 7 · Dates",
            "Step 3 of 7 · Rooms",
            "Step 4 of 7 · Add-ons",
            "Step 5 of 7 · Your details",
            "Step 6 of 7 · Estimate",
            "Done"
    );

    @FXML
    private Label stepLabel;

    @FXML
    private StackPane stepContainer;

    private final BookingDraft draft = new BookingDraft();
    private AppConfig appConfig;
    private SceneRouter router;
    private int currentIndex = 0;
    private UUID lastReservationId;
    private String lastLoyaltyNumber;

    public void setRouter(SceneRouter router) {
        this.router = router;
    }

    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
        showStep(0);
    }

    /** Header "Staff Login" button → hand the single window over to the admin login. */
    @FXML
    private void handleStaffLogin() {
        router.showAdminLogin();
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }

    public BookingDraft getDraft() {
        return draft;
    }

    public UUID getLastReservationId() {
        return lastReservationId;
    }

    public void setLastReservationId(UUID lastReservationId) {
        this.lastReservationId = lastReservationId;
    }

    /** Loyalty number issued on the last booking, or null if the guest didn't enrol. */
    public String getLastLoyaltyNumber() {
        return lastLoyaltyNumber;
    }

    public void setLastLoyaltyNumber(String lastLoyaltyNumber) {
        this.lastLoyaltyNumber = lastLoyaltyNumber;
    }

    public void goNext() {
        if (currentIndex < STEP_FILES.size() - 1) {
            showStep(currentIndex + 1);
        }
    }

    public void goBack() {
        if (currentIndex > 0) {
            showStep(currentIndex - 1);
        }
    }

    public void restart() {
        draft.reset();
        showStep(0);
    }

    /**
     * Feedback is a separate flow from the booking wizard (not part of the indexed step
     * list) — reachable from Welcome, returns to Welcome when done.
     */
    public void showFeedback() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/kiosk/feedback.fxml"));
            Parent node = loader.load();

            KioskStepController controller = loader.getController();
            controller.init(this, draft);

            stepContainer.getChildren().setAll(node);
            stepLabel.setText("Guest Feedback");

            controller.onShow();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load feedback screen", e);
        }
    }

    public void returnToWelcome() {
        showStep(0);
    }

    private void showStep(int index) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/kiosk/" + STEP_FILES.get(index)));
            Parent node = loader.load();

            KioskStepController controller = loader.getController();
            controller.init(this, draft);

            stepContainer.getChildren().setAll(node);
            stepLabel.setText(STEP_LABELS.get(index));
            currentIndex = index;

            controller.onShow();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load kiosk step: " + STEP_FILES.get(index), e);
        }
    }
}
