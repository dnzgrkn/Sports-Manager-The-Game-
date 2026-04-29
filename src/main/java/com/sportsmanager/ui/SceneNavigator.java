package com.sportsmanager.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public final class SceneNavigator {

    private static final String MAIN_MENU_FXML = "/fxml/MainMenu.fxml";
    private static final String LEAGUE_FXML = "/fxml/League.fxml";
    private static final String PRE_MATCH_FXML = "/fxml/PreMatch.fxml";
    private static final String MATCH_FXML = "/fxml/Match.fxml";
    private static final String SEASON_END_FXML = "/fxml/SeasonEnd.fxml";

    private static Stage primaryStage;

    private SceneNavigator() {}

    public enum Screen {
        MAIN_MENU,
        LEAGUE,
        PRE_MATCH,
        MATCH,
        SEASON_END
    }

    public static void init(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Sports Manager");
        navigateTo(Screen.MAIN_MENU);
    }

    public static void navigateTo(Screen screen) {
        if (primaryStage == null) {
            throw new IllegalStateException("SceneNavigator.init(stage) must be called first");
        }

        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    SceneNavigator.class.getResource(getFxmlPath(screen)),
                    "Missing FXML for screen: " + screen
            ));
            Scene scene = new Scene(loader.load());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to navigate to screen: " + screen, e);
        }
    }

    private static String getFxmlPath(Screen screen) {
        return switch (screen) {
            case MAIN_MENU -> MAIN_MENU_FXML;
            case LEAGUE -> LEAGUE_FXML;
            case PRE_MATCH -> PRE_MATCH_FXML;
            case MATCH -> MATCH_FXML;
            case SEASON_END -> SEASON_END_FXML;
        };
    }
}
