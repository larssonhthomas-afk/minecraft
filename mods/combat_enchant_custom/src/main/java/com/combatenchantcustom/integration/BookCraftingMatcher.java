package com.combatenchantcustom.integration;

import com.combatenchantcustom.logic.ItemView;
import com.combatenchantcustom.logic.RecipeValidator;
import com.combatenchantcustom.logic.UnbrokenChainLogic;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public final class BookCraftingMatcher {

    private BookCraftingMatcher() {}

    public static ItemStack tryCraftBook(RecipeInputInventory grid) {
        if (grid == null) return null;
        List<ItemView> views = new ArrayList<>(grid.size());
        for (int i = 0; i < grid.size(); i++) {
            views.add(ItemMarker.toView(grid.getStack(i)));
        }
        if (!RecipeValidator.validateGrid(views)) return null;
        return createUnbrokenChainBook();
    }

    public static ItemStack createUnbrokenChainBook() {
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        book.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(UnbrokenChainLogic.BOOK_NAME)
                        .styled(s -> s.withColor(Formatting.GOLD).withItalic(false)));
        NbtCompound nbt = new NbtCompound();
        nbt.putString(ItemMarker.NBT_KEY, ItemMarker.CHAIN_ENCHANT_VALUE);
        book.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return book;
    }

    public static boolean isUnbrokenChainBook(ItemStack stack) {
        return ItemMarker.CHAIN_ENCHANT_VALUE.equals(ItemMarker.readMarker(stack));
    }
}
