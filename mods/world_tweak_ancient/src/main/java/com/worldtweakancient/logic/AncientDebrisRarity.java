package com.worldtweakancient.logic;

/**
 * Hanterar rariteten på Ancient Debris ore-generering.
 * <p>
 * Ancient Debris ska vara {@code rarityMultiplier} gånger ovanligare än vanilla.
 * Implementeras genom en {@code rarity_filter} placement-modifier i den datapack som
 * modden levererar (se resources/data/minecraft/worldgen/placed_feature/...).
 * <p>
 * Klassen är ren logik utan Minecraft-importer så den kan enhetstestas.
 */
public final class AncientDebrisRarity {

    public static final int DEFAULT_MULTIPLIER = 10;

    private final int rarityMultiplier;

    public AncientDebrisRarity(int rarityMultiplier) {
        if (rarityMultiplier < 1) {
            throw new IllegalArgumentException(
                    "rarityMultiplier måste vara >= 1, var: " + rarityMultiplier);
        }
        this.rarityMultiplier = rarityMultiplier;
    }

    public static AncientDebrisRarity defaults() {
        return new AncientDebrisRarity(DEFAULT_MULTIPLIER);
    }

    public int rarityMultiplier() {
        return rarityMultiplier;
    }

    /**
     * @return sannolikheten (0.0–1.0) att en ore-generering körs jämfört med vanilla.
     */
    public double generationProbability() {
        return 1.0 / rarityMultiplier;
    }
}
