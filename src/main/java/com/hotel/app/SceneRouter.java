package com.hotel.app;

import com.hotel.controller.admin.AdminShellController;
import com.hotel.controller.admin.LoginController;
import com.hotel.controller.kiosk.KioskShellController;
import com.hotel.model.AdminUser;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Owns the single application window and swaps its scene root between the three top-level
 * views — the public kiosk, the staff login, and the admin shell. This replaces the old
 * "open a new Stage, close the old one" pattern so the whole app lives in one window and a
 * user can move between the kiosk and admin sides without spawning extra windows (and
 * without two processes fighting over the H2 file lock).
 *
 * One SceneRouter and one {@link AppConfig} exist per app run; every view is handed both.
 */
public class SceneRouter {

    private final Stage stage;
    private final AppConfig appConfig;

    public SceneRouter(Stage stage, AppConfig appConfig) {
        this.stage = stage;
        this.appConfig = appConfig;
    }

    /** The public self-check-in kiosk. The default view on launch and after admin logout. */
    public void showKiosk() {
        KioskShellController shell = swapRoot("/fxml/kiosk/KioskShell.fxml");
        shell.setRouter(this);
        shell.setAppConfig(appConfig);
        stage.setTitle("Maple Leaf Hotel — Kiosk");
    }

    /** Staff login screen, reached from the kiosk's "Staff Login" button. */
    public void showAdminLogin() {
        LoginController login = swapRoot("/fxml/admin/Login.fxml");
        login.setRouter(this);
        login.setAppConfig(appConfig);
        stage.setTitle("Maple Leaf Hotel — Staff Login");
    }

    /** The admin back-office, reached after a successful login. */
    public void showAdminShell(AdminUser admin) {
        AdminShellController shell = swapRoot("/fxml/admin/AdminShell.fxml");
        shell.setRouter(this);
        shell.setCurrentAdmin(admin);
        // setAppConfig triggers the first navigation, so it must run after router + admin are set.
        shell.setAppConfig(appConfig);
        stage.setTitle("Maple Leaf Hotel — Admin");
    }

    /**
     * Loads a top-level FXML and installs it as the current window's root, reusing the one
     * Scene after it exists (only the very first view creates it).
     */
    private <T> T swapRoot(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = stage.getScene();
            if (scene == null) {
                stage.setScene(new Scene(root));
            } else {
                scene.setRoot(root);
            }
            return loader.getController();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load view: " + fxmlPath, e);
        }
    }
}
