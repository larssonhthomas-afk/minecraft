package com.combatenchantcustom.logic;

public record ItemView(
        String itemId,
        String customName,
        boolean hasPlayerProfile,
        String enchantMarkerId
) {
    public ItemView {
        if (itemId == null) throw new IllegalArgumentException("itemId must not be null");
    }

    public static ItemView empty() {
        return new ItemView("minecraft:air", null, false, null);
    }

    public boolean isEmpty() {
        return "minecraft:air".equals(itemId);
    }
}
