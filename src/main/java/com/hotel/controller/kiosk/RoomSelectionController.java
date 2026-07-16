package com.hotel.controller.kiosk;

import com.hotel.model.enums.RoomType;
import com.hotel.service.RoomFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

public class RoomSelectionController implements KioskStepController {

    @FXML
    private Label singlePriceLabel;
    @FXML
    private Label doublePriceLabel;
    @FXML
    private Label deluxePriceLabel;
    @FXML
    private Label penthousePriceLabel;

    @FXML
    private Label singleCapacityLabel;
    @FXML
    private Label doubleCapacityLabel;
    @FXML
    private Label deluxeCapacityLabel;
    @FXML
    private Label penthouseCapacityLabel;

    @FXML
    private Spinner<Integer> singleQty;
    @FXML
    private Spinner<Integer> doubleQty;
    @FXML
    private Spinner<Integer> deluxeQty;
    @FXML
    private Spinner<Integer> penthouseQty;

    @FXML
    private Label errorLabel;

    private KioskShellController shell;
    private BookingDraft draft;

    @Override
    public void init(KioskShellController shell, BookingDraft draft) {
        this.shell = shell;
        this.draft = draft;

        describe(RoomType.SINGLE, singlePriceLabel, singleCapacityLabel);
        describe(RoomType.DOUBLE, doublePriceLabel, doubleCapacityLabel);
        describe(RoomType.DELUXE, deluxePriceLabel, deluxeCapacityLabel);
        describe(RoomType.PENTHOUSE, penthousePriceLabel, penthouseCapacityLabel);

        singleQty.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10,
                draft.getRoomSelections().getOrDefault(RoomType.SINGLE, 0)));
        doubleQty.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10,
                draft.getRoomSelections().getOrDefault(RoomType.DOUBLE, 0)));
        deluxeQty.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10,
                draft.getRoomSelections().getOrDefault(RoomType.DELUXE, 0)));
        penthouseQty.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10,
                draft.getRoomSelections().getOrDefault(RoomType.PENTHOUSE, 0)));
    }

    private void describe(RoomType type, Label priceLabel, Label capacityLabel) {
        priceLabel.setText(String.format("$%.2f / night", RoomFactory.basePriceFor(type)));
        capacityLabel.setText("Sleeps " + RoomFactory.capacityFor(type));
    }

    @FXML
    private void handleBack() {
        shell.goBack();
    }

    @FXML
    private void handleNext() {
        draft.setRoomQuantity(RoomType.SINGLE, singleQty.getValue());
        draft.setRoomQuantity(RoomType.DOUBLE, doubleQty.getValue());
        draft.setRoomQuantity(RoomType.DELUXE, deluxeQty.getValue());
        draft.setRoomQuantity(RoomType.PENTHOUSE, penthouseQty.getValue());

        if (draft.getTotalRoomQuantity() == 0) {
            errorLabel.setText("Select at least one room.");
            return;
        }

        int totalGuests = draft.getAdults() + draft.getChildren();
        int totalCapacity = draft.getRoomSelections().entrySet().stream()
                .mapToInt(entry -> RoomFactory.capacityFor(entry.getKey()) * entry.getValue())
                .sum();

        if (totalCapacity < totalGuests) {
            errorLabel.setText(totalGuests + " guests exceed the selected rooms' capacity ("
                    + totalCapacity + "). Choose a larger room or add another room.");
            return;
        }

        errorLabel.setText("");
        shell.goNext();
    }
}
