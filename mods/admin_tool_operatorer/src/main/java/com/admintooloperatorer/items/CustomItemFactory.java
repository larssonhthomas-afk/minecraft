package com.admintooloperatorer.items;

import com.combatenchantcustom.integration.BookCraftingMatcher;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Creates ItemStacks for all custom items handled by /give customitem.
 */
public final class CustomItemFactory {

    public static final String NBT_KEY = "admin_tool_item_id";

    private CustomItemFactory() {}

    public static ItemStack create(String id) {
        if (id == null) return ItemStack.EMPTY;
        return switch (id.toLowerCase()) {
            case "neutral_player_head" -> neutralPlayerHead();
            case "warden_key"          -> namedItem(Items.TRIAL_KEY,          "warden_key",  "Warden Key",  Formatting.GREEN);
            case "whiter_key"          -> namedItem(Items.OMINOUS_TRIAL_KEY,  "whiter_key",  "Whiter Key",  Formatting.LIGHT_PURPLE);
            case "dragon_key"          -> namedItem(Items.NETHER_STAR,        "dragon_key",  "Dragon Key",  Formatting.DARK_PURPLE);
            case "immunity"            -> namedItem(Items.TOTEM_OF_UNDYING,   "immunity",    "Immunity",    Formatting.GOLD);
            case "extinguish"          -> namedItem(Items.POWDER_SNOW_BUCKET, "extinguish",  "Extinguish",  Formatting.AQUA);
            case "endurance"           -> namedItem(Items.HEART_OF_THE_SEA,   "endurance",   "Endurance",   Formatting.BLUE);
            case "unbroken_chain"      -> BookCraftingMatcher.createUnbrokenChainBook();
            default -> ItemStack.EMPTY;
        };
    }

    private static ItemStack neutralPlayerHead() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Neutral Player Head")
                        .styled(s -> s.withColor(Formatting.WHITE).withItalic(false)));
        NbtCompound nbt = new NbtCompound();
        nbt.putString(NBT_KEY, "neutral_player_head");
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return stack;
    }

    private static ItemStack namedItem(Item base, String id, String displayName, Formatting color) {
        ItemStack stack = new ItemStack(base);
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(displayName)
                        .styled(s -> s.withColor(color).withItalic(false)));
        NbtCompound nbt = new NbtCompound();
        nbt.putString(NBT_KEY, id);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return stack;
    }
}
