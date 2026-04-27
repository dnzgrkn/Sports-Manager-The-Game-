package com.sportsmanager.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneNavigator {

    private static SceneNavigator instance;
    private Stage primaryStage;

    private SceneNavigator() {}

    public static SceneNavigator getInstance() {
        if (instance == null) {
            instance = new SceneNavigator();
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void navigateTo(Screen screen) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource(screen.getFxmlPath())
            );
            Parent root = loader.load();
            Scene scene = primaryStage.getScene();
            if (scene == null) {
                primaryStage.setScene(new Scene(root, 900, 700));
            } else {
                scene.setRoot(root);
            }
            primaryStage.show();
        } catch (Exception e) {
            throw new RuntimeException("Failed to navigate to screen: " + screen, e);
        }
    }
}
