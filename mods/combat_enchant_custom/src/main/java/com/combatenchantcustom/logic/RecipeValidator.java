package com.combatenchantcustom.logic;

import java.util.List;

public final class RecipeValidator {

    public static final String DRAGON_EGG_ID = "minecraft:dragon_egg";
    public static final String PLAYER_HEAD_ID = "minecraft:player_head";
    public static final int DRAGON_EGG_SLOT = 1;
    public static final int PLAYER_HEAD_SLOT = 4;
    public static final int GRID_SIZE = 9;

    private RecipeValidator() {}

    public static boolean validateGrid(List<ItemView> items) {
        if (items == null || items.size() != GRID_SIZE) return false;

        for (int i = 0; i < GRID_SIZE; i++) {
            if (i == DRAGON_EGG_SLOT || i == PLAYER_HEAD_SLOT) continue;
            ItemView v = items.get(i);
            if (v != null && !v.isEmpty()) return false;
        }

        ItemView egg = items.get(DRAGON_EGG_SLOT);
        ItemView head = items.get(PLAYER_HEAD_SLOT);

        if (egg == null || !DRAGON_EGG_ID.equals(egg.itemId())) return false;
        if (head == null || !PLAYER_HEAD_ID.equals(head.itemId())) return false;
        return head.hasPlayerProfile();
    }
}
