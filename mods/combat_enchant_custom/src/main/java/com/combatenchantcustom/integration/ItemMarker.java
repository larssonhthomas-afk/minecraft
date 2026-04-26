package com.combatenchantcustom.integration;

import com.combatenchantcustom.logic.ItemView;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public final class ItemMarker {

    public static final String NBT_KEY = "combat_enchant_custom";
    public static final String CHAIN_ENCHANT_VALUE = "unbroken_chain";

    private ItemMarker() {}

    public static boolean hasChainEnchant(ItemStack stack) {
        return CHAIN_ENCHANT_VALUE.equals(readMarker(stack));
    }

    public static String readMarker(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component == null) return null;
        NbtCompound nbt = component.copyNbt();
        if (!nbt.contains(NBT_KEY)) return null;
        return nbt.getString(NBT_KEY);
    }

    public static void writeChainEnchant(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        NbtComponent existing = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound nbt = existing.copyNbt();
        nbt.putString(NBT_KEY, CHAIN_ENCHANT_VALUE);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        Text lore = Text.literal("Unbroken Chain")
                .styled(s -> s.withColor(Formatting.GOLD).withBold(true).withItalic(false));
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(lore)));
    }

    public static ItemView toView(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return ItemView.empty();
        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
        Text customNameText = stack.get(DataComponentTypes.CUSTOM_NAME);
        String customName = customNameText == null ? null : customNameText.getString();
        ProfileComponent profile = stack.get(DataComponentTypes.PROFILE);
        boolean hasProfile = profile != null
                && (profile.id().isPresent() || profile.name().isPresent());
        return new ItemView(itemId, customName, hasProfile, readMarker(stack));
    }
}
