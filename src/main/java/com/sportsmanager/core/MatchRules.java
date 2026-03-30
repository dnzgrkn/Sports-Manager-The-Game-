package com.sportsmanager.core;

public interface MatchRules {
    int getWinPoints();
    int getDrawPoints();
    int getLossPoints();
    int getMaxSubstitutions();
    double getInjuryProbability();
}
