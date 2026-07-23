package com.hotel.controller.admin;

import com.hotel.model.Billing;
import com.hotel.model.Reservation;
import com.hotel.service.ActivityLogService;
import com.hotel.service.BillingException;
import com.hotel.service.BillingService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class CheckoutController implements AdminScreenController {

    @FXML
    private Label pageTitleLabel;
    @FXML
    private Label emptyStateLabel;
    @FXML
    private VBox checkoutContent;
    @FXML
    private Label summaryLabel;
    @FXML
    private Label totalDueLabel;
    @FXML
    private Label totalPaidLabel;
    @FXML
    private Label balanceLabel;
    @FXML
    private Button checkoutButton;
    @FXML
    private Label messageLabel;

    private BillingService billingService;
    private ActivityLogService activityLogService;

    private AdminShellController shell;
    private Reservation reservation;

    @Override
    public void setShell(AdminShellController shell) {
        this.shell = shell;
        // Uses the app-wide BillingService, whose RoomAvailabilityPublisher already has the
        // real WaitlistSubscriber attached (wired once in AppConfig) — checkout's Observer
        // notification actually reaches the waitlist now, not a throwaway local instance.
        this.billingService = shell.getAppConfig().getBillingService();
        this.activityLogService = shell.getAppConfig().getActivityLogService();

        this.reservation = shell.getSelectedReservation();
        if (reservation == null) {
            showEmptyState();
        } else {
            showContent();
            refresh();
        }
    }

    private void showEmptyState() {
        emptyStateLabel.setManaged(true);
        emptyStateLabel.setVisible(true);
        checkoutContent.setManaged(false);
        checkoutContent.setVisible(false);
    }

    private void showContent() {
        emptyStateLabel.setManaged(false);
        emptyStateLabel.setVisible(false);
        checkoutContent.setManaged(true);
        checkoutContent.setVisible(true);
    }

    private void refresh() {
        pageTitleLabel.setText("Checkout — " + reservation.getGuest().getName());
        summaryLabel.setText(String.format("%s → %s · status %s",
                reservation.getCheckIn(), reservation.getCheckOut(), reservation.getStatus()));

        Billing billing = billingService.getOrCreateBilling(reservation);
        totalDueLabel.setText(String.format("$%.2f", billing.getTotalDue()));
        totalPaidLabel.setText(String.format("$%.2f", billing.getTotalPaid()));
        balanceLabel.setText(String.format("$%.2f", billing.getBalance()));

        boolean settled = billing.getBalance() <= 0.005;
        checkoutButton.setDisable(!settled);
        messageLabel.setText(settled
                ? "Balance is settled — ready to check out."
                : "Settle the outstanding balance on the Payments screen before checking out.");
    }

    @FXML
    private void checkoutGuest() {
        try {
            billingService.checkout(reservation);
            if (shell.getCurrentAdmin() != null) {
                activityLogService.record(shell.getCurrentAdmin(), "CHECKOUT", "Reservation",
                        reservation.getId().toString(),
                        "Checked out " + reservation.getGuest().getName() + "; rooms marked available.");
            }
            messageLabel.setText("Checked out. Rooms are now marked available.");
            checkoutButton.setDisable(true);
            refresh();
        } catch (BillingException e) {
            messageLabel.setText(e.getMessage());
        }
    }
}
