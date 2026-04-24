package com.rankedsmprank.logic;

import java.util.List;

/**
 * Immutable descriptor for one rank tier.
 * All HP values are in raw Minecraft health points (20 = 10 hearts, 2 HP = 1 heart).
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
            new RankDefinition(2, 28.0f, 1.80f, 1.80f, 23),
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

    /** Returns the maxHealth attribute value to set (2 HP = 1 heart, normal proportion). */
    public float computeMaxHealth() {
        return rankHp;
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

    /** Number of barrier (locked) slots shown in the GUI to pad the last row. */
    public int barrierSlots() {
        return actualExtraSlots() - extraSlots;
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
