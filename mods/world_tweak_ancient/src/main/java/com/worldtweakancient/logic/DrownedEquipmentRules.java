package com.worldtweakancient.logic;

/**
 * Regler för Drowned-spawn-utrustning. Ren logik utan Minecraft-importer.
 * <p>
 * Tridenten är hårdkodad bortbannad enligt mod-specifikationen, men utformningen
 * tillåter framtida konfiguration utan att behöva ändra Mixin-koden.
 */
public final class DrownedEquipmentRules {

    public static final String TRIDENT = "minecraft:trident";

    private final boolean tridentAllowed;

    public DrownedEquipmentRules(boolean tridentAllowed) {
        this.tridentAllowed = tridentAllowed;
    }

    public static DrownedEquipmentRules defaults() {
        return new DrownedEquipmentRules(false);
    }

    public boolean isTridentAllowed() {
        return tridentAllowed;
    }

    /**
     * @return true om Drowned ska få ha en main-hand-vapen alls. Vanilla väljer mellan
     *         Trident och Fishing Rod – när Trident är otillåten skippar vi hela
     *         tilldelningen så att Drowned spawnar utan main-hand-vapen.
     */
    public boolean shouldSkipMainHandAssignment() {
        return !tridentAllowed;
    }
}
