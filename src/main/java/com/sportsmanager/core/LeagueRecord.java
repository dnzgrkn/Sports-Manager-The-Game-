package com.sportsmanager.core;

import java.util.UUID;

public class LeagueRecord {
    private final UUID teamId;
    private final String teamName;
    private int played;
    private int wins;
    private int draws;
    private int losses;
    private int goalsFor;
    private int goalsAgainst;
    private int points;

    public LeagueRecord(UUID teamId, String teamName) {
        this.teamId = teamId;
        this.teamName = teamName;
    }

    public void recordWin(int gf, int ga) {
        played++;
        wins++;
        goalsFor += gf;
        goalsAgainst += ga;
        points += 3;
    }

    public void recordDraw(int gf, int ga) {
        played++;
        draws++;
        goalsFor += gf;
        goalsAgainst += ga;
        points += 1;
    }

    public void recordLoss(int gf, int ga) {
        played++;
        losses++;
        goalsFor += gf;
        goalsAgainst += ga;
    }

    public int getGoalDifference() {
        return goalsFor - goalsAgainst;
    }

    public UUID getTeamId()     { return teamId; }
    public String getTeamName() { return teamName; }
    public int getPlayed()      { return played; }
    public int getWins()        { return wins; }
    public int getDraws()       { return draws; }
    public int getLosses()      { return losses; }
    public int getGoalsFor()    { return goalsFor; }
    public int getGoalsAgainst(){ return goalsAgainst; }
    public int getPoints()      { return points; }
}