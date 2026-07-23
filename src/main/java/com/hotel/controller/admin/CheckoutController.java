package com.hotel.controller.admin;

import com.hotel.model.Billing;
import com.hotel.model.Reservation;
import com.hotel.repository.AuditLogRepository;
import com.hotel.repository.BillingRepository;
import com.hotel.repository.PaymentRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.service.ActivityLogService;
import com.hotel.service.BillingException;
import com.hotel.service.BillingService;
import com.hotel.util.LoggerService;
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

    private final BillingService billingService;
    private final ActivityLogService activityLogService;

    private AdminShellController shell;
    private Reservation reservation;

    public CheckoutController() {
        // TODO Phase 10: inject these from AppConfig instead of constructing per-controller.
        com.hotel.service.LoyaltyService loyaltyService = new com.hotel.service.LoyaltyService(
                new com.hotel.repository.LoyaltyAccountRepository(),
                new com.hotel.repository.LoyaltyConfigRepository(),
                new com.hotel.repository.LoyaltyTransactionRepository(), new BillingRepository());
        billingService = new BillingService(new BillingRepository(), new PaymentRepository(),
                new ReservationRepository(), new RoomRepository(), loyaltyService);
        activityLogService = new ActivityLogService(new AuditLogRepository(), LoggerService.getInstance());
    }

    @Override
    public void setShell(AdminShellController shell) {
        this.shell = shell;
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
