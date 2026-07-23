package com.hotel.controller.admin;

import com.hotel.app.AppConfig;
import com.hotel.model.AdminUser;
import com.hotel.security.AuthService;
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

    private AppConfig appConfig;
    private AuthService authService;
    private ActivityLogService activityLogService;
    private LoggerService loggerService;

    /**
     * Called by AdminMain on first launch, and by AdminShellController.handleLogout() on
     * every subsequent re-login — same AppConfig instance both times, so the whole app run
     * shares one composition root rather than rebuilding it per session.
     */
    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
        this.authService = appConfig.getAuthService();
        this.activityLogService = appConfig.getActivityLogService();
        this.loggerService = appConfig.getLoggerService();
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

                AdminShellController shellController = loader.getController();
                shellController.setCurrentAdmin(admin.get());
                shellController.setAppConfig(appConfig);

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
