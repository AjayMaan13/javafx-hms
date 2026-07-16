package com.hotel.controller.kiosk;

import com.hotel.model.Addon;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.model.enums.RoomType;
import com.hotel.service.BookingValidationException;
import com.hotel.service.PricingService;
import com.hotel.service.RoomFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EstimateController implements KioskStepController {

    @FXML
    private Label guestNameLabel;
    @FXML
    private Label roomsLabel;
    @FXML
    private Label addonsLabel;
    @FXML
    private Label subtotalLabel;
    @FXML
    private Label taxLabel;
    @FXML
    private Label loyaltyLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private Label errorLabel;

    private KioskShellController shell;
    private BookingDraft draft;

    @Override
    public void init(KioskShellController shell, BookingDraft draft) {
        this.shell = shell;
        this.draft = draft;
    }

    @Override
    public void onShow() {
        guestNameLabel.setText(draft.getGuestFullName() + "  ·  " + draft.getGuestEmail());

        // Pricing preview only — these Room objects aren't persisted or reserved yet.
        // Real availability is re-checked (and real rooms assigned) when Confirm is pressed.
        List<Room> previewRooms = new ArrayList<>();
        StringBuilder roomsSummary = new StringBuilder();
        for (Map.Entry<RoomType, Integer> entry : draft.getRoomSelections().entrySet()) {
            int quantity = entry.getValue();
            if (quantity <= 0) {
                continue;
            }
            for (int i = 0; i < quantity; i++) {
                previewRooms.add(RoomFactory.create(entry.getKey(), "preview"));
            }
            roomsSummary.append(quantity).append(" x ").append(entry.getKey()).append("   ");
        }
        roomsLabel.setText(roomsSummary.length() == 0 ? "No rooms selected" : roomsSummary.toString().trim());

        List<Addon> addons = shell.getAppConfig().getAddonRepository().findAll().stream()
                .filter(addon -> draft.getSelectedAddonIds().contains(addon.getId()))
                .collect(Collectors.toList());
        double addonTotal = addons.stream().mapToDouble(Addon::getPrice).sum();
        addonsLabel.setText(addons.isEmpty() ? "None"
                : addons.stream().map(Addon::getName).collect(Collectors.joining(", ")));

        PricingService pricingService = shell.getAppConfig().getPricingService();
        double roomSubtotal = pricingService.calculateSubtotal(previewRooms, draft.getCheckIn(), draft.getCheckOut());
        double subtotal = roomSubtotal + addonTotal;
        double tax = pricingService.calculateTax(subtotal);
        double total = subtotal + tax;

        subtotalLabel.setText(String.format("$%.2f", subtotal));
        taxLabel.setText(String.format("$%.2f", tax));
        loyaltyLabel.setText("—");
        totalLabel.setText(String.format("$%.2f", total));
        errorLabel.setText("");
    }

    @FXML
    private void handleBack() {
        shell.goBack();
    }

    @FXML
    private void handleConfirm() {
        try {
            Reservation reservation = shell.getAppConfig().getReservationService().createReservation(draft);
            shell.setLastReservationId(reservation.getId());
            errorLabel.setText("");
            shell.goNext();
        } catch (BookingValidationException e) {
            errorLabel.setText(e.getMessage());
        }
    }
}
