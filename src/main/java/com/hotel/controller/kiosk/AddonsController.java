package com.hotel.controller.kiosk;

import com.hotel.model.Addon;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddonsController implements KioskStepController {

    @FXML
    private VBox addonsContainer;

    private BookingDraft draft;
    private KioskShellController shell;
    private final List<CheckBox> addonCheckBoxes = new ArrayList<>();

    @Override
    public void init(KioskShellController shell, BookingDraft draft) {
        this.shell = shell;
        this.draft = draft;

        addonsContainer.getChildren().clear();
        addonCheckBoxes.clear();

        List<Addon> addons = shell.getAppConfig().getAddonRepository().findAll();
        for (Addon addon : addons) {
            CheckBox checkBox = new CheckBox(addon.getName());
            checkBox.setSelected(draft.getSelectedAddonIds().contains(addon.getId()));
            checkBox.setUserData(addon.getId());

            Label priceLabel = new Label(String.format("+ $%.2f", addon.getPrice()));
            priceLabel.getStyleClass().add("price-tag");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox row = new HBox(checkBox, spacer, priceLabel);
            row.setAlignment(Pos.CENTER_LEFT);

            addonsContainer.getChildren().add(row);
            addonCheckBoxes.add(checkBox);
        }
    }

    @FXML
    private void handleBack() {
        shell.goBack();
    }

    @FXML
    private void handleNext() {
        draft.getSelectedAddonIds().clear();
        for (CheckBox checkBox : addonCheckBoxes) {
            if (checkBox.isSelected()) {
                draft.getSelectedAddonIds().add((UUID) checkBox.getUserData());
            }
        }
        shell.goNext();
    }
}
