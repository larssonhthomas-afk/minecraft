package com.goldguldspawn.logic;

public final class GoldSpawnCalculator {

    public static final double SPAWN_MULTIPLIER = 1.5;

    // Vanilla placement counts per chunk (from MC 1.21.4 worldgen data)
    public static final int VANILLA_OVERWORLD_COUNT = 4;
    public static final int VANILLA_NETHER_COUNT = 20;

    private GoldSpawnCalculator() {}

    /** Extra overworld gold ore attempts per chunk to add on top of vanilla (yields 1.5x total). */
    public static int extraOverworldCount() {
        return (int) Math.round(VANILLA_OVERWORLD_COUNT * (SPAWN_MULTIPLIER - 1.0));
    }

    /** Extra nether gold ore attempts per chunk to add on top of vanilla (yields 1.5x total). */
    public static int extraNetherCount() {
        return (int) Math.round(VANILLA_NETHER_COUNT * (SPAWN_MULTIPLIER - 1.0));
    }
}
