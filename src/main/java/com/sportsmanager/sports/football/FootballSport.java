package com.sportsmanager.sports.football;

import com.sportsmanager.core.MatchRules;
import com.sportsmanager.core.PlayerAttributeSchema;
import com.sportsmanager.core.Sport;
import com.sportsmanager.core.StandingsComparator;

import java.util.List;

public class FootballSport implements Sport {

    @Override
    public String getName() { return "Football"; }

    @Override
    public int getSquadSize() { return 11; }

    @Override
    public int getSubstituteCount() { return 4; }

    @Override
    public int getPeriodCount() { return 2; }

    @Override
    public MatchRules getMatchRules() { return new FootballMatchRules(); }

    @Override
    public List<String> getPositions() {
        return List.of("GK", "CB", "LB", "RB", "CM", "LM", "RM", "CF");
    }

    @Override
    public List<String> getTacticNames() {
        return FootballTactic.getAll().stream()
                .map(FootballTactic::getName)
                .toList();
    }

    @Override
    public PlayerAttributeSchema getAttributeSchema() {
        return new PlayerAttributeSchema();
    }

    @Override
    public StandingsComparator getStandingsComparator() {
        return null;
    }
}
