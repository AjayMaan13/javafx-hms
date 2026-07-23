package com.hotel.controller.admin;

import com.hotel.app.AppConfig;
import com.hotel.app.SceneRouter;
import com.hotel.model.AdminUser;
import com.hotel.model.Reservation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.Map;

public class AdminShellController {

    private static final Map<String, String> SCREEN_FILES = Map.of(
            "dashboard", "Dashboard.fxml",
            "reservations", "ReservationDetail.fxml",
            "payments", "Payments.fxml",
            "checkout", "Checkout.fxml",
            "loyalty", "LoyaltyView.fxml",
            "waitlist", "Waitlist.fxml",
            "feedback", "FeedbackView.fxml",
            "reports", "Reports.fxml"
    );

    private static final Map<String, String> SCREEN_LABELS = Map.of(
            "dashboard", "Dashboard",
            "reservations", "Reservation Detail",
            "payments", "Payments",
            "checkout", "Checkout",
            "loyalty", "Loyalty",
            "waitlist", "Waitlist",
            "feedback", "Feedback",
            "reports", "Reports"
    );

    @FXML
    private Label screenLabel;

    @FXML
    private StackPane contentContainer;

    private AppConfig appConfig;
    private SceneRouter router;
    private AdminUser currentAdmin;
    private Reservation selectedReservation;

    public void setRouter(SceneRouter router) {
        this.router = router;
    }

    /**
     * Called by LoginController right after this shell loads. Deliberately NOT done in an
     * FXML initialize() — that runs automatically during FXMLLoader.load(), before the
     * caller has a chance to hand over AppConfig, which would leave every screen's
     * setShell() reading a null AppConfig on the very first navigation.
     */
    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
        navigateTo("dashboard");
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }

    public void setCurrentAdmin(AdminUser currentAdmin) {
        this.currentAdmin = currentAdmin;
    }

    public AdminUser getCurrentAdmin() {
        return currentAdmin;
    }

    /**
     * The reservation the admin is currently working on. Set by double-clicking a Dashboard
     * row; read by the Reservation Detail, Payments, Checkout, and Loyalty screens. Persists
     * across navigation so you can open a reservation, then jump to its Payments/Checkout
     * screen. Null on fresh login → those screens show an empty state.
     */
    public void setSelectedReservation(Reservation reservation) {
        this.selectedReservation = reservation;
    }

    public Reservation getSelectedReservation() {
        return selectedReservation;
    }

    /** Opens Reservation Detail pre-loaded with the given reservation (e.g. from a Dashboard row). */
    public void openReservationDetail(Reservation reservation) {
        this.selectedReservation = reservation;
        navigateTo("reservations");
    }

    public void openPayments() {
        navigateTo("payments");
    }

    public void openCheckout() {
        navigateTo("checkout");
    }

    public void openLoyalty() {
        navigateTo("loyalty");
    }

    @FXML
    private void showLoyalty() {
        navigateTo("loyalty");
    }

    @FXML
    private void showDashboard() {
        navigateTo("dashboard");
    }

    @FXML
    private void showReservations() {
        navigateTo("reservations");
    }

    @FXML
    private void showPayments() {
        navigateTo("payments");
    }

    @FXML
    private void showCheckout() {
        navigateTo("checkout");
    }

    @FXML
    private void showWaitlist() {
        navigateTo("waitlist");
    }

    @FXML
    private void showFeedback() {
        navigateTo("feedback");
    }

    @FXML
    private void showReports() {
        navigateTo("reports");
    }

    @FXML
    private void handleLogout() {
        // Return the single window to the public kiosk — no new Stage, no closing the window.
        // Staff can log back in from the kiosk's "Staff Login" button.
        router.showKiosk();
    }

    private void navigateTo(String key) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/" + SCREEN_FILES.get(key)));
            Parent node = loader.load();

            Object controller = loader.getController();
            if (controller instanceof AdminScreenController) {
                ((AdminScreenController) controller).setShell(this);
            }

            contentContainer.getChildren().setAll(node);
            screenLabel.setText(SCREEN_LABELS.get(key));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load admin screen: " + key, e);
        }
    }
}
