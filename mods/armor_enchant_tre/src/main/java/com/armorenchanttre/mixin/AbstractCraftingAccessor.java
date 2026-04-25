package com.armorenchanttre.mixin;

import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.screen.AbstractCraftingScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractCraftingScreenHandler.class)
public interface AbstractCraftingAccessor {

    @Accessor("craftingInventory")
    RecipeInputInventory armorEnchantTre$getCraftingInventory();

    @Accessor("craftingResultInventory")
    CraftingResultInventory armorEnchantTre$getCraftingResultInventory();
}
