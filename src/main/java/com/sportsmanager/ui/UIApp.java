package com.sportsmanager.ui;

import javafx.application.Application;
import javafx.stage.Stage;

public class UIApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        SceneNavigator.init(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
