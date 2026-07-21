package com.hotel.controller.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class WaitlistController {

    @FXML
    private TextField guestNameField;

    @FXML
    private TextField phoneField;

    @FXML
    private ComboBox<String> roomTypeComboBox;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TableView<String[]> waitlistTable;

    @FXML
    private void initialize() {
        roomTypeComboBox.setItems(FXCollections.observableArrayList(
                "SINGLE",
                "DOUBLE",
                "DELUXE",
                "PENTHOUSE"
        ));
        roomTypeComboBox.getSelectionModel().selectFirst();
        configureWaitlistTable();
        waitlistTable.setItems(FXCollections.observableArrayList());
        waitlistTable.getItems().add(new String[]{"Jordan Smith", "DOUBLE", "2026-07-22", "2026-07-24", "WAITING"});
    }

    @FXML
    public void loadWaitlist() {
        waitlistTable.refresh();
    }

    @FXML
    public void notifyGuest() {
        String[] selected = waitlistTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected[4] = "NOTIFIED";
            waitlistTable.refresh();
        }
    }

    @FXML
    public void removeFromWaitlist() {
        String[] selected = waitlistTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            waitlistTable.getItems().remove(selected);
        }
    }

    @FXML
    private void convertToReservation() {
        updateSelectedStatus("CONVERTED");
    }

    @FXML
    private void cancelWaitlistEntry() {
        updateSelectedStatus("CANCELLED");
    }

    @FXML
    private void addToWaitlist() {
        String guestName = guestNameField.getText().isBlank()
                ? "New Guest"
                : guestNameField.getText().trim();
        String roomType = roomTypeComboBox.getValue() == null
                ? "SINGLE"
                : roomTypeComboBox.getValue();
        String start = startDatePicker.getValue() == null
                ? "-"
                : startDatePicker.getValue().toString();
        String end = endDatePicker.getValue() == null
                ? "-"
                : endDatePicker.getValue().toString();

        waitlistTable.getItems().add(new String[]{guestName, roomType, start, end, "WAITING"});
        guestNameField.clear();
        phoneField.clear();
    }

    private void updateSelectedStatus(String status) {
        String[] selected = waitlistTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected[4] = status;
            waitlistTable.refresh();
        }
    }

    @SuppressWarnings("unchecked")
    private void configureWaitlistTable() {
        for (int i = 0; i < waitlistTable.getColumns().size(); i++) {
            final int index = i;
            TableColumn<String[], String> column =
                    (TableColumn<String[], String>) waitlistTable.getColumns().get(i);
            column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[index]));
        }
    }
}
