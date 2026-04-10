package com.sportsmanager.core;

public abstract class AbstractMatch {

    protected final String homeTeamName;
    protected final String awayTeamName;
    protected int homeScore;
    protected int awayScore;
    protected boolean isFinished;

    protected AbstractMatch(String homeTeamName, String awayTeamName) {
        this.homeTeamName = homeTeamName;
        this.awayTeamName = awayTeamName;
        this.homeScore = 0;
        this.awayScore = 0;
        this.isFinished = false;
    }

    // Template Method
    public final MatchResult play() {
        preMatch();
        int periods = getPeriodCount();
        // Guard: getPeriodCount() 0 veya negatif dönerse simülasyon atlanır
        if (periods <= 0) {
            isFinished = true;
            return buildResult();
        }
        for (int i = 0; i < periods; i++) {
            simulatePeriod(i);
            postPeriodHook(i);
        }
        isFinished = true;
        return buildResult();
    }

    protected void preMatch() {}

    protected abstract void simulatePeriod(int periodIndex);

    protected void postPeriodHook(int periodIndex) {}

    protected abstract MatchResult buildResult();

    public abstract int getPeriodCount();

    public String getHomeTeamName() { return homeTeamName; }
    public String getAwayTeamName() { return awayTeamName; }
    public int getHomeScore() { return homeScore; }
    public int getAwayScore() { return awayScore; }
    public boolean isFinished() { return isFinished; }
}
