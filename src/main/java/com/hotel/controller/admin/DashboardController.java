package com.hotel.controller.admin;

import com.hotel.model.Reservation;
import com.hotel.model.enums.ReservationStatus;
import com.hotel.repository.ReservationRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardController implements AdminScreenController {

    @FXML
    private TextField nameFilterField;

    @FXML
    private TextField phoneFilterField;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private ComboBox<String> statusFilterCombo;

    @FXML
    private TableView<Reservation> reservationTable;

    private ReservationRepository reservationRepository;
    private AdminShellController shell;

    @Override
    public void setShell(AdminShellController shell) {
        this.shell = shell;
        this.reservationRepository = shell.getAppConfig().getReservationRepository();
        loadAll();
    }

    @FXML
    private void initialize() {
        configureReservationTable();
        configureRowDoubleClick();

        statusFilterCombo.getItems().add("Any Status");
        for (ReservationStatus status : ReservationStatus.values()) {
            statusFilterCombo.getItems().add(status.name());
        }
        statusFilterCombo.getSelectionModel().selectFirst();

        reservationTable.setPlaceholder(new Label("No reservations found."));
        // Data loads once setShell() supplies the real repository — not here, since this
        // FXML initialize() runs before the shell hands over AppConfig.
    }

    @FXML
    private void handleSearch() {
        String name = nameFilterField.getText() == null ? "" : nameFilterField.getText().trim().toLowerCase();
        String phone = phoneFilterField.getText() == null ? "" : phoneFilterField.getText().trim().toLowerCase();
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();
        String status = statusFilterCombo.getValue();

        List<Reservation> filtered = reservationRepository.findAllWithRooms().stream()
                .filter(r -> name.isEmpty() || r.getGuest().getName().toLowerCase().contains(name))
                .filter(r -> phone.isEmpty() || r.getGuest().getPhone().toLowerCase().contains(phone))
                .filter(r -> from == null || !r.getCheckOut().isBefore(from))
                .filter(r -> to == null || !r.getCheckIn().isAfter(to))
                .filter(r -> status == null || status.equals("Any Status") || status.equals(r.getStatus().name()))
                .collect(Collectors.toList());

        reservationTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleClearFilters() {
        nameFilterField.clear();
        phoneFilterField.clear();
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        statusFilterCombo.getSelectionModel().selectFirst();
        loadAll();
    }

    private void loadAll() {
        ObservableList<Reservation> reservations = FXCollections.observableArrayList(reservationRepository.findAllWithRooms());
        reservationTable.setItems(reservations);
    }

    private void configureRowDoubleClick() {
        reservationTable.setRowFactory(tableView -> {
            TableRow<Reservation> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty() && shell != null) {
                    shell.openReservationDetail(row.getItem());
                }
            });
            return row;
        });
    }

    @SuppressWarnings("unchecked")
    private void configureReservationTable() {
        TableColumn<Reservation, String> guestColumn =
                (TableColumn<Reservation, String>) reservationTable.getColumns().get(0);
        guestColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getGuest().getName()
        ));

        TableColumn<Reservation, String> phoneColumn =
                (TableColumn<Reservation, String>) reservationTable.getColumns().get(1);
        phoneColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getGuest().getPhone()
        ));

        TableColumn<Reservation, String> checkInColumn =
                (TableColumn<Reservation, String>) reservationTable.getColumns().get(2);
        checkInColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCheckIn().toString()
        ));

        TableColumn<Reservation, String> checkOutColumn =
                (TableColumn<Reservation, String>) reservationTable.getColumns().get(3);
        checkOutColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCheckOut().toString()
        ));

        TableColumn<Reservation, String> statusColumn =
                (TableColumn<Reservation, String>) reservationTable.getColumns().get(4);
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getStatus().name()
        ));

        TableColumn<Reservation, String> roomsColumn =
                (TableColumn<Reservation, String>) reservationTable.getColumns().get(5);
        roomsColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getRooms().size() + " room(s)"
        ));

        TableColumn<Reservation, String> balanceColumn =
                (TableColumn<Reservation, String>) reservationTable.getColumns().get(6);
        balanceColumn.setCellValueFactory(data -> new SimpleStringProperty(
                String.format("$%.2f", data.getValue().getTotal())
        ));
    }
}
