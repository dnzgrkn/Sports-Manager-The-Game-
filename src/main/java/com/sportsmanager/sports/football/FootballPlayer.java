package com.sportsmanager.sports.football;

import com.sportsmanager.core.Player;

import java.util.Map;

public class FootballPlayer extends Player {

    public FootballPlayer(String name, int age, String position) {
        super(name, age, position);
    }

    @Override
    public int computeOverallRating() {
        Map<String, Integer> a = getAttributes();
        int pace      = a.getOrDefault("pace",      0);
        int shooting  = a.getOrDefault("shooting",  0);
        int passing   = a.getOrDefault("passing",   0);
        int dribbling = a.getOrDefault("dribbling", 0);
        int defending = a.getOrDefault("defending", 0);
        int heading   = a.getOrDefault("heading",   0);
        int stamina   = a.getOrDefault("stamina",   0);

        double rating = switch (getPosition()) {
            case "GK"       -> defending * 0.4 + heading * 0.3 + pace * 0.15 + stamina * 0.15;
            case "CB"       -> defending * 0.4 + heading * 0.3 + pace * 0.15 + passing * 0.15;
            case "LB", "RB" -> pace * 0.3 + defending * 0.3 + passing * 0.2 + stamina * 0.2;
            case "CM"       -> passing * 0.35 + stamina * 0.25 + dribbling * 0.2 + defending * 0.2;
            case "LM", "RM" -> pace * 0.3 + dribbling * 0.3 + passing * 0.2 + shooting * 0.2;
            case "CF"       -> shooting * 0.4 + pace * 0.25 + dribbling * 0.2 + heading * 0.15;
            default         -> (pace + shooting + passing + dribbling + defending + heading + stamina) / 7.0;
        };

        return (int) Math.round(rating);
    }
}
