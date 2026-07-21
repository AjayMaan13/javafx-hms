package com.hotel.app;

import com.hotel.util.PersistenceManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AdminMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        AppConfig appConfig = new AppConfig();
        appConfig.seedData();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/Login.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Maple Leaf Hotel - Admin");
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
