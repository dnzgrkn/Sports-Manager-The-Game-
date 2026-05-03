package com.sportsmanager.sports.basketball;

import com.sportsmanager.core.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BasketballMatchTest {

    private static final Sport SPORT = new BasketballSport();
    private static final BasketballTactic TACTIC = BasketballTactic.getAll().get(0); // Man-to-Man

    private BasketballPlayer createPlayer(String name, String position) {
        BasketballPlayer p = new BasketballPlayer(name, 25, position);
        Map<String, Integer> attrs = new HashMap<>();
        attrs.put("shooting",   75);
        attrs.put("passing",    75);
        attrs.put("dribbling",  75);
        attrs.put("defending",  75);
        attrs.put("rebounding", 75);
        attrs.put("speed",      75);
        attrs.put("stamina",    75);
        p.setAttributes(attrs);
        return p;
    }

    private Team buildTeam(String name) {
        Team team = new Team(name, "logo.png");
        team.setActiveTactic(TACTIC);
        String[] positions = {"PG", "SG", "SF", "PF", "C"};
        for (int i = 0; i < positions.length; i++) {
            team.addPlayer(createPlayer(name + "_P" + i, positions[i]));
        }
        return team;
    }

    @Test
    void testPlay_returnsResult() {
        MatchEventBus bus = new MatchEventBus();
        BasketballMatch match = new BasketballMatch(buildTeam("Home"), buildTeam("Away"), SPORT, bus);
        MatchResult result = match.play();
        assertNotNull(result);
    }

    @Test
    void testPlay_scoresRealistic() {
        MatchEventBus bus = new MatchEventBus();
        BasketballMatch match = new BasketballMatch(buildTeam("Home"), buildTeam("Away"), SPORT, bus);
        MatchResult result = match.play();
        assertTrue(result.getHomeScore() >= 40,
                "Home team should score at least 40 points, got: " + result.getHomeScore());
        assertTrue(result.getAwayScore() >= 40,
                "Away team should score at least 40 points, got: " + result.getAwayScore());
    }

    @Test
    void testPlay_noTie() {
        MatchEventBus bus = new MatchEventBus();
        BasketballMatch match = new BasketballMatch(buildTeam("Home"), buildTeam("Away"), SPORT, bus);
        MatchResult result = match.play();
        assertNotEquals(result.getHomeScore(), result.getAwayScore(),
                "Basketball should never end in a draw");
    }

    @Test
    void testPlay_fourQuartersPlayed() {
        MatchEventBus bus = new MatchEventBus();
        List<MatchEvent> events = new ArrayList<>();
        bus.subscribe(events::add);

        BasketballMatch match = new BasketballMatch(buildTeam("Home"), buildTeam("Away"), SPORT, bus);
        match.play();

        long periodEndCount = events.stream()
                .filter(e -> e instanceof PeriodEndEvent)
                .count();
        assertEquals(4, periodEndCount, "Exactly 4 PeriodEndEvents should be published");
    }
}
