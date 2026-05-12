package com.combattweakpearl.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public abstract class BreachEnchantMixin {

    @Inject(
            method = "canApplyAtAnvil",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void combatTweakPearl$blockBreachOnMace(
            ItemStack stack,
            RegistryEntry<Enchantment> enchantment,
            CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        if (stack.isOf(Items.MACE) && enchantment.matchesKey(Enchantments.BREACH)) {
            cir.setReturnValue(false);
        }
    }
}
