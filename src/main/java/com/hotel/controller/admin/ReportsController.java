package com.hotel.controller.admin;

import com.hotel.model.AuditLog;
import com.hotel.repository.AuditLogRepository;
import com.hotel.repository.BillingRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.service.ReportingService;
import com.hotel.service.reporting.OccupancyReportRow;
import com.hotel.service.reporting.RevenueReportRow;
import com.hotel.util.CsvExporter;
import com.hotel.util.PdfExporter;
import com.hotel.util.TxtExporter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReportsController {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private ComboBox<String> reportTypeComboBox;
    @FXML
    private DatePicker fromDatePicker;
    @FXML
    private DatePicker toDatePicker;
    @FXML
    private TableView<Object> reportTable;
    @FXML
    private Label messageLabel;

    // TODO Phase 10: inject this from AppConfig instead of constructing per-controller.
    private final ReportingService reportingService = new ReportingService(
            new ReservationRepository(), new RoomRepository(), new BillingRepository(), new AuditLogRepository());
    private final CsvExporter csvExporter = new CsvExporter();
    private final TxtExporter txtExporter = new TxtExporter();
    private final PdfExporter pdfExporter = new PdfExporter();

    // Single source of truth for exports: whatever is currently on screen is what gets
    // written out, so the export always matches what the admin is looking at.
    private String currentReportType = "";
    private List<String> currentHeaders = List.of();
    private List<List<String>> currentRows = List.of();

    @FXML
    private void initialize() {
        reportTypeComboBox.getItems().setAll("Revenue", "Occupancy", "Activity Log");
        reportTypeComboBox.getSelectionModel().selectFirst();
        fromDatePicker.setValue(LocalDate.now().minusDays(7));
        toDatePicker.setValue(LocalDate.now());
    }

    @FXML
    private void generateReport() {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();
        String type = reportTypeComboBox.getValue();

        if (from == null || to == null || from.isAfter(to)) {
            messageLabel.setText("Choose a valid From/To date range.");
            return;
        }

        currentReportType = type;
        if ("Revenue".equals(type)) {
            showRevenue(reportingService.revenueReport(from, to));
        } else if ("Occupancy".equals(type)) {
            showOccupancy(reportingService.occupancyReport(from, to));
        } else {
            showActivityLog(reportingService.activityLogReport(from, to));
        }
        messageLabel.setText("");
    }

    private void showRevenue(List<RevenueReportRow> rows) {
        reportTable.getColumns().clear();
        reportTable.getColumns().addAll(
                column("Date", r -> ((RevenueReportRow) r).getDate().toString()),
                column("Reservations", r -> String.valueOf(((RevenueReportRow) r).getReservationCount())),
                column("Subtotal", r -> String.format("$%.2f", ((RevenueReportRow) r).getSubtotal())),
                column("Tax", r -> String.format("$%.2f", ((RevenueReportRow) r).getTax())),
                column("Discounts", r -> String.format("$%.2f", ((RevenueReportRow) r).getDiscounts())),
                column("Total", r -> String.format("$%.2f", ((RevenueReportRow) r).getTotal())));
        reportTable.setItems(FXCollections.observableArrayList(rows));

        currentHeaders = List.of("Date", "Reservations", "Subtotal", "Tax", "Discounts", "Total");
        currentRows = new ArrayList<>();
        for (RevenueReportRow row : rows) {
            currentRows.add(List.of(row.getDate().toString(), String.valueOf(row.getReservationCount()),
                    String.format("%.2f", row.getSubtotal()), String.format("%.2f", row.getTax()),
                    String.format("%.2f", row.getDiscounts()), String.format("%.2f", row.getTotal())));
        }
    }

    private void showOccupancy(List<OccupancyReportRow> rows) {
        reportTable.getColumns().clear();
        reportTable.getColumns().addAll(
                column("Date", r -> ((OccupancyReportRow) r).getDate().toString()),
                column("Rooms Available", r -> String.valueOf(((OccupancyReportRow) r).getRoomsAvailable())),
                column("Rooms Occupied", r -> String.valueOf(((OccupancyReportRow) r).getRoomsOccupied())),
                column("Occupancy %", r -> String.format("%.1f", ((OccupancyReportRow) r).getOccupancyPercent())));
        reportTable.setItems(FXCollections.observableArrayList(rows));

        currentHeaders = List.of("Date", "Rooms Available", "Rooms Occupied", "Occupancy %");
        currentRows = new ArrayList<>();
        for (OccupancyReportRow row : rows) {
            currentRows.add(List.of(row.getDate().toString(), String.valueOf(row.getRoomsAvailable()),
                    String.valueOf(row.getRoomsOccupied()), String.format("%.1f", row.getOccupancyPercent())));
        }
    }

    private void showActivityLog(List<AuditLog> rows) {
        reportTable.getColumns().clear();
        reportTable.getColumns().addAll(
                column("Timestamp", r -> ((AuditLog) r).getTimestamp().format(TIMESTAMP_FORMAT)),
                column("Actor", r -> ((AuditLog) r).getAdminUser().getUsername()),
                column("Action", r -> ((AuditLog) r).getAction()),
                column("Entity Type", r -> nullToEmpty(((AuditLog) r).getEntityType())),
                column("Entity ID", r -> nullToEmpty(((AuditLog) r).getEntityId())),
                column("Message", r -> nullToEmpty(((AuditLog) r).getMessage())));
        reportTable.setItems(FXCollections.observableArrayList(rows));

        currentHeaders = List.of("Timestamp", "Actor", "Action", "Entity Type", "Entity ID", "Message");
        currentRows = new ArrayList<>();
        for (AuditLog log : rows) {
            currentRows.add(List.of(log.getTimestamp().format(TIMESTAMP_FORMAT), log.getAdminUser().getUsername(),
                    log.getAction(), nullToEmpty(log.getEntityType()), nullToEmpty(log.getEntityId()),
                    nullToEmpty(log.getMessage())));
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private TableColumn<Object, String> column(String title, java.util.function.Function<Object, String> extractor) {
        TableColumn<Object, String> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new SimpleStringProperty(extractor.apply(data.getValue())));
        return col;
    }

    @FXML
    private void exportCsv() {
        export("csv", path -> csvExporter.export(path, currentHeaders, currentRows));
    }

    @FXML
    private void exportTxt() {
        export("txt", path -> txtExporter.export(path, currentHeaders, currentRows));
    }

    @FXML
    private void exportPdf() {
        export("pdf", path -> pdfExporter.export(path, currentReportType + " Report", currentHeaders, currentRows));
    }

    private interface Writer {
        void write(Path path) throws IOException;
    }

    private void export(String extension, Writer writer) {
        if (currentHeaders.isEmpty()) {
            messageLabel.setText("Generate a report first.");
            return;
        }

        String filename = currentReportType.toLowerCase().replace(" ", "_") + "_"
                + System.currentTimeMillis() + "." + extension;
        Path path = Path.of("exports", filename);

        try {
            writer.write(path);
            messageLabel.setText("Exported to " + path.toAbsolutePath());
        } catch (IOException e) {
            messageLabel.setText("Export failed: " + e.getMessage());
        }
    }
}
