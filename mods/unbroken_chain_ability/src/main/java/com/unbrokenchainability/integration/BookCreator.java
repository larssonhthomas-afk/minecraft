package com.unbrokenchainability.integration;

import com.unbrokenchainability.logic.ChainLogic;
import com.unbrokenchainability.logic.ItemView;
import com.unbrokenchainability.logic.RecipeValidator;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public final class BookCreator {

    private BookCreator() {}

    public static ItemStack tryCreateBook(RecipeInputInventory grid) {
        if (grid == null) return null;
        List<ItemView> views = new ArrayList<>(grid.size());
        for (int i = 0; i < grid.size(); i++) {
            views.add(toView(grid.getStack(i)));
        }
        if (!RecipeValidator.validateGrid(views)) return null;
        return createAbilityBook();
    }

    public static ItemStack createAbilityBook() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(ChainLogic.BOOK_DISPLAY_NAME)
                        .styled(s -> s.withColor(Formatting.GOLD).withBold(true).withItalic(false)));
        NbtCompound nbt = new NbtCompound();
        nbt.putString(ChainLogic.ABILITY_NBT_KEY, ChainLogic.ABILITY_BOOK_VALUE);
        book.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return book;
    }

    public static boolean isAbilityBook(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component == null) return false;
        NbtCompound nbt = component.copyNbt();
        if (!nbt.contains(ChainLogic.ABILITY_NBT_KEY)) return false;
        return ChainLogic.ABILITY_BOOK_VALUE.equals(nbt.getString(ChainLogic.ABILITY_NBT_KEY));
    }

    private static ItemView toView(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return ItemView.empty();
        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
        ProfileComponent profile = stack.get(DataComponentTypes.PROFILE);
        boolean hasProfile = profile != null
                && (profile.id().isPresent() || profile.name().isPresent());
        return new ItemView(itemId, null, hasProfile);
    }
}
