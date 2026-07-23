package com.hotel.controller.admin;

import com.hotel.model.Feedback;
import com.hotel.repository.FeedbackRepository;
import com.hotel.util.CsvExporter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FeedbackViewController {

    @FXML
    private TextField guestFilterField;
    @FXML
    private ComboBox<String> ratingComboBox;
    @FXML
    private ComboBox<String> sentimentComboBox;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Label averageRatingLabel;
    @FXML
    private Label messageLabel;

    @FXML
    private TableView<Feedback> feedbackTable;
    @FXML
    private TableColumn<Feedback, String> guestColumn;
    @FXML
    private TableColumn<Feedback, String> ratingColumn;
    @FXML
    private TableColumn<Feedback, String> sentimentColumn;
    @FXML
    private TableColumn<Feedback, String> commentColumn;
    @FXML
    private TableColumn<Feedback, String> dateColumn;

    private final FeedbackRepository feedbackRepository = new FeedbackRepository();
    private final CsvExporter csvExporter = new CsvExporter();

    @FXML
    private void initialize() {
        ratingComboBox.getItems().setAll("Any", "5", "4", "3", "2", "1");
        sentimentComboBox.getItems().setAll("Any", "Positive", "Neutral", "Negative");
        ratingComboBox.getSelectionModel().selectFirst();
        sentimentComboBox.getSelectionModel().selectFirst();

        guestColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReservation().getGuest().getName()));
        ratingColumn.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getRating())));
        sentimentColumn.setCellValueFactory(d -> new SimpleStringProperty(sentimentOf(d.getValue())));
        commentColumn.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getComment() == null ? "" : d.getValue().getComment()));
        dateColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCreatedAt().toString()));

        loadFeedback();
    }

    /** Simple ratings-based sentiment tag: 4-5 Positive, 3 Neutral, 1-2 Negative. */
    private String sentimentOf(Feedback feedback) {
        if (feedback.getRating() >= 4) {
            return "Positive";
        }
        if (feedback.getRating() == 3) {
            return "Neutral";
        }
        return "Negative";
    }

    @FXML
    public void loadFeedback() {
        List<Feedback> all = feedbackRepository.findAll();
        feedbackTable.setItems(FXCollections.observableArrayList(all));
        updateAverageRating(all);
    }

    @FXML
    private void viewFeedback() {
        String guest = guestFilterField.getText() == null ? "" : guestFilterField.getText().trim().toLowerCase();
        String rating = ratingComboBox.getValue();
        String sentiment = sentimentComboBox.getValue();
        LocalDate date = datePicker.getValue();

        List<Feedback> filtered = feedbackRepository.findAll().stream()
                .filter(f -> guest.isEmpty() || f.getReservation().getGuest().getName().toLowerCase().contains(guest))
                .filter(f -> rating == null || rating.equals("Any") || rating.equals(String.valueOf(f.getRating())))
                .filter(f -> sentiment == null || sentiment.equals("Any") || sentiment.equals(sentimentOf(f)))
                .filter(f -> date == null || date.equals(f.getCreatedAt()))
                .collect(Collectors.toList());

        feedbackTable.setItems(FXCollections.observableArrayList(filtered));
        updateAverageRating(filtered);
    }

    @FXML
    private void clearFilters() {
        guestFilterField.clear();
        ratingComboBox.getSelectionModel().selectFirst();
        sentimentComboBox.getSelectionModel().selectFirst();
        datePicker.setValue(null);
        loadFeedback();
    }

    @FXML
    private void exportFeedback() {
        List<Feedback> visible = feedbackTable.getItems();
        if (visible.isEmpty()) {
            messageLabel.setText("No feedback to export — adjust the filters or check back later.");
            return;
        }

        List<String> headers = List.of("Reservation ID", "Guest", "Rating", "Comment", "Date", "Sentiment");
        List<List<String>> rows = new ArrayList<>();
        for (Feedback feedback : visible) {
            rows.add(List.of(
                    feedback.getReservation().getId().toString(),
                    feedback.getReservation().getGuest().getName(),
                    String.valueOf(feedback.getRating()),
                    feedback.getComment() == null ? "" : feedback.getComment(),
                    feedback.getCreatedAt().toString(),
                    sentimentOf(feedback)));
        }

        // Summary rows: average rating + common-tag counts (per the brief's Feedback
        // summary spec), appended after the data as a lightweight trailer.
        double average = visible.stream().mapToInt(Feedback::getRating).average().orElse(0);
        rows.add(List.of("", "", "", "", "", ""));
        rows.add(List.of("Average rating", String.format("%.2f", average), "", "", "", ""));
        for (String tag : List.of("Positive", "Neutral", "Negative")) {
            long count = visible.stream().filter(f -> sentimentOf(f).equals(tag)).count();
            rows.add(List.of(tag + " count", String.valueOf(count), "", "", "", ""));
        }

        try {
            Path path = Path.of("exports", "feedback_summary_" + System.currentTimeMillis() + ".csv");
            csvExporter.export(path, headers, rows);
            messageLabel.setText("Exported to " + path.toAbsolutePath());
        } catch (IOException e) {
            messageLabel.setText("Export failed: " + e.getMessage());
        }
    }

    private void updateAverageRating(List<Feedback> feedbackList) {
        if (feedbackList.isEmpty()) {
            averageRatingLabel.setText("No feedback yet.");
            return;
        }
        double average = feedbackList.stream().mapToInt(Feedback::getRating).average().orElse(0);
        averageRatingLabel.setText(String.format("Average rating: %.1f★ (%d review%s)",
                average, feedbackList.size(), feedbackList.size() == 1 ? "" : "s"));
    }
}
