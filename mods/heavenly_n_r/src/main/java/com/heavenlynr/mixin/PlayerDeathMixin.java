package com.heavenlynr.mixin;

import com.heavenlynr.HeavenlyNRMod;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerDeathMixin {

    @Inject(method = "onDeath(Lnet/minecraft/entity/damage/DamageSource;)V", at = @At("HEAD"))
    private void heavenlyNR$onDeath(DamageSource source, CallbackInfo ci) {
        ServerPlayerEntity victim = (ServerPlayerEntity) (Object) this;
        if (HeavenlyNRMod.dataStore() == null) return;
        if (!HeavenlyNRMod.dataStore().hasAbility(victim.getUuid())) return;

        if (source.getAttacker() instanceof ServerPlayerEntity killer
                && !killer.getUuid().equals(victim.getUuid())) {
            HeavenlyNRMod.transferAbility(killer, victim);
        } else {
            // Non-PvP death: victim loses Heavenly without transfer
            HeavenlyNRMod.revokeAbility(victim);
        }
    }
}
