package com.sportsmanager.core;

public class Fixture {
    private final Team homeTeam;
    private final Team awayTeam;
    private final int weekNumber;
    private MatchResult result;

    public Fixture(Team homeTeam, Team awayTeam, int weekNumber) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.weekNumber = weekNumber;
        this.result = null;
    }

    public boolean isPlayed() {
        return result != null;
    }

    public Team getHomeTeam()      { return homeTeam; }
    public Team getAwayTeam()      { return awayTeam; }
    public int getWeekNumber()     { return weekNumber; }
    public MatchResult getResult() { return result; }
    public void setResult(MatchResult result) { this.result = result; }
}
