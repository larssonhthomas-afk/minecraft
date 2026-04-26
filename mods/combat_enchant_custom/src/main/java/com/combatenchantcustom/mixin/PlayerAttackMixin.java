package com.combatenchantcustom.mixin;

import com.combatenchantcustom.integration.ChainStateManager;
import com.combatenchantcustom.integration.ItemMarker;
import com.combatenchantcustom.logic.HitTracker;
import com.combatenchantcustom.logic.UnbrokenChainLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public abstract class PlayerAttackMixin {

    // In MC 1.21.4, Entity.damage signature is damage(ServerWorld, DamageSource, float)
    @Redirect(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    ordinal = 0
            )
    )
    private boolean combatEnchantCustom$redirectMainAttack(Entity target, ServerWorld world,
            DamageSource source, float amount) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        ItemStack mainHand = self.getMainHandStack();

        if (!ItemMarker.hasChainEnchant(mainHand)) {
            return target.damage(world, source, amount);
        }

        HitTracker.HitResult result = ChainStateManager.recordHit(self);
        float multiplier = UnbrokenChainLogic.calculateDamageMultiplier(result.bonusLevel());

        if (result.soundShouldPlay()) {
            world.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.BLOCK_CHAIN_STEP, SoundCategory.PLAYERS, 1.0f, 1.2f);
        }

        return target.damage(world, source, amount * multiplier);
    }
}
