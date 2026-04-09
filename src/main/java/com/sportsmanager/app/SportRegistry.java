package com.sportsmanager.app;

import com.sportsmanager.core.Sport;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SportRegistry {

    private static SportRegistry instance;

    private final Map<String, Sport> sports = new LinkedHashMap<>();

    private SportRegistry() {}

    public static SportRegistry getInstance() {
        if (instance == null) {
            instance = new SportRegistry();
        }
        return instance;
    }

    public void register(Sport sport) {
        sports.put(sport.getName(), sport);
    }

    public Sport get(String name) {
        return sports.get(name);
    }

    public List<String> getAvailableSportNames() {
        return new ArrayList<>(sports.keySet());
    }
}
