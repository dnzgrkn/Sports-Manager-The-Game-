package com.sportsmanager.app;

import com.sportsmanager.core.League;
import com.sportsmanager.core.Sport;

public class GenerationService {

    public League createLeague(Sport sport, String name, int teamCount) {
        if (sport == null) {
            throw new IllegalArgumentException("Sport must not be null");
        }

        return sport.generateLeague(name, teamCount);
    }
}
