package com.axecleavingcleaving.mixin;

import com.axecleavingcleaving.AxeCleavingCleavingMod;
import com.axecleavingcleaving.logic.CleavingLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// ServerPlayerEntity.damage(ServerWorld, DamageSource, float) — intermediary method_64397
@Mixin(ServerPlayerEntity.class)
public abstract class ShieldDisableMixin {

    @Unique
    private boolean axc$wasBlocking = false;

    // ThreadLocal guard prevents the extra-damage call from recursing into this Mixin
    @Unique
    private static final ThreadLocal<Boolean> axc$applyingExtra = ThreadLocal.withInitial(() -> false);

    @Inject(
        method = "damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z",
        at = @At("HEAD")
    )
    private void axc$captureBlocking(ServerWorld world, DamageSource source, float amount,
                                      CallbackInfoReturnable<Boolean> cir) {
        axc$wasBlocking = ((ServerPlayerEntity)(Object)this).isBlocking();
    }

    @Inject(
        method = "damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z",
        at = @At("RETURN")
    )
    private void axc$onDamageReturn(ServerWorld world, DamageSource source, float amount,
                                     CallbackInfoReturnable<Boolean> cir) {
        if (axc$applyingExtra.get()) return;
        if (!axc$wasBlocking) return;

        ServerPlayerEntity self = (ServerPlayerEntity)(Object)this;
        // Shield still active → was not disabled this hit
        if (self.isBlocking()) return;

        // Shield was disabled — check that the attacker has a Cleaving axe
        Entity attacker = source.getAttacker();
        if (!(attacker instanceof ServerPlayerEntity attackingPlayer)) return;

        ItemStack weapon = attackingPlayer.getMainHandStack();
        if (!AxeCleavingCleavingMod.hasCleaving(weapon)) return;

        axc$applyingExtra.set(true);
        try {
            self.damage(world, world.getDamageSources().playerAttack(attackingPlayer),
                        CleavingLogic.getExtraDamage());
        } finally {
            axc$applyingExtra.set(false);
        }
    }
}
