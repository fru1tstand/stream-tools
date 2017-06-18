package me.fru1t.streamtools;

import javafx.application.Application;
import javafx.stage.Stage;
import me.fru1t.javafx.Controller;
import me.fru1t.streamtools.controllers.MainMenuController;

/**
 * Entry point for the StreamTools application.
 */
public class StreamTools extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Controller c = Controller.create(MainMenuController.class);
        c.setUpStage(primaryStage);
        primaryStage.show();
    }
}
