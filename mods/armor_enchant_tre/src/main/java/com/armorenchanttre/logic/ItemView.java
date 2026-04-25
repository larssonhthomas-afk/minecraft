package com.armorenchanttre.logic;

/**
 * Ren data-vy av en ItemStack som logiken behöver för validering.
 * Innehåller allt logiken behöver: itemId, customName, om itemet har en spelarprofil,
 * och vilket marker-id (om något) som finns på vårt egna data-component.
 */
public record ItemView(
        String itemId,
        String customName,
        boolean hasPlayerProfile,
        String enchantMarkerId
) {

    public ItemView {
        if (itemId == null) {
            throw new IllegalArgumentException("itemId must not be null");
        }
    }

    public static ItemView empty() {
        return new ItemView("minecraft:air", null, false, null);
    }

    public static ItemView of(String itemId) {
        return new ItemView(itemId, null, false, null);
    }

    public static ItemView of(String itemId, String customName) {
        return new ItemView(itemId, customName, false, null);
    }

    public boolean isEmpty() {
        return "minecraft:air".equals(itemId);
    }
}
