package com.combatenchantcustom.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnbrokenChainLogicTest {

    @Test
    void zeroBonusLevelReturnsOne() {
        assertEquals(1.0f, UnbrokenChainLogic.calculateDamageMultiplier(0), 0.001f);
    }

    @Test
    void negativeBonusLevelReturnsOne() {
        assertEquals(1.0f, UnbrokenChainLogic.calculateDamageMultiplier(-1), 0.001f);
    }

    @Test
    void oneLevelGivesThreePercent() {
        assertEquals(1.03f, UnbrokenChainLogic.calculateDamageMultiplier(1), 0.001f);
    }

    @Test
    void twoLevelsGivesSixPercent() {
        assertEquals(1.06f, UnbrokenChainLogic.calculateDamageMultiplier(2), 0.001f);
    }

    @Test
    void fiveLevelsGivesFifteenPercent() {
        assertEquals(1.15f, UnbrokenChainLogic.calculateDamageMultiplier(5), 0.001f);
    }

    @Test
    void tenLevelsGivesThirtyPercent() {
        assertEquals(1.30f, UnbrokenChainLogic.calculateDamageMultiplier(10), 0.001f);
    }

    @Test
    void beyondMaxCappsAtThirtyPercent() {
        assertEquals(1.30f, UnbrokenChainLogic.calculateDamageMultiplier(15), 0.001f);
        assertEquals(1.30f, UnbrokenChainLogic.calculateDamageMultiplier(100), 0.001f);
    }

    @Test
    void swordItemIdsRecognized() {
        assertTrue(UnbrokenChainLogic.isSwordItemId("minecraft:wooden_sword"));
        assertTrue(UnbrokenChainLogic.isSwordItemId("minecraft:stone_sword"));
        assertTrue(UnbrokenChainLogic.isSwordItemId("minecraft:iron_sword"));
        assertTrue(UnbrokenChainLogic.isSwordItemId("minecraft:golden_sword"));
        assertTrue(UnbrokenChainLogic.isSwordItemId("minecraft:diamond_sword"));
        assertTrue(UnbrokenChainLogic.isSwordItemId("minecraft:netherite_sword"));
    }

    @Test
    void nonSwordItemIdsRejected() {
        assertFalse(UnbrokenChainLogic.isSwordItemId("minecraft:diamond_axe"));
        assertFalse(UnbrokenChainLogic.isSwordItemId("minecraft:bow"));
        assertFalse(UnbrokenChainLogic.isSwordItemId("minecraft:trident"));
        assertFalse(UnbrokenChainLogic.isSwordItemId(null));
        assertFalse(UnbrokenChainLogic.isSwordItemId(""));
    }

    @Test
    void bookNameConstantIsCorrect() {
        assertEquals("Unbroken_chain", UnbrokenChainLogic.BOOK_NAME);
    }

    @Test
    void bonusPerLevelIsThreePercent() {
        assertEquals(0.03f, UnbrokenChainLogic.BONUS_PER_LEVEL, 0.001f);
    }

    @Test
    void maxBonusIsThirtyPercent() {
        assertEquals(0.30f, UnbrokenChainLogic.MAX_BONUS, 0.001f);
    }
}
