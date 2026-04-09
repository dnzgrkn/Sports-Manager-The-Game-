package com.sportsmanager.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void testIsAvailable_whenNotInjured() {
        Player player = new TestPlayer();

        assertTrue(player.isAvailable());
    }

    @Test
    void testIsAvailable_whenInjured() {
        Player player = new TestPlayer();

        player.setInjured(3);

        assertFalse(player.isAvailable());
    }

    @Test
    void testDecrementInjury() {
        Player player = new TestPlayer();
        player.setInjured(3);

        player.decrementInjury();
        player.decrementInjury();
        player.decrementInjury();

        assertTrue(player.isAvailable());
    }

    @Test
    void testAttributes_defaultEmpty() {
        Player player = new TestPlayer();

        assertTrue(player.getAttributes().isEmpty());
    }

    private static class TestPlayer extends Player {
        TestPlayer() {
            super("Test Player", 20, "Test Position");
        }

        @Override
        public int computeOverallRating() {
            return 0;
        }
    }
}
