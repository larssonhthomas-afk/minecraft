package com.rankedsmprank.mixin;

import com.rankedsmprank.RankedSmpRankActions;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerDeathMixin {

    @Inject(method = "onDeath(Lnet/minecraft/entity/damage/DamageSource;)V", at = @At("HEAD"))
    private void ranked$onDeath(DamageSource source, CallbackInfo ci) {
        ServerPlayerEntity victim = (ServerPlayerEntity) (Object) this;
        if (source.getAttacker() instanceof ServerPlayerEntity killer
                && !killer.getUuid().equals(victim.getUuid())) {
            RankedSmpRankActions.processPvPKill(killer, victim);
        }
    }
}
