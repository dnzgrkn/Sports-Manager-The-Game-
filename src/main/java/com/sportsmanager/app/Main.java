package com.sportsmanager.app;

import com.sportsmanager.sports.basketball.BasketballSport;
import com.sportsmanager.sports.football.FootballSport;
import com.sportsmanager.ui.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        SceneNavigator.init(stage);
    }

    public static void main(String[] args) {
        SportRegistry.getInstance().register(new FootballSport());
        SportRegistry.getInstance().register(new BasketballSport());
        launch();
    }
}
