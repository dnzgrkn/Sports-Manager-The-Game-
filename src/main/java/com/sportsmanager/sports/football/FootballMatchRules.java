package com.sportsmanager.sports.football;

import com.sportsmanager.core.MatchRules;

public class FootballMatchRules implements MatchRules {

    @Override
    public int getWinPoints() { return 3; }

    @Override
    public int getDrawPoints() { return 1; }

    @Override
    public int getLossPoints() { return 0; }

    @Override
    public int getMaxSubstitutions() { return 5; }

    @Override
    public double getInjuryProbability() { return 0.008; }
}
