package com.combatenchantcustom.mixin;

import com.combatenchantcustom.integration.ItemMarker;
import com.combatenchantcustom.logic.AnvilValidator;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilUnbrokenChainMixin {

    @Shadow @Final private Property levelCost;

    @Inject(method = "updateResult", at = @At("TAIL"))
    private void combatEnchantCustom$overrideResult(CallbackInfo ci) {
        ForgingAccessor self = (ForgingAccessor) (Object) this;
        Inventory input = self.combatEnchantCustom$getInput();
        CraftingResultInventory output = self.combatEnchantCustom$getOutput();
        if (input == null || output == null) return;

        ItemStack left = input.getStack(0);
        ItemStack right = input.getStack(1);

        if (!AnvilValidator.canEnchant(ItemMarker.toView(left), ItemMarker.toView(right))) return;

        ItemStack result = left.copy();
        ItemMarker.writeChainEnchant(result);
        // Vanilla requires levelCost > 0 for the output slot to be takeable
        this.levelCost.set(1);
        output.setStack(0, result);
    }
}
