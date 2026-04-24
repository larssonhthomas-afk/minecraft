package com.rankedsmprank.logic;

import java.util.List;

/**
 * Immutable descriptor for one rank tier.
 * All HP values are in raw Minecraft health points (20 = 10 hearts).
 * Hearts above 10 (HP > 20) are intentionally worth 1 HP each instead of 2.
 */
public record RankDefinition(
        int tier,
        float rankHp,
        float potionMultiplier,
        float xpMultiplier,
        int extraSlots
) {

    public static final List<RankDefinition> ALL = List.of(
            new RankDefinition(1, 30.0f, 2.00f, 2.00f, 27),
            new RankDefinition(2, 28.0f, 1.80f, 1.80f, 24),
            new RankDefinition(3, 27.0f, 1.65f, 1.65f, 18),
            new RankDefinition(4, 26.0f, 1.50f, 1.50f, 14),
            new RankDefinition(5, 25.0f, 1.35f, 1.35f,  9),
            new RankDefinition(6, 23.0f, 1.25f, 1.25f,  0),
            new RankDefinition(7, 22.0f, 1.15f, 1.15f,  0),
            new RankDefinition(8, 21.0f, 1.10f, 1.10f,  0),
            new RankDefinition(9, 20.0f, 1.00f, 1.00f,  0)
    );

    public static RankDefinition forTier(int tier) {
        for (RankDefinition def : ALL) {
            if (def.tier() == tier) return def;
        }
        throw new IllegalArgumentException("Invalid tier: " + tier);
    }

    /**
     * Computes the Minecraft maxHealth attribute value to set on the player.
     *
     * Default (cleanMode=false): each HP above 20 renders as a FULL heart on screen.
     * Clean   (cleanMode=true):  each HP above 20 renders as a HALF heart (saves screen space).
     */
    public float computeMaxHealth(boolean cleanMode) {
        if (rankHp <= 20.0f) return rankHp;
        if (cleanMode) return rankHp;
        return 20.0f + (rankHp - 20.0f) * 2.0f;
    }

    /** Number of inventory rows needed (each row = 9 slots, rounded up). */
    public int extraRows() {
        if (extraSlots == 0) return 0;
        return (int) Math.ceil(extraSlots / 9.0);
    }

    /** Actual slot count used in the chest GUI (always a multiple of 9). */
    public int actualExtraSlots() {
        return extraRows() * 9;
    }

    public boolean hasExtraInventory() {
        return extraSlots > 0;
    }

    public String rankLabel() {
        return "[R" + tier + "]";
    }

    public static boolean isValidTier(int tier) {
        return tier >= 1 && tier <= 9;
    }
}
