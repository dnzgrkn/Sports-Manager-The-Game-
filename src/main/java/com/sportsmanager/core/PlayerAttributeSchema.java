package com.sportsmanager.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class PlayerAttributeSchema {

    private final Map<String, int[]> schema = new LinkedHashMap<>();
    private final Random random = new Random();

    public void addAttribute(String name, int min, int max) {
        schema.put(name, new int[]{min, max});
    }

    public int[] getRange(String name) {
        return schema.get(name);
    }

    public Set<String> getAttributeNames() {
        return schema.keySet();
    }

    public boolean isValid(String name, int value) {
        int[] range = schema.get(name);
        if (range == null) return false;
        return value >= range[0] && value <= range[1];
    }

    public int clamp(String name, int value) {
        int[] range = schema.get(name);
        if (range == null) return value;
        return Math.max(range[0], Math.min(range[1], value));
    }

    public int generateRandom(String name) {
        int[] range = schema.get(name);
        if (range == null) throw new IllegalArgumentException("Unknown attribute: " + name);
        return range[0] + random.nextInt(range[1] - range[0] + 1);
    }
}