package com.sportsmanager.core;

public abstract class MatchEvent {

    public enum EventType {
        GOAL, INJURY, SUBSTITUTION, PERIOD_END, MATCH_END
    }

    private final long timestamp;
    private final String description;
    private final EventType type;

    protected MatchEvent(EventType type, String description) {
        this.timestamp = System.currentTimeMillis();
        this.type = type;
        this.description = description;
    }

    public long getTimestamp()    { return timestamp; }
    public String getDescription(){ return description; }
    public EventType getType()    { return type; }
}
