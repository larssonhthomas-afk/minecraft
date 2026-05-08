package com.netheritedetvanilla.logic;

import java.util.List;

/**
 * Validerar att en 3x3 crafting-grid innehåller exakt de ingredienser som krävs
 * för att crafta ett Netherite Ingot:
 *   4x minecraft:netherite_scrap
 *   1x Dragon Key  (firework_star + customName "Dragon Key")
 *   1x Wither Key  (firework_star + customName "Wither Key")
 *   1x Warden Key  (firework_star + customName "Warden Key")
 *   1x Player Head med spelarprofil (Drop Mod-huvud)
 *   1x minecraft:gold_block
 *
 * Receptet är shapeless — position i griden spelar ingen roll.
 */
public final class IngredientValidator {

    public static final String NETHERITE_SCRAP    = "minecraft:netherite_scrap";
    public static final String FIREWORK_STAR     = "minecraft:firework_star";
    public static final String PLAYER_HEAD       = "minecraft:player_head";
    public static final String GOLD_BLOCK        = "minecraft:gold_block";

    public static final String DRAGON_KEY_NAME   = "Dragon Key";
    public static final String WITHER_KEY_NAME   = "Wither Key";
    public static final String WARDEN_KEY_NAME   = "Warden Key";

    public static final int GRID_SIZE            = 9;

    private IngredientValidator() {}

    public static boolean validate(List<ItemView> grid) {
        if (grid == null || grid.size() != GRID_SIZE) return false;

        int ancientDebris = 0;
        boolean dragonKey = false;
        boolean witherKey = false;
        boolean wardenKey = false;
        boolean playerHead = false;
        boolean goldBlock  = false;

        for (ItemView view : grid) {
            if (view == null || view.isEmpty()) return false;

            String id   = view.itemId();
            String name = view.customName();

            if (NETHERITE_SCRAP.equals(id)) {
                ancientDebris++;
            } else if (FIREWORK_STAR.equals(id) && DRAGON_KEY_NAME.equals(name)) {
                if (dragonKey) return false;
                dragonKey = true;
            } else if (FIREWORK_STAR.equals(id) && WITHER_KEY_NAME.equals(name)) {
                if (witherKey) return false;
                witherKey = true;
            } else if (FIREWORK_STAR.equals(id) && WARDEN_KEY_NAME.equals(name)) {
                if (wardenKey) return false;
                wardenKey = true;
            } else if (PLAYER_HEAD.equals(id) && view.hasPlayerProfile()) {
                if (playerHead) return false;
                playerHead = true;
            } else if (GOLD_BLOCK.equals(id)) {
                if (goldBlock) return false;
                goldBlock = true;
            } else {
                return false;
            }
        }

        return ancientDebris == 4 && dragonKey && witherKey && wardenKey && playerHead && goldBlock;
    }
}
