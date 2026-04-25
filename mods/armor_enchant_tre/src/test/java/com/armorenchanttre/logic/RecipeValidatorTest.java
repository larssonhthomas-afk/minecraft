package com.armorenchanttre.logic;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecipeValidatorTest {

    private static ItemView key(String name) {
        return new ItemView(RecipeValidator.KEY_ITEM_ID, name, false, null);
    }

    private static ItemView playerHead(boolean withProfile) {
        return new ItemView(RecipeValidator.PLAYER_HEAD_ITEM_ID, null, withProfile, null);
    }

    private static List<ItemView> emptyGrid() {
        List<ItemView> g = new ArrayList<>();
        for (int i = 0; i < RecipeValidator.GRID_SIZE; i++) g.add(ItemView.empty());
        return g;
    }

    private static List<ItemView> gridWith(ItemView keyStack, ItemView headStack) {
        List<ItemView> g = emptyGrid();
        g.set(RecipeValidator.KEY_SLOT, keyStack);
        g.set(RecipeValidator.HEAD_SLOT, headStack);
        return g;
    }

    @Test
    void slotConstants_areTopMiddleAndCenter() {
        assertEquals(1, RecipeValidator.KEY_SLOT, "Nyckel ska ligga på top-mid (index 1, 0-indexed).");
        assertEquals(4, RecipeValidator.HEAD_SLOT, "Spelarhuvud ska ligga i center (index 4, 0-indexed).");
        assertEquals(9, RecipeValidator.GRID_SIZE);
    }

    @Test
    void validate_immunityRecipe_returnsImmunity() {
        assertSame(EnchantmentType.IMMUNITY,
                RecipeValidator.validate(key("Dragon Key"), playerHead(true)));
    }

    @Test
    void validate_enduranceRecipe_returnsEndurance() {
        assertSame(EnchantmentType.ENDURANCE,
                RecipeValidator.validate(key("Warden Key"), playerHead(true)));
    }

    @Test
    void validate_extinguishRecipe_returnsExtinguish() {
        assertSame(EnchantmentType.EXTINGUISH,
                RecipeValidator.validate(key("Wither Key"), playerHead(true)));
    }

    @Test
    void validate_playerHeadWithoutProfile_isRejected() {
        assertNull(RecipeValidator.validate(key("Dragon Key"), playerHead(false)),
                "Vanlig skull utan profil får inte godkännas — endast Drop Mod-huvuden.");
    }

    @Test
    void validate_keyWithoutCustomName_isRejected() {
        ItemView vanillaFireworkStar = new ItemView(RecipeValidator.KEY_ITEM_ID, null, false, null);
        assertNull(RecipeValidator.validate(vanillaFireworkStar, playerHead(true)),
                "Vanlig firework_star utan rätt CUSTOM_NAME får inte godkännas.");
    }

    @Test
    void validate_keyWithWrongCustomName_isRejected() {
        ItemView fakeKey = new ItemView(RecipeValidator.KEY_ITEM_ID, "Fake Key", false, null);
        assertNull(RecipeValidator.validate(fakeKey, playerHead(true)));
    }

    @Test
    void validate_wrongItemTypes_areRejected() {
        ItemView paperPretendingToBeKey = new ItemView("minecraft:paper", "Dragon Key", false, null);
        assertNull(RecipeValidator.validate(paperPretendingToBeKey, playerHead(true)));

        ItemView skullInsteadOfHead = new ItemView("minecraft:wither_skeleton_skull", null, true, null);
        assertNull(RecipeValidator.validate(key("Dragon Key"), skullInsteadOfHead));
    }

    @Test
    void validate_nullInputs_returnsNull() {
        assertNull(RecipeValidator.validate(null, playerHead(true)));
        assertNull(RecipeValidator.validate(key("Dragon Key"), null));
    }

    @Test
    void readKeyType_recognisesNamedKey() {
        assertSame(EnchantmentType.IMMUNITY, RecipeValidator.readKeyType(key("Dragon Key")));
        assertSame(EnchantmentType.ENDURANCE, RecipeValidator.readKeyType(key("Warden Key")));
        assertSame(EnchantmentType.EXTINGUISH, RecipeValidator.readKeyType(key("Wither Key")));
    }

    @Test
    void isValidPlayerHead_requiresProfile() {
        assertTrue(RecipeValidator.isValidPlayerHead(playerHead(true)));
        assertFalse(RecipeValidator.isValidPlayerHead(playerHead(false)));
        assertFalse(RecipeValidator.isValidPlayerHead(new ItemView("minecraft:player_head", "X", false, null)));
    }

    @Test
    void validateGrid_immunityRecipe_returnsImmunity() {
        assertSame(EnchantmentType.IMMUNITY, RecipeValidator.validateGrid(
                gridWith(key("Dragon Key"), playerHead(true))));
    }

    @Test
    void validateGrid_enduranceRecipe_returnsEndurance() {
        assertSame(EnchantmentType.ENDURANCE, RecipeValidator.validateGrid(
                gridWith(key("Warden Key"), playerHead(true))));
    }

    @Test
    void validateGrid_extinguishRecipe_returnsExtinguish() {
        assertSame(EnchantmentType.EXTINGUISH, RecipeValidator.validateGrid(
                gridWith(key("Wither Key"), playerHead(true))));
    }

    @Test
    void validateGrid_keyInWrongSlot_returnsNull() {
        List<ItemView> grid = emptyGrid();
        grid.set(0, key("Dragon Key"));
        grid.set(RecipeValidator.HEAD_SLOT, playerHead(true));
        assertNull(RecipeValidator.validateGrid(grid),
                "Nyckel i fel slot får inte ge ett giltigt recept.");
    }

    @Test
    void validateGrid_headInWrongSlot_returnsNull() {
        List<ItemView> grid = emptyGrid();
        grid.set(RecipeValidator.KEY_SLOT, key("Dragon Key"));
        grid.set(8, playerHead(true));
        assertNull(RecipeValidator.validateGrid(grid),
                "Spelarhuvud i fel slot får inte ge ett giltigt recept.");
    }

    @Test
    void validateGrid_extraJunkInOtherSlot_returnsNull() {
        List<ItemView> grid = gridWith(key("Dragon Key"), playerHead(true));
        grid.set(0, new ItemView("minecraft:stick", null, false, null));
        assertNull(RecipeValidator.validateGrid(grid),
                "Övriga slottar måste vara tomma.");
    }

    @Test
    void validateGrid_emptyGrid_returnsNull() {
        assertNull(RecipeValidator.validateGrid(emptyGrid()));
    }

    @Test
    void validateGrid_missingHead_returnsNull() {
        List<ItemView> grid = emptyGrid();
        grid.set(RecipeValidator.KEY_SLOT, key("Dragon Key"));
        assertNull(RecipeValidator.validateGrid(grid));
    }

    @Test
    void validateGrid_missingKey_returnsNull() {
        List<ItemView> grid = emptyGrid();
        grid.set(RecipeValidator.HEAD_SLOT, playerHead(true));
        assertNull(RecipeValidator.validateGrid(grid));
    }

    @Test
    void validateGrid_wrongSize_returnsNull() {
        assertNull(RecipeValidator.validateGrid(Collections.emptyList()));
        assertNull(RecipeValidator.validateGrid(List.of(
                key("Dragon Key"), playerHead(true))));
    }

    @Test
    void validateGrid_nullStacks_returnsNull() {
        assertNull(RecipeValidator.validateGrid(null));
    }
}
