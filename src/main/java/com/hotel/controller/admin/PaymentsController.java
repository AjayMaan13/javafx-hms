package com.hotel.controller.admin;

import com.hotel.model.Billing;
import com.hotel.model.Payment;
import com.hotel.model.Reservation;
import com.hotel.model.enums.PaymentMethod;
import com.hotel.repository.AuditLogRepository;
import com.hotel.repository.BillingRepository;
import com.hotel.repository.PaymentRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.service.ActivityLogService;
import com.hotel.service.BillingException;
import com.hotel.service.BillingService;
import com.hotel.util.LoggerService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

public class PaymentsController implements AdminScreenController {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private Label pageTitleLabel;
    @FXML
    private Label emptyStateLabel;
    @FXML
    private VBox paymentsContent;

    @FXML
    private Label totalDueLabel;
    @FXML
    private Label totalPaidLabel;
    @FXML
    private Label balanceLabel;

    // Discount controls — enforcement is by the logged-in admin's role (see DiscountService).
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private ComboBox<String> discountComboBox;
    @FXML
    private TextField customDiscountField;
    @FXML
    private TextField appliedByField;

    @FXML
    private ComboBox<String> methodComboBox;
    @FXML
    private TextField amountField;
    @FXML
    private Label messageLabel;

    @FXML
    private TableView<Payment> paymentTable;
    @FXML
    private TableColumn<Payment, String> paymentDateColumn;
    @FXML
    private TableColumn<Payment, String> paymentMethodColumn;
    @FXML
    private TableColumn<Payment, String> paymentAmountColumn;

    private final BillingService billingService;
    private final com.hotel.service.DiscountService discountService;
    private final ActivityLogService activityLogService;

    private AdminShellController shell;
    private Reservation reservation;

    public PaymentsController() {
        // TODO Phase 10: inject these from AppConfig instead of constructing per-controller.
        com.hotel.service.LoyaltyService loyaltyService = new com.hotel.service.LoyaltyService(
                new com.hotel.repository.LoyaltyAccountRepository(),
                new com.hotel.repository.LoyaltyConfigRepository(),
                new com.hotel.repository.LoyaltyTransactionRepository(), new BillingRepository());
        billingService = new BillingService(new BillingRepository(), new PaymentRepository(),
                new ReservationRepository(), new RoomRepository(), loyaltyService);
        discountService = new com.hotel.service.DiscountService(
                new BillingRepository(), new com.hotel.config.DiscountPolicy());
        activityLogService = new ActivityLogService(new AuditLogRepository(), LoggerService.getInstance());
    }

    @FXML
    private void initialize() {
        methodComboBox.getItems().setAll(
                Arrays.stream(PaymentMethod.values()).map(Enum::name).collect(Collectors.toList()));
        methodComboBox.getSelectionModel().selectFirst();

        paymentDateColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTimestamp().format(TIMESTAMP_FORMAT)));
        paymentMethodColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getMethod().name()));
        paymentAmountColumn.setCellValueFactory(data -> new SimpleStringProperty(
                String.format("$%.2f", data.getValue().getAmount())));
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
        paymentsContent.setManaged(false);
        paymentsContent.setVisible(false);
    }

    private void showContent() {
        emptyStateLabel.setManaged(false);
        emptyStateLabel.setVisible(false);
        paymentsContent.setManaged(true);
        paymentsContent.setVisible(true);
    }

    private void refresh() {
        pageTitleLabel.setText("Payments — " + reservation.getGuest().getName());

        Billing billing = billingService.getOrCreateBilling(reservation);
        totalDueLabel.setText(String.format("$%.2f", billing.getTotalDue()));
        totalPaidLabel.setText(String.format("$%.2f", billing.getTotalPaid()));
        balanceLabel.setText(String.format("$%.2f", billing.getBalance()));

        paymentTable.setItems(FXCollections.observableArrayList(billingService.paymentHistory(reservation)));
    }

    @FXML
    private void processPayment() {
        applyAmount(1);
    }

    @FXML
    private void refundPayment() {
        applyAmount(-1);
    }

    private void applyAmount(int sign) {
        Double amount = parseAmount();
        if (amount == null) {
            return;
        }

        PaymentMethod method = PaymentMethod.valueOf(methodComboBox.getValue());
        double signedAmount = sign * amount;

        try {
            billingService.recordPayment(reservation, method, signedAmount);
            if (shell.getCurrentAdmin() != null) {
                activityLogService.record(shell.getCurrentAdmin(),
                        sign > 0 ? "PAYMENT_RECORDED" : "REFUND_ISSUED", "Reservation",
                        reservation.getId().toString(),
                        String.format("%s of $%.2f via %s.", sign > 0 ? "Payment" : "Refund", amount, method));
            }
            amountField.clear();
            messageLabel.setText("");
            refresh();
        } catch (BillingException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    private Double parseAmount() {
        String raw = amountField.getText() == null ? "" : amountField.getText().trim();
        try {
            double amount = Double.parseDouble(raw);
            if (amount <= 0) {
                messageLabel.setText("Enter a positive amount (use Issue Refund for money going back).");
                return null;
            }
            return amount;
        } catch (NumberFormatException e) {
            messageLabel.setText("Enter a valid dollar amount.");
            return null;
        }
    }

    @FXML
    private void applyDiscount() {
        if (shell.getCurrentAdmin() == null) {
            messageLabel.setText("You must be logged in to apply a discount.");
            return;
        }

        Double percent = parseDiscountPercent();
        if (percent == null) {
            return;
        }

        try {
            double discount = discountService.apply(reservation, percent, shell.getCurrentAdmin());
            activityLogService.record(shell.getCurrentAdmin(), "DISCOUNT_APPLIED", "Reservation",
                    reservation.getId().toString(),
                    String.format("%.1f%% discount ($%.2f) applied by %s.",
                            percent, discount, shell.getCurrentAdmin().getUsername()));
            customDiscountField.clear();
            messageLabel.setText(String.format("Applied a %.1f%% discount ($%.2f).", percent, discount));
            refresh();
        } catch (com.hotel.service.DiscountException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    /** Reads the discount percent from the custom field, falling back to the leading number
     *  of a selected preset (e.g. "15% Admin Maximum" → 15). */
    private Double parseDiscountPercent() {
        String custom = customDiscountField.getText() == null ? "" : customDiscountField.getText().trim();
        if (!custom.isEmpty()) {
            try {
                return Double.parseDouble(custom);
            } catch (NumberFormatException e) {
                messageLabel.setText("Enter a valid discount percentage.");
                return null;
            }
        }
        String preset = discountComboBox.getValue();
        if (preset != null) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)").matcher(preset);
            if (m.find()) {
                return Double.parseDouble(m.group(1));
            }
        }
        messageLabel.setText("Enter a discount percentage or choose a preset.");
        return null;
    }

    @FXML
    private void clearDiscount() {
        if (roleComboBox != null) {
            roleComboBox.getSelectionModel().clearSelection();
        }
        if (discountComboBox != null) {
            discountComboBox.getSelectionModel().clearSelection();
        }
        customDiscountField.clear();
        appliedByField.clear();
    }
}
