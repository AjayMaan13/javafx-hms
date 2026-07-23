package com.hotel.controller.admin;

import com.hotel.controller.kiosk.BookingDraft;
import com.hotel.events.WaitlistSubscriber;
import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.Waitlist;
import com.hotel.model.enums.RoomType;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.WaitlistRepository;
import com.hotel.service.ActivityLogService;
import com.hotel.service.BookingValidationException;
import com.hotel.service.ReservationService;
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

    private GuestRepository guestRepository;
    private WaitlistRepository waitlistRepository;
    private ReservationService reservationService;
    private ActivityLogService activityLogService;

    private AdminShellController shell;

    @Override
    public void setShell(AdminShellController shell) {
        this.shell = shell;
        this.guestRepository = shell.getAppConfig().getGuestRepository();
        this.waitlistRepository = shell.getAppConfig().getWaitlistRepository();
        // App-wide ReservationService, whose RoomAvailabilityPublisher already has the real
        // WaitlistSubscriber attached (wired once in AppConfig).
        this.reservationService = shell.getAppConfig().getReservationService();
        this.activityLogService = shell.getAppConfig().getActivityLogService();
        loadWaitlist();
    }

    @FXML
    private void initialize() {
        roomTypeComboBox.getItems().setAll(RoomType.values());

        guestColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGuest().getName()));
        roomTypeColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRequestedType().name()));
        startColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStartDate().toString()));
        endColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEndDate().toString()));
        statusColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        // Data loads once setShell() supplies the real repositories — not here.
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
