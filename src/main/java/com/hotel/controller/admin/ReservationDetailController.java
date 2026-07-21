package com.hotel.controller.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class ReservationDetailController {

    @FXML
    private TableView<String[]> roomsTable;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private Label messageLabel;

    @FXML
    private void initialize() {
        statusComboBox.setItems(FXCollections.observableArrayList(
                "PENDING",
                "CONFIRMED",
                "CHECKED_IN",
                "CHECKED_OUT",
                "CANCELLED"
        ));
        statusComboBox.getSelectionModel().selectFirst();
        configureRoomsTable();
        roomsTable.setItems(FXCollections.observableArrayList());
        roomsTable.getItems().add(new String[]{"204", "DOUBLE", "$160.00", "2"});
    }

    @FXML
    public void loadReservationDetails() {
        messageLabel.setText("Reservation details loaded.");
    }

    @FXML
    public void updateReservation() {
        messageLabel.setText("Changes saved with status " + statusComboBox.getValue() + ".");
    }

    @FXML
    public void cancelReservation() {
        statusComboBox.setValue("CANCELLED");
        messageLabel.setText("Reservation marked cancelled for M2 demo.");
    }

    @FXML
    private void addRoom() {
        roomsTable.getItems().add(new String[]{"301", "DELUXE", "$220.00", "2"});
        messageLabel.setText("Room added to the reservation view.");
    }

    @FXML
    private void closeWindow(javafx.event.ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @SuppressWarnings("unchecked")
    private void configureRoomsTable() {
        for (int i = 0; i < roomsTable.getColumns().size(); i++) {
            final int index = i;
            TableColumn<String[], String> column =
                    (TableColumn<String[], String>) roomsTable.getColumns().get(i);
            column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[index]));
        }
    }
}
