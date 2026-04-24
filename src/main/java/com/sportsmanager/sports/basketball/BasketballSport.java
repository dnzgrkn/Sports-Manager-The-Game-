package com.sportsmanager.sports.basketball;

import com.sportsmanager.core.*;

import java.util.List;

public class BasketballSport implements Sport {

    @Override
    public String getName() { return "Basketball"; }

    @Override
    public int getSquadSize() { return 5; }

    @Override
    public int getSubstituteCount() { return 7; }

    @Override
    public int getPeriodCount() { return 4; }

    @Override
    public MatchRules getMatchRules() { return new BasketballMatchRules(); }

    @Override
    public List<String> getPositions() {
        return List.of("PG", "SG", "SF", "PF", "C");
    }

    @Override
    public List<String> getTacticNames() {
        return BasketballTactic.getAll().stream()
                .map(BasketballTactic::getName)
                .toList();
    }

    @Override
    public PlayerAttributeSchema getAttributeSchema() {
        return new PlayerAttributeSchema();
    }

    @Override
    public StandingsComparator getStandingsComparator() { return null; }

    @Override
    public League generateLeague(String name, int teamCount) {
        throw new UnsupportedOperationException("Basketball league generation not implemented yet");
    }

    @Override
    public AbstractMatch createMatch(Team home, Team away, MatchEventBus eventBus) {
        throw new UnsupportedOperationException("Basketball match creation not implemented yet");
    }
}
