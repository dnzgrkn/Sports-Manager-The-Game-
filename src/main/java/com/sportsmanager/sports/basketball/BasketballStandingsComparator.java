package com.sportsmanager.sports.basketball;

import com.sportsmanager.core.LeagueRecord;
import com.sportsmanager.core.StandingsComparator;
import java.util.Comparator;
import java.util.List;

public class BasketballStandingsComparator implements StandingsComparator {

    private static final Comparator<LeagueRecord> H2H = (a, b) -> {
        int aH2H = a.getHeadToHeadPoints(b.getTeamId());
        int bH2H = b.getHeadToHeadPoints(a.getTeamId());
        return Integer.compare(bH2H, aH2H);
    };

    private static final Comparator<LeagueRecord> RANDOM =
            (a, b) -> Math.random() < 0.5 ? -1 : 1;


    private static final Comparator<LeagueRecord> CHAIN =
            Comparator.<LeagueRecord, Integer>comparing(LeagueRecord::getPoints, Comparator.reverseOrder())
                    .thenComparing(LeagueRecord::getGoalDifference, Comparator.reverseOrder())
                    .thenComparing(H2H)
                    .thenComparing(RANDOM);

    @Override
    public int compare(LeagueRecord a, LeagueRecord b) {
        return CHAIN.compare(a, b);
    }
    public void sort(List<LeagueRecord> standings) {
        standings.sort(this);
    }


}