package com.hotel.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class CheckoutController {

    @FXML
    private Label messageLabel;

    @FXML
    public void loadCheckout() {
        messageLabel.setText("Checkout screen loaded.");
    }

    @FXML
    public void checkoutGuest() {
        messageLabel.setText("Balance settled and room marked available for M2 demo.");
    }
}
