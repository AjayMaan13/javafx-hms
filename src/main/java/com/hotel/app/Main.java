package com.hotel.app;

import com.hotel.controller.kiosk.KioskShellController;
import com.hotel.util.PersistenceManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        AppConfig appConfig = new AppConfig();
        appConfig.seedData();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/kiosk/KioskShell.fxml"));
        Parent root = loader.load();

        KioskShellController shell = loader.getController();
        shell.setAppConfig(appConfig);

        primaryStage.setTitle("Maple Leaf Hotel — Kiosk");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop() {
        PersistenceManager.getInstance().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
