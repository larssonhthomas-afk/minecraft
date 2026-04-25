package com.armorenchanttre.integration;

import com.armorenchanttre.logic.EnchantmentType;
import com.armorenchanttre.logic.ItemView;
import com.armorenchanttre.logic.RecipeValidator;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        return createEnchantedBook(type);
    }

    public static ItemStack createEnchantedBook(EnchantmentType type) {
        ItemStack result = new ItemStack(Items.ENCHANTED_BOOK);
        result.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(type.bookName()).formatted(Formatting.BLUE));
        NbtCompound nbt = new NbtCompound();
        nbt.putString(ItemMarker.NBT_KEY, type.id());
        result.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return result;
    }

    public static ItemStack createKey(EnchantmentType type) {
        ItemStack key = new ItemStack(Items.FIREWORK_STAR);
        key.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(type.keyName()).formatted(Formatting.GOLD));
        return key;
    }

    public static ItemStack createNeutralPlayerHead() {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        head.set(DataComponentTypes.PROFILE,
                new ProfileComponent(Optional.of("Neutral"), Optional.empty(), new PropertyMap()));
        return head;
    }
}
