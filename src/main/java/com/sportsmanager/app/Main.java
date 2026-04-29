package com.sportsmanager.app;

import com.sportsmanager.core.Sport;
import com.sportsmanager.ui.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        SceneNavigator.init(stage);
    }

    public static void main(String[] args) {
        String[] sports = {
                "com.sportsmanager.sports.football.FootballSport",
                "com.sportsmanager.sports.basketball.BasketballSport"
        };

        for (String className : sports) {
            try {
                Sport s = (Sport) Class.forName(className)
                        .getDeclaredConstructor()
                        .newInstance();
                SportRegistry.getInstance().register(s);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Could not load sport: " + className, e);
            }
        }

        launch();
    }
}
