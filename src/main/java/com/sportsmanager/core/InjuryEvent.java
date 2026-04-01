package com.sportsmanager.core;

public class InjuryEvent extends MatchEvent {

    private final String playerName;
    private final int gamesMissed;

    public InjuryEvent(String playerName, int gamesMissed) {
        super(EventType.INJURY, playerName + " injured — " + gamesMissed + " game(s) missed");
        this.playerName = playerName;
        this.gamesMissed = gamesMissed;
    }

    public String getPlayerName() { return playerName; }
    public int getGamesMissed()   { return gamesMissed; }
}
