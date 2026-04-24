package com.dropnr.logic;

/**
 * Generell kontroll för att avgöra om ett item är ett player head, oavsett vems huvud.
 * Används av crafting-integration så att recept kan acceptera vilket player head som helst
 * som ingrediens istället för att binda receptet till en specifik skull.
 */
public final class PlayerHeadIngredient {

    public static final String PLAYER_HEAD_ID = "minecraft:player_head";

    private PlayerHeadIngredient() {}

    public static boolean isPlayerHead(String itemId) {
        return PLAYER_HEAD_ID.equals(itemId);
    }
}
