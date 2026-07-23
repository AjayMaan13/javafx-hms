package com.hotel.controller.admin;

import com.hotel.model.Billing;
import com.hotel.model.LoyaltyAccount;
import com.hotel.model.LoyaltyTransaction;
import com.hotel.model.Reservation;
import com.hotel.repository.AuditLogRepository;
import com.hotel.repository.BillingRepository;
import com.hotel.repository.LoyaltyAccountRepository;
import com.hotel.repository.LoyaltyConfigRepository;
import com.hotel.repository.LoyaltyTransactionRepository;
import com.hotel.service.ActivityLogService;
import com.hotel.service.BillingService;
import com.hotel.service.LoyaltyException;
import com.hotel.service.LoyaltyService;
import com.hotel.util.LoggerService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class LoyaltyViewController implements AdminScreenController {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private Label pageTitleLabel;
    @FXML
    private Label emptyStateLabel;
    @FXML
    private VBox notEnrolledBox;
    @FXML
    private VBox enrolledBox;

    @FXML
    private Label numberLabel;
    @FXML
    private Label tierLabel;
    @FXML
    private Label balanceLabel;
    @FXML
    private TextField redeemPointsField;
    @FXML
    private Label redeemHintLabel;
    @FXML
    private Label messageLabel;

    @FXML
    private TableView<LoyaltyTransaction> historyTable;
    @FXML
    private TableColumn<LoyaltyTransaction, String> txDateColumn;
    @FXML
    private TableColumn<LoyaltyTransaction, String> txTypeColumn;
    @FXML
    private TableColumn<LoyaltyTransaction, String> txDeltaColumn;
    @FXML
    private TableColumn<LoyaltyTransaction, String> txBalanceColumn;
    @FXML
    private TableColumn<LoyaltyTransaction, String> txDiscountColumn;

    private final LoyaltyService loyaltyService;
    private final BillingService billingService;
    private final ActivityLogService activityLogService;

    private AdminShellController shell;
    private Reservation reservation;

    public LoyaltyViewController() {
        BillingRepository billingRepository = new BillingRepository();
        loyaltyService = new LoyaltyService(new LoyaltyAccountRepository(), new LoyaltyConfigRepository(),
                new LoyaltyTransactionRepository(), billingRepository);
        // TODO Phase 10: inject from AppConfig instead of per-controller construction.
        // This screen never calls checkout(), so the publisher here never actually fires —
        // it's only present to satisfy BillingService's constructor.
        billingService = new BillingService(billingRepository, new com.hotel.repository.PaymentRepository(),
                new com.hotel.repository.ReservationRepository(), new com.hotel.repository.RoomRepository(),
                loyaltyService, new com.hotel.events.RoomAvailabilityPublisher());
        activityLogService = new ActivityLogService(new AuditLogRepository(), LoggerService.getInstance());
    }

    @FXML
    private void initialize() {
        txDateColumn.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTimestamp().format(TIMESTAMP_FORMAT)));
        txTypeColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getType().name()));
        txDeltaColumn.setCellValueFactory(d -> new SimpleStringProperty(
                (d.getValue().getPointsDelta() >= 0 ? "+" : "") + d.getValue().getPointsDelta()));
        txBalanceColumn.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getBalanceAfter())));
        txDiscountColumn.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDiscountAmount() > 0 ? String.format("$%.2f", d.getValue().getDiscountAmount()) : "—"));
    }

    @Override
    public void setShell(AdminShellController shell) {
        this.shell = shell;
        this.reservation = shell.getSelectedReservation();
        if (reservation == null) {
            emptyStateLabel.setManaged(true);
            emptyStateLabel.setVisible(true);
            notEnrolledBox.setManaged(false);
            notEnrolledBox.setVisible(false);
            enrolledBox.setManaged(false);
            enrolledBox.setVisible(false);
        } else {
            emptyStateLabel.setManaged(false);
            emptyStateLabel.setVisible(false);
            refresh();
        }
    }

    private void refresh() {
        pageTitleLabel.setText("Loyalty — " + reservation.getGuest().getName());
        Optional<LoyaltyAccount> account = loyaltyService.findAccount(reservation.getGuest());

        boolean enrolled = account.isPresent();
        notEnrolledBox.setManaged(!enrolled);
        notEnrolledBox.setVisible(!enrolled);
        enrolledBox.setManaged(enrolled);
        enrolledBox.setVisible(enrolled);
        messageLabel.setText("");

        account.ifPresent(a -> {
            numberLabel.setText(a.getLoyaltyNumber());
            tierLabel.setText(a.getTier());
            balanceLabel.setText(String.valueOf(a.getPointsBalance()));
            historyTable.setItems(FXCollections.observableArrayList(loyaltyService.history(a)));

            Billing billing = billingService.getOrCreateBilling(reservation);
            redeemHintLabel.setText(String.format(
                    "Bill total $%.2f · current balance $%.2f. Redemption is capped per the loyalty policy.",
                    billing.getTotalDue(), billing.getBalance()));
        });
    }

    @FXML
    private void enrollGuest() {
        LoyaltyAccount account = loyaltyService.enroll(reservation.getGuest());
        if (shell.getCurrentAdmin() != null) {
            activityLogService.record(shell.getCurrentAdmin(), "LOYALTY_ENROLLED", "Guest",
                    reservation.getGuest().getId().toString(),
                    "Enrolled " + reservation.getGuest().getName() + " (" + account.getLoyaltyNumber() + ").");
        }
        refresh();
    }

    @FXML
    private void redeemPoints() {
        Optional<LoyaltyAccount> account = loyaltyService.findAccount(reservation.getGuest());
        if (account.isEmpty()) {
            return;
        }

        int points;
        try {
            points = Integer.parseInt(redeemPointsField.getText().trim());
        } catch (NumberFormatException e) {
            messageLabel.setText("Enter a whole number of points.");
            return;
        }

        try {
            Billing billing = billingService.getOrCreateBilling(reservation);
            double discount = loyaltyService.redeem(account.get(), points, billing,
                    shell.getCurrentAdmin() == null ? null : shell.getCurrentAdmin().getId());
            if (shell.getCurrentAdmin() != null) {
                activityLogService.record(shell.getCurrentAdmin(), "LOYALTY_REDEEMED", "Reservation",
                        reservation.getId().toString(),
                        String.format("Redeemed %d points for a $%.2f discount.", points, discount));
            }
            redeemPointsField.clear();
            messageLabel.setText(String.format("Redeemed %d points for a $%.2f discount.", points, discount));
            refresh();
        } catch (LoyaltyException e) {
            messageLabel.setText(e.getMessage());
        }
    }
}
