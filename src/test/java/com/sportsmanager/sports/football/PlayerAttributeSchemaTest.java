package com.sportsmanager.sports.football;

import com.sportsmanager.core.PlayerAttributeSchema;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerAttributeSchemaTest {

    @Test
    void testIsValid_withinRange() {
        PlayerAttributeSchema schema = createSchema();

        assertTrue(schema.isValid("pace", 15));
    }

    @Test
    void testIsValid_outsideRange() {
        PlayerAttributeSchema schema = createSchema();

        assertFalse(schema.isValid("pace", 21));
    }

    @Test
    void testClamp_belowMin() {
        PlayerAttributeSchema schema = createSchema();

        assertEquals(10, schema.clamp("pace", 9));
    }

    @Test
    void testClamp_aboveMax() {
        PlayerAttributeSchema schema = createSchema();

        assertEquals(20, schema.clamp("pace", 21));
    }

    @Test
    void testGenerateRandom_alwaysInRange() {
        PlayerAttributeSchema schema = createSchema();

        for (int i = 0; i < 100; i++) {
            assertTrue(schema.isValid("pace", schema.generateRandom("pace")));
        }
    }

    private PlayerAttributeSchema createSchema() {
        PlayerAttributeSchema schema = new PlayerAttributeSchema();
        schema.addAttribute("pace", 10, 20);
        return schema;
    }
}
