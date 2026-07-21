package com.hotel.controller.admin;

import com.hotel.model.Reservation;
import com.hotel.model.Guest;
import com.hotel.model.enums.ReservationStatus;
import com.hotel.repository.ReservationRepository;
import javafx.event.ActionEvent;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

public class DashboardController {

    @FXML
    private TableView<Reservation> reservationTable;

    private final ReservationRepository reservationRepository;

    public DashboardController() {
        reservationRepository = new ReservationRepository();
    }

    @FXML
    private void initialize() {
        configureReservationTable();
        reservationTable.setPlaceholder(new Label("No kiosk reservations found yet."));
        ObservableList<Reservation> reservations = FXCollections.observableArrayList(
                reservationRepository.findAll()
        );
        if (reservations.isEmpty()) {
            reservations.add(createDemoReservation());
        }
        reservationTable.setItems(reservations);
    }

    @FXML
    private void openReservations(ActionEvent event) {
        openWindow("/fxml/admin/ReservationDetail.fxml",
                "Reservation Details");
    }

    @FXML
    private void openPayments(ActionEvent event) {
        openWindow("/fxml/admin/Payments.fxml",
                "Payments");
    }

    @FXML
    private void openCheckout(ActionEvent event) {
        openWindow("/fxml/admin/Checkout.fxml",
                "Checkout");
    }

    @FXML
    private void openWaitlist(ActionEvent event) {
        openWindow("/fxml/admin/Waitlist.fxml",
                "Waitlist");
    }

    @FXML
    private void openFeedback(ActionEvent event) {
        openWindow("/fxml/admin/FeedbackView.fxml",
                "Feedback");
    }

    @FXML
    private void openReports(ActionEvent event) {
        openWindow("/fxml/admin/Reports.fxml",
                "Reports");
    }

    @FXML
    private void logout(ActionEvent event) {

        Stage dashboardStage =
                (Stage) ((javafx.scene.Node) event.getSource())
                        .getScene()
                        .getWindow();

        dashboardStage.close();

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/admin/Login.fxml"));

            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Admin Login");
            loginStage.setScene(new Scene(root));
            loginStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openWindow(String fxmlFile, String title) {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(title);
            stage.setScene(new Scene(root));

            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
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
        roomsColumn.setCellValueFactory(data -> new SimpleStringProperty("View details"));

        TableColumn<Reservation, String> balanceColumn =
                (TableColumn<Reservation, String>) reservationTable.getColumns().get(6);
        balanceColumn.setCellValueFactory(data -> new SimpleStringProperty(
                String.format("$%.2f", data.getValue().getTotal())
        ));
    }

    private Reservation createDemoReservation() {
        Guest guest = new Guest(
                "Demo Guest",
                "555-0100",
                "demo@example.com",
                "Front Desk",
                "A1A 1A1"
        );
        Reservation reservation = new Reservation(
                guest,
                LocalDate.now(),
                LocalDate.now().plusDays(2),
                2,
                0,
                ReservationStatus.CONFIRMED
        );
        reservation.setTotal(299.62);
        return reservation;
    }
}
