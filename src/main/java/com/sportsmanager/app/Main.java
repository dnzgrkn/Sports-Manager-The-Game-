package com.sportsmanager.app;

import com.sportsmanager.sports.football.FootballSport;

public class Main {

    public static void main(String[] args) {
        SportRegistry.getInstance().register(new FootballSport());
    }
}
