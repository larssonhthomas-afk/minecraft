package com.axecleavingcleaving.logic;

import java.util.Set;

public final class CleavingLogic {

    public static final String CLEAVING_NBT_KEY = "axe_cleaving_cleaving";
    public static final String CLEAVING_BOOK_NBT_KEY = "axe_cleaving_cleaving_book";
    public static final float EXTRA_DAMAGE = 3.0f; // 1.5 hearts

    private static final Set<String> AXE_ITEM_IDS = Set.of(
            "minecraft:wooden_axe",
            "minecraft:stone_axe",
            "minecraft:iron_axe",
            "minecraft:golden_axe",
            "minecraft:diamond_axe",
            "minecraft:netherite_axe"
    );

    private CleavingLogic() {}

    public static boolean isAxeById(String itemId) {
        if (itemId == null) return false;
        return AXE_ITEM_IDS.contains(itemId);
    }

    public static float getExtraDamage() {
        return EXTRA_DAMAGE;
    }

    public static boolean hasCleaving(boolean cleavingFlag) {
        return cleavingFlag;
    }

    public static boolean isCleavingBook(boolean bookFlag) {
        return bookFlag;
    }
}
