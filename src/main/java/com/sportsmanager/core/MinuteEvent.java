package com.sportsmanager.core;

public class MinuteEvent extends MatchEvent {
    private final int minute;

    public MinuteEvent(int minute) {
        super(EventType.MINUTE, minute + "'");
        this.minute = minute;
    }

    public int getMinute() { return minute; }
}
