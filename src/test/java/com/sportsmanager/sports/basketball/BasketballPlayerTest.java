package com.sportsmanager.sports.basketball;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BasketballPlayerTest {

    private BasketballPlayer createPlayer(String position, Map<String, Integer> attrs) {
        BasketballPlayer p = new BasketballPlayer("Test", 25, position);
        p.setAttributes(attrs);
        return p;
    }

    private Map<String, Integer> baseAttributes(int value) {
        Map<String, Integer> attrs = new HashMap<>();
        attrs.put("shooting",   value);
        attrs.put("passing",    value);
        attrs.put("dribbling",  value);
        attrs.put("defending",  value);
        attrs.put("rebounding", value);
        attrs.put("speed",      value);
        attrs.put("stamina",    value);
        return attrs;
    }

    private Map<String, Integer> attrWith(String key, int high, int base) {
        Map<String, Integer> attrs = baseAttributes(base);
        attrs.put(key, high);
        return attrs;
    }

    @Test
    void testRating_PG() {
        // PG formula: passing*0.30 + dribbling*0.25 + speed*0.20 + shooting*0.15 + stamina*0.10
        // passing (0.30) should contribute more than dribbling (0.25)
        BasketballPlayer highPassing   = createPlayer("PG", attrWith("passing",   90, 50));
        BasketballPlayer highDribbling = createPlayer("PG", attrWith("dribbling", 90, 50));

        assertTrue(highPassing.computeOverallRating() > highDribbling.computeOverallRating(),
                "PG: passing should contribute more to rating than dribbling");
    }

    @Test
    void testRating_C() {
        // C formula: rebounding*0.40 + defending*0.30 + shooting*0.15 + stamina*0.15
        // rebounding (0.40) should contribute more than defending (0.30)
        BasketballPlayer highRebounding = createPlayer("C", attrWith("rebounding", 90, 50));
        BasketballPlayer highDefending  = createPlayer("C", attrWith("defending",  90, 50));

        assertTrue(highRebounding.computeOverallRating() > highDefending.computeOverallRating(),
                "C: rebounding should contribute more to rating than defending");
    }

    @Test
    void testRating_differentPositions() {
        // PG weights passing highly (0.30), SG weights shooting highly (0.35)
        // Same attributes with high shooting => SG rates higher than PG
        Map<String, Integer> attrs = baseAttributes(50);
        attrs.put("shooting", 90);

        BasketballPlayer pg = createPlayer("PG", attrs);
        BasketballPlayer sg = createPlayer("SG", attrs);

        assertNotEquals(pg.computeOverallRating(), sg.computeOverallRating(),
                "Same attributes should produce different ratings for PG vs SG");
    }
}
