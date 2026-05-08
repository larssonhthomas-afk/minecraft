package com.netheritedetvanilla.integration;

import com.netheritedetvanilla.logic.ItemView;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

/**
 * Översätter en MC ItemStack till en platt ItemView som logiken arbetar med.
 */
public final class ItemMarker {

    private ItemMarker() {}

    public static ItemView toView(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return ItemView.empty();

        String itemId = Registries.ITEM.getId(stack.getItem()).toString();

        Text customNameText = stack.get(DataComponentTypes.CUSTOM_NAME);
        String customName = customNameText == null ? null : customNameText.getString();

        ProfileComponent profile = stack.get(DataComponentTypes.PROFILE);
        boolean hasProfile = profile != null
                && (profile.id().isPresent() || profile.name().isPresent());

        return new ItemView(itemId, customName, hasProfile);
    }
}
