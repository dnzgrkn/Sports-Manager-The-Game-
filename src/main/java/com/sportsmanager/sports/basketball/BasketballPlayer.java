package com.sportsmanager.sports.basketball;

import com.sportsmanager.core.Player;

import java.util.Map;

public class BasketballPlayer extends Player {

    public BasketballPlayer(String name, int age, String position) {
        super(name, age, position);
    }

    @Override
    public int computeOverallRating() {
        Map<String, Integer> a = getAttributes();
        int shooting   = a.getOrDefault("shooting",   0);
        int passing    = a.getOrDefault("passing",    0);
        int dribbling  = a.getOrDefault("dribbling",  0);
        int defending  = a.getOrDefault("defending",  0);
        int rebounding = a.getOrDefault("rebounding", 0);
        int speed      = a.getOrDefault("speed",      0);
        int stamina    = a.getOrDefault("stamina",    0);

        double rating = switch (getPosition()) {
            case "PG" -> passing * 0.30 + dribbling * 0.25 + speed * 0.20 + shooting * 0.15 + stamina * 0.10;
            case "SG" -> shooting * 0.35 + dribbling * 0.20 + speed * 0.20 + passing * 0.15 + stamina * 0.10;
            case "SF" -> shooting * 0.25 + defending * 0.20 + rebounding * 0.20 + speed * 0.20 + stamina * 0.15;
            case "PF" -> rebounding * 0.35 + defending * 0.25 + shooting * 0.20 + stamina * 0.15 + passing * 0.05;
            case "C"  -> rebounding * 0.40 + defending * 0.30 + shooting * 0.15 + stamina * 0.15;
            default   -> (shooting + passing + dribbling + defending + rebounding + speed + stamina) / 7.0;
        };

        return (int) Math.round(rating);
    }
}
