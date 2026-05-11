package com.unbrokenchainability.mixin;

import com.unbrokenchainability.UnbrokenChainAbilityMod;
import com.unbrokenchainability.integration.AbilityStateManager;
import com.unbrokenchainability.logic.ChainLogic;
import com.unbrokenchainability.logic.HitTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerAttackMixin {

    @Unique
    private int unbrokenChainAbility$pendingBonusLevel = 0;

    // Capture target, validate conditions, record hit — store bonus level for later use
    @Inject(method = "attack", at = @At("HEAD"))
    private void unbrokenChainAbility$onAttackHead(Entity target, CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        this.unbrokenChainAbility$pendingBonusLevel = 0;

        if (UnbrokenChainAbilityMod.dataStore() == null) return;
        if (!UnbrokenChainAbilityMod.dataStore().hasAbility(self.getUuid())) return;

        String itemId = Registries.ITEM.getId(self.getMainHandStack().getItem()).toString();
        if (!ChainLogic.isSwordItemId(itemId)) return;

        HitTracker.HitResult result = AbilityStateManager.recordHit(self, target);
        this.unbrokenChainAbility$pendingBonusLevel = result.bonusLevel();
    }

    // MC 1.21.4: attack() calls Entity.sidedDamage(DamageSource, float) — NOT Entity.damage()
    @ModifyArg(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;sidedDamage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    ordinal = 0
            ),
            index = 1
    )
    private float unbrokenChainAbility$applyDamageBonus(float amount) {
        int bonusLevel = this.unbrokenChainAbility$pendingBonusLevel;
        if (bonusLevel <= 0) return amount;
        return amount * ChainLogic.calculateDamageMultiplier(bonusLevel);
    }

    // Play loud chain-break sound for every hit after the 3-hit threshold
    @Inject(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;sidedDamage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    ordinal = 0
            )
    )
    private void unbrokenChainAbility$playChainSound(Entity target, CallbackInfo ci) {
        if (this.unbrokenChainAbility$pendingBonusLevel <= 0) return;
        PlayerEntity self = (PlayerEntity) (Object) this;
        self.getWorld().playSound(null,
                target.getX(), target.getY(), target.getZ(),
                SoundEvents.BLOCK_CHAIN_BREAK, SoundCategory.PLAYERS, 2.5f, 0.8f);
    }
}
