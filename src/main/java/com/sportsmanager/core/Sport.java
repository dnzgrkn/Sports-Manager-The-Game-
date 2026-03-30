package com.sportsmanager.core;

import java.util.List;

public interface Sport {
    String getName();
    int getSquadSize();
    int getSubstituteCount();
    int getPeriodCount();
    MatchRules getMatchRules();
    StandingsComparator getStandingsComparator();
    List<String> getPositions();
    PlayerAttributeSchema getAttributeSchema();
    List<String> getTacticNames();
}
