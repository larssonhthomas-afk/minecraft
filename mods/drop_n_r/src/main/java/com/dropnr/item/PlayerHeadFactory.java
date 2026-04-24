package com.dropnr.item;

import com.mojang.authlib.GameProfile;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * Bygger ett player_head knutet till ett spelarprofil så att texturen och namnet
 * matchar spelaren vars huvud det representerar.
 */
public final class PlayerHeadFactory {

    private PlayerHeadFactory() {}

    public static ItemStack create(GameProfile profile) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        stack.set(DataComponentTypes.PROFILE, new ProfileComponent(profile));
        return stack;
    }
}
