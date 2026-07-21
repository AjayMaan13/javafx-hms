package com.hotel.controller.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class PaymentsController {

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
    private TableView<String[]> paymentTable;

    @FXML
    private TableView<String[]> discountAuditTable;

    @FXML
    private void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList(
                "Admin - max 15%",
                "Manager - max 30%"
        ));
        discountComboBox.setItems(FXCollections.observableArrayList(
                "5% Courtesy Discount",
                "10% Loyalty Discount",
                "15% Admin Maximum",
                "30% Manager Maximum"
        ));
        methodComboBox.setItems(FXCollections.observableArrayList(
                "CASH",
                "CARD",
                "LOYALTY_POINTS"
        ));
        methodComboBox.getSelectionModel().selectFirst();
        configureTable(paymentTable);
        configureTable(discountAuditTable);
        paymentTable.setItems(FXCollections.observableArrayList());
        paymentTable.getItems().add(new String[]{"2026-07-21 09:30", "CARD", "$150.00"});
        discountAuditTable.setItems(FXCollections.observableArrayList());
        discountAuditTable.getItems().add(new String[]{"2026-07-21 09:45", "ADMIN", "5%", "admin", "Within cap"});
    }

    @FXML
    public void loadPayments() {
        paymentTable.refresh();
    }

    @FXML
    public void processPayment() {
        String amount = amountField.getText().isBlank() ? "$0.00" : "$" + amountField.getText().trim();
        paymentTable.getItems().add(new String[]{"Now", methodComboBox.getValue(), amount});
        amountField.clear();
    }

    @FXML
    public void refundPayment() {
        String amount = amountField.getText().isBlank() ? "-$0.00" : "-$" + amountField.getText().trim();
        paymentTable.getItems().add(new String[]{"Now", methodComboBox.getValue(), amount});
        amountField.clear();
    }

    @FXML
    private void applyDiscount() {
        String role = roleComboBox.getValue() == null ? "ADMIN" : roleComboBox.getValue();
        String discount = customDiscountField.getText().isBlank()
                ? discountComboBox.getValue()
                : customDiscountField.getText().trim() + "% Custom";
        String appliedBy = appliedByField.getText().isBlank() ? "admin" : appliedByField.getText().trim();
        discountAuditTable.getItems().add(new String[]{"Now", role, discount, appliedBy, "Recorded for M2"});
    }

    @FXML
    private void clearDiscount() {
        roleComboBox.getSelectionModel().clearSelection();
        discountComboBox.getSelectionModel().clearSelection();
        customDiscountField.clear();
        appliedByField.clear();
    }

    @SuppressWarnings("unchecked")
    private void configureTable(TableView<String[]> table) {
        for (int i = 0; i < table.getColumns().size(); i++) {
            final int index = i;
            TableColumn<String[], String> column =
                    (TableColumn<String[], String>) table.getColumns().get(i);
            column.setCellValueFactory(data -> new SimpleStringProperty(
                    index < data.getValue().length ? data.getValue()[index] : ""
            ));
        }
    }
}
