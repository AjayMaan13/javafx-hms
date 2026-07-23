package com.hotel.controller.admin;

import com.hotel.model.AdminUser;
import com.hotel.model.Reservation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

public class AdminShellController {

    private static final Map<String, String> SCREEN_FILES = Map.of(
            "dashboard", "Dashboard.fxml",
            "reservations", "ReservationDetail.fxml",
            "payments", "Payments.fxml",
            "checkout", "Checkout.fxml",
            "waitlist", "Waitlist.fxml",
            "feedback", "FeedbackView.fxml",
            "reports", "Reports.fxml"
    );

    private static final Map<String, String> SCREEN_LABELS = Map.of(
            "dashboard", "Dashboard",
            "reservations", "Reservation Detail",
            "payments", "Payments",
            "checkout", "Checkout",
            "waitlist", "Waitlist",
            "feedback", "Feedback",
            "reports", "Reports"
    );

    @FXML
    private Label screenLabel;

    @FXML
    private StackPane contentContainer;

    private AdminUser currentAdmin;
    private Reservation selectedReservationForDetail;

    @FXML
    private void initialize() {
        navigateTo("dashboard");
    }

    public void setCurrentAdmin(AdminUser currentAdmin) {
        this.currentAdmin = currentAdmin;
    }

    public AdminUser getCurrentAdmin() {
        return currentAdmin;
    }

    /** Opens Reservation Detail pre-loaded with the given reservation (e.g. from a Dashboard row). */
    public void openReservationDetail(Reservation reservation) {
        this.selectedReservationForDetail = reservation;
        navigateTo("reservations");
    }

    /** Consumed by ReservationDetailController on load; cleared after reading so a later
     *  direct nav to "Reservations" via the side menu correctly shows the empty state. */
    public Reservation consumeSelectedReservationForDetail() {
        Reservation reservation = this.selectedReservationForDetail;
        this.selectedReservationForDetail = null;
        return reservation;
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/Login.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Maple Leaf Hotel — Admin");
            loginStage.setScene(new Scene(root));
            loginStage.show();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load Login screen", e);
        }

        ((Stage) contentContainer.getScene().getWindow()).close();
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
