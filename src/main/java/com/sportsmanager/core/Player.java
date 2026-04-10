package com.sportsmanager.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class Player {
    private final UUID id;
    private String name;
    private int age;
    private int fitness;
    private int injuredForGames;
    private String position;
    private Map<String, Integer> attributes;

    public Player(String name, int age, String position) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.age = age;
        this.position = position;
        this.fitness = 100;
        this.injuredForGames = 0;
        this.attributes = new HashMap<>();
    }

    public abstract int computeOverallRating();

    public boolean isAvailable() {
        return injuredForGames == 0;
    }

    public void decrementInjury() {
        // Guard: 0'ın altına düşmesini engelle
        if (injuredForGames > 0) {
            injuredForGames--;
        }
    }

    public void setInjured(int games) {
        this.injuredForGames = games;
    }

    public UUID getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public int getFitness() { return fitness; }
    public void setFitness(int fitness) { this.fitness = fitness; }

    public int getInjuredForGames() { return injuredForGames; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public Map<String, Integer> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Integer> attributes) { this.attributes = attributes; }
}
