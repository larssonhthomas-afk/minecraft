package com.rankedsmprank.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RankDefinitionTest {

    @Test
    void allRanksHaveUniqueValidTiers() {
        assertEquals(9, RankDefinition.ALL.size());
        long distinct = RankDefinition.ALL.stream().mapToInt(RankDefinition::tier).distinct().count();
        assertEquals(9, distinct);
    }

    @Test
    void tiersRange1To9() {
        for (RankDefinition d : RankDefinition.ALL) {
            assertTrue(d.tier() >= 1 && d.tier() <= 9, "tier out of range: " + d.tier());
        }
    }

    @Test
    void forTierReturnsCorrectDefinition() {
        RankDefinition r1 = RankDefinition.forTier(1);
        assertEquals(1, r1.tier());
        assertEquals(30.0f, r1.rankHp(), 0.001f);
        assertEquals(2.00f, r1.potionMultiplier(), 0.001f);
        assertEquals(27, r1.extraSlots());

        RankDefinition r9 = RankDefinition.forTier(9);
        assertEquals(9, r9.tier());
        assertEquals(20.0f, r9.rankHp(), 0.001f);
        assertEquals(1.00f, r9.potionMultiplier(), 0.001f);
        assertEquals(0, r9.extraSlots());
    }

    @Test
    void forTierThrowsOnInvalidTier() {
        assertThrows(IllegalArgumentException.class, () -> RankDefinition.forTier(0));
        assertThrows(IllegalArgumentException.class, () -> RankDefinition.forTier(10));
    }

    @Test
    void computeMaxHealthRank9Unchanged() {
        RankDefinition r9 = RankDefinition.forTier(9);
        assertEquals(20.0f, r9.computeMaxHealth(false), 0.001f);
        assertEquals(20.0f, r9.computeMaxHealth(true), 0.001f);
    }

    @Test
    void computeMaxHealthDefaultModeDoublesExtraHp() {
        // Rank 8: 21 HP → default → 20 + (21-20)*2 = 22
        RankDefinition r8 = RankDefinition.forTier(8);
        assertEquals(22.0f, r8.computeMaxHealth(false), 0.001f);
    }

    @Test
    void computeMaxHealthCleanModeUsesRankHpDirectly() {
        // Rank 8: 21 HP → clean → 21
        RankDefinition r8 = RankDefinition.forTier(8);
        assertEquals(21.0f, r8.computeMaxHealth(true), 0.001f);
    }

    @Test
    void computeMaxHealthRank1DefaultMode() {
        // Rank 1: 30 HP → default → 20 + (30-20)*2 = 40
        RankDefinition r1 = RankDefinition.forTier(1);
        assertEquals(40.0f, r1.computeMaxHealth(false), 0.001f);
    }

    @Test
    void computeMaxHealthRank1CleanMode() {
        RankDefinition r1 = RankDefinition.forTier(1);
        assertEquals(30.0f, r1.computeMaxHealth(true), 0.001f);
    }

    @Test
    void extraInventoryFields() {
        assertEquals(0, RankDefinition.forTier(9).extraSlots());
        assertEquals(0, RankDefinition.forTier(6).extraSlots());
        assertEquals(9, RankDefinition.forTier(5).extraSlots());
        assertEquals(14, RankDefinition.forTier(4).extraSlots());
        assertEquals(18, RankDefinition.forTier(3).extraSlots());
        assertEquals(24, RankDefinition.forTier(2).extraSlots());
        assertEquals(27, RankDefinition.forTier(1).extraSlots());
    }

    @Test
    void extraRowsRoundsUp() {
        assertEquals(0, RankDefinition.forTier(9).extraRows());
        assertEquals(1, RankDefinition.forTier(5).extraRows()); // 9 slots → 1 row
        assertEquals(2, RankDefinition.forTier(4).extraRows()); // 14 slots → 2 rows
        assertEquals(2, RankDefinition.forTier(3).extraRows()); // 18 slots → 2 rows
        assertEquals(3, RankDefinition.forTier(2).extraRows()); // 24 slots → 3 rows
        assertEquals(3, RankDefinition.forTier(1).extraRows()); // 27 slots → 3 rows
    }

    @Test
    void actualExtraSlotsIsMultipleOf9() {
        for (RankDefinition d : RankDefinition.ALL) {
            assertEquals(0, d.actualExtraSlots() % 9,
                    "Tier " + d.tier() + " actualExtraSlots not multiple of 9");
        }
    }

    @Test
    void hasExtraInventoryMatchesExtraSlots() {
        for (RankDefinition d : RankDefinition.ALL) {
            assertEquals(d.extraSlots() > 0, d.hasExtraInventory());
        }
    }

    @Test
    void isValidTier() {
        assertTrue(RankDefinition.isValidTier(1));
        assertTrue(RankDefinition.isValidTier(9));
        assertFalse(RankDefinition.isValidTier(0));
        assertFalse(RankDefinition.isValidTier(10));
        assertFalse(RankDefinition.isValidTier(-1));
    }

    @Test
    void rankLabelFormat() {
        assertEquals("[R1]", RankDefinition.forTier(1).rankLabel());
        assertEquals("[R9]", RankDefinition.forTier(9).rankLabel());
    }

    @Test
    void potionMultipliersAreAtLeastOne() {
        for (RankDefinition d : RankDefinition.ALL) {
            assertTrue(d.potionMultiplier() >= 1.0f,
                    "Tier " + d.tier() + " potionMultiplier < 1");
        }
    }

    @Test
    void xpMultipliersAreAtLeastOne() {
        for (RankDefinition d : RankDefinition.ALL) {
            assertTrue(d.xpMultiplier() >= 1.0f,
                    "Tier " + d.tier() + " xpMultiplier < 1");
        }
    }
}
