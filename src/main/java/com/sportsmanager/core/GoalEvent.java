package com.sportsmanager.core;

public class GoalEvent extends MatchEvent {

    private final String scorerName;
    private final int minute;

    public GoalEvent(String scorerName, int minute) {
        super(EventType.GOAL, scorerName + " scored at minute " + minute);
        this.scorerName = scorerName;
        this.minute = minute;
    }

    public String getScorerName() { return scorerName; }
    public int getMinute()        { return minute; }
}
