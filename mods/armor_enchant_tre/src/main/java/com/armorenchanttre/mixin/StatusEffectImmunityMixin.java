package com.armorenchanttre.mixin;

import com.armorenchanttre.integration.ItemMarker;
import com.armorenchanttre.logic.ArmorEffects;
import com.armorenchanttre.logic.EnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Blockerar potion effects som Immunity-boots ska skydda mot
 * (Poison, Wither, Weakness, Slow Falling).
 */
@Mixin(LivingEntity.class)
public abstract class StatusEffectImmunityMixin {

    @Inject(
            method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void armorEnchantTre$blockImmunityEffects(StatusEffectInstance effect, Entity source,
            CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof ServerPlayerEntity player)) return;

        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);
        EnchantmentType feetEnchant = EnchantmentType.fromId(ItemMarker.readMarker(boots));
        if (feetEnchant != EnchantmentType.IMMUNITY) return;

        String effectId = Registries.STATUS_EFFECT.getId(effect.getEffectType().value()).toString();
        if (ArmorEffects.shouldBlockEffect(feetEnchant, effectId)) {
            cir.setReturnValue(false);
        }
    }
}
