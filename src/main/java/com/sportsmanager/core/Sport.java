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

    default List<Tactic> getTactics() {
        return getTacticNames().stream().map(Tactic::new).toList();
    }

    /** League oluşturmayı sport implementasyonuna devreder; app katmanı DataLoader'a bağımlı olmaz. */
    League generateLeague(String name, int teamCount);

    /** Match oluşturmayı sport implementasyonuna devreder; app katmanı FootballMatch'e bağımlı olmaz. */
    AbstractMatch createMatch(Team home, Team away, MatchEventBus eventBus);
}
