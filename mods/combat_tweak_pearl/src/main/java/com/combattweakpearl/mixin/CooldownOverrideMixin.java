package com.combattweakpearl.mixin;

import com.combattweakpearl.logic.CooldownLogic;
import net.minecraft.component.type.UseCooldownComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(UseCooldownComponent.class)
public abstract class CooldownOverrideMixin {

    @Inject(
            method = "set(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void combatTweakPearl$overrideCooldown(ItemStack stack, LivingEntity entity, CallbackInfo ci) {
        if (!(entity instanceof PlayerEntity player)) return;
        int ticks;
        if (stack.isOf(Items.ENDER_PEARL)) {
            ticks = CooldownLogic.PEARL_COOLDOWN_TICKS;
        } else if (stack.isOf(Items.WIND_CHARGE)) {
            ticks = CooldownLogic.WIND_CHARGE_COOLDOWN_TICKS;
        } else {
            return;
        }
        player.getItemCooldownManager().set(stack, ticks);
        ci.cancel();
    }
}
