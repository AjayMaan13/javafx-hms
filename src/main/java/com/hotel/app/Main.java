package com.hotel.app;

import com.hotel.util.PersistenceManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        AppConfig appConfig = new AppConfig();
        appConfig.seedData();

        // One window, one composition root. The router swaps between the kiosk and admin
        // views inside this single Stage — no more separate windows or launchers.
        SceneRouter router = new SceneRouter(primaryStage, appConfig);
        router.showKiosk();
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
