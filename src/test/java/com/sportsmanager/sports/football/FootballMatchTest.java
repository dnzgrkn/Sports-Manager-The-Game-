package com.sportsmanager.sports.football;

import com.sportsmanager.core.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FootballMatchTest {

    private static final Sport SPORT = new FootballSport();
    private static final FootballTactic TACTIC =
            new FootballTactic("4-4-2 Balanced", 1.0, 1.0, FootballTactic.PressingLevel.MEDIUM);

    private FootballPlayer createPlayer(String name, String position) {
        FootballPlayer p = new FootballPlayer(name, 25, position);
        Map<String, Integer> attrs = new HashMap<>();
        attrs.put("pace",      75);
        attrs.put("shooting",  75);
        attrs.put("passing",   75);
        attrs.put("dribbling", 75);
        attrs.put("defending", 75);
        attrs.put("heading",   75);
        attrs.put("stamina",   75);
        p.setAttributes(attrs);
        return p;
    }

    private Team buildTeam(String name) {
        Team team = new Team(name, "logo.png");
        team.setActiveTactic(TACTIC);
        String[] positions = {"GK", "CB", "CB", "LB", "RB", "CM", "CM", "LM", "RM", "CF", "CF"};
        for (int i = 0; i < positions.length; i++) {
            team.addPlayer(createPlayer(name + "_P" + i, positions[i]));
        }
        return team;
    }

    @Test
    void testPlay_returnsResult() {
        MatchEventBus bus = new MatchEventBus();
        FootballMatch match = new FootballMatch(buildTeam("Home"), buildTeam("Away"), SPORT, bus);
        MatchResult result = match.play();
        assertNotNull(result);
    }

    @Test
    void testPlay_scoresNonNegative() {
        MatchEventBus bus = new MatchEventBus();
        FootballMatch match = new FootballMatch(buildTeam("Home"), buildTeam("Away"), SPORT, bus);
        MatchResult result = match.play();
        assertTrue(result.getHomeScore() >= 0);
        assertTrue(result.getAwayScore() >= 0);
    }

    @Test
    void testPlay_periodCount() {
        FootballMatch match = new FootballMatch(buildTeam("Home"), buildTeam("Away"), SPORT, new MatchEventBus());
        assertEquals(2, match.getPeriodCount());
    }

    @Test
    void testPlay_eventsBusReceivesEvents() {
        MatchEventBus bus = new MatchEventBus();
        List<MatchEvent> received = new ArrayList<>();
        bus.subscribe(received::add);

        FootballMatch match = new FootballMatch(buildTeam("Home"), buildTeam("Away"), SPORT, bus);
        match.play();

        long periodEndCount = received.stream()
                .filter(e -> e instanceof PeriodEndEvent)
                .count();
        assertTrue(periodEndCount >= 2, "Expected at least 2 PeriodEndEvents, got: " + periodEndCount);
    }

    @Test
    void testPlay_simulationIsReproducible_withSeed() {
        long seed = 42L;

        MatchEventBus bus1 = new MatchEventBus();
        FootballMatch match1 = new FootballMatch(buildTeam("Home"), buildTeam("Away"), SPORT, bus1, seed);
        MatchResult result1 = match1.play();

        MatchEventBus bus2 = new MatchEventBus();
        FootballMatch match2 = new FootballMatch(buildTeam("Home"), buildTeam("Away"), SPORT, bus2, seed);
        MatchResult result2 = match2.play();

        assertEquals(result1.getHomeScore(), result2.getHomeScore());
        assertEquals(result1.getAwayScore(), result2.getAwayScore());
    }
}
