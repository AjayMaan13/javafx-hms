package com.hotel.controller.admin;

import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.model.enums.ReservationStatus;
import com.hotel.model.enums.RoomType;
import com.hotel.repository.AuditLogRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.service.ActivityLogService;
import com.hotel.util.LoggerService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReservationDetailController implements AdminScreenController {

    @FXML
    private Label pageTitleLabel;
    @FXML
    private Label emptyStateLabel;
    @FXML
    private VBox detailContent;

    @FXML
    private TextField guestNameField;
    @FXML
    private TextField guestPhoneField;
    @FXML
    private DatePicker checkInPicker;
    @FXML
    private DatePicker checkOutPicker;
    @FXML
    private Label messageLabel;

    @FXML
    private TableView<Room> roomsTable;
    @FXML
    private TableColumn<Room, String> roomNumberColumn;
    @FXML
    private TableColumn<Room, String> roomTypeColumn;
    @FXML
    private TableColumn<Room, String> roomRateColumn;
    @FXML
    private TableColumn<Room, String> roomNightsColumn;

    @FXML
    private ComboBox<RoomType> newRoomTypeCombo;
    @FXML
    private ComboBox<String> statusComboBox;

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final ActivityLogService activityLogService;
    private final com.hotel.service.ReservationService reservationService;

    private AdminShellController shell;
    private Reservation reservation;
    private final ObservableList<Room> pendingRooms = FXCollections.observableArrayList();

    public ReservationDetailController() {
        reservationRepository = new ReservationRepository();
        roomRepository = new RoomRepository();
        activityLogService = new ActivityLogService(new AuditLogRepository(), LoggerService.getInstance());

        // TODO Phase 10: inject these from AppConfig instead of constructing per-controller.
        // Cancel routes through ReservationService (not the repo directly) so the Observer
        // fires; the waitlist subscriber's effect is a DB write, so it's globally visible
        // regardless of which publisher instance triggered it.
        com.hotel.repository.GuestRepository guestRepository = new com.hotel.repository.GuestRepository();
        com.hotel.repository.AddonRepository addonRepository = new com.hotel.repository.AddonRepository();
        com.hotel.service.PricingService pricingService = new com.hotel.service.PricingService(
                new com.hotel.service.pricing.StandardPricingStrategy());
        com.hotel.service.LoyaltyService loyaltyService = new com.hotel.service.LoyaltyService(
                new com.hotel.repository.LoyaltyAccountRepository(), new com.hotel.repository.LoyaltyConfigRepository(),
                new com.hotel.repository.LoyaltyTransactionRepository(), new com.hotel.repository.BillingRepository());
        com.hotel.events.RoomAvailabilityPublisher publisher = new com.hotel.events.RoomAvailabilityPublisher();
        publisher.attach(new com.hotel.events.WaitlistSubscriber(new com.hotel.repository.WaitlistRepository()));
        com.hotel.service.BillingService billingService = new com.hotel.service.BillingService(
                new com.hotel.repository.BillingRepository(), new com.hotel.repository.PaymentRepository(),
                reservationRepository, roomRepository, loyaltyService, publisher);
        reservationService = new com.hotel.service.ReservationService(guestRepository, roomRepository,
                reservationRepository, addonRepository, pricingService, billingService, publisher);
    }

    @Override
    public void setShell(AdminShellController shell) {
        this.shell = shell;
        Reservation selected = shell.getSelectedReservation();
        if (selected == null) {
            showEmptyState();
        } else {
            loadReservation(selected.getId());
        }
    }

    @FXML
    private void goToPayments() {
        shell.openPayments();
    }

    @FXML
    private void goToCheckout() {
        shell.openCheckout();
    }

    @FXML
    private void initialize() {
        statusComboBox.getItems().setAll(
                Arrays.stream(ReservationStatus.values()).map(Enum::name).collect(Collectors.toList()));
        newRoomTypeCombo.getItems().setAll(RoomType.values());

        roomNumberColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoomNumber()));
        roomTypeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType().name()));
        roomRateColumn.setCellValueFactory(data -> new SimpleStringProperty(
                String.format("$%.2f", data.getValue().getBasePrice())));
        roomNightsColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(currentNights())));

        checkInPicker.valueProperty().addListener((obs, oldVal, newVal) -> roomsTable.refresh());
        checkOutPicker.valueProperty().addListener((obs, oldVal, newVal) -> roomsTable.refresh());

        roomsTable.setItems(pendingRooms);
    }

    private void showEmptyState() {
        emptyStateLabel.setManaged(true);
        emptyStateLabel.setVisible(true);
        detailContent.setManaged(false);
        detailContent.setVisible(false);
    }

    private void loadReservation(UUID reservationId) {
        emptyStateLabel.setManaged(false);
        emptyStateLabel.setVisible(false);
        detailContent.setManaged(true);
        detailContent.setVisible(true);

        this.reservation = reservationRepository.findByIdWithRooms(reservationId)
                .orElseThrow(() -> new IllegalStateException("Reservation no longer exists: " + reservationId));

        pageTitleLabel.setText(
                "Reservation " + shortId(reservation.getId()) + " — " + reservation.getGuest().getName());
        guestNameField.setText(reservation.getGuest().getName());
        guestPhoneField.setText(reservation.getGuest().getPhone());
        checkInPicker.setValue(reservation.getCheckIn());
        checkOutPicker.setValue(reservation.getCheckOut());
        statusComboBox.setValue(reservation.getStatus().name());

        pendingRooms.setAll(reservation.getRooms());
        roomsTable.refresh();
        messageLabel.setText("");
    }

    private String shortId(UUID id) {
        return "#" + id.toString().substring(0, 8).toUpperCase();
    }

    private long currentNights() {
        LocalDate in = checkInPicker.getValue();
        LocalDate out = checkOutPicker.getValue();
        if (in == null || out == null || !in.isBefore(out)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(in, out);
    }

    @FXML
    private void addRoom() {
        RoomType type = newRoomTypeCombo.getValue();
        LocalDate checkIn = checkInPicker.getValue();
        LocalDate checkOut = checkOutPicker.getValue();

        if (type == null) {
            messageLabel.setText("Choose a room type first.");
            return;
        }
        if (checkIn == null || checkOut == null || !checkIn.isBefore(checkOut)) {
            messageLabel.setText("Set valid check-in/check-out dates before adding a room.");
            return;
        }

        List<UUID> alreadyHeldIds = pendingRooms.stream().map(Room::getId).collect(Collectors.toList());
        List<Room> available = roomRepository.findAvailable(type, checkIn, checkOut).stream()
                .filter(room -> !alreadyHeldIds.contains(room.getId()))
                .collect(Collectors.toList());

        if (available.isEmpty()) {
            messageLabel.setText("No additional " + type + " rooms available for these dates.");
            return;
        }

        pendingRooms.add(available.get(0));
        roomsTable.refresh();
        messageLabel.setText("");
    }

    @FXML
    private void updateReservation() {
        LocalDate checkIn = checkInPicker.getValue();
        LocalDate checkOut = checkOutPicker.getValue();
        String statusText = statusComboBox.getValue();

        if (checkIn == null || checkOut == null) {
            messageLabel.setText("Check-in and check-out dates are required.");
            return;
        }
        if (!checkIn.isBefore(checkOut)) {
            messageLabel.setText("Check-out must be after check-in.");
            return;
        }
        if (pendingRooms.isEmpty()) {
            messageLabel.setText("A reservation needs at least one room.");
            return;
        }

        for (Room room : pendingRooms) {
            if (roomRepository.hasConflict(room.getId(), checkIn, checkOut, reservation.getId())) {
                messageLabel.setText("Room " + room.getRoomNumber() + " is already booked for those dates.");
                return;
            }
        }

        ReservationStatus status = ReservationStatus.valueOf(statusText);
        List<UUID> roomIds = pendingRooms.stream().map(Room::getId).collect(Collectors.toList());

        reservationRepository.updateDetails(reservation.getId(), checkIn, checkOut, status, roomIds);

        if (shell.getCurrentAdmin() != null) {
            activityLogService.record(shell.getCurrentAdmin(), "RESERVATION_UPDATED", "Reservation",
                    reservation.getId().toString(),
                    "Dates/status/rooms updated for " + reservation.getGuest().getName() + ".");
        }

        messageLabel.setText("Saved.");
        loadReservation(reservation.getId());
    }

    @FXML
    private void cancelReservation() {
        // Routed through ReservationService (not reservationRepository directly) so the
        // Observer fires: a matching waitlist entry gets notified when this frees a room.
        reservationService.cancelReservation(reservation.getId());

        if (shell.getCurrentAdmin() != null) {
            activityLogService.record(shell.getCurrentAdmin(), "RESERVATION_CANCELLED", "Reservation",
                    reservation.getId().toString(),
                    "Reservation for " + reservation.getGuest().getName() + " cancelled.");
        }

        messageLabel.setText("Reservation cancelled — its rooms are now free for those dates.");
        loadReservation(reservation.getId());
    }
}
