package com.hotel.controller.admin;

import com.hotel.controller.kiosk.BookingDraft;
import com.hotel.events.RoomAvailabilityPublisher;
import com.hotel.events.WaitlistSubscriber;
import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.Waitlist;
import com.hotel.model.enums.RoomType;
import com.hotel.repository.AddonRepository;
import com.hotel.repository.AuditLogRepository;
import com.hotel.repository.BillingRepository;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.LoyaltyAccountRepository;
import com.hotel.repository.LoyaltyConfigRepository;
import com.hotel.repository.LoyaltyTransactionRepository;
import com.hotel.repository.PaymentRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.WaitlistRepository;
import com.hotel.service.ActivityLogService;
import com.hotel.service.BillingService;
import com.hotel.service.BookingValidationException;
import com.hotel.service.LoyaltyService;
import com.hotel.service.PricingService;
import com.hotel.service.ReservationService;
import com.hotel.service.pricing.StandardPricingStrategy;
import com.hotel.util.LoggerService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class WaitlistController implements AdminScreenController {

    @FXML
    private TextField guestNameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;
    @FXML
    private ComboBox<RoomType> roomTypeComboBox;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Label messageLabel;

    @FXML
    private TableView<Waitlist> waitlistTable;
    @FXML
    private TableColumn<Waitlist, String> guestColumn;
    @FXML
    private TableColumn<Waitlist, String> roomTypeColumn;
    @FXML
    private TableColumn<Waitlist, String> startColumn;
    @FXML
    private TableColumn<Waitlist, String> endColumn;
    @FXML
    private TableColumn<Waitlist, String> statusColumn;

    private final GuestRepository guestRepository;
    private final WaitlistRepository waitlistRepository;
    private final ReservationService reservationService;
    private final ActivityLogService activityLogService;

    private AdminShellController shell;

    public WaitlistController() {
        // TODO Phase 10: inject these from AppConfig instead of constructing per-controller.
        guestRepository = new GuestRepository();
        waitlistRepository = new WaitlistRepository();

        RoomRepository roomRepository = new RoomRepository();
        ReservationRepository reservationRepository = new ReservationRepository();
        LoyaltyService loyaltyService = new LoyaltyService(new LoyaltyAccountRepository(),
                new LoyaltyConfigRepository(), new LoyaltyTransactionRepository(), new BillingRepository());

        // Observer: this reservationService is wired the same way as everywhere else so a
        // future checkout/cancel reachable from this screen would still notify correctly.
        RoomAvailabilityPublisher publisher = new RoomAvailabilityPublisher();
        publisher.attach(new WaitlistSubscriber(waitlistRepository));

        BillingService billingService = new BillingService(new BillingRepository(), new PaymentRepository(),
                reservationRepository, roomRepository, loyaltyService, publisher);
        reservationService = new ReservationService(guestRepository, roomRepository, reservationRepository,
                new AddonRepository(), new PricingService(new StandardPricingStrategy()), billingService, publisher);

        activityLogService = new ActivityLogService(new AuditLogRepository(), LoggerService.getInstance());
    }

    @Override
    public void setShell(AdminShellController shell) {
        this.shell = shell;
    }

    @FXML
    private void initialize() {
        roomTypeComboBox.getItems().setAll(RoomType.values());

        guestColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGuest().getName()));
        roomTypeColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRequestedType().name()));
        startColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStartDate().toString()));
        endColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEndDate().toString()));
        statusColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));

        loadWaitlist();
    }

    @FXML
    public void loadWaitlist() {
        waitlistTable.setItems(FXCollections.observableArrayList(waitlistRepository.findAll()));
    }

    @FXML
    private void addToWaitlist() {
        String name = guestNameField.getText() == null ? "" : guestNameField.getText().trim();
        String phone = phoneField.getText() == null ? "" : phoneField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        RoomType type = roomTypeComboBox.getValue();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            messageLabel.setText("Guest name, phone, and email are required.");
            return;
        }
        if (type == null) {
            messageLabel.setText("Choose a room type.");
            return;
        }
        if (start == null || end == null || !start.isBefore(end)) {
            messageLabel.setText("Choose a valid start/end date range.");
            return;
        }

        Guest guest = guestRepository.findByEmail(email)
                .orElseGet(() -> guestRepository.save(new Guest(name, phone, email, "", "")));

        waitlistRepository.save(new Waitlist(guest, type, start, end, WaitlistSubscriber.STATUS_WAITING));

        guestNameField.clear();
        phoneField.clear();
        emailField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        messageLabel.setText("");
        loadWaitlist();
    }

    @FXML
    private void cancelWaitlistEntry() {
        Waitlist selected = waitlistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Select a waitlist entry first.");
            return;
        }
        waitlistRepository.updateStatus(selected.getId(), WaitlistSubscriber.STATUS_CANCELLED);
        messageLabel.setText("");
        loadWaitlist();
    }

    @FXML
    private void convertToReservation() {
        Waitlist selected = waitlistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Select a waitlist entry first.");
            return;
        }

        BookingDraft draft = new BookingDraft();
        draft.setAdults(1);
        draft.setChildren(0);
        draft.setCheckIn(selected.getStartDate());
        draft.setCheckOut(selected.getEndDate());
        draft.setRoomQuantity(selected.getRequestedType(), 1);
        draft.setGuestFirstName(selected.getGuest().getName());
        draft.setGuestLastName("");
        draft.setGuestPhone(selected.getGuest().getPhone());
        draft.setGuestEmail(selected.getGuest().getEmail());
        draft.setGuestAddress(selected.getGuest().getAddress());
        draft.setGuestPostalCode(selected.getGuest().getPostalCode());

        try {
            Reservation reservation = reservationService.createReservation(draft);
            waitlistRepository.updateStatus(selected.getId(), WaitlistSubscriber.STATUS_CONVERTED);

            if (shell != null && shell.getCurrentAdmin() != null) {
                activityLogService.record(shell.getCurrentAdmin(), "WAITLIST_CONVERTED", "Reservation",
                        reservation.getId().toString(),
                        "Converted a waitlist entry for " + selected.getGuest().getName() + ".");
            }

            messageLabel.setText("Converted to a reservation for " + selected.getGuest().getName() + ".");
            loadWaitlist();
        } catch (BookingValidationException e) {
            messageLabel.setText("Could not convert: " + e.getMessage());
        }
    }
}
