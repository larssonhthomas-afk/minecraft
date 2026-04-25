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

/**
 * Letar i en crafting-grid (3x3) efter exakt en bok + en nyckel + ett spelarhuvud.
 * Returnerar resulterande enchantad bok-stack om receptet matchar — annars null.
 */
public final class BookCraftingMatcher {

    private BookCraftingMatcher() {}

    public static ItemStack tryCraftEnchantedBook(RecipeInputInventory grid) {
        if (grid == null) return null;

        ItemStack book = null;
        ItemStack key = null;
        ItemStack head = null;

        for (int i = 0; i < grid.size(); i++) {
            ItemStack stack = grid.getStack(i);
            if (stack.isEmpty()) continue;

            ItemView v = ItemMarker.toView(stack);
            if (RecipeValidator.readBookType(v) != null) {
                if (book != null) return null;
                book = stack;
            } else if (RecipeValidator.readKeyType(v) != null) {
                if (key != null) return null;
                key = stack;
            } else if (RecipeValidator.isValidPlayerHead(v)) {
                if (head != null) return null;
                head = stack;
            } else {
                return null;
            }
        }

        if (book == null || key == null || head == null) return null;

        EnchantmentType type = RecipeValidator.validate(
                ItemMarker.toView(book),
                ItemMarker.toView(key),
                ItemMarker.toView(head));
        if (type == null) return null;

        ItemStack result = new ItemStack(Items.BOOK);
        result.set(DataComponentTypes.CUSTOM_NAME, Text.literal(type.bookName()));
        NbtCompound nbt = new NbtCompound();
        nbt.putString(ItemMarker.NBT_KEY, type.id());
        result.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return result;
    }
}
