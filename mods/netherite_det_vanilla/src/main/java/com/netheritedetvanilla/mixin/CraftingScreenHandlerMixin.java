package com.netheritedetvanilla.mixin;

import com.netheritedetvanilla.integration.CraftingMatcher;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.CraftingScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skriver över crafting-resultatet när griden matchar Netherite-receptet:
 * 4x Ancient Debris + Dragon Key + Wither Key + Warden Key + Player Head + Gold Block.
 */
@Mixin(CraftingScreenHandler.class)
public abstract class CraftingScreenHandlerMixin {

    @Inject(method = "onContentChanged", at = @At("TAIL"))
    private void netheriteDetVanilla$overrideResult(Inventory inventory, CallbackInfo ci) {
        AbstractCraftingAccessor self = (AbstractCraftingAccessor)(Object) this;
        RecipeInputInventory grid = self.netheriteDetVanilla$getCraftingInventory();
        CraftingResultInventory output = self.netheriteDetVanilla$getCraftingResultInventory();
        if (grid == null || output == null) return;

        ItemStack result = CraftingMatcher.tryCraft(grid);
        if (result != null) {
            output.setStack(0, result);
        } else if (output.getStack(0).isOf(Items.NETHERITE_INGOT)) {
            output.setStack(0, ItemStack.EMPTY);
        }
    }
}
