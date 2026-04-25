package com.armorenchanttre.mixin;

import com.armorenchanttre.integration.BookCraftingMatcher;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skriver över crafting-resultatet om gridens innehåll matchar vårt recept för
 * att skapa en enchantad bok (bok + nyckel + spelarhuvud).
 */
@Mixin(CraftingScreenHandler.class)
public abstract class CraftingArmorEnchantMixin {

    @Inject(method = "onContentChanged", at = @At("TAIL"))
    private void armorEnchantTre$overrideResult(Inventory inventory, CallbackInfo ci) {
        AbstractCraftingAccessor self = (AbstractCraftingAccessor)(Object) this;
        RecipeInputInventory grid = self.armorEnchantTre$getCraftingInventory();
        CraftingResultInventory output = self.armorEnchantTre$getCraftingResultInventory();
        if (grid == null || output == null) return;

        ItemStack result = BookCraftingMatcher.tryCraftEnchantedBook(grid);
        if (result != null) {
            output.setStack(0, result);
        }
    }
}
