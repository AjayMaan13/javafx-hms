package com.hotel.controller.admin;

import com.hotel.app.AppConfig;
import com.hotel.app.SceneRouter;
import com.hotel.model.AdminUser;
import com.hotel.security.AuthService;
import com.hotel.service.ActivityLogService;
import com.hotel.util.LoggerService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Optional;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private AppConfig appConfig;
    private SceneRouter router;
    private AuthService authService;
    private ActivityLogService activityLogService;
    private LoggerService loggerService;

    public void setRouter(SceneRouter router) {
        this.router = router;
    }

    /** "Back to Kiosk" link — for staff who reached this screen by accident. */
    @FXML
    private void handleBackToKiosk() {
        router.showKiosk();
    }

    /**
     * Called by the SceneRouter each time the login view is shown — same AppConfig instance
     * every time, so the whole app run shares one composition root rather than rebuilding it
     * per session.
     */
    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
        this.authService = appConfig.getAuthService();
        this.activityLogService = appConfig.getActivityLogService();
        this.loggerService = appConfig.getLoggerService();
    }

    @FXML
    private void handleLogin() {

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

            // Hand the single window over to the admin shell — no new Stage.
            router.showAdminShell(admin.get());

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
