package com.hotel.controller.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ReportsController {

    @FXML
    private ComboBox<String> reportTypeComboBox;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private TableView<String[]> reportTable;

    @FXML
    private void initialize() {
        reportTypeComboBox.setItems(FXCollections.observableArrayList(
                "Revenue",
                "Occupancy",
                "Activity Log"
        ));
        reportTypeComboBox.getSelectionModel().selectFirst();
        configureReportTable();
        reportTable.setItems(FXCollections.observableArrayList(
                new String[]{"2026-07-21", "Sample revenue", "$299.62", "1"},
                new String[]{"2026-07-21", "Admin login audit", "-", "-"}
        ));
    }

    @FXML
    public void loadReports() {
        reportTable.refresh();
    }

    @FXML
    public void generateDailyReport() {
        String type = reportTypeComboBox.getValue() == null ? "Revenue" : reportTypeComboBox.getValue();
        reportTable.getItems().add(new String[]{"Now", type + " report generated", "$0.00", "0"});
    }

    @FXML
    public void generateRevenueReport() {
        reportTypeComboBox.setValue("Revenue");
        generateDailyReport();
    }

    @FXML
    public void generateOccupancyReport() {
        reportTypeComboBox.setValue("Occupancy");
        generateDailyReport();
    }

    @FXML
    private void exportCsv() {
        reportTable.getItems().add(new String[]{"Now", "CSV export clicked", "-", "-"});
    }

    @FXML
    private void exportPdf() {
        reportTable.getItems().add(new String[]{"Now", "PDF export clicked", "-", "-"});
    }

    @FXML
    private void exportTxt() {
        reportTable.getItems().add(new String[]{"Now", "TXT export clicked", "-", "-"});
    }

    @SuppressWarnings("unchecked")
    private void configureReportTable() {
        for (int i = 0; i < reportTable.getColumns().size(); i++) {
            final int index = i;
            TableColumn<String[], String> column =
                    (TableColumn<String[], String>) reportTable.getColumns().get(i);
            column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[index]));
        }
    }
}
