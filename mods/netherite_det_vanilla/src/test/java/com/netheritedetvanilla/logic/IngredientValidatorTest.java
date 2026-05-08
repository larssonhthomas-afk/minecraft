package com.netheritedetvanilla.logic;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IngredientValidatorTest {

    private static ItemView debris() {
        return ItemView.of(IngredientValidator.ANCIENT_DEBRIS);
    }

    private static ItemView dragonKey() {
        return new ItemView(IngredientValidator.FIREWORK_STAR, IngredientValidator.DRAGON_KEY_NAME, false);
    }

    private static ItemView witherKey() {
        return new ItemView(IngredientValidator.FIREWORK_STAR, IngredientValidator.WITHER_KEY_NAME, false);
    }

    private static ItemView wardenKey() {
        return new ItemView(IngredientValidator.FIREWORK_STAR, IngredientValidator.WARDEN_KEY_NAME, false);
    }

    private static ItemView playerHead(boolean withProfile) {
        return new ItemView(IngredientValidator.PLAYER_HEAD, null, withProfile);
    }

    private static ItemView goldBlock() {
        return ItemView.of(IngredientValidator.GOLD_BLOCK);
    }

    private static List<ItemView> validGrid() {
        List<ItemView> g = new ArrayList<>();
        g.add(debris());
        g.add(debris());
        g.add(debris());
        g.add(debris());
        g.add(dragonKey());
        g.add(witherKey());
        g.add(wardenKey());
        g.add(playerHead(true));
        g.add(goldBlock());
        return g;
    }

    @Test
    void validate_fullValidGrid_returnsTrue() {
        assertTrue(IngredientValidator.validate(validGrid()));
    }

    @Test
    void validate_orderShouldNotMatter_returnsTrue() {
        List<ItemView> g = new ArrayList<>();
        g.add(goldBlock());
        g.add(playerHead(true));
        g.add(wardenKey());
        g.add(witherKey());
        g.add(dragonKey());
        g.add(debris());
        g.add(debris());
        g.add(debris());
        g.add(debris());
        assertTrue(IngredientValidator.validate(g));
    }

    @Test
    void validate_missingOneDebris_returnsFalse() {
        List<ItemView> g = validGrid();
        g.set(0, ItemView.empty());
        assertFalse(IngredientValidator.validate(g));
    }

    @Test
    void validate_playerHeadWithoutProfile_returnsFalse() {
        List<ItemView> g = validGrid();
        g.set(7, playerHead(false));
        assertFalse(IngredientValidator.validate(g));
    }

    @Test
    void validate_wrongKey_returnsFalse() {
        List<ItemView> g = validGrid();
        g.set(4, new ItemView(IngredientValidator.FIREWORK_STAR, "Fake Key", false));
        assertFalse(IngredientValidator.validate(g));
    }

    @Test
    void validate_vanillaFireworkStarInsteadOfKey_returnsFalse() {
        List<ItemView> g = validGrid();
        g.set(4, ItemView.of(IngredientValidator.FIREWORK_STAR));
        assertFalse(IngredientValidator.validate(g));
    }

    @Test
    void validate_wrongItemInSlot_returnsFalse() {
        List<ItemView> g = validGrid();
        g.set(8, ItemView.of("minecraft:iron_block"));
        assertFalse(IngredientValidator.validate(g));
    }

    @Test
    void validate_duplicateDragonKey_returnsFalse() {
        List<ItemView> g = validGrid();
        g.set(5, dragonKey());
        assertFalse(IngredientValidator.validate(g));
    }

    @Test
    void validate_duplicateGoldBlock_returnsFalse() {
        List<ItemView> g = validGrid();
        g.set(0, goldBlock());
        assertFalse(IngredientValidator.validate(g));
    }

    @Test
    void validate_emptySlot_returnsFalse() {
        List<ItemView> g = validGrid();
        g.set(3, ItemView.empty());
        assertFalse(IngredientValidator.validate(g));
    }

    @Test
    void validate_nullGrid_returnsFalse() {
        assertFalse(IngredientValidator.validate(null));
    }

    @Test
    void validate_wrongGridSize_returnsFalse() {
        assertFalse(IngredientValidator.validate(List.of(debris())));
        assertFalse(IngredientValidator.validate(new ArrayList<>()));
    }

    @Test
    void validate_missingDragonKey_returnsFalse() {
        List<ItemView> g = validGrid();
        g.set(4, debris());
        assertFalse(IngredientValidator.validate(g));
    }

    @Test
    void validate_missingWitherKey_returnsFalse() {
        List<ItemView> g = validGrid();
        g.set(5, debris());
        assertFalse(IngredientValidator.validate(g));
    }

    @Test
    void validate_missingWardenKey_returnsFalse() {
        List<ItemView> g = validGrid();
        g.set(6, debris());
        assertFalse(IngredientValidator.validate(g));
    }

    @Test
    void validate_missingGoldBlock_returnsFalse() {
        List<ItemView> g = validGrid();
        g.set(8, debris());
        assertFalse(IngredientValidator.validate(g));
    }

    @Test
    void validate_wrongPlayerHeadItem_returnsFalse() {
        List<ItemView> g = validGrid();
        g.set(7, new ItemView("minecraft:skeleton_skull", null, true));
        assertFalse(IngredientValidator.validate(g));
    }
}
