package com.combatenchantcustom.mixin;

import com.combatenchantcustom.integration.ChainStateManager;
import com.combatenchantcustom.integration.ItemMarker;
import com.combatenchantcustom.logic.HitTracker;
import com.combatenchantcustom.logic.UnbrokenChainLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerAttackMixin {

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
    private float combatEnchantCustom$modifyAttackDamage(float amount) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (!ItemMarker.hasChainEnchant(self.getMainHandStack())) return amount;
        HitTracker.HitResult result = ChainStateManager.recordHit(self);
        return amount * UnbrokenChainLogic.calculateDamageMultiplier(result.bonusLevel());
    }

    @Inject(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;sidedDamage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    ordinal = 0
            )
    )
    private void combatEnchantCustom$playChainSound(Entity attackTarget, CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        ItemStack mainHand = self.getMainHandStack();
        if (!ItemMarker.hasChainEnchant(mainHand)) return;
        if (ChainStateManager.getBonusLevel(self) > 0) {
            self.getWorld().playSound(null,
                    attackTarget.getX(), attackTarget.getY(), attackTarget.getZ(),
                    SoundEvents.BLOCK_CHAIN_STEP, SoundCategory.PLAYERS, 1.0f, 1.2f);
        }
    }
}
