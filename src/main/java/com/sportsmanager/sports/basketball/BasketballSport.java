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
        PlayerAttributeSchema schema = new PlayerAttributeSchema();
        schema.addAttribute("shooting", 40, 99);
        schema.addAttribute("passing", 30, 99);
        schema.addAttribute("dribbling", 30, 99);
        schema.addAttribute("defending", 20, 99);
        schema.addAttribute("rebounding", 20, 99);
        schema.addAttribute("speed", 40, 99);
        schema.addAttribute("stamina", 40, 99);
        return schema;
    }

    @Override
    public StandingsComparator getStandingsComparator() {
        return new BasketballStandingsComparator();
    }

    @Override
    public League generateLeague(String name, int teamCount) {
        return new BasketballDataLoader(this).generateLeague(name, teamCount);
    }

    @Override
    public List<Tactic> getTactics() {
        return List.copyOf(BasketballTactic.getAll());
    }

    @Override
    public AbstractMatch createMatch(Team home, Team away, MatchEventBus eventBus) {
        return new BasketballMatch(home, away, this, eventBus);
    }
}
