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
        PlayerAttributeSchema schema = new PlayerAttributeSchema();
        schema.addAttribute("pace",      40, 99);
        schema.addAttribute("shooting",  20, 99);
        schema.addAttribute("passing",   30, 99);
        schema.addAttribute("dribbling", 30, 99);
        schema.addAttribute("defending", 20, 99);
        schema.addAttribute("heading",   20, 99);
        schema.addAttribute("stamina",   40, 99);
        return schema;
    }

    @Override
    public StandingsComparator getStandingsComparator() {
        return new FootballStandingsComparator();
    }
}
