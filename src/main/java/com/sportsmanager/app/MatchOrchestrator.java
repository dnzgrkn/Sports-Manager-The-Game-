package com.sportsmanager.app;

import com.sportsmanager.core.AbstractMatch;
import com.sportsmanager.core.Fixture;
import com.sportsmanager.core.League;
import com.sportsmanager.core.MatchEventBus;
import com.sportsmanager.core.MatchResult;
import com.sportsmanager.core.Sport;

public class MatchOrchestrator {

    private final GameSession gameSession;

    public MatchOrchestrator() {
        this(GameSession.getInstance());
    }

    public MatchOrchestrator(GameSession gameSession) {
        this.gameSession = gameSession;
    }

    public MatchResult runPlayerMatch(Fixture fixture, MatchEventBus eventBus) {
        if (fixture == null) {
            throw new IllegalArgumentException("Fixture must not be null");
        }
        if (eventBus == null) {
            throw new IllegalArgumentException("MatchEventBus must not be null");
        }
        if (fixture.isPlayed()) {
            return fixture.getResult();
        }

        Sport sport = requireSport();
        League league = requireLeague();

        AbstractMatch match = sport.createMatch(
                fixture.getHomeTeam(),
                fixture.getAwayTeam(),
                eventBus
        );
        MatchResult result = match.play();
        fixture.setResult(result);
        league.updateStandings(fixture);
        return result;
    }

    public void runAIMatch(Fixture fixture) {
        runPlayerMatch(fixture, new MatchEventBus());
    }

    private Sport requireSport() {
        Sport sport = gameSession.getActiveSport();
        if (sport == null) {
            throw new IllegalStateException("No active sport in session");
        }
        return sport;
    }

    private League requireLeague() {
        League league = gameSession.getActiveLeague();
        if (league == null) {
            throw new IllegalStateException("No active league in session");
        }
        return league;
    }
}
