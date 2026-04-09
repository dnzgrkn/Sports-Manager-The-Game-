package com.sportsmanager.core;

import com.sportsmanager.sports.football.FootballPlayer;
import com.sportsmanager.sports.football.FootballTactic;
import com.sportsmanager.sports.football.FootballTactic.PressingLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TeamTest {

    private Team team;
    private FootballPlayer healthyPlayer1;
    private FootballPlayer healthyPlayer2;
    private FootballPlayer injuredPlayer;

    @BeforeEach
    void setUp() {
        team = new Team("Test FC", "logo.png");

        healthyPlayer1 = new FootballPlayer("Alice", 25, "CM");
        healthyPlayer2 = new FootballPlayer("Bob", 23, "CF");
        injuredPlayer  = new FootballPlayer("Charlie", 27, "GK");
        injuredPlayer.setInjured(3);

        team.addPlayer(healthyPlayer1);
        team.addPlayer(healthyPlayer2);
        team.addPlayer(injuredPlayer);
    }

    @Test
    void testGetAvailablePlayers_excludesInjured() {
        List<Player> available = team.getAvailablePlayers();

        assertTrue(available.contains(healthyPlayer1));
        assertTrue(available.contains(healthyPlayer2));
        assertFalse(available.contains(injuredPlayer));
        assertEquals(2, available.size());
    }

    @Test
    void testMakeSubstitution_valid() {
        team.setActiveTactic(new FootballTactic("4-3-3", 1.0, 1.0, PressingLevel.MEDIUM));

        boolean result = team.makeSubstitution(healthyPlayer1, healthyPlayer2);

        assertTrue(result);
        assertEquals(1, team.getMaxSubstitutionsUsed());
    }

    @Test
    void testGetStartingLineup_returnsCorrectCount() {
        List<Player> lineup = team.getStartingLineup(2);

        assertEquals(2, lineup.size());
        // Only available players should appear
        for (Player p : lineup) {
            assertTrue(p.isAvailable());
        }
    }
}
