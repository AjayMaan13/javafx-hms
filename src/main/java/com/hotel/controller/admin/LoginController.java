package com.hotel.controller.admin;

import com.hotel.model.AdminUser;
import com.hotel.repository.AdminUserRepository;
import com.hotel.repository.AuditLogRepository;
import com.hotel.security.AuthService;
import com.hotel.security.BCryptPasswordHasher;
import com.hotel.service.ActivityLogService;
import com.hotel.util.LoggerService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private final AuthService authService;
    private final ActivityLogService activityLogService;
    private final LoggerService loggerService;

    public LoginController() {
        authService = new AuthService(new AdminUserRepository(), new BCryptPasswordHasher());
        activityLogService = new ActivityLogService(new AuditLogRepository(), LoggerService.getInstance());
        loggerService = LoggerService.getInstance();
    }

    @FXML
    private void handleLogin(ActionEvent event) {

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username and password.");
            return;
        }

        Optional<AdminUser> admin = authService.login(username, password);

        if (admin.isPresent()) {

            activityLogService.record(admin.get(), "LOGIN_SUCCESS", "AdminUser",
                    admin.get().getId().toString(), "Admin logged in.");

            try {

                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/admin/AdminShell.fxml")
                );

                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setTitle("Maple Leaf Hotel — Admin");
                stage.setScene(new Scene(root));
                stage.show();

                Stage current =
                        (Stage) ((Node) event.getSource()).getScene().getWindow();

                current.close();

            } catch (IOException e) {
                e.printStackTrace();
                messageLabel.setText("Unable to open admin dashboard.");
            }

        } else {

            // No valid AdminUser to attach to an audit row, so failed attempts are
            // logged to the file log only, not persisted to the audit_log table.
            loggerService.warning("Failed login attempt for username=" + username);

            messageLabel.setText("Invalid username or password.");

            usernameField.clear();
            passwordField.clear();
            usernameField.requestFocus();
        }
    }
}
