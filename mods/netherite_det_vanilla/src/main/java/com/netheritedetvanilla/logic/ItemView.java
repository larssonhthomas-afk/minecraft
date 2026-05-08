package com.netheritedetvanilla.logic;

/**
 * Ren data-vy av en ItemStack som logiken behöver för validering.
 * Innehåller itemId, customName och om itemet har en spelarprofil (Drop Mod-huvud).
 */
public record ItemView(
        String itemId,
        String customName,
        boolean hasPlayerProfile
) {

    public ItemView {
        if (itemId == null) throw new IllegalArgumentException("itemId must not be null");
    }

    public static ItemView empty() {
        return new ItemView("minecraft:air", null, false);
    }

    public static ItemView of(String itemId) {
        return new ItemView(itemId, null, false);
    }

    public static ItemView of(String itemId, String customName) {
        return new ItemView(itemId, customName, false);
    }

    public boolean isEmpty() {
        return "minecraft:air".equals(itemId);
    }
}
