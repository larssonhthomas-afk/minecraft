package com.unbrokenchainability.mixin;

import com.unbrokenchainability.integration.BookCreator;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreenHandler.class)
public abstract class CraftingMixin {

    @Inject(method = "onContentChanged", at = @At("TAIL"))
    private void unbrokenChainAbility$overrideResult(Inventory inventory, CallbackInfo ci) {
        AbstractCraftingAccessor self = (AbstractCraftingAccessor) (Object) this;
        RecipeInputInventory grid = self.unbrokenChainAbility$getCraftingInventory();
        CraftingResultInventory output = self.unbrokenChainAbility$getCraftingResultInventory();
        if (grid == null || output == null) return;

        ItemStack result = BookCreator.tryCreateBook(grid);
        if (result != null) {
            output.setStack(0, result);
        }
    }
}
