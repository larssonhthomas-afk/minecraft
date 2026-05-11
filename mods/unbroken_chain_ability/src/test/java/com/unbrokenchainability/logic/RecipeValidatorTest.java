package com.unbrokenchainability.logic;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecipeValidatorTest {

    private static List<ItemView> emptyGrid() {
        List<ItemView> grid = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) grid.add(ItemView.empty());
        return grid;
    }

    @Test
    void validRecipeAccepted() {
        List<ItemView> grid = emptyGrid();
        grid.set(RecipeValidator.DRAGON_EGG_SLOT, new ItemView(RecipeValidator.DRAGON_EGG_ID, null, false));
        grid.set(RecipeValidator.PLAYER_HEAD_SLOT, new ItemView(RecipeValidator.PLAYER_HEAD_ID, null, true));
        assertTrue(RecipeValidator.validateGrid(grid));
    }

    @Test
    void missingDragonEggFails() {
        List<ItemView> grid = emptyGrid();
        grid.set(RecipeValidator.PLAYER_HEAD_SLOT, new ItemView(RecipeValidator.PLAYER_HEAD_ID, null, true));
        assertFalse(RecipeValidator.validateGrid(grid));
    }

    @Test
    void missingPlayerHeadFails() {
        List<ItemView> grid = emptyGrid();
        grid.set(RecipeValidator.DRAGON_EGG_SLOT, new ItemView(RecipeValidator.DRAGON_EGG_ID, null, false));
        assertFalse(RecipeValidator.validateGrid(grid));
    }

    @Test
    void playerHeadWithoutProfileFails() {
        List<ItemView> grid = emptyGrid();
        grid.set(RecipeValidator.DRAGON_EGG_SLOT, new ItemView(RecipeValidator.DRAGON_EGG_ID, null, false));
        grid.set(RecipeValidator.PLAYER_HEAD_SLOT, new ItemView(RecipeValidator.PLAYER_HEAD_ID, null, false));
        assertFalse(RecipeValidator.validateGrid(grid));
    }

    @Test
    void extraItemInOtherSlotFails() {
        List<ItemView> grid = emptyGrid();
        grid.set(RecipeValidator.DRAGON_EGG_SLOT, new ItemView(RecipeValidator.DRAGON_EGG_ID, null, false));
        grid.set(RecipeValidator.PLAYER_HEAD_SLOT, new ItemView(RecipeValidator.PLAYER_HEAD_ID, null, true));
        grid.set(0, new ItemView("minecraft:dirt", null, false));
        assertFalse(RecipeValidator.validateGrid(grid));
    }

    @Test
    void wrongItemInEggSlotFails() {
        List<ItemView> grid = emptyGrid();
        grid.set(RecipeValidator.DRAGON_EGG_SLOT, new ItemView("minecraft:dirt", null, false));
        grid.set(RecipeValidator.PLAYER_HEAD_SLOT, new ItemView(RecipeValidator.PLAYER_HEAD_ID, null, true));
        assertFalse(RecipeValidator.validateGrid(grid));
    }

    @Test
    void nullGridFails() {
        assertFalse(RecipeValidator.validateGrid(null));
    }

    @Test
    void wrongGridSizeFails() {
        assertFalse(RecipeValidator.validateGrid(List.of(ItemView.empty())));
    }
}
