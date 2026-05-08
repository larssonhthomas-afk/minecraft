package com.goldguldspawn.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GoldSpawnCalculatorTest {

    @Test
    void extraOverworldCountIsHalfOfVanilla() {
        assertEquals(2, GoldSpawnCalculator.extraOverworldCount());
    }

    @Test
    void extraNetherCountIsHalfOfVanilla() {
        assertEquals(10, GoldSpawnCalculator.extraNetherCount());
    }

    @Test
    void spawnMultiplierIsOnePointFive() {
        assertEquals(1.5, GoldSpawnCalculator.SPAWN_MULTIPLIER, 0.001);
    }

    @Test
    void totalOverworldCountIs150Percent() {
        int total = GoldSpawnCalculator.VANILLA_OVERWORLD_COUNT + GoldSpawnCalculator.extraOverworldCount();
        double expected = GoldSpawnCalculator.VANILLA_OVERWORLD_COUNT * GoldSpawnCalculator.SPAWN_MULTIPLIER;
        assertEquals(expected, total, 0.001);
    }

    @Test
    void totalNetherCountIs150Percent() {
        int total = GoldSpawnCalculator.VANILLA_NETHER_COUNT + GoldSpawnCalculator.extraNetherCount();
        double expected = GoldSpawnCalculator.VANILLA_NETHER_COUNT * GoldSpawnCalculator.SPAWN_MULTIPLIER;
        assertEquals(expected, total, 0.001);
    }

    @Test
    void extraCountsArePositive() {
        assertTrue(GoldSpawnCalculator.extraOverworldCount() > 0);
        assertTrue(GoldSpawnCalculator.extraNetherCount() > 0);
    }

    @Test
    void extraCountsDoNotExceedVanillaBase() {
        assertTrue(GoldSpawnCalculator.extraOverworldCount() < GoldSpawnCalculator.VANILLA_OVERWORLD_COUNT);
        assertTrue(GoldSpawnCalculator.extraNetherCount() < GoldSpawnCalculator.VANILLA_NETHER_COUNT);
    }
}
