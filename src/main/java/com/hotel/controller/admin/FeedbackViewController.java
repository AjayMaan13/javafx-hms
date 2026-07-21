package com.hotel.controller.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class FeedbackViewController {

    @FXML
    private TextField guestFilterField;

    @FXML
    private ComboBox<String> ratingComboBox;

    @FXML
    private ComboBox<String> sentimentComboBox;

    @FXML
    private TableView<String[]> feedbackTable;

    @FXML
    private void initialize() {
        ratingComboBox.setItems(FXCollections.observableArrayList(
                "Any",
                "5",
                "4",
                "3",
                "2",
                "1"
        ));
        sentimentComboBox.setItems(FXCollections.observableArrayList(
                "Any",
                "Positive",
                "Neutral",
                "Negative"
        ));
        ratingComboBox.getSelectionModel().selectFirst();
        sentimentComboBox.getSelectionModel().selectFirst();
        configureFeedbackTable();
        feedbackTable.setItems(FXCollections.observableArrayList(
                new String[]{"Jordan Smith", "5", "Great front desk service.", "2026-07-21"},
                new String[]{"Avery Patel", "4", "Clean room and quick checkout.", "2026-07-20"}
        ));
    }

    @FXML
    public void loadFeedback() {
        feedbackTable.refresh();
    }

    @FXML
    public void viewFeedback() {
        String guest = guestFilterField.getText().trim().toLowerCase();
        if (guest.isBlank()) {
            loadFeedback();
            return;
        }
        feedbackTable.setItems(FXCollections.observableArrayList(
                feedbackTable.getItems().filtered(row -> row[0].toLowerCase().contains(guest))
        ));
    }

    @FXML
    private void exportFeedback() {
        feedbackTable.getItems().add(new String[]{"System", "-", "PDF export clicked for M2 demo.", "Now"});
    }

    @SuppressWarnings("unchecked")
    private void configureFeedbackTable() {
        for (int i = 0; i < feedbackTable.getColumns().size(); i++) {
            final int index = i;
            TableColumn<String[], String> column =
                    (TableColumn<String[], String>) feedbackTable.getColumns().get(i);
            column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[index]));
        }
    }
}
