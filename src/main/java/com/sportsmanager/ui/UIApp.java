package com.sportsmanager.ui;

import com.sportsmanager.app.SportRegistry;
import com.sportsmanager.core.Sport;
import javafx.application.Application;
import javafx.stage.Stage;

public class UIApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sports Manager");
        primaryStage.setResizable(false);
        SceneNavigator.getInstance().setPrimaryStage(primaryStage);
        SceneNavigator.getInstance().navigateTo(Screen.MAIN_MENU);
    }

    public static void main(String[] args) {
        registerSports();
        launch(args);
    }

    private static void registerSports() {
        String[] sportClasses = {
            "com.sportsmanager.sports.football.FootballSport",
            "com.sportsmanager.sports.basketball.BasketballSport"
        };
        for (String className : sportClasses) {
            try {
                Sport sport = (Sport) Class.forName(className)
                        .getDeclaredConstructor()
                        .newInstance();
                SportRegistry.getInstance().register(sport);
            } catch (Exception e) {
                System.err.println("Could not load sport: " + className + " — " + e.getMessage());
            }
        }
    }
}
