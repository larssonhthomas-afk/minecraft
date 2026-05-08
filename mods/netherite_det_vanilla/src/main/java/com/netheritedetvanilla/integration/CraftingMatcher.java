package com.netheritedetvanilla.integration;

import com.netheritedetvanilla.logic.IngredientValidator;
import com.netheritedetvanilla.logic.ItemView;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Läser crafting-griden, omvandlar till ItemViews och delegerar till IngredientValidator.
 * Returnerar en Netherite Ingot om receptet matchar, annars null.
 */
public final class CraftingMatcher {

    private CraftingMatcher() {}

    public static ItemStack tryCraft(RecipeInputInventory grid) {
        if (grid == null) return null;

        List<ItemView> views = new ArrayList<>(grid.size());
        for (int i = 0; i < grid.size(); i++) {
            views.add(ItemMarker.toView(grid.getStack(i)));
        }

        if (!IngredientValidator.validate(views)) return null;

        return new ItemStack(Items.NETHERITE_INGOT);
    }
}
