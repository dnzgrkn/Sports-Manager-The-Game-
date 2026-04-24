package com.sportsmanager.sports.basketball;

import com.sportsmanager.core.Tactic;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BasketballTactic extends Tactic {

    private final double attackMod;
    private final double defenseMod;

    public BasketballTactic(String name, double attackMod, double defenseMod) {
        super(name);
        this.attackMod  = attackMod;
        this.defenseMod = defenseMod;
    }

    public double getAttackMod()  { return attackMod; }
    public double getDefenseMod() { return defenseMod; }

    public Map<String, String> getParameters() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("style", getName());
        return params;
    }

    public static List<BasketballTactic> getAll() {
        return List.of(
            new BasketballTactic("Man-to-Man",    1.00, 1.10),
            new BasketballTactic("Zone Defense",  0.90, 1.30),
            new BasketballTactic("Fast Break",    1.30, 0.85),
            new BasketballTactic("Post Up",       1.10, 1.00),
            new BasketballTactic("Pick and Roll", 1.20, 0.95)
        );
    }
}
