package com.sportsmanager.sports.basketball;

import com.sportsmanager.core.MatchRules;

public class BasketballMatchRules implements MatchRules {

    @Override
    public int getWinPoints() { return 2; }

    @Override
    public int getDrawPoints() { return 0; }

    @Override
    public int getLossPoints() { return 1; }

    @Override
    public int getMaxSubstitutions() { return Integer.MAX_VALUE; }

    @Override
    public double getInjuryProbability() { return 0.003; }
}
