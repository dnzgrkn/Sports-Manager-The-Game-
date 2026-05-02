package com.sportsmanager.app;

import com.sportsmanager.core.League;
import com.sportsmanager.core.Sport;
import com.sportsmanager.core.Team;

public class GameSession {

    private static GameSession instance;

    private final GenerationService generationService = new GenerationService();

    private League activeLeague;
    private Team playerTeam;
    private Sport activeSport;
    private int currentWeek;
    private int lastTrainedWeek = -1;

    private GameSession() {}

    public static GameSession getInstance() {
        if (instance == null) {
            instance = new GameSession();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    public void startNewGame(Sport sport, String leagueName, int teamCount) {
        this.activeSport = sport;
        this.currentWeek = 0;
        this.lastTrainedWeek = -1;

        this.activeLeague = generationService.createLeague(sport, leagueName, teamCount);
        this.activeLeague.setCurrentWeek(0);
        this.playerTeam = activeLeague.getTeams().get(0);
    }

    public League getActiveLeague()              { return activeLeague; }
    public void setActiveLeague(League league)   { this.activeLeague = league; }

    public Team getPlayerTeam()                  { return playerTeam; }
    public void setPlayerTeam(Team team)         { this.playerTeam = team; }

    public Sport getActiveSport()                { return activeSport; }
    public void setActiveSport(Sport sport)      { this.activeSport = sport; }

    public int getCurrentWeek()                  { return currentWeek; }
    public void setCurrentWeek(int week)         { this.currentWeek = week; }

    public int getLastTrainedWeek()              { return lastTrainedWeek; }
    public void setLastTrainedWeek(int week)     { this.lastTrainedWeek = week; }
}
