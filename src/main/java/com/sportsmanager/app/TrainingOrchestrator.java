package com.sportsmanager.app;

import com.sportsmanager.core.Player;
import com.sportsmanager.core.PlayerAttributeSchema;
import com.sportsmanager.core.Sport;
import com.sportsmanager.core.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TrainingOrchestrator {

    private final GameSession gameSession;
    private final Random random;

    public TrainingOrchestrator() {
        this(GameSession.getInstance(), new Random());
    }

    public TrainingOrchestrator(GameSession gameSession, Random random) {
        this.gameSession = gameSession;
        this.random = random;
    }

    public void applyTraining(Team team, String focusAttribute) {
        if (team == null) {
            throw new IllegalArgumentException("Team must not be null");
        }
        if (focusAttribute == null || focusAttribute.isBlank()) {
            throw new IllegalArgumentException("Focus attribute must not be blank");
        }

        PlayerAttributeSchema schema = requireSport().getAttributeSchema();
        if (schema.getRange(focusAttribute) == null) {
            throw new IllegalArgumentException("Unknown trainable attribute: " + focusAttribute);
        }

        for (Player player : team.getAvailablePlayers()) {
            Map<String, Integer> attributes = player.getAttributes();
            int currentValue = attributes.getOrDefault(focusAttribute, 0);
            int gain = 1 + random.nextInt(3);
            int updatedValue = schema.clamp(focusAttribute, currentValue + gain);
            attributes.put(focusAttribute, updatedValue);
        }
    }

    public List<String> getTrainableAttributes(Sport sport) {
        if (sport == null) {
            throw new IllegalArgumentException("Sport must not be null");
        }

        return new ArrayList<>(sport.getAttributeSchema().getAttributeNames());
    }

    private Sport requireSport() {
        Sport sport = gameSession.getActiveSport();
        if (sport == null) {
            throw new IllegalStateException("No active sport in session");
        }
        return sport;
    }
}
