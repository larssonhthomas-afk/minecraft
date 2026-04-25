package com.armorenchanttre.mixin;

import com.armorenchanttre.integration.ItemMarker;
import com.armorenchanttre.logic.AnvilCombiner;
import com.armorenchanttre.logic.EnchantmentType;
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

/**
 * Skriver över städets resultat när inputs matchar (rustning + vår enchantade bok).
 * Resultatet är samma rustning med vår enchant-marker satt; XP-kostnad är 0.
 */
@Mixin(AnvilScreenHandler.class)
public abstract class AnvilEnchantMixin {

    @Shadow @Final private Property levelCost;

    @Inject(method = "updateResult", at = @At("TAIL"))
    private void armorEnchantTre$overrideResult(CallbackInfo ci) {
        ForgingAccessor self = (ForgingAccessor)(Object) this;
        Inventory in = self.armorEnchantTre$getInput();
        CraftingResultInventory out = self.armorEnchantTre$getOutput();
        if (in == null || out == null) return;

        ItemStack left = in.getStack(0);
        ItemStack right = in.getStack(1);

        EnchantmentType type = AnvilCombiner.combine(
                ItemMarker.toView(left),
                ItemMarker.toView(right));
        if (type == null) return;

        ItemStack result = left.copy();
        ItemMarker.writeMarker(result, type);
        this.levelCost.set(AnvilCombiner.xpCost());
        out.setStack(0, result);
    }
}
