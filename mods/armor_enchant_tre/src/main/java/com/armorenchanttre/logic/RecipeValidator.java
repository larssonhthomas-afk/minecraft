package com.armorenchanttre.logic;

import java.util.List;

/**
 * Validerar crafting-recipes för att skapa en enchantad bok.
 * Alla tre ingredienser måste matcha exakt en EnchantmentType:
 *   - oenchantad bok med customName som matchar bookName
 *   - firework_star med customName som matchar nyckelns displayName (Drop Mod-nyckel)
 *   - player_head med en spelarprofil (Drop Mod-huvud), inte en vanlig skull
 *
 * Returnerar EnchantmentType om kombinationen är giltig, annars null.
 */
public final class RecipeValidator {

    public static final String BOOK_ITEM_ID = "minecraft:book";
    public static final String KEY_ITEM_ID = "minecraft:firework_star";
    public static final String PLAYER_HEAD_ITEM_ID = "minecraft:player_head";

    public static final int KEY_SLOT = 1;
    public static final int HEAD_SLOT = 4;
    public static final int GRID_SIZE = 9;

    private RecipeValidator() {}

    public static EnchantmentType validate(ItemView key, ItemView head) {
        if (key == null || head == null) return null;
        EnchantmentType byKey = readKeyType(key);
        if (byKey == null) return null;
        if (!isValidPlayerHead(head)) return null;
        return byKey;
    }

    /**
     * Validerar 3x3-grid med strikta slot-positioner: nyckel i top-mid (index 1)
     * och spelarhuvud i center (index 4). Alla andra slottar måste vara tomma.
     * Den oenchantade boken är inte längre en ingrediens — själva nyckeln avgör
     * vilken EnchantmentType som ska genereras.
     */
    public static EnchantmentType validateGrid(List<ItemView> stacks) {
        if (stacks == null) return null;
        if (stacks.size() != GRID_SIZE) return null;
        for (int i = 0; i < GRID_SIZE; i++) {
            if (i == KEY_SLOT || i == HEAD_SLOT) continue;
            ItemView v = stacks.get(i);
            if (v != null && !v.isEmpty()) return null;
        }
        return validate(stacks.get(KEY_SLOT), stacks.get(HEAD_SLOT));
    }

    public static EnchantmentType readKeyType(ItemView key) {
        if (key == null || !KEY_ITEM_ID.equals(key.itemId())) return null;
        String name = key.customName();
        if (name == null) return null;
        for (EnchantmentType t : EnchantmentType.values()) {
            if (t.keyName().equals(name)) return t;
        }
        return null;
    }

    public static boolean isValidPlayerHead(ItemView head) {
        if (head == null) return false;
        if (!PLAYER_HEAD_ITEM_ID.equals(head.itemId())) return false;
        return head.hasPlayerProfile();
    }
}
