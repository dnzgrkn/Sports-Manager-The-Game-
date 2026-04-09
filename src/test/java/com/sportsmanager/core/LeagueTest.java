package com.sportsmanager.core;

import com.sportsmanager.sports.football.FootballSport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LeagueTest {

    private List<Team> teams;
    private League league;
    private FootballSport sport;

    @BeforeEach
    void setUp() {
        sport = new FootballSport();
        teams = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            teams.add(new Team("Team " + i, "logo" + i + ".png"));
        }
        league = new League("Test League", teams, sport);
    }

    @Test
    void testGenerateFixtures_correctCount() {
        league.generateFixtures();
        int n = teams.size();
        int expected = n * (n - 1);
        assertEquals(expected, league.getFixtures().size());
    }

    @Test
    void testGenerateFixtures_everyTeamPlaysEachOther() {
        league.generateFixtures();

        // Count how many times each (home, away) pair appears
        Map<String, Integer> pairCount = new HashMap<>();
        for (Fixture f : league.getFixtures()) {
            String key = f.getHomeTeam().getId() + "->" + f.getAwayTeam().getId();
            pairCount.merge(key, 1, Integer::sum);
        }

        // Every ordered pair of distinct teams must appear exactly once
        for (Team home : teams) {
            for (Team away : teams) {
                if (home == away) continue;
                String key = home.getId() + "->" + away.getId();
                assertEquals(1, pairCount.getOrDefault(key, 0),
                        "Expected exactly 1 fixture for " + home.getName() + " vs " + away.getName());
            }
        }
    }

    @Test
    void testUpdateStandings_homeWin() {
        league.generateFixtures();
        Fixture fixture = league.getFixtures().get(0);
        Team home = fixture.getHomeTeam();
        Team away = fixture.getAwayTeam();

        fixture.setResult(new MatchResult(home.getName(), away.getName(), 2, 0));
        league.updateStandings(fixture);

        LeagueRecord homeRecord = league.getStandings().get(home.getId());
        LeagueRecord awayRecord = league.getStandings().get(away.getId());

        assertEquals(3, homeRecord.getPoints());
        assertEquals(0, awayRecord.getPoints());
        assertEquals(1, homeRecord.getWins());
        assertEquals(1, awayRecord.getLosses());
    }

    @Test
    void testUpdateStandings_draw() {
        league.generateFixtures();
        Fixture fixture = league.getFixtures().get(0);
        Team home = fixture.getHomeTeam();
        Team away = fixture.getAwayTeam();

        fixture.setResult(new MatchResult(home.getName(), away.getName(), 1, 1));
        league.updateStandings(fixture);

        LeagueRecord homeRecord = league.getStandings().get(home.getId());
        LeagueRecord awayRecord = league.getStandings().get(away.getId());

        assertEquals(1, homeRecord.getPoints());
        assertEquals(1, awayRecord.getPoints());
        assertEquals(1, homeRecord.getDraws());
        assertEquals(1, awayRecord.getDraws());
    }
}
