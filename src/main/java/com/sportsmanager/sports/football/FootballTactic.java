package com.sportsmanager.sports.football;

import com.sportsmanager.core.Tactic;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FootballTactic extends Tactic {

    public enum PressingLevel { LOW, MEDIUM, HIGH }

    private final double attackMod;
    private final double defenseMod;
    private final PressingLevel pressingLevel;

    public FootballTactic(String name, double attackMod, double defenseMod, PressingLevel pressingLevel) {
        super(name);
        this.attackMod    = attackMod;
        this.defenseMod   = defenseMod;
        this.pressingLevel = pressingLevel;
    }

    public double getAttackMod()    { return attackMod; }
    public double getDefenseMod()   { return defenseMod; }
    public PressingLevel getPressingLevel() { return pressingLevel; }

    public Map<String, String> getParameters() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("formation",    getName());
        params.put("pressingLevel", pressingLevel.name());
        return params;
    }

    public static List<FootballTactic> getAll() {
        return List.of(
            new FootballTactic("4-3-3 Attack",    1.30, 0.90, PressingLevel.HIGH),
            new FootballTactic("4-4-2 Balanced",  1.00, 1.00, PressingLevel.MEDIUM),
            new FootballTactic("5-3-2 Defensive", 0.80, 1.30, PressingLevel.LOW),
            new FootballTactic("3-5-2 Pressing",  1.10, 0.95, PressingLevel.HIGH),
            new FootballTactic("4-2-3-1 Counter", 1.05, 1.10, PressingLevel.MEDIUM)
        );
    }
}
