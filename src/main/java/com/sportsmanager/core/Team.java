package com.sportsmanager.core;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Team {
    private final UUID id;
    private String name;
    private String logoPath;
    private final List<Player> squad;
    private Tactic activeTactic;
    private int maxSubstitutionsUsed;

    public Team(String name, String logoPath) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.logoPath = logoPath;
        this.squad = new ArrayList<>();
        this.maxSubstitutionsUsed = 0;
    }

    public void addPlayer(Player p) {
        squad.add(p);
    }

    public List<Player> getAvailablePlayers() {
        List<Player> available = new ArrayList<>();
        for (Player p : squad) {
            if (p.isAvailable()) {
                available.add(p);
            }
        }
        return available;
    }

    public List<Player> getStartingLineup(int count) {
        List<Player> available = getAvailablePlayers();
        return available.subList(0, Math.min(count, available.size()));
    }

    public boolean makeSubstitution(Player out, Player in) {
        if (activeTactic == null) return false;
        if (!squad.contains(out) || !squad.contains(in)) return false;
        maxSubstitutionsUsed++;
        return true;
    }

    public void setActiveTactic(Tactic t) {
        this.activeTactic = t;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLogoPath() { return logoPath; }
    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }
    public List<Player> getSquad() { return squad; }
    public Tactic getActiveTactic() { return activeTactic; }
    public int getMaxSubstitutionsUsed() { return maxSubstitutionsUsed; }
}
