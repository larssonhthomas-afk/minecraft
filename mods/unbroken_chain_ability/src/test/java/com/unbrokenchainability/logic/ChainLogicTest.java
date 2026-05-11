package com.unbrokenchainability.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChainLogicTest {

    @Test
    void zeroBonusLevelReturnsOne() {
        assertEquals(1.0f, ChainLogic.calculateDamageMultiplier(0), 0.001f);
    }

    @Test
    void negativeBonusLevelReturnsOne() {
        assertEquals(1.0f, ChainLogic.calculateDamageMultiplier(-1), 0.001f);
    }

    @Test
    void oneLevelGivesThreePercent() {
        assertEquals(1.03f, ChainLogic.calculateDamageMultiplier(1), 0.001f);
    }

    @Test
    void tenLevelsGivesThirtyPercent() {
        assertEquals(1.30f, ChainLogic.calculateDamageMultiplier(10), 0.001f);
    }

    @Test
    void beyondMaxCapsAtThirtyPercent() {
        assertEquals(1.30f, ChainLogic.calculateDamageMultiplier(100), 0.001f);
    }

    @Test
    void isSwordItemIdDetectsSwords() {
        assertTrue(ChainLogic.isSwordItemId("minecraft:diamond_sword"));
        assertTrue(ChainLogic.isSwordItemId("minecraft:netherite_sword"));
        assertTrue(ChainLogic.isSwordItemId("minecraft:iron_sword"));
    }

    @Test
    void isSwordItemIdRejectsNonSwords() {
        assertFalse(ChainLogic.isSwordItemId("minecraft:diamond_axe"));
        assertFalse(ChainLogic.isSwordItemId("minecraft:bow"));
        assertFalse(ChainLogic.isSwordItemId(null));
        assertFalse(ChainLogic.isSwordItemId(""));
    }
}
