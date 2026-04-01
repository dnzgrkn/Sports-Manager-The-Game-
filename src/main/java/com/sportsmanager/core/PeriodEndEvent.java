package com.sportsmanager.core;

public class PeriodEndEvent extends MatchEvent {

    private final int periodIndex;
    private final int homeScore;
    private final int awayScore;

    public PeriodEndEvent(int periodIndex, int homeScore, int awayScore) {
        super(EventType.PERIOD_END,
              "Period " + (periodIndex + 1) + " ended — " + homeScore + ":" + awayScore);
        this.periodIndex = periodIndex;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
    }

    public int getPeriodIndex() { return periodIndex; }
    public int getHomeScore()   { return homeScore; }
    public int getAwayScore()   { return awayScore; }
}
