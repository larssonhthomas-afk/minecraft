package com.armorenchanttre.integration;

import com.armorenchanttre.logic.EnchantmentType;
import com.armorenchanttre.logic.ItemView;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

/**
 * Översätter mellan riktiga ItemStacks och vår platta ItemView som logiken arbetar med.
 * Använder CUSTOM_DATA-componenten för att lagra vår enchant-marker så vi slipper
 * registrera ett eget DataComponentType.
 */
public final class ItemMarker {

    public static final String NBT_KEY = "armor_enchant_tre";

    private ItemMarker() {}

    public static ItemView toView(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return ItemView.empty();

        String itemId = Registries.ITEM.getId(stack.getItem()).toString();

        Text customNameText = stack.get(DataComponentTypes.CUSTOM_NAME);
        String customName = customNameText == null ? null : customNameText.getString();

        ProfileComponent profile = stack.get(DataComponentTypes.PROFILE);
        boolean hasProfile = profile != null
                && (profile.id().isPresent() || profile.name().isPresent());

        String marker = readMarker(stack);

        return new ItemView(itemId, customName, hasProfile, marker);
    }

    public static String readMarker(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component == null) return null;
        NbtCompound nbt = component.copyNbt();
        if (!nbt.contains(NBT_KEY)) return null;
        return nbt.getString(NBT_KEY);
    }

    public static void writeMarker(ItemStack stack, EnchantmentType type) {
        if (stack == null || stack.isEmpty() || type == null) return;
        NbtComponent existing = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound nbt = existing.copyNbt();
        nbt.putString(NBT_KEY, type.id());
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }
}
