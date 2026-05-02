package com.admintooloperatorer.items;

import com.combatenchantcustom.integration.BookCraftingMatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Creates ItemStacks for all custom items handled by /give customitem.
 * Keys and heads come from drop_n_r; enchant-effect items from armor_enchant_tre;
 * enchant books from combat_enchant_custom.
 */
public final class CustomItemFactory {

    private CustomItemFactory() {}

    public static ItemStack create(String id) {
        if (id == null) return ItemStack.EMPTY;
        return switch (id.toLowerCase()) {
            case "neutral_player_head" -> fromRegistry("drop_n_r",         "neutral_player_head");
            case "warden_key"          -> fromRegistry("drop_n_r",         "warden_key");
            case "whiter_key"          -> fromRegistry("drop_n_r",         "whiter_key");
            case "dragon_key"          -> fromRegistry("drop_n_r",         "dragon_key");
            case "immunity"            -> fromRegistry("armor_enchant_tre", "immunity");
            case "extinguish"          -> fromRegistry("armor_enchant_tre", "extinguish");
            case "endurance"           -> fromRegistry("armor_enchant_tre", "endurance");
            case "unbroken_chain"      -> BookCraftingMatcher.createUnbrokenChainBook();
            default -> ItemStack.EMPTY;
        };
    }

    private static ItemStack fromRegistry(String namespace, String itemId) {
        var item = Registries.ITEM.get(Identifier.of(namespace, itemId));
        if (item == Items.AIR) return ItemStack.EMPTY;
        return new ItemStack(item);
    }
}
