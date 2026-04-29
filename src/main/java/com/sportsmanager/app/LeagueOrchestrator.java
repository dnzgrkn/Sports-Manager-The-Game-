package com.sportsmanager.app;

import com.sportsmanager.core.AbstractMatch;
import com.sportsmanager.core.Fixture;
import com.sportsmanager.core.League;
import com.sportsmanager.core.MatchEventBus;
import com.sportsmanager.core.MatchResult;
import com.sportsmanager.core.Player;
import com.sportsmanager.core.Team;

import java.util.List;

public class LeagueOrchestrator {

    private final GameSession gameSession;
    private boolean playerMatchThisWeek;
    private Fixture playerFixtureThisWeek;
    private boolean seasonOver;

    public LeagueOrchestrator() {
        this(GameSession.getInstance());
    }

    public LeagueOrchestrator(GameSession gameSession) {
        this.gameSession = gameSession;
    }

    public void advanceWeek() {
        League league = requireLeague();

        int nextWeek = gameSession.getCurrentWeek() + 1;
        gameSession.setCurrentWeek(nextWeek);
        league.setCurrentWeek(nextWeek);

        List<Fixture> fixtures = league.getFixturesForWeek(nextWeek);
        Team playerTeam = gameSession.getPlayerTeam();

        playerFixtureThisWeek = null;
        playerMatchThisWeek = false;

        for (Fixture fixture : fixtures) {
            if (isPlayerFixture(fixture, playerTeam)) {
                playerFixtureThisWeek = fixture;
                playerMatchThisWeek = true;
                continue;
            }

            if (!fixture.isPlayed()) {
                simulateFixture(fixture);
            }
        }

        decrementAllInjuries(league);
        seasonOver = nextWeek >= league.getTotalWeeks();
    }

    public boolean hasPlayerMatchThisWeek() {
        return playerMatchThisWeek;
    }

    public Fixture getPlayerFixtureThisWeek() {
        return playerFixtureThisWeek;
    }

    public boolean isSeasonOver() {
        return seasonOver;
    }

    private League requireLeague() {
        League league = gameSession.getActiveLeague();
        if (league == null) {
            throw new IllegalStateException("No active league in session");
        }
        if (gameSession.getActiveSport() == null) {
            throw new IllegalStateException("No active sport in session");
        }
        return league;
    }

    private boolean isPlayerFixture(Fixture fixture, Team playerTeam) {
        if (playerTeam == null) {
            return false;
        }

        return fixture.getHomeTeam().getId().equals(playerTeam.getId())
                || fixture.getAwayTeam().getId().equals(playerTeam.getId());
    }

    private void simulateFixture(Fixture fixture) {
        AbstractMatch match = gameSession.getActiveSport().createMatch(
                fixture.getHomeTeam(),
                fixture.getAwayTeam(),
                new MatchEventBus()
        );
        MatchResult result = match.play();
        fixture.setResult(result);
        gameSession.getActiveLeague().updateStandings(fixture);
    }

    private void decrementAllInjuries(League league) {
        for (Team team : league.getTeams()) {
            for (Player player : team.getSquad()) {
                player.decrementInjury();
            }
        }
    }
}
