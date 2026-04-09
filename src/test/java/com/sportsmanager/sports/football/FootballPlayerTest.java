package com.sportsmanager.sports.football;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FootballPlayerTest {

    @Test
    void testComputeOverallRating_GK() {
        FootballPlayer highDefending = createPlayer("GK", attributesWith("defending", 90));
        FootballPlayer highPace = createPlayer("GK", attributesWith("pace", 90));

        assertTrue(highDefending.computeOverallRating() > highPace.computeOverallRating());
    }

    @Test
    void testComputeOverallRating_CF() {
        FootballPlayer highShooting = createPlayer("CF", attributesWith("shooting", 90));
        FootballPlayer highPace = createPlayer("CF", attributesWith("pace", 90));

        assertTrue(highShooting.computeOverallRating() > highPace.computeOverallRating());
    }

    @Test
    void testComputeOverallRating_CM() {
        FootballPlayer highPassing = createPlayer("CM", attributesWith("passing", 90));
        FootballPlayer highDribbling = createPlayer("CM", attributesWith("dribbling", 90));

        assertTrue(highPassing.computeOverallRating() > highDribbling.computeOverallRating());
    }

    @Test
    void testOverallRating_range() {
        String[] positions = {"GK", "CB", "LB", "RB", "CM", "LM", "RM", "CF", "UNKNOWN"};

        for (String position : positions) {
            int minRating = createPlayer(position, baseAttributes(0)).computeOverallRating();
            int maxRating = createPlayer(position, baseAttributes(99)).computeOverallRating();

            assertTrue(minRating >= 0 && minRating <= 99);
            assertTrue(maxRating >= 0 && maxRating <= 99);
        }
    }

    @Test
    void testOverallRating_highStatGK_vs_highStatCF() {
        Map<String, Integer> attributes = new HashMap<>();
        attributes.put("pace", 30);
        attributes.put("shooting", 20);
        attributes.put("passing", 45);
        attributes.put("dribbling", 40);
        attributes.put("defending", 95);
        attributes.put("heading", 90);
        attributes.put("stamina", 70);

        FootballPlayer goalkeeper = createPlayer("GK", attributes);
        FootballPlayer centerForward = createPlayer("CF", attributes);

        assertNotEquals(goalkeeper.computeOverallRating(), centerForward.computeOverallRating());
    }

    private FootballPlayer createPlayer(String position, Map<String, Integer> attributes) {
        FootballPlayer player = new FootballPlayer("Test Player", 25, position);
        player.setAttributes(attributes);
        return player;
    }

    private Map<String, Integer> attributesWith(String key, int value) {
        Map<String, Integer> attributes = baseAttributes(50);
        attributes.put(key, value);
        return attributes;
    }

    private Map<String, Integer> baseAttributes(int value) {
        Map<String, Integer> attributes = new HashMap<>();
        attributes.put("pace", value);
        attributes.put("shooting", value);
        attributes.put("passing", value);
        attributes.put("dribbling", value);
        attributes.put("defending", value);
        attributes.put("heading", value);
        attributes.put("stamina", value);
        return attributes;
    }
}
