package com.sportsmanager.sports.football;

import com.sportsmanager.core.LeagueRecord;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FootballStandingsComparatorTest {

    private final FootballStandingsComparator comparator = new FootballStandingsComparator();

    private LeagueRecord record(String name) {
        return new LeagueRecord(UUID.randomUUID(), name);
    }

    @Test
    void testSort_byPoints() {
        LeagueRecord low = record("LowPoints");
        low.recordLoss(0, 1);   // 0 pts

        LeagueRecord high = record("HighPoints");
        high.recordWin(2, 0);   // 3 pts

        List<LeagueRecord> standings = new ArrayList<>(List.of(low, high));
        comparator.sort(standings);

        assertEquals("HighPoints", standings.get(0).getTeamName());
    }

    @Test
    void testSort_equalPoints_byGoalDifference() {
        LeagueRecord lowGD = record("LowGD");
        lowGD.recordWin(1, 0);  // 3 pts, GD +1

        LeagueRecord highGD = record("HighGD");
        highGD.recordWin(3, 0); // 3 pts, GD +3

        List<LeagueRecord> standings = new ArrayList<>(List.of(lowGD, highGD));
        comparator.sort(standings);

        assertEquals("HighGD", standings.get(0).getTeamName());
    }

    @Test
    void testSort_equalPointsAndGD_byGoalsFor() {
        LeagueRecord lowGF = record("LowGF");
        lowGF.recordWin(2, 1);  // 3 pts, GD +1, GF 2

        LeagueRecord highGF = record("HighGF");
        highGF.recordWin(3, 2); // 3 pts, GD +1, GF 3

        List<LeagueRecord> standings = new ArrayList<>(List.of(lowGF, highGF));
        comparator.sort(standings);

        assertEquals("HighGF", standings.get(0).getTeamName());
    }

    @Test
    void testSort_doesNotThrow_withEmptyList() {
        List<LeagueRecord> standings = new ArrayList<>();
        assertDoesNotThrow(() -> comparator.sort(standings));
        assertTrue(standings.isEmpty());
    }

    @Test
    void testSort_doesNotThrow_withSingleEntry() {
        LeagueRecord only = record("OnlyTeam");
        only.recordDraw(1, 1);

        List<LeagueRecord> standings = new ArrayList<>(List.of(only));
        assertDoesNotThrow(() -> comparator.sort(standings));
        assertEquals(1, standings.size());
    }
}
