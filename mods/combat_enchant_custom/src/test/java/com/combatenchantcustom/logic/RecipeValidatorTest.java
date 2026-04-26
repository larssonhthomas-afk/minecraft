package com.combatenchantcustom.logic;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecipeValidatorTest {

    private List<ItemView> emptyGrid() {
        List<ItemView> grid = new ArrayList<>();
        for (int i = 0; i < 9; i++) grid.add(ItemView.empty());
        return grid;
    }

    @Test
    void validGridReturnsTrue() {
        List<ItemView> grid = emptyGrid();
        grid.set(RecipeValidator.DRAGON_EGG_SLOT, new ItemView("minecraft:dragon_egg", null, false, null));
        grid.set(RecipeValidator.PLAYER_HEAD_SLOT, new ItemView("minecraft:player_head", null, true, null));
        assertTrue(RecipeValidator.validateGrid(grid));
    }

    @Test
    void nullGridReturnsFalse() {
        assertFalse(RecipeValidator.validateGrid(null));
    }

    @Test
    void wrongSizeReturnsFalse() {
        assertFalse(RecipeValidator.validateGrid(List.of(ItemView.empty())));
    }

    @Test
    void dragonEggInWrongSlotReturnsFalse() {
        List<ItemView> grid = emptyGrid();
        grid.set(0, new ItemView("minecraft:dragon_egg", null, false, null));
        grid.set(RecipeValidator.PLAYER_HEAD_SLOT, new ItemView("minecraft:player_head", null, true, null));
        assertFalse(RecipeValidator.validateGrid(grid));
    }

    @Test
    void playerHeadInWrongSlotReturnsFalse() {
        List<ItemView> grid = emptyGrid();
        grid.set(RecipeValidator.DRAGON_EGG_SLOT, new ItemView("minecraft:dragon_egg", null, false, null));
        grid.set(3, new ItemView("minecraft:player_head", null, true, null));
        assertFalse(RecipeValidator.validateGrid(grid));
    }

    @Test
    void playerHeadWithoutProfileReturnsFalse() {
        List<ItemView> grid = emptyGrid();
        grid.set(RecipeValidator.DRAGON_EGG_SLOT, new ItemView("minecraft:dragon_egg", null, false, null));
        grid.set(RecipeValidator.PLAYER_HEAD_SLOT, new ItemView("minecraft:player_head", null, false, null));
        assertFalse(RecipeValidator.validateGrid(grid));
    }

    @Test
    void extraItemInGridReturnsFalse() {
        List<ItemView> grid = emptyGrid();
        grid.set(RecipeValidator.DRAGON_EGG_SLOT, new ItemView("minecraft:dragon_egg", null, false, null));
        grid.set(RecipeValidator.PLAYER_HEAD_SLOT, new ItemView("minecraft:player_head", null, true, null));
        grid.set(0, new ItemView("minecraft:stone", null, false, null));
        assertFalse(RecipeValidator.validateGrid(grid));
    }

    @Test
    void wrongIngredientAtEggSlotReturnsFalse() {
        List<ItemView> grid = emptyGrid();
        grid.set(RecipeValidator.DRAGON_EGG_SLOT, new ItemView("minecraft:egg", null, false, null));
        grid.set(RecipeValidator.PLAYER_HEAD_SLOT, new ItemView("minecraft:player_head", null, true, null));
        assertFalse(RecipeValidator.validateGrid(grid));
    }

    @Test
    void wrongIngredientAtHeadSlotReturnsFalse() {
        List<ItemView> grid = emptyGrid();
        grid.set(RecipeValidator.DRAGON_EGG_SLOT, new ItemView("minecraft:dragon_egg", null, false, null));
        grid.set(RecipeValidator.PLAYER_HEAD_SLOT, new ItemView("minecraft:skeleton_skull", null, false, null));
        assertFalse(RecipeValidator.validateGrid(grid));
    }

    @Test
    void dragonEggSlotIsTopCenter() {
        assertEquals(1, RecipeValidator.DRAGON_EGG_SLOT);
    }

    @Test
    void playerHeadSlotIsCenter() {
        assertEquals(4, RecipeValidator.PLAYER_HEAD_SLOT);
    }
}
