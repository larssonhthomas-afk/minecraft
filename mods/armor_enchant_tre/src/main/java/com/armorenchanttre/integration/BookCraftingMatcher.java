package com.armorenchanttre.integration;

import com.armorenchanttre.logic.EnchantmentType;
import com.armorenchanttre.logic.ItemView;
import com.armorenchanttre.logic.RecipeValidator;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Samlar alla stackar i crafting-griden som ItemViews och delegerar till
 * RecipeValidator.validateGrid för sortering och validering.
 * Returnerar resulterande enchantad bok-stack om receptet matchar — annars null.
 */
public final class BookCraftingMatcher {

    private BookCraftingMatcher() {}

    public static ItemStack tryCraftEnchantedBook(RecipeInputInventory grid) {
        if (grid == null) return null;

        List<ItemView> views = new ArrayList<>(grid.size());
        for (int i = 0; i < grid.size(); i++) {
            views.add(ItemMarker.toView(grid.getStack(i)));
        }

        EnchantmentType type = RecipeValidator.validateGrid(views);
        if (type == null) return null;

        ItemStack result = new ItemStack(Items.BOOK);
        result.set(DataComponentTypes.CUSTOM_NAME, Text.literal(type.bookName()));
        NbtCompound nbt = new NbtCompound();
        nbt.putString(ItemMarker.NBT_KEY, type.id());
        result.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return result;
    }
}
