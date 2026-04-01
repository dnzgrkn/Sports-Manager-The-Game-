package com.sportsmanager.core;

public class MatchResult {
    private final String homeTeamName;
    private final String awayTeamName;
    private final int homeScore;
    private final int awayScore;

    public MatchResult(String homeTeamName, String awayTeamName, int homeScore, int awayScore) {
        this.homeTeamName = homeTeamName;
        this.awayTeamName = awayTeamName;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
    }

    public boolean isHomeWin() {
        return homeScore > awayScore;
    }

    public boolean isDraw() {
        return homeScore == awayScore;
    }

    public String getHomeTeamName() { return homeTeamName; }
    public String getAwayTeamName() { return awayTeamName; }
    public int getHomeScore() { return homeScore; }
    public int getAwayScore() { return awayScore; }
}
